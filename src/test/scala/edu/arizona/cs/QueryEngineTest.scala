package edu.arizona.cs
import org.apache.lucene.document.Document
import scala.io.Source
import org.scalatest.FunSuite
import scala.collection.JavaConversions
import scala.collection.JavaConversions._
import scala.collection.immutable._
import org.clulab.processors.clu.CluProcessor
import scala.collection.mutable.ListBuffer



class QueryEngineTest extends FunSuite{

  val questionsFile = "questions.txt"
  val doc_names_q1 = List("Doc1", "Doc2")


  test("QueryEngine.Improved") {
    val proc = new CluProcessor()

    val pathOfIndex = "./src/main/resources/lemmIndDir"
    val pathToDocs = "./src/main/resources/lemmatized"
    val objQueryEngine: QueryEngine = new QueryEngine(pathToDocs, pathOfIndex, true)

    val source = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream(questionsFile))
    val docs = source.mkString("").split("\n\n") //category + query + answer

    val queries = new ListBuffer[List[String]]()
    val answers = new ListBuffer[Array[String]]()

    for (doc <- docs) {
      val lines = doc.split("\n")
      val q = lines.slice(0,2).mkString(" ").toLowerCase().replaceAll("\\(|\\)|\\!|\\.", "")
      val lemmQ = proc.mkDocument(q)
      proc.lemmatize(lemmQ)
      val lemmatizedQuery = lemmQ.sentences.head.lemmas.head.toList
      queries += lemmatizedQuery
      var answer = Array(lines.last.mkString("").toLowerCase())
      if (answer.head contains "|") {
        answer = answer.head.split("\\|")
      }
      answers += answer
    }


    var all = 0
    var correct = 0
    for (q <- queries.toList) {
      all += 1
      val index = queries.toList.indexOf(q)

      println("QUERY: " + q.mkString(" "))
      println("CORRECT ANSWER: " + answers.toList(index).mkString(" "))
      val common_query: List[String] = q
      println("ORIGINAL RESULTS:")
      var ans1 = objQueryEngine.runQ1(common_query)
      val pattern =
      """(\d\d\d+)""".r

      val digits = new ListBuffer[String]
      for(m <- pattern.findAllIn(q.mkString(" ")).matchData) {
        digits += m.group(1)
      }

      val digitList = digits.toList


      if (digitList.size > 0) {

        val potentialAnswers = new ListBuffer[ResultClass]
        for (ans <- ans1) {

          val text = ans.DocName.get("text")

          if (text.contains(digitList(0) + " ")) {
            potentialAnswers prepend ans
          }

        }
        if (potentialAnswers.length > 0) {
          ans1 prepend potentialAnswers.last
          println("PROMOTED ANSWER: " + potentialAnswers.last.DocName.get("docid"))
        }

      }


      if (answers(index).contains(ans1.head.DocName.get("docid").toLowerCase())) {
        correct += 1
        println("CORRECT!!!")
      }
      println("\n\n")

    }

    val acc = correct.toDouble/all
    println("ACCURACY: " + acc)
    assert(acc > .20)

  }



}
