package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

public class MetaDeserializer extends JsonDeserializer<Meta> {
    @Override
    public Meta deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        
        Integer found = node.has("found") ? node.get("found").asInt() : null;
        Object limit = null;
        Integer page = node.has("page") ? node.get("page").asInt() : null;
        
        if (node.has("limit")) {
            JsonNode limitNode = node.get("limit");
            if (limitNode instanceof IntNode) {
                limit = limitNode.asInt();
            } else if (limitNode instanceof TextNode) {
                limit = limitNode.asText();
            }
        }
        
        return new Meta(found,(Integer) limit, page);
    }
}