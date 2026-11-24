package com.interviewprep.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SoapEnvelopeLessonTest {
    @Test
    void readsNamespacedOperation() throws Exception {
        String xml = """
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                  <s:Body><GetTopic xmlns="urn:test"/></s:Body>
                </s:Envelope>
                """;
        assertEquals("GetTopic", SoapEnvelopeLesson.readOperation(xml));
    }

    @Test
    void rejectsMissingBodyOperation() {
        assertThrows(Exception.class, () -> SoapEnvelopeLesson.readOperation(
                "<Envelope/>"));
    }
}
