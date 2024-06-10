package net.koeppster.dctm.utils;

import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfServerMap;
import com.documentum.fc.common.DfException;

public class Docbase {
    private String docbase; 
    private String status;
    private DocbaseServer[] servers;

    public String getDocbase() {
        return docbase;
    }
    public String getStatus() {
        return status;
    }
    public DocbaseServer[] getServers() {
        return servers;
    }

    public Docbase(String docbase, String status, IDfServerMap map) {
        this.docbase = docbase;
        this.status = status;
        this.servers = DocbaseServer.getDocbaseServers(map);
    }

    public static Docbase[] getDocbases(IDfDocbaseMap map) throws DfException {
        Docbase[] docbases = new Docbase[map.getDocbaseCount()];
        for (int i=0;i<map.getDocbaseCount();i++) {
            Docbase docBase = new Docbase(map.getDocbaseName(i),map.getDormancyStatus(i),(IDfServerMap)map.getServerMap(i));
            docbases[i]=docBase;
        }
        return new Docbase[0];
    }
}
