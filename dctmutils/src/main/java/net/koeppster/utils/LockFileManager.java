package net.koeppster.utils;

import com.documentum.fc.common.DfLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Java class that manages synchronized access to a lock file and stores ownership in JSON,
 * including both thread ID and the local hostname. Uses Jackson to serialize/deserialize the lock
 * data.
 *
 * <p>Provides two public methods:
 *
 * <ul>
 *   <li><b>getLock(threadId)</b>: blocks until it can (1) obtain an OS-level lock on the file, (2)
 *       parse the file's JSON content, (3) verify that the file is either empty or already owned by
 *       the same (threadId + hostname), (4) update the file JSON with the new ownership, and (5)
 *       release the OS-level lock.
 *   <li><b>releaseLock(threadId)</b>: obtains an OS-level lock, checks the JSON content to see if
 *       it matches (threadId + hostname), and if so, clears the file. Then releases the OS-level
 *       lock.
 * </ul>
 *
 * <p>These methods are synchronized, so only one thread in this JVM can call them concurrently.
 * However, the OS-level lock can coordinate across multiple processes or JVMs if the filesystem
 * supports it (especially on the same host). For multiple hosts, reliability depends on your
 * filesystem's network lock capabilities.
 */
public class LockFileManager {

  // For JSON serialization/deserialization
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final Path lockFilePath;
  private final String localHostName;

  /**
   * Construct a LockFileManager for a specific lock file path. Automatically detects the local
   * hostname.
   *
   * @param filePath The path to the lock file.
   */
  public LockFileManager(String filePath) {
    this.lockFilePath = Paths.get(filePath);
    this.localHostName = detectHostName();
  }

  /**
   * Attempts to acquire logical ownership of the lock file for the given threadId. This method:
   *
   * <ol>
   *   <li>Opens the lock file (creating it if necessary).
   *   <li>Obtains an exclusive OS-level lock (blocking until available).
   *   <li>Deserializes any existing JSON ownership data (threadId + hostName) using Jackson.
   *   <li>If another distinct owner is found, release the OS-level lock and retry after a delay.
   *   <li>If the file is empty or owned by the same combination (threadId, localHostName), write
   *       our new ownership in JSON.
   *   <li>Close the channel, releasing the OS-level lock. The file now reflects that we own it.
   * </ol>
   *
   * @param threadId A unique identifier for the owning thread or process.
   * @throws IOException If an I/O error occurs while operating on the file.
   * @throws InterruptedException If the thread is interrupted during sleep.
   */
  public void getLock(String threadId) throws IOException, InterruptedException {
    DfLogger.debug(this,"getLock({0})",new String[] {threadId},null);
    RandomAccessFile raf = new RandomAccessFile(lockFilePath.toFile(), "rw");
    FileChannel channel = raf.getChannel();
    int i=0;
    while (i < 250) {
      try {
          // lock() will block until it obtains an exclusive lock
        FileLock fileLock = channel.lock();
        DfLogger.debug(this,"Checking lock for thread {0}", new String[] {threadId}, null );

        // Now hold the OS-level lock on the file
        LockOwner currentOwner = parseLockOwner(readFileContent(channel));
        DfLogger.debug(this,"currentOwner: {0}", new Object[] {currentOwner.toString()},null);

        // If there's no owner OR owned by the same threadId + hostName, we can take ownership
        if (currentOwner.checkEmpty()
            || (currentOwner.getThreadId().equals(threadId)
                && currentOwner.getHostName().equals(localHostName))) {
          DfLogger.debug(this,"Setting lock for thread {0}", new String[] {threadId}, null);
          LockOwner newOwner = new LockOwner(threadId, localHostName);
          writeFileContent(channel, objectMapper.writeValueAsString(newOwner));
          // Once we close the channel, the OS-level lock is released
          // but the file now reflects that we own it.
          fileLock.release();
          raf.close();
          return;
        }
       fileLock.release();
      } catch (Throwable e) {
        DfLogger.warn(this,"Exception Thrown: {0}", new String[] {e.getMessage()},null);
      }
      DfLogger.debug(this, "Slepping...",null,null);
      // Sleep briefly then retry.
      Thread.sleep(100);
      i++;
    }
  }

