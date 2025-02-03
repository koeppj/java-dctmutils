package net.koeppster.dctm.commands.exporter;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.tongfei.progressbar.ProgressBar;
import net.koeppster.dctm.commands.AbstractCmd;
import net.koeppster.dctm.types.PasswordString;
import net.koeppster.dctm.types.StringArray;
import net.koeppster.dctm.types.StringArrayType;
import net.koeppster.dctm.utils.UtilsArgsParserFactory;
import net.koeppster.dctm.utils.UtilsException;
import net.koeppster.dctm.utils.UtilsFunction;
import net.koeppster.utils.LockFileManager;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class ExportCmd extends AbstractCmd implements UtilsFunction {

  public static final String CMD_EXPORT = "export";
  public static final String ARG_FILE = "file";
  public static final String[] ARG_FILE_NAMES = {"-f", "--file"};
  public static final String ARG_FILE_HELP =
      "Output File containing exported file paths and metadata (in CSV Format)";
  public static final String ARG_DIR = "output";
  public static final String[] ARG_DIR_NAMES = {"-o", "--output"};
  public static final String ARG_DIR_HELP = "Root output directory for exported files";
  public static final String ARG_QUERY = "query";
  public static final String[] ARG_QUERY_NAMES = {"-q", "--query"};
  public static final String ARG_QUERY_HELP =
      "Qualification (everything after 'SELECT * FROM ') to use to select objects to export";
  public static final String ARG_ATTRIBS = "attribs";
  public static final String[] ARG_ATTRIBS_NAMES = {"-a", "--attribs"};
  public static final String ARG_ATTRIBS_HELP =
      "Comma seperated list of attributes to include in the export";
  public static final String ARG_DATABASE = "database";
  public static final String[] ARG_DATABASE_NAMES = {"-d", "--database"};
  public static final String ARG_DATABASE_HELP =
      "Filename of the database containing the items to be processed (for restarts)";
  public static final String ARG_WARNING = "warning";
  public static final String[] ARG_WARNING_NAMES = {"-w", "--warning"};
  public static final String ARG_WARNING_HELP = "File to write warnings to (default is stderr)";
  public static final String ARG_RESET = "reset";
  public static final String[] ARG_RESET_NAMES = {"--reset"};
  public static final String ARG_RESET_HELP =
      "Flag that, if specified, resets the Queue Items database";
  public static final String ARG_THREADS = "threads";
  public static final String[] ARG_THREADS_NAMES = {"-t","--threads"};
  public static final String ARG_THREADS_HELP = "Number of Candidate Parser Thresds (default is three)";

  private IDfSessionManager sessionManager = null;
  private CSVPrinter exportPrinter = null;
  private PrintStream warningStream = System.err;
  private ArrayList<String> attributes = new ArrayList<String>();
  private ArrayList<String> customAttribs = null;
  private ExportDatabaseManager queueManager = null;
  private File outputDir = null;
  private String candidateQuery = null;
  private ExecutorService itemProcessorService = null;
  private String repo = null;
  private LockFileManager lockFileManager = null;
  
    public void execute(Namespace arg0) throws UtilsException {
      try {
        initialize(arg0);
        buildCandidateList();
        processCandidateList();
      } finally {
        shutdown();
      }
    }
  
    public static void addCommandToArgParser(UtilsArgsParserFactory argParser)
        throws ArgumentParserException {
      Subparser cmd = argParser.addSubparser(CMD_EXPORT, "Export Objects", new ExportCmd());
  
      argParser.addHostArg(cmd, false);
      argParser.addRepoArg(cmd);
      argParser.addUserArg(cmd);
      argParser.addPasswordArg(cmd);
      argParser.addArgument(
          cmd,
          ARG_DIR_NAMES,
          ARG_DIR,
          ARG_DIR_HELP,
          true,
          Arguments.fileType().verifyCanWrite().verifyIsDirectory());
      argParser.addArgument(
          cmd,
          ARG_FILE_NAMES,
          ARG_FILE,
          ARG_FILE_HELP,
          false,
          Arguments.fileType().verifyCanCreate());
      argParser.addArgument(cmd, ARG_QUERY_NAMES, ARG_QUERY, ARG_QUERY_HELP, false);
      argParser.addArgument(
          cmd, ARG_ATTRIBS_NAMES, ARG_ATTRIBS, ARG_ATTRIBS_HELP, false, new StringArrayType());
      argParser.addArgument(
          cmd,
          ARG_DATABASE_NAMES,
          ARG_DATABASE,
          ARG_DATABASE_HELP,
          true,
          Arguments.fileType().verifyCanCreate());
      argParser.addArgument(
          cmd,
          ARG_WARNING_NAMES,
          ARG_WARNING,
          ARG_WARNING_HELP,
          false,
          Arguments.fileType().verifyCanCreate());
      argParser.addArgumentFlag(cmd, ARG_RESET_NAMES, ARG_RESET, ARG_RESET_HELP);
      argParser.addArgument(cmd, ARG_THREADS_NAMES, ARG_THREADS, ARG_THREADS_HELP, false, Integer.class);
    }
  
    private void buildCandidateList() throws UtilsException {
      DfLogger.debug(this, "Building candidate list", null, null);
      IDfCollection coll = null;
      IDfSession session = null;
      try {
        session = sessionManager.getSession(repo);
        IDfQuery query = new DfQuery();
        query.setDQL(
            "SELECT i_chronicle_id, r_object_id, r_modify_date FROM ".concat(this.candidateQuery));
        coll = query.execute(session, IDfQuery.DF_READ_QUERY);
        IDfCollectionIterator iter = new IDfCollectionIterator(coll);
        Iterable<ExportQueueItem> iterable = () -> iter;
        for (ExportQueueItem obj : ProgressBar.wrap(iterable, "Building Candidate List")) {
          addToandidateList(obj);
        }
      } catch (DfException e) {
        throw new UtilsException(String.format("Error querying Documentum: %s", e.getMessage()), e);
      } finally {
        if (coll != null) {
          try {
            coll.close();
          } catch (DfException e) {
            DfLogger.warn(this, "Error closing collection", null, e);
          }
        }
        if ((null != session) && session.isConnected()) {
          sessionManager.release(session);
        }
      }
    }
  
    private void addToandidateList(ExportQueueItem arg0) {
      queueManager.putItem(arg0);
    }
  
    private void processCandidateList() throws UtilsException {
      DfLogger.debug(this, "Processing Queue Items", null, null);
      Set<ExportQueueItem> items = queueManager.getOpenItemList();
      CountDownLatch latch = new CountDownLatch(items.size());
      try (ProgressBar pb = new ProgressBar("Processing Candidate List", items.size())) {
        for (ExportQueueItem obj: items) {
          ExportQueueItemProcessor processor =
              new ExportQueueItemProcessor(
                  queueManager, sessionManager,repo, customAttribs, exportPrinter, outputDir, warningStream, lockFileManager);
          itemProcessorService.submit(() -> {
              try {
                  processor.processCandidate(obj);
              } catch (UtilsException | InterruptedException e) {
                  Thread.currentThread().interrupt();
                  throw new RuntimeException(e);
              }
              pb.step();
              latch.countDown();
          });
        }
        latch.await();
      } catch (RuntimeException | InterruptedException e) {
        Thread.currentThread().interrupt(); 
        throw new UtilsException("Issue processing item",e);
      } 
      finally {
        itemProcessorService.shutdown();
        try {
            if (!itemProcessorService.awaitTermination(60, TimeUnit.SECONDS)) {
                itemProcessorService.shutdownNow();
                if (!itemProcessorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor service did not terminate.");
                }
            }
        } catch (InterruptedException e) {
            itemProcessorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
      }              
    }
  
    private void initialize(Namespace ns) throws UtilsException {
      DfLogger.debug(this, "Initializing", null, null);
      this.repo = ns.getString(UtilsArgsParserFactory.ARG_REPO);
      try {

        IDfClient client = getClient(ns.get(UtilsArgsParserFactory.ARG_HOST));
        IDfLoginInfo loginInfo = new DfLoginInfo();
        loginInfo.setUser(ns.getString(UtilsArgsParserFactory.ARG_USER));
        PasswordString password = (PasswordString) ns.get(UtilsArgsParserFactory.ARG_PASS);
        loginInfo.setPassword(password.getPassword());
        sessionManager = client.newSessionManager();
        sessionManager.setIdentity(ns.getString(UtilsArgsParserFactory.ARG_REPO), loginInfo);
      } catch (DfException e) {
        throw new UtilsException(
            String.format("Error connecting to Documentum: %s", e.getMessage()), e);
      } catch (IOException e) {
        throw new UtilsException(String.format("Error Decrypting Password: %s", e.getMessage()), e);
      }
      attributes.add("i_chronicle_id");
      attributes.add("r_object_id");
      attributes.add("path");
      if (null != ns.get(ARG_ATTRIBS)) {
        if (null != ns.get(ARG_ATTRIBS)) {
          customAttribs = ((StringArray) ns.get(ARG_ATTRIBS)).toArrayList();
          for (String s : customAttribs) {
            if (!attributes.contains(s)) {
              attributes.add(s);
            }
          }
        }
        DfLogger.debug(this, "Attributes List: {0}", new Object[] {attributes}, null);
      }
      if (null != ns.get(ARG_FILE)) {
        try {
          exportPrinter = new CSVPrinter(new FileWriter(ns.getString(ARG_FILE)), CSVFormat.EXCEL);
          exportPrinter.printRecord(attributes);
        } catch (Exception e) {
          throw new UtilsException(
              String.format("Error creating export file: %s", e.getMessage()), e);
        }
      }
      if (null != ns.get(ARG_WARNING)) {
        try {
          warningStream = new PrintStream((File) ns.get(ARG_WARNING));
        } catch (Exception e) {
          throw new UtilsException(
              String.format("Error creating warning file: %s", e.getMessage()), e);
        }
      }
  
      // Set up the retry DB and file system lock
      File dbFile = (File) ns.get(ARG_DATABASE);
      boolean resetDb = ns.get(ARG_RESET);
      this.queueManager = new ExportDatabaseManager(dbFile, resetDb);

      String dbLocation = dbFile.getParentFile().getAbsolutePath();
      this.lockFileManager = new LockFileManager(dbLocation.concat("/").concat("filesystem.lck"));
  
      this.outputDir = (File) ns.get(ARG_DIR);
  
      this.candidateQuery = ns.get(ARG_QUERY);
  
      if (null == ns.get(ARG_THREADS)) {
        this.itemProcessorService = Executors.newFixedThreadPool(3);
      } else {
        this.itemProcessorService = Executors.newFixedThreadPool((Integer) ns.get(ARG_THREADS));
      }
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

  private void shutdown() {
    try {
      if (exportPrinter != null) {
        DfLogger.debug(this, "Closing Export File", null, null);
        exportPrinter.flush();
        exportPrinter.close();
      }
      if (warningStream != null) {
        DfLogger.debug(this, "Closing Warning Stream", null, null);
        warningStream.flush();
        warningStream.close();
      }
    } catch (IOException e) {
      DfLogger.warn(this, "Error Shutting Down", null, e);
    }
    if (null != queueManager) {
      queueManager.shutdown();
    }
  }
}
