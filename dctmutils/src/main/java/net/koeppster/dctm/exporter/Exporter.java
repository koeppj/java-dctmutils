package net.koeppster.dctm.exporter;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;

public class Exporter {

  private Namespace args;
  private IDfCollection items = null;
  private IDfSessionManager sessionMg;
  private String[] propNames;
  private String dateFormat;

  /*
   * Location of output csv file containing metadata for each file extracted
   * (If the option was selected)
   */
  private File metaOutputFile = null;

  // Root folder of the output location
  private File rootOutput = null;

  public Exporter(Namespace arg0) {
    this.args = arg0;
  }

  public void export() {}

  /**
   * Constructs the filename that should be used for the object whem saved to the file system. Does
   * this by getting the doc extension based on the format of the primary rendition. If the last
   * three characters of <code>arg0.object_name</code> do not match the dos extension as specified
   * but in the <code>dm_format</code> append <code>dm_format.dos_extension</code> when returning
   * the object name.
   *
   * @param arg0 The object to process
   * @return object_name[.dos_extenson]
   */
  private String buildFileName(IDfSysObject arg0) {
    return null;
  }

  /**
   * Returns a name/value set of all object attrribute values whee the atteibute name matches an
   * entry in {@link #propNames}. Non string values will be converted to string using {@link
   * java.lang.Object#toString()} with the exception of date values. Date values will be outptut
   * according to the format specified by {@link #dateFormat}. For repeating attributes the values
   * will be converted as described above and the represented as an array
   *
   * @param arg0 The {@link com.documentum.fc.client.IDfSysObject} containing the attribute values
   * @return A {@link java.util.Map} of attribute names and values
   */
  private Map<String, Object> getProperties(IDfSysObject arg0) {
    return null;
  }

  /**
   * Returns the first complete path the object is linked to. The "first" path is the full path to
   * to the folder specified by <code>arg0.i_folder_id[0]</code>.
   *
   * @param arg0 The object id
   * @return First full path
   */
  private String getFolderPath(IDfSysObject arg0) {
    return null;
  }

  /**
   * Verify/Create Folder. Check for the presence of a folder path matching the one provided. If any
   * part does not exist, create it.
   *
   * @param arg0 Full path to the folder.
   * @return Handle to the folder
   * @throws IOException If the folder path does not exist and cannot be created
   */
  private File checkFolder(String arg0) throws IOException {
    File dir = new File(arg0);
    if (!dir.exists()) {
      FileUtils.forceMkdir(dir);
    }
    return dir;
  }

  private void setup() {}

  private void shutdown() {}
}
