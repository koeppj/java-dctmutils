package net.koeppster.dctm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
public class UtilsArgsParserFactory {

    private HashMap<String,ArgType> stdArgTypes = new HashMap<String,ArgType>();
    private ArgumentParser parser;
    private Subparsers subParsers;
    private Properties defaultsProps = new Properties();

    public static final String ARG_PORT = "port";
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

    public Namespace getArguments(String[] args) throws ArgumentParserException, FileNotFoundException, IOException {
        /*
         * First parse to to see if a config file has been specified.
         */
        Namespace ns = parser.parseArgs(args);
        if (null != ns.get(ARG_CONFIG)) {
            File configFile = ns.get(ARG_CONFIG);
            this.defaultsProps.load(new FileInputStream(configFile));
            return parser.parseArgs(args);
        }
        return ns;
    }

    private void addConfigArg() {
        ArgType configArgType = stdArgTypes.get(ARG_CONFIG);
        parser.addArgument(configArgType.getargNames())
                .dest(ARG_CONFIG)
                .help(configArgType.getHelpText())
                .type(Arguments.fileType().verifyCanRead());
    }

    /**
     * 
     * @param parser
     * @param argName
     * @return
     */
    private Argument addSDtdArgument(ArgumentParser parser, String argName) {
        ArgType argType = stdArgTypes.get(argName);
        @SuppressWarnings("unchecked")
        Argument newArg = 
            parser.addArgument(argType.getargNames())
                .dest(argName)
                .help(argType.getHelpText())
                .type(argType.getArgType());
        return newArg;
    }

    private Argument addHostArg(ArgumentParser arg0) {
        return addSDtdArgument(arg0, ARG_HOST);
    }

    private Argument addPortArg(ArgumentParser arg0) {
        return addSDtdArgument(arg0, ARG_PORT);
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
        addPortArg(pingBrokerCmds).required(true);
    }

    private void addPrintMapCmd() {
        Subparser pingBrokerCmds = subParsers.addParser(CMD_PRINTMAP).help("Print Docbroker Map");
        addHostArg(pingBrokerCmds).required(true);
        addPortArg(pingBrokerCmds).required(true);
    }

    private void addPingDocbaseCmd() {
        Subparser cmd = subParsers.addParser(CMD_PINGDOCBASE).help("Ping Docbase");
        addHostArg(cmd).required(true);
        addPortArg(cmd).required(true);
        addRepoArg(cmd).required(true);
    }

    private void addCheckLoginCmd() {
        Subparser cmd = subParsers.addParser(CMD_CHECKLOGIN).help("Check Login");
        addHostArg(cmd).required(true);
        addPortArg(cmd).required(true);
        addRepoArg(cmd).required(true);
        addUserArg(cmd).required(true);
        addPasswordArg(cmd).required(true);
    }

    private void fillAllStdArgs(Properties props) {
        stdArgTypes.put(ARG_HOST, new ArgType(new String[] {"-b", "--broker"},"Docborker Host Name or IP",null,props));
        stdArgTypes.put(ARG_PORT, new ArgType(new String[] {"-p", "--port"}, "Docborker Port",null, props,Integer.class));
        stdArgTypes.put(ARG_REPO, new ArgType(new String[] {"-r", "--repo"}, "Repository",null,props));
        stdArgTypes.put(ARG_USER, new ArgType(new String[] {"-u", "--user"}, "User Name",null,props));
        stdArgTypes.put(ARG_PASS, new ArgType(new String[] {"--pass"}, "Password",null,props));
        stdArgTypes.put(ARG_CONFIG, new ArgType(new String[] {"-c","--config"},"Config File","Properties file containing key value pairs for defaults",props));
    }

    /**
     * Constructor
     * @param prog The name of the Arg Parset
     * @param args The command link args.
     */
    public UtilsArgsParserFactory(String prog) {

        this.parser = ArgumentParsers.newFor(prog).build();
        fillAllStdArgs(defaultsProps);

        addConfigArg();
        subParsers = 
            parser.addSubparsers().description("Available Comands").dest(ARG_CMD);

        addPingBrokerCmd();
        addPingDocbaseCmd();
        addPrintMapCmd();
        addCheckLoginCmd();
    } 

    public void printHelp() {
        parser.printHelp();
    }
}
