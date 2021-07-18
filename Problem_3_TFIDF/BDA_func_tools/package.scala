
import java.io.{BufferedWriter, File, PrintWriter}

import org.apache.commons.math3.util.Precision.round

import scala.io.Source

package object BDA_func_tools {

  def TFIDF(term: String, document: List[String], booksDir: String): Double = {
    /**
     * calculates TF.IDF coefficient for term and documents from given directory
     */
    val bookNames = getListOfFiles(booksDir)

    var words = countWords(document).toMap
    var tf = 0.0
    if (words.contains(term))
      tf = words(term).toDouble/words.values.max
    val log2 = (x: Double) => math.log10(x)/math.log10(2.0)
    var n: Int = 0  // counter for documents where 'term' exists
    for (title <- bookNames) {
      val book = prepareBook(title)
      words = countWords(book).toMap
      if(words.contains(term)) //words.keys.exists(w => w==term)
        n += 1
    }
    val idf = log2(bookNames.length.toDouble/n)

    round(tf*idf, 4)
  }

  def prepareBook(filePath: String, remove_stop_words:Boolean=true, stopwordsDir: String="stopwords/stopwords_en.txt"): List[String] = {
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
  book.toList
  }

  def countWords(text: List[String]): List[(String, Int)] = {
    /**
     * return list of sorted words and their frequency from input list of words
     */
    val words = text.groupBy(identity)
      .transform((_,v) => v.length)
      .toList.sortWith((x,y) => x._2 > y._2)

    words
  }

  def getListOfFiles(dir: String): List[String] = {
    /**
     * return list of file path from given directory
     */
    val file = new File(dir)
    file.listFiles.filter(_.isFile)
      .map(_.getPath).toList
  }

  def getTitles: List[String] ={
    /**
     * return <shorted> list of chosen titles
     */
    List("Alice-Lewis C.",
      "Dracula-Bram S.",
      "Emma-Austen J.",
      "Grimms Tales-Grimm J.W.",
      "Moby Dick-Melville H.",
      "Pride and Prejustice-Austen J.",
      "Sherlock Holmes-Doyle A.C.",
      "The Importance...-Wilde O.",
      "Dorian Gray-Wilde O.",
      "Dr Jekyll-Stevenson R.")
  }



  def completeTFIDFDataBase(dirName: String): Unit ={
    /**
     * generates txt files for each book with  three columns: [Word, Count, TFIDF]
     * dirName - directory of books
     */
    val titles = getTitles

    val documents = getListOfFiles(dirName)
    var preparedBooks = Map[String, List[(String, Int)]]()

    for (i <- documents.indices) {
      // pętla wszystkie książki
      preparedBooks += (titles(i) -> countWords(prepareBook(documents(i))))
    }
    println("SPREPAROWANO WSZYSTKIE KSIĄŻKI")
    var allWords = List[String]()
    for (freqList <- preparedBooks.values) {
      val onlyWords = freqList.toMap.keys
      allWords = allWords ++ onlyWords.filterNot(allWords.contains(_))
    }
    for (title <- preparedBooks.keys){
      val writer = new BufferedWriter(new PrintWriter(new File(title + ".txt")))
      writer.write("Word,Count,TF.IDF")
      writer.write("\n")

      for (word <- allWords) {
        val tfidf = TFIDF(word, preparedBooks(title), preparedBooks)
        if (preparedBooks(title).toMap.contains(word)) {
          val count = preparedBooks(title).toMap.get(word).get
          writer.write(word + "," + count.toString + "," + tfidf.toString)
          writer.write("\n")
        } else {
          writer.write(word + "," + 0 + "," + tfidf.toString)
          writer.write("\n")
        }
      }
      println("Wygenerowane dla: " + title)
      writer.close()
    }
  }

  def TFIDF(term: String, prepared_document: List[(String, Int)], wordSet: Map[String, List[(String, Int)]]): Double = {
    /**
     * szybsza wersja funkcji na potrzeby zadania i złożoności: przyjmuje gotową listę słów i wystąpień dla danej książki
     * i bazę tak spreparowanych dokumentów.
     */
    var words = prepared_document.toMap
    var tf = 0.0
    if (words.keys.exists(w => w==term))
      tf = words(term).toDouble/words.values.max
    val log2 = (x: Double) => math.log10(x)/math.log10(2.0)
    var n: Int = 0  // counter for documents where 'term' exists
    for (title <- wordSet.keys) {
      words = wordSet(title).toMap
      if(words.keys.exists(w => w==term))
        n += 1
    }
    val idf = log2(wordSet.keys.size.toDouble/n.toDouble)
    tf*idf
  }

  def generateRankingData(comparator: String = "TFIDF", rows: Int = 20, dataDir: String = "all_tfidf_data", sveSingleFile: Boolean = false): Map[String, Seq[(String, Int, Double)]] = {
    /**
     * returns map of titles and connected sequences with TFIDF data for every word
     * saves data to single txt file or separated file for each book in dir
     */
    val titles = getTitles.sorted

    val dataPaths = getListOfFiles(dataDir)
    var resultCollection = Map[String, Seq[(String, Int, Double)]]()
    for (i <- dataPaths.indices) {
      val data = Source.fromFile(dataPaths(i)).getLines().drop(1).toSeq
      var dataset: Seq[(String, Int, Double)] = Nil

      for (string <- data) {
        val array = string.split(",")
        val (x, y, z) = (array(0), array(1), array(2))
        dataset = dataset :+ (x, y.toInt, round(z.toDouble, 4))
      }
      var bestN: Seq[(String, Int, Double)] = Nil
      comparator match {
        case "TFIDF" =>
          bestN = dataset.sortWith(_._3 > _._3).take(rows)
        case "Word" =>
          bestN = dataset.sortWith(_._1 > _._1).take(rows)
        case "Count" =>
          bestN = dataset.sortWith(_._2 > _._2).take(rows)
        case _ => new IllegalArgumentException("ZLY COMPARATOR. Dostępne tylko: TFIDF, Word, Count")
      }
      resultCollection += (titles(i) -> bestN)
    }
    // SAVE MODE
    if (!sveSingleFile) {
      // Save every book data to separate files
      val saveDir = "ResultData/"
      for ((title, seq) <- resultCollection) {
        val file: File = new File(saveDir + title + ".txt")
        file.getParentFile.mkdirs()
        val writer = new BufferedWriter(new PrintWriter(file))
        writer.write("Word  --  Count  --  TF.IDF")
        writer.write("\n")
        for ( (word, count, tfidf) <- seq) {
          writer.write(word + "  " + count + "  " + tfidf)
          writer.write("\n")
        }
        writer.close()
      }
    } else {
      // save all data to single file
      val saveDir = "ResultData_SingleFile/"
      val file: File = new File(saveDir + "final_data.txt")
      file.getParentFile.mkdirs()
      val writer = new BufferedWriter(new PrintWriter(file))

      for (title <- resultCollection.keys) {
        val seq = resultCollection(title)
        writer.write(title)
        writer.write("\n\n")
        writer.write("Word  Count  TF.IDF")
        writer.write("\n")
        for ((word, count, tfidf) <- seq){
          writer.write(word + " | " + count.toString + " | " + tfidf.toString + " | ")
          writer.write("\n")
        }
        writer.write("\n")
      }
      writer.close()
    }
    resultCollection
  }

  def recommendBook(word: String, booksDir: String="books", returnWithTFIDF: Boolean=false): Seq[Any] = {
    /**
     * used for task no. 7 - returns sequence of sorted titles according to tf.idf coeff.
     * Optionally it can return also TFIDF value for each book
     */
    val titlePaths = getListOfFiles(booksDir)
    val titles = getTitles.sorted
    var ranking: Seq[(String, Double)] = Nil

    titlePaths.indices
      .foreach(i => {
        print("-*")
        val doc = prepareBook(titlePaths(i))
        val tfidf = TFIDF(word, doc, booksDir)
        ranking = ranking :+ (titles(i), tfidf)
      })
    println()
    println("Recommendation for term: " + word)
    val sortedRanking = ranking.sortWith(_._2 > _._2)
    if (!returnWithTFIDF){
      sortedRanking.map(_._1)
    } else {
      sortedRanking
    }
  }

  def test_recommendation_system(listOfWords: List[String], saveToTxt: Boolean=false): Unit ={
    /**
     * print and/or save function for recommendation system.
     * Generates recommendation ranking for each word from listOfWords
     */
    if (!saveToTxt) {
      for (word <- listOfWords){
        val r = recommendBook(word, returnWithTFIDF = true)
        for ((title, tfidf) <- r) {
          println(title + " -->   TFIDF = " + tfidf)
        }
      }
    } else {
      // save all data to file
      val file: File = new File("RecommendationResults/recommend_data.txt")
      file.getParentFile.mkdirs()
      val writer = new BufferedWriter(new PrintWriter(file))

      for (word <- listOfWords){
        val r = recommendBook(word, returnWithTFIDF = true)
        writer.write("\n")
        writer.write("Recommendations for word: " + word)
        writer.write("\n")
        for ((title, tfidf) <- r) {
          println(title + " -->   TFIDF = " + tfidf)
          writer.write(title + " -->   TFIDF = " + tfidf)
          writer.write("\n")
        }
      }
      writer.close()
    }
  }


}
