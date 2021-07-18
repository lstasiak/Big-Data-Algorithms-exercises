import BDA_func_tools._

object JaccardTest2 {

  def main(args: Array[String]): Unit = {

    //test_shingles_jaccard()
    val books = getListOfFiles("books")


    // Result for problem 18
    //calculate_jaccard_results(books)

//     Results for problem 19 - estimated Jaccard Dist
    for (h <- Array(50, 100, 250)){
      val MH = new Minhashing(h, k = 4, books)
      MH.estimateJaccard()
      println("Done for H = " + h.toString)
    }

    }


}
