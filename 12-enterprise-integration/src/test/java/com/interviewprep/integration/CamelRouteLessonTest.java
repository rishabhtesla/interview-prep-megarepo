package com.interviewprep.integration;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CamelRouteLessonTest {
    @Test
    void normalizesValidMessagesAndRoutesInvalidMessages() throws Exception {
        try (CamelRouteLesson lesson = new CamelRouteLesson()) {
            lesson.start();
            lesson.endpoint("mock:normalized").expectedBodiesReceived("ORDER-7");
            lesson.endpoint("mock:rejected").expectedBodiesReceived("invalid order");

            assertEquals("ORDER-7", lesson.send(" order-7 "));
            assertEquals("invalid order", lesson.send("invalid order"));

            assertIsSatisfied(lesson.endpoint("mock:normalized"),
                    lesson.endpoint("mock:rejected"));
        }
    }
}
