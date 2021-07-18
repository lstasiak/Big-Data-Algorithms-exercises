import scala.collection.mutable.ArrayBuffer
import scala.util.hashing.MurmurHash3

class HLLver2(b: Int) {

  assert(b >= 4 && b <= 16)

  val numBitsForRegisterIndex: Int = b // right-most
  val m: Int = math.pow(2, numBitsForRegisterIndex).toInt // register size

  val numBitsForRegisterValue = 32

  val register: ArrayBuffer[Int] = ArrayBuffer.fill(m)(0) // M

  val alpha: Double = m match {
    case 16 => 0.673
    case 32 => 0.697
    case 64 => 0.709
    case _  => 0.7213/(1 + 1.079/m)
  }

  def addValue(value: Int): Unit = {
    /** Create hash of the value and convert to corresponding binary string */
    val hash = MurmurHash3.stringHash(value.toString, 11).toBinaryString

    /** Decimal value of rightmost numBitsForRegisterIndex */
    val registerIndex = Integer.parseInt(hash.takeRight(numBitsForRegisterIndex), 2) // w

    /** RegisterValue = Run of zeros from right in next numBitsForRegisterValue bits + 1 */
    val zeroRunLength = hash.dropRight(numBitsForRegisterIndex).takeRight(numBitsForRegisterValue).reverse.takeWhile(_ != '1').length
    val registerValue = zeroRunLength+1

    /** Replace value in register if new value is greater than currently stored value */
    register(registerIndex) = if(register(registerIndex) < registerValue) registerValue else register(registerIndex)
  }

  def getCount: Int = {
    /**
     * Get the estimated count of values seen so far by HLL algo
     */
    val mean = harmonicMean(register)
    val countEstimate: Double = alpha * math.pow(m, 2) * mean  // raw estimation
    estimate(countEstimate).toInt
  }

  def estimate(estimate: Double) : Double = {

    val correctedEstimate = estimate match {
      /** Low value correction */
      case 1 if estimate < 5/2 * m =>
        val zeroRegisterCount = register.count(_ == 0)
        if(zeroRegisterCount != 0) m * math.log(m/zeroRegisterCount.toFloat) else estimate

      /** High value correction */
      case 1 if estimate > (1/30 * math.pow(2, 32)) =>
        -1 * math.pow(2, 32) * math.log(1 - estimate/math.pow(2, 32))

      /** No correction */
      case _ => estimate
    }

    correctedEstimate
  }

  def harmonicMean(values: Seq[Int]) : Double = {
    1/values.foldLeft(0: Double)((a, b) => a + math.pow(2, -1 * b))
  }

  def get_relative_error(): Double = {
    1.04/math.sqrt(m)
  }



}
