/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package net.koeppster.dctm.utils;

import com.documentum.fc.common.*;

import java.io.File;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Utils {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //Configurator.setRootLevel(Level.OFF);
    int exitCode = 0;
    try {
      File configFile = findConfigFile(args);
      UtilsArgsParserFactory parser = new UtilsArgsParserFactory("dctmutils", configFile);
      Namespace ns = parser.getArguments(args);
      ((UtilsFunction) ns.get("func")).execute(ns);
    } catch (ArgumentParserException e) {
      DfLogger.error(Utils.class, "Failed to parse arguments.  Error {0}", new String[] {e.getMessage()}, e);
      System.err.println(e.getMessage());
      exitCode = 1;
    } catch (UtilsException e) {
      DfLogger.error(Utils.class, "Failed to execute command.  Error {0}", new String[] {e.getMessage()}, e);
      System.err.println(e.getMessage());
      exitCode = 1;
    } catch (IllegalArgumentException e) {
      DfLogger.error(Utils.class, "Failed to open file.  Error {0}", new String[] {e.getMessage()}, e);
      System.err.printf("Configuration File cound not be read.%n");
      System.err.printf("Error reported:%s.%n", e.getMessage());
      exitCode = 1;
    } catch (Throwable t) {
      DfLogger.fatal(Utils.class, "Unexpected error {0}", new String[] {t.getMessage()}, t);
      System.err.printf("Unexpected error %s%n", t.getMessage());
      exitCode = 1;
    } 
    System.exit(exitCode);
  }

  private static File findConfigFile(String[] args) {
      // Loop through the array of strings
      for (int i = 0; i < args.length; i++) {
          // Check for '-c' or '--config'
          if ("-c".equals(args[i]) || "--config".equals(args[i])) {
              // Ensure there is a next element in the array
              if (i + 1 < args.length) {
                  // Get the next element which should be the file path
                  File configFile = new File(args[i + 1]);
  
                  // Check if the file exists and is readable
                  if (configFile.exists() && configFile.canRead()) {
                      return configFile; // Return the valid file
                  } else {
                      throw new IllegalArgumentException("File cannot be read: " + args[i + 1]);
                  }
              } else {
                  throw new IllegalArgumentException("No file path provided after '-c' or '--config'");
              }
          }
      }
      
      // Return null if no '-c' or '--config' element is found
      return null;
  }
}
