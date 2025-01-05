/**
 * A factory class for creating and configuring an ArgumentParser for command-line utilities.
 * This class provides methods to add various arguments and subparsers to the ArgumentParser.
 * It also supports loading default properties from a specified file.
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * UtilsArgsParserFactory factory = new UtilsArgsParserFactory("progName", new File("defaults.properties"));
 * Namespace ns = factory.getArguments(args);
 * }
 * </pre>
 * 
 * <p>Supported arguments:</p>
 * <ul>
 *   <li>-c, --config: Properties file containing defaults</li>
 *   <li>-b, --broker: Docbroker (will use dfc.properties if not specified)</li>
 *   <li>-r, --repo: Repository Name</li>
 *   <li>-u, --user: Repository User Name</li>
 *   <li>-p, --password: Repository Password</li>
 * </ul>
 * 
 * <p>Supported subcommands:</p>
 * <ul>
 *   <li>Subcommands are dynamically loaded from classes implementing UtilsFunction interface in the package "net.koeppster"</li>
 * </ul>
 * 
 * <p>Exceptions:</p>
 * <ul>
 *   <li>ArgumentParserException: If there is an error in parsing the arguments</li>
 *   <li>FileNotFoundException: If a specified file is not found</li>
 *   <li>IOException: If an I/O error occurs</li>
 *   <li>NoSuchMethodException: If a specified method is not found</li>
 *   <li>SecurityException: If a security violation occurs</li>
 *   <li>IllegalAccessException: If access to a method is illegal</li>
 *   <li>IllegalArgumentException: If an illegal argument is passed</li>
 *   <li>InvocationTargetException: If an exception is thrown by an invoked method</li>
 *   <li>ClassNotFoundException: If a specified class is not found</li>
 * </ul>
 */
package net.koeppster.dctm.utils;

import com.documentum.fc.common.DfLogger;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;
import net.koeppster.dctm.types.DocbrokerSpecType;
import net.koeppster.dctm.types.PasswordType;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.impl.type.ReflectArgumentType;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;


public class UtilsArgsParserFactory {

  private ArgumentParser parser;
  private Subparsers subParsers;
  private Properties defaultsProps = new Properties();

  public static final String ARG_HOST = "host";
  public static final String ARG_REPO = "repo";
  public static final String ARG_USER = "user";
  public static final String ARG_PASS = "pass";
  public static final String ARG_CONFIG = "config";
  public static final String ARG_CMD = "cmd";

  /**
   * Parses the provided command-line arguments and returns a Namespace object
   * containing the parsed arguments.
   *
   * @param args the command-line arguments to be parsed
   * @return a Namespace object containing the parsed arguments
   * @throws ArgumentParserException if there is an error in parsing the arguments
   * @throws FileNotFoundException if a specified file is not found
   * @throws IOException if an I/O error occurs
   */
  public Namespace getArguments(String[] args)
      throws ArgumentParserException, FileNotFoundException, IOException {
    /*
     * First parse to to see if a config file has been specified.
     */
    Namespace ns = parser.parseArgs(args);
    return ns;
  }

  private void addConfigArg() throws ArgumentParserException {
    addArgument(
        parser,
        new String[] {"-c", "--config"},
        ARG_CONFIG,
        "Properties file containing defaults",
        false,
        Arguments.fileType().verifyCanRead().verifyExists());
  }

  /**
   * Adds the host argument to the provided ArgumentParser.
   *
   * @param arg0 The ArgumentParser to which the host argument will be added.
   * @param required A boolean indicating whether the host argument is required.
   * @throws ArgumentParserException If there is an error adding the argument to the parser.
   */
  public void addHostArg(ArgumentParser arg0, boolean required) throws ArgumentParserException {
    addArgument(
        arg0,
        new String[] {"-b", "--broker"},
        ARG_HOST,
        "Docbroker (will use dfc.properties of not specifed)",
        required,
        new DocbrokerSpecType());
  }

