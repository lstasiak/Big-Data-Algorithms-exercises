import java.io.{BufferedWriter, File, PrintWriter}

import BDA_func_tools.{estimateJaccardDistance, get_shingles, prepareBook}

import scala.util.Random
import scala.util.control.Breaks.{break, breakable}
import scala.util.hashing.MurmurHash3

class Minhashing(H: Int, k: Int, books_paths: List[String]) {

  var signatures: Map[Int, Array[String]] = Map[Int, Array[String]]()

  for (i <- 0 to 9) {
    signatures += (i -> Array.fill[String](H)(""))
  }

  var dataset: Map[Int, Set[String]] = Map()

  // prepare books
  for ((book, i) <- books_paths.zip(0 to 9)) {
    dataset += (i -> get_shingles(prepareBook(book), k))
  }
  val all_elements: Set[String] = dataset.values.reduce((a, b) => a.union(b))

//  def generate_minhashes(): Array[(String, Int)] = {
//
//    val minhashes = Array[String]()
//
//    for (set <- dataset.values) {
//      val permutation = Random.shuffle(all_elements).toSeq
//      breakable {
//        for (element <- permutation) {
//          if (set.contains(element)) {
//            minhashes :+ element
//            break()
//          }
//        }
//      }
//    }
//    minhashes.zipWithIndex
//  }
//
//  def generate_signatures(): Unit = {
//    for (i <- 0 until H) {
//      for ((value, set) <- generate_minhashes()) {
//        signatures(set)(i) = value
//      }
//    }
//  }

  def generate_signatures(): Unit = {

    //val hash_fcn = all_elements.map(s => MurmurHash3.stringHash(s, 11) % all_elements.size).toArray

    for ((k, set) <- dataset){
      val arr = set.toArray
      for (i <- 0 until H) {
        var minHashVal = 1000000
        for (shingle <- arr) {
          val hashValue = math.abs(MurmurHash3.stringHash(shingle, i) % arr.size)

          if (hashValue < minHashVal)
            minHashVal = hashValue
        }
        signatures(k)(i) = arr(minHashVal)
      }
    }


  }

  def estimateJaccard(): Unit = {
    generate_signatures()

    val sortedSeq = signatures.toSeq.sortBy(_._1)

    // write results
    val writer = new BufferedWriter(new PrintWriter(new File("estimated_distance_H" + H.toString + "_k" + k.toString + ".txt")))
    writer.write("/     ")
    (0 to 9).foreach(i => writer.write(i.toString + "     "))
    writer.write("\n")
    for ((i, signature1) <- sortedSeq) {
      writer.write(i.toString + "   ")
      for (signature2 <- sortedSeq.map(s => s._2)) {
        val estimated = estimateJaccardDistance(signature1, signature2)
        writer.write(estimated.toString + "   ")
      }
      writer.write("\n")
    }
    writer.close()
  }



}