  /**
   * Releases the logical ownership of the lock file if the current JSON ownership matches (threadId
   * + localHostName). If it does, clear the file content.
   *
   * @param threadId The identifier that currently owns the lock.
   * @throws IOException If an I/O error occurs while operating on the file.
   */
  public void releaseLock(String threadId) throws IOException {
    DfLogger.debug(this,"releaseLock({0})",new String[] {threadId},null);
    try (RandomAccessFile raf = new RandomAccessFile(lockFilePath.toFile(), "rw");
        FileChannel channel = raf.getChannel();
        FileLock fileLock = channel.lock()) {

      LockOwner currentOwner = parseLockOwner(readFileContent(channel));
      DfLogger.debug(this,"Checking lock on {0}",new String[] {currentOwner.toString()},null);
      if (currentOwner.getThreadId().equals(threadId)
          && currentOwner.getHostName().equals(localHostName)) {
        DfLogger.debug(this,"Releasing lock for thread {0}", new String[] {threadId},null);
        // Clear the file to indicate no owner
        writeFileContent(channel, "");
        fileLock.release();
      }
    }
  }

  /**
   * Reads the current content of the file channel (which should be locked).
   *
   * @param channel The locked FileChannel.
   * @return The raw string content of the file.
   * @throws IOException If an I/O error occurs.
   */
  private String readFileContent(FileChannel channel) throws IOException {
    channel.position(0);
    ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
    channel.read(buffer);
    buffer.flip();
    String ret = StandardCharsets.UTF_8.decode(buffer).toString();
    DfLogger.debug(this, "Exiting readFileContent with {0}", new String[] {ret}, null);
    return ret;
  }

  /**
   * Overwrites the file with the given content, starting from position 0.
   *
   * @param channel The locked FileChannel.
   * @param content The string to write.
   * @throws IOException If an I/O error occurs.
   */
  private void writeFileContent(FileChannel channel, String content) throws IOException {
    DfLogger.debug(this, "writeFileContnet({0})",new String[] {content}, null);
    channel.truncate(0);
    channel.position(0);
    ByteBuffer buffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
    channel.write(buffer);
    channel.force(true);
  }

  /** Minimal helper to get the local hostname or a fallback. */
  private String detectHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown-host";
    }
  }

  /**
   * Parses the file content as JSON into a LockOwner object using Jackson. If the content is empty
   * or invalid, returns an empty LockOwner.
   */
  private LockOwner parseLockOwner(String content) {
    DfLogger.debug(this,"parseLockObject({0})", new String[] {content},null);
    content = content.trim();
    if (content.isEmpty()) {
      return new LockOwner();
    }
    try {
      return objectMapper.readValue(content, LockOwner.class);
    } catch (IOException e) {
      // If parsing fails, treat it as no owner
      return new LockOwner();
    }
  }

  /** A small DTO for the lock ownership data, with Jackson-friendly fields. */
  public static class LockOwner {
    private String threadId;
    private String hostName;

    // Default constructor needed for Jackson
    public LockOwner() {
      this.threadId = "";
      this.hostName = "";
    }

    public LockOwner(String threadId, String hostName) {
      this.threadId = threadId;
      this.hostName = hostName;
    }

    public String getThreadId() {
      return threadId;
    }

    public String getHostName() {
      return hostName;
    }

    public void setHostName(String arg0) {
        this.hostName = arg0;
    }

    public void setThreadId(String arg0) {
        this.threadId = arg0;
    }

    public boolean checkEmpty() {
      return (threadId == null || threadId.isEmpty()) && (hostName == null || hostName.isEmpty());
    }

    public String toString() {
      return String.format("{threadId: '%s', hostName: '%s'}", threadId,hostName);
    }
  }
}
