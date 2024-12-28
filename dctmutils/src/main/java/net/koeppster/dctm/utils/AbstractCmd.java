package net.koeppster.dctm.utils;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
  protected static DfClientX clientX = new DfClientX();
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
    if (null == value || value.isNullDate()) {
      return null;
    }
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
    return getJsonFromTypedObject(arg0, null);
  }

  public static ObjectNode getJsonFromTypedObject(IDfTypedObject arg0, ArrayList<String> arg1) throws DfException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    Enumeration<String> attrNames = null;
    if (null!=arg1) {
      attrNames = new Enumeration<String>() {
        private final int size = (arg1 == null) ? 0 : arg1.size();
        private int index = 0;
  
        @Override
        public boolean hasMoreElements() {
          return index < size;
        }
  
        @Override
        public String nextElement() {
          return arg1.get(index++);
        }
      };
    }
    else {
      attrNames = new Enumeration<String>() {
        @SuppressWarnings("unchecked")
        private final Enumeration<IDfAttr> attrsEnum = arg0.enumAttrs();
  
        @Override
        public boolean hasMoreElements() {
          return attrsEnum.hasMoreElements();
        }
  
        @Override
        public String nextElement() {
          return attrsEnum.nextElement().getName();
        }
      };
    }

    while (attrNames.hasMoreElements()) {
      String name = attrNames.nextElement();
      DfLogger.debug(AbstractCmd.class, "Processing attribute: " + name, null, null);

      // Skip if the attribute is not in this typed object
      if (!arg0.hasAttr(name)) {
        node.set(name, nodeFactory.missingNode());
        continue;
      }
      int type = arg0.getAttrDataType(name);
      boolean isRepeating = arg0.isAttrRepeating(name);
      if (isRepeating) {
       ArrayNode arrayNode = nodeFactory.arrayNode();
        for (int i = 0; i < arg0.getValueCount(name); i++) {
          IDfValue value = arg0.getRepeatingValue(name, i);
          arrayNode.add(getJsonValue(value, type));
        }
        node.set(name, arrayNode);
      } else {
        IDfValue value = arg0.getValue(name);
        JsonNode nodeValue = arg0.isNull(name) ? nodeFactory.missingNode() : getJsonValue(value, type);
        node.set(name, nodeValue);
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
