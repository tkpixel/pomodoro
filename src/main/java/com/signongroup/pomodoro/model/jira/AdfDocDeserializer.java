package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for {@link AdfDoc}.
 *
 * <p>Handles both Jira Server/Data Center (plain Wiki Markup string) and
 * Jira Cloud (Atlassian Document Format JSON object) for the description field.
 */
public class AdfDocDeserializer extends StdDeserializer<AdfDoc> {

    public AdfDocDeserializer() {
        super(AdfDoc.class);
    }

    @Override
    public AdfDoc deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            // Jira Server / Data Center: plain Wiki Markup string – wrap it in a minimal AdfDoc
            return AdfDoc.ofText(p.getText());
        }
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        // Jira Cloud: ADF JSON object – parse fields manually to avoid infinite recursion
        String type = null;
        int version = 0;
        List<AdfDoc.AdfParagraph> content = new ArrayList<>();

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.getCurrentName();
            p.nextToken();
            switch (fieldName) {
                case "type"    -> type = p.getText();
                case "version" -> version = p.getIntValue();
                case "content" -> p.skipChildren(); // not rendered from model directly
                default        -> p.skipChildren();
            }
        }
        return new AdfDoc(type, version, content);
    }
}
