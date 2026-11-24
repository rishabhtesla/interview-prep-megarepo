FROM python:3.12-slim AS runtime

WORKDIR /app
COPY 10-storage-cloud-devops/examples /app/examples

RUN useradd --create-home appuser
USER appuser

CMD ["python", "examples/sql_lesson.py"]