  /**
   * Adds an argument to the given ArgumentParser.  If dest is found in the defaultsProps, then the
   * value of the corresponding key is used as the default and <code>arg0.required(false) is called</codee>.
   *
   * @param arg0 The ArgumentParser to which the argument will be added.
   * @param argNames An array of strings representing the names of the argument.
   * @param dest The destination variable where the argument value will be stored.
   * @param helpText The help text describing the argument.
   * @param required A boolean indicating whether the argument is required.
   */
  public void addArgument(
      ArgumentParser arg0, String[] argNames, String dest, String helpText, boolean required) {
    DfLogger.debug(this, "Adding Argument {0} with no Class", new String[] {dest}, null);
    arg0.addArgument(argNames)
        .dest(dest)
        .help(helpText)
        .required((defaultsProps.getProperty(dest) == null) ? required : false)
        .setDefault(
            (defaultsProps.getProperty(dest) == null) ? null : defaultsProps.getProperty(dest));
  }

  /**
   * Adds a flag argument to the provided ArgumentParser.
   *
   * @param arg0 the ArgumentParser to which the flag argument will be added
   * @param argNames an array of names for the argument
   * @param dest the destination variable where the argument value will be stored
   * @param helpText the help text describing the argument
   */
  public void addArgumentFlag(
      ArgumentParser arg0, String[] argNames, String dest, String helpText)   {
    DfLogger.debug(this, "Adding Flag Argument {0}", new String[] {dest}, null);
    arg0.addArgument(argNames)
        .dest(dest)
        .help(helpText)
        .type(Boolean.class)
        .action(Arguments.storeTrue())
        .setDefault(
            (defaultsProps.getProperty(dest) == null)
                ? false
                : Boolean.valueOf(defaultsProps.getProperty(dest)));
  }

  /**
   * Adds an argument to the provided ArgumentParser.
   * 
   * @param arg0 The ArgumentParser to which the argument will be added.
   * @param argNames An array of names for the argument.
   * @param dest The destination variable name for the argument.
   * @param helpText The help text describing the argument.
   * @param required A boolean indicating whether the argument is required.
   * @param argType The of type the argument.
   * @throws ArgumentParserException
   */
  public void addArgument(
      ArgumentParser arg0,
      String[] argNames,
      String dest,
      String helpText,
      boolean required,
      Class<?> argType) throws ArgumentParserException {
    DfLogger.debug(
        this,
        "Adding Argument {0} with Class of type {1}",
        new String[] {dest, argType.getClass().getName()},
        null);
    Argument arg =
        arg0.addArgument(argNames)
            .dest(dest)
            .help(helpText)
            .required((defaultsProps.getProperty(dest) == null) ? required : false)
            .type(argType);
    if (defaultsProps.getProperty(dest) != null) {
      arg.setDefault(
          (defaultsProps.getProperty(dest) == null)
              ? null
              : new ReflectArgumentType<>(argType)
                  .convert(arg0, arg, defaultsProps.getProperty(dest)));
    }
  }

  /**
   * Adds an argument to the provided ArgumentParser.
   *
   * @param arg0 The ArgumentParser to which the argument will be added.
   * @param argNames An array of names for the argument.
   * @param dest The destination variable name for the argument.
   * @param helpText The help text describing the argument.
   * @param required A boolean indicating whether the argument is required.
   * @param argType The class type of the argument.
   * @throws ArgumentParserException If there is an error adding the argument.
   */
  public <T> void addArgument(
      ArgumentParser arg0,
      String[] argNames,
      String dest,
      String helpText,
      boolean required,
      ArgumentType<T> argType)
      throws ArgumentParserException {
    DfLogger.debug(
        this,
        "Adding Argument {0} with ArgumentType {1}",
        new String[] {dest, argType.getClass().getName()},
        null);
    Argument arg =
        arg0.addArgument(argNames)
            .dest(dest)
            .help(helpText)
            .required((defaultsProps.getProperty(dest) == null) ? required : false)
            .type(argType);
    if (defaultsProps.getProperty(dest) != null) {
      arg.setDefault(
          (defaultsProps.getProperty(dest) == null)
              ? null
              : argType.convert(arg0, arg, defaultsProps.getProperty(dest)));
    }
  }

