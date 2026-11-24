package com.interviewprep.integration;

import java.util.List;
import java.util.Map;

public final class IntegrationLesson {
    private static final String SOAP_FIXTURE = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body><GetTopic xmlns="urn:interviewprep"><id>spring</id></GetTopic></soap:Body>
            </soap:Envelope>
            """;

    private IntegrationLesson() {
    }

    public static void main(String[] args) throws Exception {
        try (CamelRouteLesson camel = new CamelRouteLesson()) {
            camel.start();
            System.out.println("Camel normalized: " + camel.send(" order-7 "));
            System.out.println("Camel rejected: " + camel.send("invalid order"));
        }
        System.out.println("SOAP operation: " + SoapEnvelopeLesson.readOperation(SOAP_FIXTURE));

        ServiceDiscoveryLesson discovery = new ServiceDiscoveryLesson(Map.of(
                "catalog-service", List.of(
                        new ServiceDiscoveryLesson.Instance("catalog-a", 8081, true),
                        new ServiceDiscoveryLesson.Instance("catalog-b", 8081, false))));
        System.out.println("discovered instance: " + discovery.resolveHealthy("catalog-service"));
    }
}
