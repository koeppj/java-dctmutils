package net.koeppster.dctm.exporter;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.acs.IDfAcsTransferPreferences;
import com.documentum.fc.client.acs.impl.DfAcsTransferPreferences;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.operations.IDfExportNode;
import com.documentum.operations.IDfExportOperation;
import com.documentum.operations.IDfOperationError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import me.tongfei.progressbar.ProgressBar;
import net.koeppster.dctm.utils.AbstractCmd;
import net.koeppster.dctm.utils.PasswordString;
import net.koeppster.dctm.utils.StringArray;
import net.koeppster.dctm.utils.StringArrayType;
import net.koeppster.dctm.utils.UtilsArgsParserFactory;
import net.koeppster.dctm.utils.UtilsException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

public class ExportCmd extends AbstractCmd {

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

  private static final String INFO_LEVEL = "INFO";
  private static final String WARN_LEVEL = "WARN";

  private IDfSession session = null;
  private CSVPrinter exportPrinter = null;
  private PrintStream warningStream = System.err;
  private ArrayList<String> attributes = new ArrayList<String>();
  private ArrayList<String> customAttribs = null;
  private ExportDatabaseManager queueManager = null;
  private File outputDir = null;
  private String candidateQuery = null;

  @Override
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
  }

  private void buildCandidateList() throws UtilsException {
    DfLogger.debug(this, "Building candidate list", null, null);
    IDfCollection coll = null;
    try {
      IDfQuery query = new DfQuery();
      query.setDQL(
          "SELECT i_chronicle_id, r_object_id, r_modify_date FROM "
              .concat(this.candidateQuery));
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
    }
  }

  private void addToandidateList(ExportQueueItem arg0) {
    queueManager.putItem(arg0);
  }

  private void processCandidateList() throws UtilsException {
    DfLogger.debug(this, "Processing Queue Items", null, null);
    Set<ExportQueueItem> items = queueManager.getOpenItemList();
    for (ExportQueueItem obj : ProgressBar.wrap(items, "Processing Candidate List")) {
      processCandidate(obj);
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
    op.setSession(session);
    op.setDestinationDirectory(fullPath);
    IDfExportNode node = (IDfExportNode) op.add(arg1);
    node.setFormat(arg1.getFormat().getName());
    boolean result = op.execute();
    if (!result) {
      IDfOperationError error = (IDfOperationError) op.getErrors().get(0);
      printWarning("ERROR", error.getMessage());
      DfLogger.warn(this,"Error downloading documment: {0}", new String[] {error.getException().getMessage()}, (Throwable) error.getException());
    }
    return result;
  }

  private void processCandidate(ExportQueueItem arg0) throws UtilsException {
    DfLogger.debug(this, "Processing candidate {0}", new String[] {arg0.toString()}, null);

    queueManager.markItemInprogress(arg0.getChronicleId());

    try {
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

      if (null != outputDir) {
        exportItemFile(firstPath, obj);
      }
    } catch (DfException | IOException e) {
      DfLogger.error(this, "Error processing candidate {0}", new String[] {arg0.toString()}, e);
      printWarning(WARN_LEVEL, String.format("Error processing candidate: %s", e.getMessage()));
      throw new UtilsException(
          String.format("Documentum Error Processing List: %s", e.getMessage()), e);
    }
    queueManager.markItemComplete(arg0.getChronicleId());
  }

  private void initialize(Namespace ns) throws UtilsException {
    DfLogger.debug(this, "Initializing", null, null);
    try {
      IDfClient client = getClient(ns.get(UtilsArgsParserFactory.ARG_HOST));
      IDfLoginInfo loginInfo = new DfLoginInfo();
      loginInfo.setUser(ns.getString(UtilsArgsParserFactory.ARG_USER));
      PasswordString password = (PasswordString) ns.get(UtilsArgsParserFactory.ARG_PASS);
      loginInfo.setPassword(password.getPassword());
      session = client.newSession(ns.getString(UtilsArgsParserFactory.ARG_REPO), loginInfo);
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

    boolean resetDb = ns.get(ARG_RESET);
    this.queueManager = new ExportDatabaseManager((File) ns.get(ARG_DATABASE),resetDb);

    this.outputDir = (File)ns.get(ARG_DIR);

    this.candidateQuery = ns.get(ARG_QUERY);
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

  private void printWarning(String level, String message) {
    warningStream.println(String.format("%s: %s", level, message));
  }

  private void shutdown() {
    try {
      if (session != null && session.isConnected()) {
        DfLogger.debug(this, "Disconnecting from Documentum", null, null);
        session.disconnect();
      }
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
    } catch (DfException | IOException e) {
      DfLogger.warn(this, "Error Shutting Down", null, e);
    }
    if (null != queueManager) {
      queueManager.shutdown();
    }
  }
}
