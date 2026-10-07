package com.interviewprep.progress;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:progress-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "catalog.connect-timeout=200ms",
    "catalog.read-timeout=200ms"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ProgressIntegrationTest {
    private static final HttpServer CATALOG = startCatalog();
    @Autowired MockMvc mvc;
    @Autowired ProgressRepository repository;

    private static HttpServer startCatalog() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/topics/", exchange -> {
                String id = exchange.getRequestURI().getPath().substring("/api/topics/".length());
                int status = switch (id) {
                    case "missing" -> 404;
                    case "broken" -> 500;
                    default -> 200;
                };
                if (id.equals("slow")) {
                    try { Thread.sleep(600); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                }
                String body = id.equals("malformed") ? "{}" : "{\"id\":\"" + id + "\"}";
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                try (exchange) {
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status, bytes.length);
                    exchange.getResponseBody().write(bytes);
                } catch (IOException ignored) {
                    // The timeout test deliberately closes the client connection first.
                }
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @DynamicPropertySource
    static void catalogUrl(DynamicPropertyRegistry registry) {
        registry.add("catalog.base-url", () -> "http://127.0.0.1:" + CATALOG.getAddress().getPort());
    }

    @AfterAll static void stopCatalog() { CATALOG.stop(0); }
    @BeforeEach void clear() { repository.deleteAll(); }

    @Test
    void putIsAnIdempotentReplacementAndReadsStoredState() throws Exception {
        String body = "{\"completed\":true,\"note\":\"Explain cleanup\"}";
        for (int i = 0; i < 2; i++) {
            mvc.perform(put("/api/progress/react-effects").contentType("application/json").content(body))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(true));
        }
        assertEquals(1, repository.count());
        mvc.perform(get("/api/progress")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value("Explain cleanup"));
        mvc.perform(put("/api/progress/react-effects").contentType("application/json")
                        .content("{\"completed\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.note").value(""));
        assertEquals(false, repository.findById("react-effects").orElseThrow().toResponse().completed());
    }

    @Test
    void invalidBodiesAndIdentifiersDoNotWrite() throws Exception {
        for (String body : new String[]{"{}", "{\"completed\":null}", "{",
                "{\"completed\":true,\"note\":\"" + "x".repeat(501) + "\"}"}) {
            mvc.perform(put("/api/progress/react-effects").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }
        mvc.perform(put("/api/progress/BAD!").contentType("application/json")
                        .content("{\"completed\":true}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    @Test
    void missingAndUnavailableCatalogDoNotInventSuccess() throws Exception {
        mvc.perform(put("/api/progress/missing").contentType("application/json")
                        .content("{\"completed\":true}"))
                .andExpect(status().isNotFound());
        for (String id : new String[]{"broken", "malformed", "slow"}) {
            mvc.perform(put("/api/progress/" + id).contentType("application/json")
                            .content("{\"completed\":true}"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503));
        }
        assertEquals(0, repository.count());
        mvc.perform(get("/api/progress")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}
