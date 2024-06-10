package net.koeppster.dctm.utils;

import com.documentum.fc.client.IDfServerMap;

public class DocbaseServer {
    private String connectionAddress;
    private String connectionAddress6;
    private String hostName;
    private String serverName;

    public String getConnectionAddress() {
        return connectionAddress;
    }

    public String getConnectionAddress6() {
        return connectionAddress6;
    }

    public String getHostName() {
        return hostName;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * Constructor for bean
     * 
     * @param arg0 hostName
     * @param arg1 serverName
     * @param arg2 connectionAddress
     * @param arg3 connectionAddress6
     * @param arg4 portNumber
     */
    public DocbaseServer(String arg0, String arg1, String arg2, String arg3) {
        this.hostName = arg0;
        this.serverName = arg1;
        this.connectionAddress = arg2;
        this.connectionAddress6 = arg3;
    }

    public static DocbaseServer[] getDocbaseServers(IDfServerMap map) {
        DocbaseServer[] servers = new DocbaseServer[map.getServerCount()];
        for (int i=0; i<map.getServerCount(); i++) {
            servers[i] = new DocbaseServer(map.getHostName(i), 
                                           map.getServerName(i), 
                                           map.getConnectionAddress(i), 
                                           map.getConnectionAddress6(i));
        }
        return servers;
    }

}
