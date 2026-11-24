package com.interviewprep.integration;

import java.util.Locale;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;

public final class CamelRouteLesson implements AutoCloseable {
    private final CamelContext context = new DefaultCamelContext();
    private ProducerTemplate producer;

    public CamelRouteLesson() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:orders")
                        .routeId("normalize-orders")
                        .choice()
                        .when(exchange -> isInvalid(exchange))
                        .to("mock:rejected")
                        .otherwise()
                        .process(CamelRouteLesson::normalize)
                        .to("mock:normalized");
            }
        });
    }

    private static boolean isInvalid(Exchange exchange) {
        Object body = exchange.getIn().getBody();
        return body == null || body.toString().isBlank()
                || body.toString().toLowerCase(Locale.ROOT).contains("invalid");
    }

    private static void normalize(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody(body.trim().toUpperCase(Locale.ROOT));
    }

    public void start() throws Exception {
        context.start();
        producer = context.createProducerTemplate();
    }

    public String send(String body) {
        return producer.requestBody("direct:orders", body, String.class);
    }

    public MockEndpoint endpoint(String uri) {
        return context.getEndpoint(uri, MockEndpoint.class);
    }

    @Override
    public void close() throws Exception {
        context.stop();
    }
}
