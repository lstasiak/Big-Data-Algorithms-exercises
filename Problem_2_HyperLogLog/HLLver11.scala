import scala.collection.mutable.ArrayBuffer
import scala.util.hashing.MurmurHash3

class HLLver11(b: Int) {

  val numBitsForRegisterIndex: Int = b // right-most
  val m: Int = math.pow(2, numBitsForRegisterIndex).toInt // register size

  val numBitsForRegisterValue = 15

  val register: ArrayBuffer[Int] = ArrayBuffer.fill(m)(0) // M

  val alpha: Double = 0.72134

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
    val Z = harmonicMean(register)
    val countEstimate: Double = alpha * math.pow(m, 2) * Z
    countEstimate.toInt
  }

  def harmonicMean(values: Seq[Int]) : Double = {
    1/values.foldLeft(0: Double)((a, b) => a + math.pow(2, -1 * b))
  }

}
