package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdfDocDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializeFromString_shouldWrapInAdfDoc() throws Exception {
        String json = "\"h2. Summary\\n\\nThe product shall be analyzed.\"";
        AdfDoc doc = mapper.readValue(json, AdfDoc.class);
        assertNotNull(doc);
        assertEquals("doc", doc.type());
        assertFalse(doc.content().isEmpty());
        assertEquals("h2. Summary\n\nThe product shall be analyzed.",
                doc.content().get(0).content().get(0).text());
    }

    @Test
    void deserializeFromObject_shouldParseNormally() throws Exception {
        String json = """
                {
                  "type": "doc",
                  "version": 1,
                  "content": []
                }
                """;
        AdfDoc doc = mapper.readValue(json, AdfDoc.class);
        assertNotNull(doc);
        assertEquals("doc", doc.type());
        assertEquals(1, doc.version());
    }

    @Test
    void deserializeNull_shouldReturnNull() throws Exception {
        AdfDoc doc = mapper.readValue("null", AdfDoc.class);
        assertNull(doc);
    }
}
