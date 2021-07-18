import BDA_func_tools._

object ProblemSheet2 {

  def main(args: Array[String]): Unit = {

    // it takes a bit longer time...
    //completeTFIDFDataBase("books")


    // to generate ranking of 20 words with highest tf.idf for each book saved separately (for each book)
    // based on all_tfidf_data with prepared database for each book
    generateRankingData()

    // test task 7.
    val testWords = List("murder", "blood", "cat", "science")
    test_recommendation_system(testWords, saveToTxt = true)
  }
}
