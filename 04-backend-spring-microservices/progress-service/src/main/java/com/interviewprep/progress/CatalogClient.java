package com.interviewprep.progress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CatalogClient {
    private final RestClient client;

    public CatalogClient(RestClient.Builder builder, @Value("${catalog.base-url}") String baseUrl) {
        client = builder.baseUrl(baseUrl).build();
    }

    public void requireTopic(String topicId) {
        try {
            var topic = client.get().uri("/api/topics/{id}", topicId)
                    .retrieve().body(CatalogTopic.class);
            if (topic == null || !topicId.equals(topic.id())) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog returned an invalid response");
            }
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
            }
            throw unavailable(ex);
        } catch (RestClientException ex) {
            throw unavailable(ex);
        }
    }

    private ResponseStatusException unavailable(Exception cause) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Catalog unavailable; progress was not saved. Try again.", cause);
    }

    record CatalogTopic(String id) {}
}
