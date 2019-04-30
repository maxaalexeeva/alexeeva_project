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
    val docs = source.mkString("").split("\n\n") //category + query + answer

    val queries = new ListBuffer[List[String]]()
    val answers = new ListBuffer[Array[String]]()
    val queryChunks = new ListBuffer[List[String]]
    val queryPOS = new ListBuffer[List[String]]

    for (doc <- docs) {
      val lines = doc.split("\n")
      val q = lines.slice(0,2).mkString(" ").toLowerCase().replaceAll("\\(|\\)|\\!|\\.|:", "")
      //println(q)
      val lemmQ = proc.mkDocument(q) //query

      proc.lemmatize(lemmQ)
      proc.tagPartsOfSpeech(lemmQ)
      proc.chunking(lemmQ)
      //for(l <- lemmQ.sentences) l.chunks.foreach(ch => println("chunk " + ch.mkString(" ")))
      val lemmatizedQuery = lemmQ.sentences.head.lemmas.head.toList
      //println("lemmatized query " + lemmatizedQuery.mkString(" "))
      queries += lemmatizedQuery

      val chunkedQuery = lemmQ.sentences.head.chunks.head.toList
      queryChunks += chunkedQuery

      val posedQuery = lemmQ.sentences.head.tags.head.toList
      queryPOS += posedQuery
      var answer = Array(lines.last.mkString("").toLowerCase())
      if (answer.head contains "|") {
        answer = answer.head.split("\\|")
      }
      answers += answer
      //println("-->" + lines.last.mkString("").toLowerCase())
    }


    //for (q <- queries.toList) println(q + " " + q.mkString(" "))
    //for (answer <- answers) println(answer.mkString(" "))
    var all = 0
    var correct = 0
    for (q <- queries.toList) {
      all += 1
      val index = queries.toList.indexOf(q)
      val relevantChunks = queryChunks(index)

      //println("rel chunks: " + relevantChunks.mkString(" "))
      println("CORRECT ANSWER: " + answers.toList(index).mkString(" "))
      val common_query: List[String] = q

      // a weighted query was supposed to include phrases with proximity and weights
      val weighted_query = new ListBuffer[String]

      if (q.contains("``")) {
        val pattern = "``(.*?)''".r
        val mtch = pattern.findAllIn(q.mkString(" "))
        weighted_query += '"' + mtch.group(1) + '"' + "~1"

      }

      for (word <- q) {

        val wordIndex = q.indexOf(word)
        word match {
          case word if (relevantChunks(wordIndex) == "I-NP") => {
            weighted_query += word + "^2"
          }


            //the idea was to prioritize phrases:
//          case word if (relevantChunks(wordIndex) == "I-NP") => {
//            val phrase = new ListBuffer[String]
//            var i = wordIndex
//            while (i < q.length && relevantChunks(i) == "I-NP") {
//              //println("THIS CHUNK " + relevantChunks(i))
//              //println("THIS WORD " + q(i))
//              phrase += q(i)
//              i = i + 1
//            }
//            //println(phrase.mkString(" "))
//            val phraseList = phrase.toList
//            if (phraseList.length > 1) {
//              weighted_query += '"' + phraseList.mkString(" ") + '"' + "~4"
//
//            }
//
//
//          }

          case _ => weighted_query += word
        }
        //println(word + "<--word")
        //println("word index " + wordIndex)
        //println("chunk " + relevantChunks(wordIndex))
        if (word == "this") {
          val phrase = new ListBuffer[String]
          var i = wordIndex + 1
          while (i < q.length && relevantChunks(i) == "I-NP") {
            //println("THIS CHUNK " + relevantChunks(i))
            //println("THIS WORD " + q(i))
            phrase += q(i)
            i = i + 1
          }
          //println(phrase.mkString(" "))
          val phraseList = phrase.toList
          if (phraseList.length > 1) {
            weighted_query += '"' + phraseList.mkString(" ") + '"' + "~1"
          }
          else {
            if (phraseList.length == 1) weighted_query += phraseList.head + "^2"
          }

        }
      }


      val weightedQueryReady = weighted_query.toList.distinct
      println("WEIGHTED QUERY " + weightedQueryReady.mkString(" "))

      //val ans1 = objQueryEngine.runQ1(common_query)
      val ans1 = objQueryEngine.runQ1(weightedQueryReady)




      if (answers(index).contains(ans1.head.DocName.get("docid").toLowerCase())) {
        correct += 1
        println("CORRECT!!!")
      }
      println(q.mkString(" "))
      //for (x <- ans1) {println("=> " + x.DocName.get("docid"))}
      println("\n\n")

    }

    val acc = correct.toDouble/all
    println("ACCURACY: " + acc)
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
