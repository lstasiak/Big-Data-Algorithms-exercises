import scala.io.Source
import org.apache.spark.sql.SparkSession

object WordCloud {

  def main(args: Array[String]): Unit = {

    val spark =
      SparkSession
        .builder
        .master("local[*]")
        .appName("CSV Writter Test")
        .getOrCreate()
    import spark.implicits._

    // read book and stopwords from txt
    val book = Source.fromFile("adventures_of_tom_sawyer.txt").mkString.toLowerCase.replaceAll("[^\\w+|']", " ").split("\\s+")
    val stopwords = Source.fromFile("stopwords_en.txt").mkString.toLowerCase.split("\\s+")
    val filtered = book.filterNot(stopwords.contains(_))

    val wordCount = filtered.groupBy(w => w).transform((_,v) => v.length)

    // sort by 2nd param
    val result = wordCount.toSeq.sortWith((x,y) => x._2 > y._2)
    // invert key and values
    val result2 = result.map(_.swap)

    val resultDF = result2.toDF("count", "word")

    resultDF.printSchema
    resultDF.repartition(1).write.csv("HERE")
    spark.stop()
  }
}
