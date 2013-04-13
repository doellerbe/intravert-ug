package org.usergrid.vx.server.operations;

import java.nio.ByteBuffer;

import org.apache.cassandra.utils.ByteBufferUtil;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/*
  keyspace, cf, type {"column"|"rowkey"|"value"} = class
  default validator
  default comparator
  key comparator

  keyspace, cf, columnname = class
  column specific validator

  keyspaec, cf, columnstart, column end, class
  ranged validator
 */
public class AssumeHandler extends AbstractIntravertHandler {

  @Override
  public void handleUser(Message<JsonObject> event) {
    Integer id = event.body.getInteger("id");
    JsonObject params = event.body.getObject("op");
    JsonObject state = event.body.getObject("state");
    JsonObject meta = state.getObject("meta");
    JsonObject metaColumn = state.getObject("metaColumn");
    JsonObject metaRanged = state.getObject("metaranged");
    if (meta == null) {
      meta = new JsonObject();
    }
    if (metaColumn == null){
      metaColumn = new JsonObject();
    }
    if (metaRanged == null){
      metaRanged = new JsonObject();
    }
    if (params.getString("type") != null){
      StringBuilder key = new StringBuilder();
      key.append(HandlerUtils.determineKs(params, state, null));
      key.append(' ');
      key.append(HandlerUtils.determineCf(params, state, null));
      key.append(' ');
      key.append(params.getString("type"));
      meta.putObject(key.toString(), new JsonObject()
        .putString("clazz", params.getString("clazz")));
      state.putObject("meta", meta);
      event.reply(new JsonObject().putString(id.toString(), "OK")
        .putObject("state", state));
    } else if (params.getField("name") != null){
      Object o = params.getField("name");
      ByteBuffer bb = HandlerUtils.byteBufferForObject(o);
      StringBuilder key = new StringBuilder();
      key.append(HandlerUtils.determineKs(params, state, null));
      key.append(' ');
      key.append(HandlerUtils.determineCf(params, state, null));
      key.append(' ');
      key.append(ByteBufferUtil.bytesToHex(bb));
      metaColumn.putObject(key.toString(), new JsonObject()
              .putString("clazz", params.getString("clazz")));
      state.putObject("metaColumn", metaColumn);
      event.reply(new JsonObject().putString(id.toString(), "OK")
              .putObject("state", state));
    } else {
      throw new RuntimeException("hit bottom this is bad ok");
    }
  }
}
