package net.koeppster.dctm.utils;

import com.documentum.fc.client.IDfClient;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;

import net.sourceforge.argparse4j.inf.Namespace;

public class CheckLoginCmd extends AbstractCmd {

  @Override
  public void execute(Namespace ns) throws UtilsException {
    try {
      IDfClient client = getClient(ns.get(UtilsArgsParserFactory.ARG_HOST));
      IDfLoginInfo li = new DfLoginInfo();
      li.setUser(ns.get(ns.get(UtilsArgsParserFactory.ARG_USER)));
      li.setPassword(ns.get(ns.get(UtilsArgsParserFactory.ARG_PASS)));
      client.authenticate(ns.get(ns.get(UtilsArgsParserFactory.ARG_REPO)), li);
      System.out.printf("Authenticated to docbase %s with user %s%n", ns.get(UtilsArgsParserFactory.ARG_REPO), ns.get(UtilsArgsParserFactory.ARG_USER));
    } catch (DfException e) {
        throw new UtilsException(String.format("Error authenticating.  Error is %s",e.getMessage()), e);    }
  }
}
