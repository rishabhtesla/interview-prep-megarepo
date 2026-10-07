package com.interviewprep.catalog;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogSeed implements CommandLineRunner {
    private final TopicRepository repository;

    public CatalogSeed(TopicRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        var topics = List.of(
            new Topic("java-collections", "java", "Collections and value semantics",
                "Explain equals/hashCode, choose collections by workload, and implement an immutable key.", 60),
            new Topic("java-concurrency", "java", "Concurrency and the memory model",
                "Reason about happens-before, executors, races, and bounded work.", 75),
            new Topic("scala-functions", "scala", "Functions and immutable data",
                "Use case classes, pattern matching, Option, and collection transformations.", 60),
            new Topic("scala-effects", "scala", "Errors and effects",
                "Compare Option, Either, Try, and Future; make failure explicit.", 60),
            new Topic("spark-partitions", "spark", "Partitions, shuffles, and skew",
                "Trace a distributed query, inspect its plan, and explain a skew mitigation.", 75),
            new Topic("spark-streaming", "spark", "Streaming and state",
                "Reason about event time, watermarks, checkpoints, and sink guarantees.", 75),
            new Topic("spring-transactions", "spring", "Transactions and persistence",
                "Trace controller to service to repository; inspect proxy boundaries and isolation.", 75),
            new Topic("spring-resilience", "spring", "Service boundaries and failures",
                "Stop catalog-service, observe progress failures, and design an outbox extension.", 90),
            new Topic("dsa-patterns", "dsa", "Arrays, maps, and sliding windows",
                "State the invariant, derive complexity, and test boundary cases.", 60),
            new Topic("dsa-graphs", "dsa", "Graphs and dynamic programming",
                "Choose BFS/DFS or a recurrence and explain correctness before implementation.", 75),
            new Topic("react-effects", "react", "Render, state, and effects",
                "Trace the study UI, cancel obsolete reads, and explain stale closures.", 75),
            new Topic("react-accessibility", "react", "Accessible forms and testing",
                "Persist a study note, handle failures, and test with roles and labels.", 60)
        );
        for (var topic : topics) {
            if (!repository.existsById(topic.toResponse().id())) {
                repository.save(topic);
            }
        }
    }
}
