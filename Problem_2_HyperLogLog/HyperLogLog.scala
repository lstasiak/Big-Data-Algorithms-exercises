import scala.io.Source

object HyperLogLog {
  def readData(path: String = "lbl-pkt-4/lbl-pkt-4.tcp"): Array[(Float, Float, Float, Float, Float, Float)] = {
    val dataset = Source.fromFile(path).getLines().map{ x =>
      val line = x.split(" ").map(value => value.toFloat)
      (line(0), line(1), line(2), line(3), line(4), line(5))
    }.toArray
    dataset
  }
  def getHosts(path: String = "lbl-pkt-4/lbl-pkt-4.tcp"): Array[(Int, Int)] = {
    val dataset = Source.fromFile(path).getLines().map{ x =>
      val line = x.split(" ").slice(1, 3).map(value => value.toInt)
      (line(0), line(1))
    }.toArray
    dataset
  }


  def main(args: Array[String]): Unit = {
    val hll1 = new HLLver2(7)
    val hll2 = new HLLver2(7)

    val sourceHosts = getHosts().map{ host =>
      val sourceHost = host._1
      hll1.addValue(sourceHost)
      sourceHost
    }

    val destinationHosts = getHosts().map{ host =>
      val destHost = host._2
      hll2.addValue(destHost)
      destHost
    }
    // Modified HyperLogLog Test
    val actualSH1 = sourceHosts.toSet.size
    val estimatedSH1 = hll1.getCount
    val actualSH2 = destinationHosts.toSet.size
    val estimatedSH2 = hll2.getCount

    println("Actual Source Hosts count: " + actualSH1.toString)
    println("Estimated Source Hosts count: " + estimatedSH1.toString + " +/- " + hll1.get_relative_error().toString)
    println("Actual Destination Hosts count: " + actualSH2.toString)
    println("Estimated Destination Hosts count: " + estimatedSH2.toString + " +/- " + hll2.get_relative_error().toString)

  }
}

