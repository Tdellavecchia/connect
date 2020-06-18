/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.linuxforhealth.connect.processor;

import java.util.Arrays;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend JSONObject to override toString to print attributes in a specific order.
 */
public class LinuxForHealthMessage extends JSONObject {
    private JSONObject meta;

    public LinuxForHealthMessage() {}

    // Set up JSON structure and common fields
    public LinuxForHealthMessage(Exchange exchange) {
        meta = new JSONObject();
        meta.put("routeId", exchange.getFromRouteId());
        meta.put("uuid", exchange.getIn().getHeader("uuid", String.class));
        meta.put("routeUrl", exchange.getIn().getHeader("routeUrl", String.class));
        meta.put("dataFormat", exchange.getIn().getHeader("dataFormat", String.class));
        meta.put("timestamp", exchange.getIn().getHeader("timestamp", String.class));
        meta.put("dataStoreUrl", exchange.getIn().getHeader("dataStoreUrl", String.class));
        this.put("meta", meta);
    }

    // Set error fields
    public void setError(String errorMsg) {
        meta.put("status", "error");
        this.put("data", errorMsg);
    }

    // Set fields for successful data storage
    public void setDataStoreResult(List<RecordMetadata> metaRecords) {
        JSONArray kafkaMeta  = new JSONArray();

        for (RecordMetadata m: metaRecords) {
            kafkaMeta.put(m);
        }
        meta.put("status", "success");
        meta.put("dataRecordLocation", kafkaMeta);
    }

    // Set the data field to the data to be stored
    public void setData(Object data) {
        this.put("data", data);
    }

    /**
     * Override to support ordered fields and rendering of polymorphic data.
     */
    @Override
    public String toString() {
        String result = "{\"meta\":{"+
            getString(meta, "routeId")+","+
            getString(meta, "uuid")+","+
            getString(meta, "routeUrl")+","+
            getString(meta, "dataFormat")+","+
            getString(meta, "timestamp")+","+
            getString(meta, "dataStoreUrl");

        if (meta.has("status")) result += ","+getString(meta, "status");
        if (meta.has("dataRecordLocation")) result += ","+getObjectString(meta, "dataRecordLocation");
        result += "}";
        if (this.has("data")) result += ","+getObjectString(this, "data");
        result += "}";

        return result;
    }

    private String getString(JSONObject obj, String name) {
        return "\""+name+"\":\""+obj.getString(name)+"\"";
    }

    // Extend to support different object types as needed
    private String getObjectString(JSONObject obj, String name) {
        Object dataObj = (Object) obj.get(name);
        String result;

        if (dataObj instanceof byte[]) {
            result = "\""+name+"\":"+Arrays.toString((byte[]) dataObj);
        } else if (dataObj instanceof String) {
            result = "\""+name+"\":\""+dataObj.toString()+"\"";  // quoted value
        } else {
            result = "\""+name+"\":"+dataObj.toString();  // value not quoted
        }

        return result;
    }
}
