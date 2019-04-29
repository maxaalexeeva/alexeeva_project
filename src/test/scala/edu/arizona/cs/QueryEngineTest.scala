package edu.arizona.cs
import org.apache.lucene.document.Document
import scala.io.Source
import org.scalatest.FunSuite
import scala.collection.JavaConversions
import scala.collection.JavaConversions._
import scala.collection.immutable._
import org.clulab.processors.clu.CluProcessor


class QueryEngineTest extends FunSuite{

  //val inputFileFullPath = "input.txt"
  val questionsFile = "questions.txt"
  val doc_names_q1 = List("Doc1", "Doc2")


  test("QueryEngine.Q1") {
    val proc = new CluProcessor()

    val pathOfIndex = "./src/main/resources/lemmIndDir"
    val pathToDocs = "./src/main/resources/lemmatized"

    //val inputFileFullPath = "input.txt"
    val objQueryEngine: QueryEngine = new QueryEngine(pathToDocs, pathOfIndex, true)

    val source = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream(questionsFile))
    val docs = source.mkString("").split("\n\n")


    for (doc <- docs) {
      val lines = doc.split("\n")
      val q = lines.slice(0,2).mkString(" ").toLowerCase()
      println(q)
      val lemmQ = proc.mkDocument(q)
      proc.lemmatize(lemmQ)
      val lemmatizedQuery = lemmQ.sentences.head.lemmas.head
      println("lemmatized query " + lemmatizedQuery.mkString(" "))

      println("-->" + lines.last.mkString("").toLowerCase())
    }

    val common_query: List[String] = List("information", "retrieval")
    val ans1 = objQueryEngine.runQ1(common_query)
    val doc_names_q1 = List("Doc1", "Doc2")
    var counter1 = 0
    for (x <- ans1) {
      assert(x.DocName.get("docid") == doc_names_q1(counter1))
      counter1 = counter1 + 1
    }
  }

//  test("QueryEngine.Q13a") {
// val objQueryEngine: QueryEngine = new QueryEngine(inputFileFullPath)
//
//    val common_query: List[String] = List("information", "retrieval")
//    val ans1 = objQueryEngine.runQ13a(common_query)
//    var counter1 = 0
//    for (x <- ans1) {
//      assert(x.DocName.get("docid") == doc_names_q1(counter1))
//      counter1 = counter1 + 1
//    }
//  }
//
//  test("QueryEngine.Q13b") {
//  val inputFileFullPath = "input.txt"
//    val objQueryEngine: QueryEngine = new QueryEngine(inputFileFullPath)
//    val common_query: List[String] = List("information", "retrieval")
//    val ans1 = objQueryEngine.runQ13b(common_query)
//    assert(ans1.size()  == 0)
//
//  }
//
//  test("QueryEngine.Q13c") {
//   val inputFileFullPath = "input.txt"
//    val objQueryEngine: QueryEngine = new QueryEngine(inputFileFullPath)
//    val common_query: List[String] = List("information", "retrieval")
//    val ans1 = objQueryEngine.runQ13c(common_query)
//    assert(ans1(0).DocName.get("docid")== doc_names_q1(0))
//  }

}
