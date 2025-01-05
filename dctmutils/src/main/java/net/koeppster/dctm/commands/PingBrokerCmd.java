package net.koeppster.dctm.commands;

import java.io.IOException;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;

import net.koeppster.dctm.types.DocbrokerSpec;
import net.koeppster.dctm.utils.UtilsArgsParserFactory;
import net.koeppster.dctm.utils.UtilsException;
import net.koeppster.dctm.utils.UtilsFunction;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;


public class PingBrokerCmd extends AbstractCmd implements UtilsFunction {

  public static final String CMD_PINGBROKER = "pingbroker";

  public static void addCommandToArgParser(UtilsArgsParserFactory argParser) throws ArgumentParserException {
    Subparser cmd = argParser.addSubparser(CMD_PINGBROKER, "Ping Docbroker", new PingBrokerCmd());
    argParser.addHostArg(cmd, true);
  }

/**
   * Prints a Docbroker Map. 
   * 
   * @param broker 
   * @return
   * @throws IOException 
   * @throws DatabindException 
   * @throws StreamWriteException 
   */
  public void execute(Namespace ns) throws UtilsException {
    DfLogger.debug(this, "PingBrokerCmd.execute({0})", new String[] {ns.toString()}, null);
    try {
      DocbrokerSpec spec = (DocbrokerSpec)ns.get(null == UtilsArgsParserFactory.ARG_HOST ? null : UtilsArgsParserFactory.ARG_HOST);
      getDocbaseMap(spec);
      System.out.printf("Docbroker %s responded%n", ns.get(UtilsArgsParserFactory.ARG_HOST).toString());
    } catch (DfException e) {
      throw new UtilsException("Error Pinging Docbroker.  Message is %s%n", e);
    }
  }
}
