package interviewprep.scalabasics

import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration.*

/** Future is eager and memoized; it is not a lazy, cancellable effect description. */
object FuturesAndEffects:
  def main(args: Array[String]): Unit =
    val executor = Executors.newFixedThreadPool(2)
    given ExecutionContext = ExecutionContext.fromExecutor(executor)
    try
      val executions = new AtomicInteger(0)
      val started = Promise[Unit]()
      val gate = Promise[Int]()
      val eager = Future {
        executions.incrementAndGet()
        started.success(())
        21
      }
      // Await is confined to this executable/test boundary, never a worker body.
      Await.result(started.future, 5.seconds)
      Checks.equal(1, executions.get(), "Future starts without waiting for its result")
      Checks.equal(21, Await.result(eager, 5.seconds), "first observation")
      Checks.equal(21, Await.result(eager, 5.seconds), "repeated observation reuses completion")
      Checks.equal(1, executions.get(), "observing a Future does not rerun its body")

      val chained = gate.future.flatMap(value => Future(value * 2)).map(_ + 1)
      Checks.that(!chained.isCompleted, "flatMap waits for the upstream value")
      gate.success(20)
      Checks.equal(41, Await.result(chained, 5.seconds), "dependent computations compose")
      Checks.that(!gate.trySuccess(99), "Promise accepts at most one completion")

      // Construct independent Futures before combining them; a for-comprehension
      // that creates the second Future in flatMap would introduce a dependency.
      val left = Future(10)
      val right = Future(32)
      val combined = for
        a <- left
        b <- right
      yield a + b
      Checks.equal(42, Await.result(combined, 5.seconds), "combine independently started tasks")

      val recovered = Future.failed[Int](new IllegalArgumentException("bad input"))
        .recover { case _: IllegalArgumentException => -1 }
      Checks.equal(-1, Await.result(recovered, 5.seconds), "recover selected failure as a value")
      val fallback = Future.failed[Int](new IllegalStateException("offline"))
        .recoverWith { case _: IllegalStateException => Future.successful(7) }
      Checks.equal(7, Await.result(fallback, 5.seconds), "recoverWith flattens async fallback")
      val ordered = Future.sequence(List(Future(3), Future(1), Future(2)))
      Checks.equal(List(3, 1, 2), Await.result(ordered, 5.seconds),
        "sequence preserves input order, not completion order")
    finally
      // Resource ownership stays with the creator, even if a self-check throws.
      executor.shutdown()
      try
        if !executor.awaitTermination(5, TimeUnit.SECONDS) then
          executor.shutdownNow()
          Checks.that(executor.awaitTermination(5, TimeUnit.SECONDS), "executor terminated")
      catch
        case interrupted: InterruptedException =>
          executor.shutdownNow()
          Thread.currentThread().interrupt()
          throw interrupted
    println("FuturesAndEffects passed: eagerness, composition, recovery, executor ownership.")
