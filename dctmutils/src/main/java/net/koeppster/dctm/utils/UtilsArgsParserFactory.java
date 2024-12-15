package net.koeppster.dctm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;

import com.documentum.fc.common.DfLogger;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class UtilsArgsParserFactory {

  private HashMap<String, ArgType> stdArgTypes = new HashMap<String, ArgType>();
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
  public static final String CMD_EXPORT = "export";

  public Namespace getArguments(String[] args)
      throws ArgumentParserException, FileNotFoundException, IOException {
    /*
     * First parse to to see if a config file has been specified.
     */
    Namespace ns = parser.parseArgs(args);
    if (null != ns.get(ARG_CONFIG)) {
      File configFile = (File) ns.get(ARG_CONFIG);
      DfLogger.debug(this, "Loading Config from file {0}", new String[] {configFile.getAbsolutePath()}, null);
      System.out.printf("Using config file %s to load defaults%n",configFile.getAbsolutePath());
      this.defaultsProps.load(new FileInputStream(configFile));
      return parser.parseArgs(args);
    }
    return ns;
  }

  private void addConfigArg() {
    ArgType configArgType = stdArgTypes.get(ARG_CONFIG);
    parser
        .addArgument(configArgType.getargNames())
        .dest(ARG_CONFIG)
        .help(configArgType.getHelpText())
        .type(Arguments.fileType().verifyCanRead().verifyExists());
  }

  /**
   * @param parser
   * @param argName
   * @return
   */
  private Argument addSDtdArgument(ArgumentParser parser, String argName) {
    ArgType argType = stdArgTypes.get(argName);
    @SuppressWarnings("unchecked")
    Argument newArg =
        parser
            .addArgument(argType.getargNames())
            .dest(argName)
            .help(argType.getHelpText())
            .type(argType.getArgType());
    return newArg;
  }

  private Argument addHostArg(ArgumentParser arg0) {
    return addSDtdArgument(arg0, ARG_HOST).type(new DocbrokerSpec());
  }

  private Argument addRepoArg(ArgumentParser arg0) {
    return addSDtdArgument(arg0, ARG_REPO);
  }

  private Argument addUserArg(ArgumentParser arg0) {
    return addSDtdArgument(arg0, ARG_USER);
  }

  private Argument addPasswordArg(ArgumentParser arg0) {
    return addSDtdArgument(arg0, ARG_PASS);
  }

  //
  // Add the Subpaesers
  //
  private void addPingBrokerCmd() {
    Subparser pingBrokerCmds = subParsers.addParser(CMD_PINGBROKER).help("Ping Docbroker");
    addHostArg(pingBrokerCmds).required(true);
  }

  private void addPrintMapCmd() {
    Subparser pingBrokerCmds = subParsers.addParser(CMD_PRINTMAP).help("Print Docbroker Map");
    addHostArg(pingBrokerCmds).required(false);
  }

  private void addPingDocbaseCmd() {
    Subparser cmd = subParsers.addParser(CMD_PINGDOCBASE).help("Ping Docbase");
    addHostArg(cmd).required(true);
    addRepoArg(cmd).required(true);
  }

  private void addCheckLoginCmd() {
    Subparser cmd = subParsers.addParser(CMD_CHECKLOGIN).help("Check Login");
    addHostArg(cmd).required(true);
    addRepoArg(cmd).required(true);
    addUserArg(cmd).required(true);
    addPasswordArg(cmd).required(true);
  }

  private void addExportCmd() {
    Subparser cmd = subParsers.addParser(CMD_EXPORT).help("Export Objects");
    addHostArg(cmd).required(false);
    addRepoArg(cmd).required(true);
    addUserArg(cmd).required(true);
    addPasswordArg(cmd).required(true);
  }

  private void fillAllStdArgs(Properties props) {
    stdArgTypes.put(
        ARG_HOST,
        new ArgType(new String[] {"-b", "--broker"}, "Docborker Host and Port (<hostname or IP>[:<port>])", null, props));
    stdArgTypes.put(
        ARG_REPO, new ArgType(new String[] {"-r", "--repo"}, "Repository", null, props));
    stdArgTypes.put(ARG_USER, new ArgType(new String[] {"-u", "--user"}, "User Name", null, props));
    stdArgTypes.put(ARG_PASS, new ArgType(new String[] {"-p","--password"}, "Password", null, props));
    stdArgTypes.put(
        ARG_CONFIG,
        new ArgType(
            new String[] {"-c", "--config"},
            "Config File",
            "Properties file containing key value pairs for defaults",
            props));
  }

  /**
   * Constructor for Utilities Argument Parser
   *
   * @param prog The name of the Arg Parset
   * @param args The command link args.
   */
  public UtilsArgsParserFactory(String prog) {
    DfLogger.debug(this, "Entering UtilsArgParserFactory", null, null);

    this.parser = ArgumentParsers.newFor(prog).build();

    // Construct the map of arg info that are used for multiple subcommands.
    fillAllStdArgs(defaultsProps);

    // The arg use to set the pproperties file containng defaults.
    addConfigArg();

    subParsers = parser.addSubparsers().description("Available Comands").dest(ARG_CMD);

    addPingBrokerCmd();
    addPingDocbaseCmd();
    addPrintMapCmd();
    addCheckLoginCmd();
    DfLogger.debug(this, "Exiting UtilsArgParserFactory", null, null);
  }

  public void printHelp() {
    parser.printHelp();
  }

  public void printHelp(OutputStream stream) {
    parser.printHelp(new PrintWriter(stream));
  }
}
