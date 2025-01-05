package net.koeppster.dctm.types;

import com.google.common.net.HostAndPort;

public class DocbrokerSpec  {

  private String host = null;
  private String port = "1489";

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  private void convert(String arg) {
    HostAndPort hpParser = HostAndPort.fromString(arg).withDefaultPort(1489);
    this.host = hpParser.getHost();
    this.port = Integer.toString(hpParser.getPort());
  }

  public DocbrokerSpec(String arg0, String arg1) {
    this.host = arg0;
    this.port = arg1;
  }

  public DocbrokerSpec() {}

  public DocbrokerSpec(String arg) {
    this.convert(arg);
  }

  public String toString() {
    return (null == host) ? null : "broker at ".concat(host).concat(":").concat(port);
  }

  public static DocbrokerSpec valueOf(String arg) {
    return new DocbrokerSpec(arg);
  }
}
