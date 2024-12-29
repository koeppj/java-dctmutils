package net.koeppster.dctm.utils;

import java.io.IOException;

import com.documentum.fc.client.IDfClient;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import net.sourceforge.argparse4j.inf.Namespace;

public class CheckLoginCmd extends AbstractCmd implements UtilsFunction{

  public void execute(Namespace ns) throws UtilsException {
    DfLogger.debug(this, "Executing CheckLoginCmd.execute({0})", new String[] {ns.toString()},null);
    try {
      IDfClient client = getClient(ns.get(UtilsArgsParserFactory.ARG_HOST));
      IDfLoginInfo li = new DfLoginInfo();
      PasswordString password = (PasswordString)ns.get(UtilsArgsParserFactory.ARG_PASS);
      li.setUser(ns.get(UtilsArgsParserFactory.ARG_USER));
      li.setPassword(password.getPassword());
      client.authenticate(ns.get(UtilsArgsParserFactory.ARG_REPO), li);
      System.out.printf(
          "Authenticated to docbase %s with user %s%n",
          ns.get(UtilsArgsParserFactory.ARG_REPO), ns.get(UtilsArgsParserFactory.ARG_USER));
    } catch (DfException e) {
      throw new UtilsException(
          String.format(
              "Error authenticating user %s to docbase %s.  Error is %s",
              ns.get(UtilsArgsParserFactory.ARG_USER),
              ns.get(UtilsArgsParserFactory.ARG_REPO),
              e.getMessage()),
          e);
    } catch (IOException e) {
      throw new UtilsException(
          String.format(
              "Unexpected Error password.  Error is %s",
              ns.get(UtilsArgsParserFactory.ARG_USER),
              ns.get(UtilsArgsParserFactory.ARG_REPO),
              e.getMessage()),
          e);
    }
  }
}
