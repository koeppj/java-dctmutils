package net.koeppster.dctm.utils;

import java.io.IOException;

import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.common.DfException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sourceforge.argparse4j.inf.Namespace;


public class PingBrokerCmd extends AbstractCmd {

  private static ObjectMapper mapper = new ObjectMapper();

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
    try {
      IDfDocbaseMap map = getDocbaseMap(ns.get(UtilsArgsParserFactory.ARG_HOST));
      System.out.printf("Docbroker %n responded%n", ns.get(UtilsArgsParserFactory.ARG_HOST));
    } catch (DfException e) {
      throw new UtilsException("Error Pinging Docbroker.  Message is %s%n", e);
    }
  }
}
