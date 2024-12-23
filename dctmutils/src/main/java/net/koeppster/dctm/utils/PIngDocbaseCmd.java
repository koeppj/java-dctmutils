package net.koeppster.dctm.utils;

import com.documentum.fc.common.DfException;

import net.sourceforge.argparse4j.inf.Namespace;

public class PIngDocbaseCmd extends AbstractCmd {

  @Override
  public void execute(Namespace ns) throws UtilsException {
    try {
      getServerMap(ns.get(UtilsArgsParserFactory.ARG_HOST), ns.get(UtilsArgsParserFactory.ARG_REPO));
      System.out.printf("Found docbase %s%n", ns.get(UtilsArgsParserFactory.ARG_REPO));
    } catch (DfException e) {
      throw new UtilsException(String.format("Docbase not found.  Error is %s",e.getMessage()), e);
    }
  }
}