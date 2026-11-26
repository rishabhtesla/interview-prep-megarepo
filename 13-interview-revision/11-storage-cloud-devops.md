# Storage, cloud, containers, Kubernetes, and CI/CD

## Storage decision flow

```text
business invariant
  -> access pattern
  -> consistency/latency/retention
  -> storage choice
  -> index/partition/key design
  -> backup/restore and observability
```

## Interview questions and answers

### PostgreSQL/MySQL versus Redis/DynamoDB/S3?

**Answer:** A relational database is the default for relational invariants,
transactions, joins, and SQL queries. Redis is a fast disposable/cache or
coordination layer with TTL and memory constraints. DynamoDB is a
partition-key/access-pattern-first managed key-value/document store. S3 is
durable object storage, not a POSIX filesystem or transactional database.
Choose from access patterns and failure/retention needs.

### What does an index cost?

**Answer:** It reduces reads for matching predicates/orderings but consumes
storage and write/update work. The leading column, selectivity, query shape,
statistics, and data distribution matter. Verify with the target database's
query plan; an index created because a column exists is not automatically
useful.

### What is cache-aside?

**Answer:** The application reads cache first; on a miss it loads the source
and writes the cache with a TTL. Writes update/invalidate the source and cache
according to the consistency policy.

```text
read -> cache hit -> return
     -> miss/expired -> database -> cache with TTL -> return
```

Prevent stampede with request coalescing, jittered expiry, stale-while-revalidate,
or a lock. Prevent penetration with validation, negative caching, and bounded
queries. A cache is not the source of truth.

### What are Parquet and JSONL/GZIP?

**Answer:** JSONL is line-oriented interchange; GZIP compresses bytes but is
not columnar. Parquet stores columns and metadata, enabling projection and
predicate benefits for analytics. Object naming, checksums, completion markers,
retention, and retry-safe writes matter more than the extension.

## Container and Kubernetes questions

### Image versus container?

**Answer:** An image is an immutable layered filesystem/configuration artifact;
a container is a running process created from it with runtime isolation and
resource limits. A `Dockerfile` should use a small runtime stage, a non-root
user, explicit configuration, and no secret in layers.

### Deployment versus Service?

**Answer:** A Deployment declares desired replicated Pod state and performs
rollouts. A Service gives changing Pods a stable network endpoint. Readiness
controls whether traffic is sent; liveness controls whether a process should
be restarted. They are not interchangeable.

### How do you make a rollout safe?

**Answer:** Keep API/schema changes backward compatible, use readiness gates,
bounded startup/shutdown, resource requests/limits, rollout health metrics,
and a tested rollback. Use expand-and-contract for database migrations:
expand schema, deploy compatible readers/writers, backfill, switch reads,
contract old fields later.

## AWS and infrastructure questions

### What does EMR Graviton modernization require?

**Answer:** Verify Spark/Hadoop/native-library and Java compatibility, benchmark
representative workloads, compare executor CPU/memory/storage/network, test
shuffle and serialization behavior, and measure cost per successful workload.
“Graviton is cheaper” is not evidence until runtime, failure, and operations
are compared.

### Lambda, Step Functions, SQS, and EC2?

**Answer:** Lambda is event-triggered short-lived compute with runtime/time
limits. Step Functions coordinates stateful workflows and retries. SQS buffers
work with visibility timeout. EC2 gives more host/runtime control and
operational responsibility. Choose based on duration, state, concurrency,
networking, and operational ownership.

## Code and configuration walkthrough

Run local lessons:

```bash
cd 10-storage-cloud-devops
python3 examples/sql_lesson.py
python3 examples/ttl_cache.py
python3 examples/jsonl_gzip.py
docker build -f docker/Dockerfile .
kubectl apply --dry-run=client -f k8s/lesson.yaml
```

Inspect:

- `sql/schema.sql` and `sql/queries.sql` for keys, joins, and plans.
- `examples/ttl_cache.py` for expiry without pretending it is Redis.
- `docker/Dockerfile` for multi-stage/non-root image structure.
- `k8s/lesson.yaml` for Deployment, Service, and probes.
- `cloud/cloudformation.yaml` and `cloud/cdk-example.ts` for IaC shape.
- `.gitlab-ci.yml` for format -> test -> build -> scan -> package flow.

Configuration belongs in environment/secret references:

```yaml
env:
  - name: DATABASE_URL
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: database-url
```

This is a Kubernetes shape; use a real secret-management and RBAC design in
production.

## Failure exercise

Break the readiness probe and explain why the Pod can be running but receive no
traffic. Then make a database migration add a non-null column before old
instances can write it; explain why rolling deployment fails and rewrite it as
expand-and-contract.

## Revision card

```text
Choose storage from invariants and access patterns.
Indexes trade write/storage cost for read access.
Redis accelerates; it does not replace the source of truth.
S3 objects need explicit commit/retention/integrity contracts.
Images are immutable artifacts; containers are running processes.
Readiness controls traffic; liveness controls restart.
IaC and CI/CD should produce reviewable, repeatable evidence.
```
