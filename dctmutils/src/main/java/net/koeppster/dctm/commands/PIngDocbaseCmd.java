package net.koeppster.dctm.commands;

import com.documentum.fc.common.DfException;

import net.koeppster.dctm.utils.UtilsArgsParserFactory;
import net.koeppster.dctm.utils.UtilsException;
import net.koeppster.dctm.utils.UtilsFunction;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PIngDocbaseCmd extends AbstractCmd implements UtilsFunction {

  public static final String CMD_PINGDOCBASE = "pingdocbase";

  public static void addCommandToArgParser(UtilsArgsParserFactory argParser) throws ArgumentParserException {
    Subparser cmd = argParser.addSubparser(CMD_PINGDOCBASE, "Ping Docbase", new PIngDocbaseCmd());
    argParser.addHostArg(cmd, false);
    argParser.addRepoArg(cmd);
  }

public void execute(Namespace ns) throws UtilsException {
    try {
      getServerMap(ns.get(UtilsArgsParserFactory.ARG_HOST), ns.get(UtilsArgsParserFactory.ARG_REPO));
      System.out.printf("Found docbase %s%n", ns.get(UtilsArgsParserFactory.ARG_REPO));
    } catch (DfException e) {
      throw new UtilsException(String.format("Docbase not found.  Error is %s",e.getMessage()), e);
    }
  }
}