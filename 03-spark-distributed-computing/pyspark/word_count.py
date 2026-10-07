"""Example 1: tokenize a bundled text file, aggregate, sort deterministically."""
from course import run_batch, word_counts


def job(spark, data_dir):
    word_counts(spark.read.text(str(data_dir / "lines.txt"))).show(truncate=False)


if __name__ == "__main__":
    run_batch("course-word-count", job)
