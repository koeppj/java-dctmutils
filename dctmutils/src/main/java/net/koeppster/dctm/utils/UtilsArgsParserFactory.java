package net.koeppster.dctm.utils;

import com.documentum.fc.common.DfLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import net.koeppster.dctm.exporter.ExportCmd;
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

  public static final String CMD_PINGBROKER = "pingbroker";
  public static final String CMD_PINGDOCBASE = "pingdocbase";
  public static final String CMD_PRINTMAP = "printmap";
  public static final String CMD_CHECKLOGIN = "checklogin";
  public static final String CMD_ENCRYPT = "encrypt";
  public static final String CMD_EXPORT = "export";

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

  public void addArgumentFlag(
      ArgumentParser arg0, String[] argNames, String dest, String helpText) {
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

  public void addArgument(
      ArgumentParser arg0,
      String[] argNames,
      String dest,
      String helpText,
      boolean required,
      Class<?> argType)
      throws ArgumentParserException {
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

  public void addRepoArg(ArgumentParser arg0) {
    DfLogger.debug(this, "Adding Repo Argument", null, null);
    addArgument(arg0, new String[] {"-r", "--repo"}, ARG_REPO, "Repository Name", true);
  }

  public void addUserArg(ArgumentParser arg0) {
    DfLogger.debug(this, "Adding User Argument", null, null);
    addArgument(arg0, new String[] {"-u", "--user"}, ARG_USER, "Repository User Name", true);
  }

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

  //
  // Add the Subpaesers
  //
  void addPingBrokerCmd() throws ArgumentParserException {
    Subparser pingBrokerCmds =
        subParsers
            .addParser(CMD_PINGBROKER)
            .help("Ping Docbroker")
            .setDefault("func", new PingBrokerCmd());
    addHostArg(pingBrokerCmds, true);
  }

  void addPrintMapCmd() throws ArgumentParserException {
    Subparser pingBrokerCmds =
        subParsers
            .addParser(CMD_PRINTMAP)
            .help("Print Docbroker Map")
            .setDefault("func", new PrintMapCmd());
    addHostArg(pingBrokerCmds, false);
  }

  void addPingDocbaseCmd() throws ArgumentParserException {
    Subparser cmd =
        subParsers
            .addParser(CMD_PINGDOCBASE)
            .help("Ping Docbase")
            .setDefault("func", new PIngDocbaseCmd());
    addHostArg(cmd, false);
    addRepoArg(cmd);
  }

  void addCheckLoginCmd() throws ArgumentParserException {
    Subparser cmd =
        subParsers
            .addParser(CMD_CHECKLOGIN)
            .help("Check Login")
            .setDefault("func", new CheckLoginCmd());
    addHostArg(cmd, false);
    addRepoArg(cmd);
    addUserArg(cmd);
    addPasswordArg(cmd);
  }

  void addEncryptCmd() throws ArgumentParserException {
    Subparser cmd =
        subParsers
            .addParser(CMD_ENCRYPT)
            .help("Encrypt Password Text")
            .setDefault("func", new PasswordEncryptCmd());
    addArgument(
        cmd,
        new String[] {"-p", "--password"},
        ARG_PASS,
        "Password to encrypt",
        true,
        new PasswordType());
  }

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
   */
  public UtilsArgsParserFactory(String prog, File propsFile)
      throws FileNotFoundException, IOException, ArgumentParserException {
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

    addPingBrokerCmd();
    addPingDocbaseCmd();
    addPrintMapCmd();
    addCheckLoginCmd();
    addEncryptCmd();
    ExportCmd.addCommandToArgParser(this);
    DfLogger.debug(this, "Exiting UtilsArgParserFactory", null, null);
  }

  public void printHelp() {
    parser.printHelp();
  }

  public void printHelp(OutputStream stream) {
    parser.printHelp(new PrintWriter(stream));
  }

  public void addArgument(
      Subparser cmd, String argDatabase, String[] argDatabaseNames, String argDatabaseHelp) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addArgument'");
  }
}
