# Storage, cloud, containers, and delivery

This track explains where data lives and how code reaches production. It uses
SQLite and a local TTL cache for zero-credential lessons, then shows how the
same decisions map to PostgreSQL, MySQL, Redis, S3, DynamoDB, Docker,
Kubernetes, AWS CDK, CloudFormation, and GitLab CI/CD.

## Lesson order

### 1. Relational data and SQL

Files:

- [`examples/sql_lesson.py`](examples/sql_lesson.py)
- [`sql/schema.sql`](sql/schema.sql)
- [`sql/queries.sql`](sql/queries.sql)

Run:

```bash
python3 examples/sql_lesson.py
```

Actions:

1. Create customers and orders.
2. Insert rows with a foreign-key relationship.
3. Join orders to customers.
4. Aggregate revenue.
5. Add an index and inspect the query plan.

Read the SQL in this order:

```text
schema -> primary/foreign key -> insert -> join -> group -> index -> explain
```

The local runner uses SQLite so no database server is required. PostgreSQL and
MySQL use the same relational ideas but differ in types, indexes, locking,
query plans, and operational behavior. Always verify performance on the target
database.

### 2. Redis and TTL

Source: [`examples/ttl_cache.py`](examples/ttl_cache.py).

Run:

```bash
python3 examples/ttl_cache.py
```

The cache stores a value with an expiration timestamp:

```text
request -> cache lookup
       hit -> return cached value
       miss/expired -> load source -> write with TTL -> return value
```

The example shows that a cache is not the source of truth. A cache miss must
load from the database, and expiry can cause a thundering herd. Production
questions include key design, serialization, invalidation, stampede control,
memory limits, and whether stale data is acceptable.

### 3. Files, S3, JSONL, GZIP, and Parquet

JSONL is convenient for line-oriented interchange; GZIP compresses bytes but
is not a columnar query format. Parquet stores columns and metadata for
analytics. S3 is object storage, not a POSIX filesystem or a database.

Run the local JSONL/GZIP round trip:

```bash
python3 examples/jsonl_gzip.py
```

The temporary file is deleted after the example. For a production object,
define the key, content type, compression, checksum, retention, and commit
protocol. A reader must not infer “complete dataset” merely because an object
exists.

Use the Spark track for Parquet execution. This track focuses on ownership,
object naming, retention, checksum/integrity, and retry-safe writes.

```text
producer -> temporary object -> validation -> committed object/prefix
```

Do not treat a partially uploaded object as a successful dataset.

### 4. Docker

Source: [`docker/Dockerfile`](docker/Dockerfile).

Build from the repository root:

```bash
docker build -f 10-storage-cloud-devops/docker/Dockerfile .
```

The multi-stage shape separates build tools from the runtime image. Explain:

- Build context.
- Layers and cache invalidation.
- Non-root execution.
- Port declaration versus actual publishing.
- Configuration through environment variables.
- Why secrets must not be copied into an image layer.

### 5. Kubernetes

Source: [`k8s/lesson.yaml`](k8s/lesson.yaml).

The manifest describes a Deployment and Service. A Deployment declares desired
replicas; the controller reconciles actual Pods. A Service gives stable
networking to changing Pods. `readinessProbe` controls traffic eligibility;
`livenessProbe` controls restart decisions. They are not interchangeable.

```bash
kubectl apply --dry-run=client -f k8s/lesson.yaml
```

Use a local cluster such as kind or minikube only after understanding the
manifest. Never apply this to a production cluster without resource, security,
network, and rollout review.

### 6. AWS and infrastructure as code

The `cloud/` files are reference templates:

- `cloud/cloudformation.yaml`: declarative S3 bucket and queue shape.
- `cloud/cdk-example.ts`: CDK resource relationship example.
- `sql/pgvector.sql`: tenant-filtered PostgreSQL vector-search boundary.

The action is to declare infrastructure, review the generated plan, apply with
least privilege, observe outputs, and destroy disposable resources. AWS lessons
must document region, IAM permissions, cost, data retention, and cleanup.

### 7. CI/CD and quality gates

Source: [`.gitlab-ci.yml`](.gitlab-ci.yml).

Pipeline flow:

```text
format/lint -> unit tests -> build -> security scan -> package -> deploy
```

JaCoCo measures Java coverage; it does not prove correctness. SonarQube
reports quality/security signals; it does not replace code review. A pipeline
must fail on meaningful errors and publish artifacts/logs for diagnosis.

## Interview questions

1. When should data be in PostgreSQL/MySQL, Redis, DynamoDB, or S3?
2. What does an index improve and what does it cost?
3. What is a cache stampede and how do you reduce it?
4. Why is Parquet useful for analytics?
5. What is the difference between a Docker image and a container?
6. Readiness versus liveness probe?
7. Deployment versus Service in Kubernetes?
8. How do you make a database migration safe to roll back?
9. Why should infrastructure be reviewed as code?
10. Which CI step prevents a known dependency CVE from reaching production?

## Revision card

```text
database = source of truth for relational invariants
cache = disposable acceleration layer
object storage = durable blobs with explicit naming/lifecycle
container image = immutable packaged filesystem/process contract
Deployment = desired Pod state
Service = stable network endpoint
pipeline = repeatable evidence-producing delivery process
```
