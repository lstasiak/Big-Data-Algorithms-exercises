
import java.io.{BufferedWriter, File, PrintWriter}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

package object BDA_func_tools {

  def getListOfFiles(dir: String): List[String] = {
    /**
     * return list of file path from given directory
     */
    val file = new File(dir)
    file.listFiles.filter(_.isFile)
      .map(_.getPath).toList
  }

  def prepareBook(filePath: String, remove_stop_words:Boolean=true, stopwordsDir: String="stopwords/stopwords_en.txt"): String = {
    /**
     * returns list of words without (optional) stopwords for given txt file
     */
    var book = Source.fromFile(filePath).mkString
      .toLowerCase.replaceAll("\\W|_", " ")
      .split("\\s+")

    if (remove_stop_words){
      val stopwords = Source.fromFile(stopwordsDir).mkString
        .toLowerCase
        .split("\\s+")
      book = book.filterNot(stopwords.contains(_))
    }
    book.mkString("")
  }

  def get_shingles(document: String, k: Int): Set[String] = {
    /**
     * divide string into k-shingles (substrings)
     */
    val set_of_shingles = ArrayBuffer[String]()
    var n = 0
    while (n < document.length - (k-1)) {
      set_of_shingles += document.slice(n, k+n)
      n = n + 1
    }
    set_of_shingles.toSet
  }

  def jaccard_similarity[T](set1: Set[T], set2: Set[T]): Double = {
    /**
     * calculate Jaccard similarity between two sets
     */
    set1.intersect(set2).size / set1.union(set2).size.toDouble
  }

  def jaccard(f1: String, f2: String, k: Int=7): Double = {
    /**
     * calculate Jaccard Distance between two sets of shingles for given two strings
     */
    val f1_shingles = get_shingles(f1, k)
    val f2_shingles = get_shingles(f2, k)
    val result: Double = 1 - jaccard_similarity[String](f1_shingles, f2_shingles)

    BigDecimal(result).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def calculate_jaccard_results(books_paths: List[String]): Unit = {
    var books: Map[Int, String] = Map()

    // prepare books
    for ((book, i) <- books_paths.zip(1 to 10)) {
      books += (i -> prepareBook(book))
    }
    val booksSorted = books.toSeq.sorted //sorted by index
    println("SPREPAROWANO WSZYSTKIE KSIĄŻKI")

    // writer
    val writer = new BufferedWriter(new PrintWriter(new File("jaccard_distance_k7.txt")))
    writer.write("/     ")
    (1 to 10).foreach(i => writer.write(i.toString + "     "))
    writer.write("\n")
    for ((i, book) <- booksSorted) {
      writer.write(i.toString + "   ")
      for (second <- booksSorted.map(t => t._2)) {
        val distance = jaccard(book, second)
        writer.write(distance.toString + "   ")
      }
      writer.write("\n")
      println(i)
    }
    writer.close()
  }

  def test_shingles_jaccard(): Unit = {
    val str1 = "helloitsme"
    val str2 = "helloitsyou"

    println("Jaccard similarity:")
    println(jaccard_similarity(str1.toSet, str2.toSet)) // should be 0.7
    println("Jaccard Distance")
    println(jaccard(str1, str2))
  }

  def estimateJaccardDistance(signature1: Seq[String], signature2: Seq[String]): Double = {
    var estimatedSimilarity = 0.0
    for ((s1,s2) <- signature1.zip(signature2)) {
      if (s1 == s2)
        estimatedSimilarity = estimatedSimilarity + 1
    }
    val estimated_distance = 1 - estimatedSimilarity/signature1.size.toDouble
    estimated_distance
  }

}
