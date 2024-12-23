package net.koeppster.dctm.utils;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Enumeration;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfDocbrokerClient;
import com.documentum.fc.client.IDfServerMap;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfPreferences;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfTime;
import com.documentum.fc.common.IDfValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractCmd implements UtilsFunction {
  static DfClientX clientX = new DfClientX();
  static JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
  static ObjectMapper mapper = new ObjectMapper();
  static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

  public static IDfClient getClient(DocbrokerSpec broker) throws DfException {

    if (null != broker) {
      IDfClient client = clientX.getLocalClient();
      IDfTypedObject clientConfig = client.getClientConfig();
      clientConfig.setString(DfPreferences.DFC_DOCBROKER_HOST, broker.getHost());
      clientConfig.setInt(DfPreferences.DFC_DOCBROKER_PORT, Integer.parseInt(broker.getPort()));
      return client;
    }
    else {
      return clientX.getLocalClient();
    }
  }

  private static String docbaseDateToString(IDfTime value) {
    Instant instant = value.getDate().toInstant();
    ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
    return zdt.format(formatter);
  }

  private static JsonNode getJsonValue(IDfValue value, int type) {
    switch (type) {
      case IDfType.DF_BOOLEAN:
        return nodeFactory.booleanNode(value.asBoolean());
      case IDfType.DF_DOUBLE:
        return nodeFactory.numberNode(value.asDouble());
      case IDfType.DF_ID:
        return nodeFactory.textNode(value.asId().toString());
      case IDfType.DF_INTEGER:
        return nodeFactory.numberNode(value.asInteger());
      case IDfType.DF_STRING:
        return nodeFactory.textNode(value.asString());
      case IDfType.DF_TIME:
        return nodeFactory.textNode(docbaseDateToString(value.asTime()));
      default:
        return nodeFactory.textNode(value.asString());
    }
  }
  public static ObjectNode getJsonFromTypedObject(IDfTypedObject arg0) throws DfException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    @SuppressWarnings("unchecked")
    Enumeration<IDfAttr> attrs = arg0.enumAttrs();
    while (attrs.hasMoreElements()) {
      IDfAttr attr = attrs.nextElement();
      String name = attr.getName();
      int type = attr.getDataType();
      boolean isRepeating = attr.isRepeating();
      if (isRepeating) {
       ArrayNode arrayNode = nodeFactory.arrayNode();
        for (int i = 0; i < arg0.getValueCount(name); i++) {
          IDfValue value = arg0.getRepeatingValue(name, i);
          arrayNode.add(getJsonValue(value, type));
        }
        node.set(name, arrayNode);
      } else {
        IDfValue value = arg0.getValue(name);
        node.set(name, getJsonValue(value, type));
      }
    }
    return node;
  }

  /**
   * Returns an {@link com.documentum.fc.client.IDfDocbaseMap}.  If the host and port are supplied 
   * then that docbroker is used.  Otherwise dfc.properties is used.
   * @param host
   * @param port
   * @return
   * @throws DfException 
  */
  public static IDfDocbaseMap getDocbaseMap(DocbrokerSpec broker) throws DfException {
    if (null == broker) {
      IDfClient client = new DfClient();
      return client.getDocbaseMap();
    } else {
      IDfDocbrokerClient client = clientX.getDocbrokerClient();
      return client.getDocbaseMapFromSpecificDocbroker("tcp", broker.getHost(), broker.getPort());
    }
  }

  public static IDfServerMap getServerMap(DocbrokerSpec broker, String docBase) throws DfException {
    DfLogger.debug(UtilsArgsParserFactory.class,"Entering getServerMap({0},{1})", new Object[] {broker,docBase},null);
    if (null == broker) {
      IDfClient client = new DfClient();
      return (IDfServerMap)client.getServerMap(docBase);
    }
    else {
      IDfDocbrokerClient client = clientX.getDocbrokerClient();
      return (IDfServerMap)client.getServerMapFromSpecificDocbroker(docBase, "tcp", broker.getHost(), broker.getPort());
    }
  }
   
}
