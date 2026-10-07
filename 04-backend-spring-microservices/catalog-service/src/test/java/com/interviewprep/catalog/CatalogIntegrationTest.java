package com.interviewprep.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:catalog-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CatalogIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CatalogSeed seed;
    @Autowired TopicRepository repository;

    @Test
    void catalogIsSeededOnceAndCanBeFiltered() throws Exception {
        seed.run();
        org.junit.jupiter.api.Assertions.assertEquals(12, repository.count());
        mvc.perform(get("/api/topics")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
        mvc.perform(get("/api/topics").param("track", "spring"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].track").value("spring"));
        mvc.perform(get("/api/topics").param("track", "unknown"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void detailAndErrorsRespectContract() throws Exception {
        mvc.perform(get("/api/topics/react-effects")).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Render, state, and effects"));
        mvc.perform(get("/api/topics/missing")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Topic not found"));
        mvc.perform(get("/api/topics").param("track", "BAD!")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/topics/BAD!")).andExpect(status().isBadRequest());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
