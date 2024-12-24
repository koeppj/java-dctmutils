package net.koeppster.dctm.utils;

import java.io.IOException;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;

import net.sourceforge.argparse4j.inf.Namespace;


public class PingBrokerCmd extends AbstractCmd {

  /**
   * Prints a Docbroker Map. 
   * 
   * @param broker 
   * @return
   * @throws IOException 
   * @throws DatabindException 
   * @throws StreamWriteException 
   */
  @Override
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
