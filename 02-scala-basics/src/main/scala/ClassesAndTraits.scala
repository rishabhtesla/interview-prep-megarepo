package interviewprep.scalabasics

/** Traits define contracts; composition avoids dependence on trait initialization order. */
object ClassesAndTraits:
  trait PriceRule:
    def apply(cents: Long): Long

  final class ThresholdDiscount(threshold: Long, discount: Long) extends PriceRule:
    require(threshold >= 0 && discount >= 0 && discount <= threshold)
    override def apply(cents: Long): Long =
      if cents >= threshold then cents - discount else cents

  trait ReceiptFormatter:
    def format(cents: Long): String

  object PlainReceipt extends ReceiptFormatter:
    override def format(cents: Long): String = s"Total: $cents cents"

  final class Checkout(rule: PriceRule, formatter: ReceiptFormatter):
    def receipt(cents: Long): String =
      require(cents >= 0, "price cannot be negative")
      val discounted = rule(cents)
      require(discounted >= 0 && discounted <= cents, "rule violated price contract")
      formatter.format(discounted)

  def main(args: Array[String]): Unit =
    val discount = new ThresholdDiscount(1000, 100)
    val checkout = new Checkout(discount, PlainReceipt)
    Checks.equal("Total: 900 cents", checkout.receipt(1000), "inclusive threshold")
    Checks.equal("Total: 999 cents", checkout.receipt(999), "below threshold unchanged")
    Checks.equal("Total: 0 cents", checkout.receipt(0), "zero allowed")

    // A local test implementation isolates presentation without creating an inheritance tree.
    val fakeFormatter = new ReceiptFormatter:
      override def format(cents: Long): String = s"[$cents]"
    Checks.equal("[900]", new Checkout(discount, fakeFormatter).receipt(1000),
      "behavior is replaceable through its interface")
    val invalidRule = new PriceRule:
      override def apply(cents: Long): Long = cents + 1
    Checks.that(scala.util.Try(new Checkout(invalidRule, PlainReceipt).receipt(10)).isFailure,
      "a subtype must obey behavioral contracts, not just compile")
    Checks.that(scala.util.Try(checkout.receipt(-1)).isFailure, "input validation")
    println("ClassesAndTraits passed: contracts, substitution, composition, test doubles.")
