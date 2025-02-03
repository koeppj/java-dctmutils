package net.koeppster.dctm.commands.exporter;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.operations.IDfExportNode;
import com.documentum.operations.IDfExportOperation;
import com.documentum.operations.IDfOperationError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import net.koeppster.dctm.commands.AbstractCmd;
import net.koeppster.dctm.utils.UtilsException;
import net.koeppster.utils.LockFileManager;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class ExportQueueItemProcessor extends AbstractCmd {

  private static final String INFO_LEVEL = "INFO";
  private static final String WARN_LEVEL = "WARN";

  private ExportDatabaseManager queueManager;
  private IDfSessionManager sessionManager;
  private ArrayList<String> customAttribs;
  private String repo;
  private CSVPrinter exportPrinter;
  private File outputDir;
  private PrintStream warningStream;
  private LockFileManager lockFileManager;

  public ExportQueueItemProcessor(
      ExportDatabaseManager queueManager,
      IDfSessionManager sessionManager,
      String repo,
      ArrayList<String> customAttribs,
      CSVPrinter exportPrinter,
      File outputDir,
      PrintStream warningStream,
      LockFileManager lockFileManeger) {
    this.queueManager = queueManager;
    this.sessionManager = sessionManager;
    this.repo = repo;
    this.customAttribs = customAttribs;
    this.exportPrinter = exportPrinter;
    this.outputDir = outputDir;
    this.warningStream = warningStream;
    this.lockFileManager = lockFileManeger;
  }

  public void processCandidate(ExportQueueItem arg0) throws UtilsException, InterruptedException {
    DfLogger.debug(this, "Processing candidate {0}", new String[] {arg0.toString()}, null);

    IDfSession session = null;
    queueManager.markItemInprogress(arg0.getChronicleId());

    try {
      session = sessionManager.getSession(repo);
      IDfSysObject obj = (IDfSysObject) session.getObject(new DfId(arg0.getObjectId()));

      // If there is no content do not export it
      if (obj.getContentsId().isNull()) {
        DfLogger.debug(
            this,
            "No content for object {0} - Skipping",
            new String[] {obj.getObjectId().getId()},
            null);
        printWarning(
            INFO_LEVEL, String.format("Object %s has no content", obj.getObjectId().getId()));
        queueManager.markItemComplete(arg0.getChronicleId());
        return;
      }
      // Get the full path to the object in the repository (first one only)
      IDfFolder folder = (IDfFolder) session.getObject(obj.getFolderId(0));
      String firstPath = folder.getFolderPath(0);

      // Issue warning if the is more than one path
      if (obj.getFolderIdCount() > 1) {
        DfLogger.warn(
            this,
            "Object {0} has more than one path",
            new String[] {obj.getObjectId().getId()},
            null);
        printWarning(
            WARN_LEVEL,
            String.format("Object %s has more than one path", obj.getObjectId().getId()));
      }

      // Create the node that will be used for reporting and repreatablity.
      ObjectNode node = new ObjectNode(JsonNodeFactory.instance);

      // weird bug where the object is is no added to the node
      node.put("r_object_id", obj.getObjectId().getId());

      // Calculate the file name based on the object name and extension
      String extension = obj.getFormat().getDOSExtension();
      String fileName = obj.getObjectName().replaceAll("[^a-zA-Z0-9\\.\\- ]", "_");
      if (!obj.getObjectName().endsWith(extension)) {
        fileName = fileName.concat(".").concat(extension);
      }


      // Add path and filename to exported filename to the attributes list
      node.put("path", firstPath.concat("/").concat(fileName));

      if (null != customAttribs) {
        ObjectNode extractedNodes = getJsonFromTypedObject(obj, customAttribs);
        node.setAll(extractedNodes);
      }

      if (null != exportPrinter) {
        exportPrinter.printRecord(getIterbleNode(node));
      }

      // If the output dir parameter was speficied output the file.
      if (null != outputDir) {
        // Check if the file already exists
        String fullPath = this.outputDir.getAbsolutePath().concat(firstPath);
        File dir = new File(fullPath);
        if (!dir.exists()) {
          FileUtils.forceMkdir(dir);
        }
        this.lockFileManager.getLock(Thread.currentThread().getName());
        fileName = determineFileName(fullPath, fileName, 0);
        obj.getFile(fullPath.concat("/").concat(fileName));
        DfLogger.debug(this,"Saved file {0}", new String[] {fullPath.concat("/").concat(fileName)},null);
        this.lockFileManager.releaseLock(Thread.currentThread().getName());
      }
    } catch (Throwable e) {
      DfLogger.error(this, "Error processing candidate {0}", new String[] {arg0.toString()}, e);
      printWarning(WARN_LEVEL, String.format("Error processing candidate: %s", e.getMessage()));
      throw new UtilsException(
          String.format("Documentum Error Processing List: %s", e.getMessage()), e);
    }
    finally {
      try {
        this.lockFileManager.releaseLock(Thread.currentThread().getName());
      } catch (IOException e) {
        DfLogger.warn(this, "Error releasing lock: {0}", new String[] {e.getMessage()}, e);
      }
      if ((null != session) && session.isConnected()) {
        sessionManager.release(session);
      }
    }
    queueManager.markItemComplete(arg0.getChronicleId());
  }

  /**
   * Determine the filemame by checking if one already exists and return an incremented
   * value much like downloads from a browser
   * @param arg0 The full patch to the file
   * @param arg1 The filename itself
   * @param arg2 The starting number (0 means first try without the ())
   * @return
   */
  private String determineFileName(String arg0, String arg1, int arg2) {
    DfLogger.debug(this,"determineFileName({0},{1},{2})",new Object[] {arg0,arg1,arg2},null);
    File file = new File(arg0.concat("/").concat(arg1));
    if (!file.exists()) {
      return arg1;
    }
    arg2++;
    if (arg2 == 1) {
      String preStr = StringUtils.substringBeforeLast(arg1, ".");
      String postStr = StringUtils.substringAfterLast(arg1, ".");
      return determineFileName(arg0,preStr.concat(" (1).").concat(postStr),arg2);
    }
    else {
      String incString = " (".concat(Integer.toString(arg2)).concat(")");
      String preStr = StringUtils.substringBeforeLast(arg1,incString);
      String postStr = StringUtils.substringAfterLast(arg1,incString);
      return determineFileName(arg0, preStr.concat(" (").concat(Integer.toString(arg2)).concat(")").concat(postStr), arg2);
    }
  }

  /**
   * Use the ExportService to download the file.
   * @param arg0 Location to export the file to.
   * @param arg1 The Object ID to download.
   * @return <code>if the operation succeded.
   * @throws DfException
   * @throws IOException
   */
  private boolean exportItemFile(String arg0, IDfSysObject arg1) throws DfException, IOException {
    String fullPath = this.outputDir.getAbsolutePath().concat("/").concat(arg0);
    File dir = new File(fullPath);
    if (!dir.exists()) {
      FileUtils.forceMkdir(dir);
    }
    IDfExportOperation op = clientX.getExportOperation();
    op.setSession(arg1.getSession());
    op.setDestinationDirectory(fullPath);
    IDfExportNode node = (IDfExportNode) op.add(arg1);
    node.setFormat(arg1.getFormat().getName());
    boolean result = op.execute();
    if (!result) {
      IDfOperationError error = (IDfOperationError) op.getErrors().get(0);
      warningStream.println(String.format("%s: %s", "ERROR", error.getMessage()));
      DfLogger.warn(
          this,
          "Error downloading documment: {0}",
          new String[] {error.getException().getMessage()},
          (Throwable) error.getException());
    }
    return result;
  }

  private Iterable<Object> getIterbleNode(ObjectNode node) {
    return new Iterable<Object>() {
      @Override
      public Iterator<Object> iterator() {
        return new Iterator<Object>() {
          private Iterator<String> keys = node.fieldNames();

          @Override
          public boolean hasNext() {
            return keys.hasNext();
          }

          @Override
          public Object next() {
            JsonNode currentNode = node.get(keys.next());
            return valueFromJsonNode(currentNode);
          }
        };
      }
    };
  }

  private Object valueFromJsonNode(JsonNode node) {
    DfLogger.debug(this, "Processing Json Node: {0}", new Object[] {node}, null);
    if (node.isTextual()) {
      return node.asText();
    } else if (node.isBoolean()) {
      return node.asBoolean();
    } else if (node.isInt()) {
      return node.asInt();
    } else if (node.isLong()) {
      return node.asLong();
    } else if (node.isDouble()) {
      return node.asDouble();
    } else if (node.isNull()) {
      return null;
    } else if (node.isObject()) {
      return null;
    } else if (node.isArray()) {
      return getIterbleNode((ObjectNode) node);
    } else {
      return null;
    }
  }

  private void printWarning(String level, String message) {
    warningStream.println(String.format("%s: %s", level, message));
  }
}
