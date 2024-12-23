package net.koeppster.dctm.utils;

import com.google.common.net.HostAndPort;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

public class DocbrokerSpec extends Object implements ArgumentType<DocbrokerSpec> {

  private String host = null;

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  private String port = "1489";

  private void convert(String arg) {
    HostAndPort hpParser = HostAndPort.fromString(arg).withDefaultPort(1489);
    this.host = hpParser.getHost();
    this.port = Integer.toString(hpParser.getPort());
  }

  @Override
  public DocbrokerSpec convert(ArgumentParser parser, Argument arg, String value)
      throws ArgumentParserException {
    try {
      this.convert(value);
    } catch (IllegalArgumentException e) {
      throw new ArgumentParserException("Error parsing Docbroker Connection Spec.", e, parser, arg);
    }
    return this;
  }

  public DocbrokerSpec(String arg0, String arg1) {
    this.host = arg0;
    this.port = arg1;
  }

  public DocbrokerSpec() {
  }

  public DocbrokerSpec(String arg) {
    this.convert(arg);
  }

  public String toString() {
    return (null == host) ? null : host.concat(":").concat(port);
  }

  public static DocbrokerSpec valueOf(String arg) {
    return new DocbrokerSpec(arg);
  }
}
