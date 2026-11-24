package com.interviewprep.integration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class ServiceDiscoveryLesson {
    public record Instance(String host, int port, boolean healthy) {
    }

    private final Map<String, List<Instance>> registry;
    private final AtomicInteger cursor = new AtomicInteger();

    public ServiceDiscoveryLesson(Map<String, List<Instance>> registry) {
        this.registry = Map.copyOf(registry);
    }

    public Instance resolveHealthy(String serviceName) {
        List<Instance> instances = registry.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            throw new IllegalArgumentException("service is not registered: " + serviceName);
        }
        for (int attempt = 0; attempt < instances.size(); attempt++) {
            Instance candidate = instances.get(
                    Math.floorMod(cursor.getAndIncrement(), instances.size()));
            if (candidate.healthy()) {
                return candidate;
            }
        }
        throw new IllegalStateException("no healthy instance: " + serviceName);
    }
}
