package net.koeppster.dctm.commands;

import com.documentum.fc.client.IDfDocbaseMap;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.koeppster.dctm.utils.UtilsArgsParserFactory;
import net.koeppster.dctm.utils.UtilsException;
import net.koeppster.dctm.utils.UtilsFunction;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PrintMapCmd extends AbstractCmd implements UtilsFunction {

    public static final String CMD_PRINTMAP = "printmap";

  public static void addCommandToArgParser(UtilsArgsParserFactory argParser) throws ArgumentParserException {
    Subparser cmd = argParser.addSubparser(CMD_PRINTMAP, "Print Docbroker Map", new PrintMapCmd());
    argParser.addHostArg(cmd, false);
  }

  public void execute(Namespace ns) throws UtilsException {
    try {
      IDfDocbaseMap map = AbstractCmd.getDocbaseMap(ns.get(UtilsArgsParserFactory.ARG_HOST));
      ObjectNode rootNode = AbstractCmd.getJsonFromTypedObject(map);
      mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, rootNode);
    } catch (Throwable e) {
      throw new UtilsException(String.format("Failed to print Docbase Map.  Error is %s",e.getMessage()), e);    }
  }
}