  /**
   * Adds the repository argument to the provided argument parser.
   *
   * @param arg0 the argument parser to which the repository argument will be added
   */
  public void addRepoArg(ArgumentParser arg0) {
    DfLogger.debug(this, "Adding Repo Argument", null, null);
    addArgument(arg0, new String[] {"-r", "--repo"}, ARG_REPO, "Repository Name", true);
  }

  /**
   * Adds the user argument to the provided argument parser.
   *
   * @param arg0 the argument parser to which the user argument will be added
   */
  public void addUserArg(ArgumentParser arg0) {
    DfLogger.debug(this, "Adding User Argument", null, null);
    addArgument(arg0, new String[] {"-u", "--user"}, ARG_USER, "Repository User Name", true);
  }

  /**
   * Adds a password argument to the provided ArgumentParser.
   *
   * @param arg0 the ArgumentParser to which the password argument will be added
   * @throws ArgumentParserException if there is an error adding the argument
   */
  public void addPasswordArg(ArgumentParser arg0) throws ArgumentParserException {
    DfLogger.debug(this, "Adding Password Argument", null, null);
    addArgument(
        arg0,
        new String[] {"-p", "--password"},
        ARG_PASS,
        "Repository Password",
        true,
        new PasswordType());
  }

  /**
   * Adds a subparser to the parser with the specified command, help text, and function.
   *
   * @param cmd the command name for the subparser
   * @param helpText the help text for the subparser
   * @param func the {@link UtilFunction} to be executed when the subparser is invoked
   * @return the created subparser
   */
  public Subparser addSubparser(String cmd, String helpText, UtilsFunction func) {
    Subparser sub = subParsers.addParser(cmd).help(helpText).setDefault("func", func);
    return sub;
  }

  /**
   * Constructor for Utilities Argument Parser
   *
   * @param prog The name of the Arg Parser
   * @param propsFile The @{link File} containing the default properties. If null, no defaults are
   *     loaded.
   * @throws ArgumentParserException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   */
  public UtilsArgsParserFactory(String prog, File propsFile)
      throws FileNotFoundException,
          IOException,
          ArgumentParserException,
          NoSuchMethodException,
          SecurityException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          ClassNotFoundException {
    DfLogger.debug(this, "Entering UtilsArgParserFactory", null, null);

    if (null != propsFile) {
      DfLogger.debug(
          this, "Loading defaults from {0}", new String[] {propsFile.getAbsolutePath()}, null);
      this.defaultsProps.load(new FileInputStream(propsFile));
    }

    this.parser = ArgumentParsers.newFor(prog).build();

    // The arg use to set the pproperties file containng defaults.
    addConfigArg();
    subParsers = parser.addSubparsers().description("Available Comands");

    addCommands();
    DfLogger.debug(this, "Exiting UtilsArgParserFactory", null, null);
  }

  private void addCommands()
      throws ClassNotFoundException,
          NoSuchMethodException,
          SecurityException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException {
    try (ScanResult result =
        new ClassGraph().enableAllInfo().acceptPackages("net.koeppster").scan()) {
      List<ClassInfo> classes = result.getClassesImplementing(UtilsFunction.class.getName());
      for (ClassInfo classInfo : classes) {
        Class<?> classZ = Class.forName(classInfo.getName());
        addCommand(classZ);
      }
    }
  }

  private void addCommand(Class<?> classZ)
      throws NoSuchMethodException,
          SecurityException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException {
    Method method = classZ.getMethod("addCommandToArgParser", this.getClass());
    method.invoke(method, this);
  }

  public void printHelp() {
    parser.printHelp();
  }

  public void printHelp(OutputStream stream) {
    parser.printHelp(new PrintWriter(stream));
  }
}
