package com.interviewprep.progress;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {
    private final CatalogClient catalog;
    private final ProgressStore store;

    public ProgressService(CatalogClient catalog, ProgressStore store) {
        this.catalog = catalog;
        this.store = store;
    }

    public List<ProgressResponse> list() {
        return store.list();
    }

    public ProgressResponse replace(String topicId, ProgressRequest request) {
        // Validate remotely before opening the short local database transaction.
        catalog.requireTopic(topicId);
        return store.replace(topicId, request);
    }
}
