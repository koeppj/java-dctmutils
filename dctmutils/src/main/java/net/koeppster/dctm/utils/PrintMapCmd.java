package net.koeppster.dctm.utils;

import com.documentum.fc.client.IDfDocbaseMap;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sourceforge.argparse4j.inf.Namespace;

public class PrintMapCmd extends AbstractCmd {

    @Override
    public void execute(Namespace ns) throws UtilsException {
    try {
      IDfDocbaseMap map = AbstractCmd.getDocbaseMap(ns.get(UtilsArgsParserFactory.ARG_HOST));
      ObjectNode rootNode = AbstractCmd.getJsonFromTypedObject(map);
      mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, rootNode);
    } catch (Throwable e) {
      throw new UtilsException(String.format("Failed to print Docbase Map.  Error is %s",e.getMessage()), e);    }
  }
}
