
package edu.arizona.cs
import org.apache.lucene.analysis.standard.StandardAnalyzer
import java.io._

import org.apache.lucene.search.similarities.Similarity
import org.apache.lucene.search.similarities.ClassicSimilarity
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory

import scala.collection.mutable.ListBuffer
import java.io.File
import java.io.IOException
import java.util.Scanner

import org.clulab.processors.shallownlp.ShallowNLPProcessor
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.io.Source
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.{immutable, mutable}

object Preprocess {

  def main(args: Array[String]): Unit = {

    println("Hi")
    val qa = new Preprocess
    qa.train("wiki")

//    try {
//      val fileName: String = "input.txt"
//      println("********Welcome to  Homework 3!")
//      val query13a: List[String] = List("information", "retrieval")
//      val objQueryEngine: QueryEngine = new QueryEngine(fileName)
//      val ans2: ListBuffer[ResultClass] = objQueryEngine.runQ1(query13a)
//      println(":value of ans2 is" + ans2)
//
//      for (x <- ans2) {
//        println("value of x is " + x.DocName.get("docid"))
//      }
//    } catch {
//      case ex: Exception => println(ex.getMessage)
//
//    }
  }

}

class Preprocess() {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def train(collection: String): Unit = {

    val files = getListOfFiles("./src/main/resources/wiki")
    val proc = new ShallowNLPProcessor()

    val data = for {
      file <- files
      fileText = "\n\n" + Source.fromFile(file).getLines().mkString("\n\n").replaceAll("\\[ tpl \\]|\\[\\[File.*?\\]\\]|\\[\\[Image.*?\\]\\]|^\n|\\[\\[Media.*?\\]\\]|url.*? ", "")

    }  yield (file, fileText)

    //home/masha/alexeeva_project/src/main/resources/lemmatized

    for (i <- data) {
      val currentFileName = i._1.toString.split("/")//.slice(0, 5).mkString(" ")
      val newFileName = "./src/main/resources/lemmatized/" + currentFileName.slice(5, currentFileName.length).mkString("/")
      //println(newFileName)
      val fw = new FileWriter(newFileName, true)
      val allData = i._2.split("\n\n\\[\\[") //split on [[ to separate entries
      for (line <- allData) {
        //println(line)
//        println(allData.indexOf(line))

        val splitLine = line.split("\\]\\]\n\n") //split on ]] to separate title from body
        fw.write(splitLine.head.replaceAll("\n*", "") + "\t")
        val textToLemmatize = splitLine.tail.mkString(" ").replaceAll("  +|\n", "").split("\\. ").filter(m => m.length > 0) //break body up into sentences
        //println("-->" + splitLine.head)
        //for (sent <- textToLemmatize) println("sent" + sent)
        //val doc = proc.annotateFromSentences(textToLemmatize) //lemmatize

        for {
          sent <- proc.annotateFromSentences(textToLemmatize).sentences
          lemmas <- sent.lemmas
        } fw.write(lemmas.mkString(" ") + ". ")
        fw.write("\n")

      }

       fw.close()

    }



//    val fw = new FileWriter("test.txt", true)
//
//    val allData = data.mkString(" ").split("\\[\\[") //split on [[ to separate entries
//    //println(allData.length)
//
//    for (line <- allData) {
//      //println(line)
//      println(allData.indexOf(line))
//
//      val splitLine = line.split("\\]\\]") //split on ]] to separate title from body
//      fw.write(splitLine.head + "\t")
//      val textToLemmatize = splitLine.tail.mkString(" ").split("\\. ").filter(m => m.length>5) //break body up into sentences
//
//      val doc = proc.annotateFromSentences(textToLemmatize.headOption) //lemmatize
//
//      for {
//        sent <- doc.sentences
//        lemmas <- sent.lemmas
//        } fw.write(lemmas.mkString(" ") + "." + "\n")
//////            println(splitLine.head + "\n")
//////            println(bodyLemmatized.mkString(" "))
////
////      //fw.write(splitLine.head + "\t" + bodyLemmatized.mkString(" ") + "\n")
////
//    }


   // fw.close()

    println("tada")
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
}
//  def runQ1(query: List[String]): ListBuffer[ResultClass] = {
//    val ans = returnDummyResults()
//    return ans;
//  }
//
//  def runQ13a(query: List[String]): ListBuffer[ResultClass] = {
//    val ans = returnDummyResults()
//    return ans;
//  }
//
//  def runQ13b(query: List[String]): ListBuffer[ResultClass] = {
//    val ans = new ListBuffer[ResultClass]()
//    return ans;
//  }
//
//  def runQ13c(query: List[String]): ListBuffer[ResultClass] = {
//    val ans = returnDummyResults()
//    return ans;
//  }
//
//
//  private def returnDummyResults(): ListBuffer[ResultClass] = {
//    var doc_score_list = new ListBuffer[ResultClass]()
//
//    for (i <- 0.until(2)) {
//      val doc: Document = new Document()
//      doc.add(new TextField("title", "", Field.Store.YES))
//      doc.add(
//        new StringField("docid",
//          "Doc" + java.lang.Integer.toString(i + 1),
//          Field.Store.YES))
//      val objResultClass: ResultClass = new ResultClass()
//      objResultClass.DocName = doc
//      doc_score_list += (objResultClass)
//    }
//    return doc_score_list
//  }





//      for (sent<- doc.sentences) {
//        sent.lemmas.foreach(lem => println("=>" + lem.mkString(" ")))
//      }
//      println(splitLine.head + "\n")
//      println(splitLine.tail.mkString(" ") + "\n")
//      println(splitLine.head.mkString(" "))
//      println("\n")
//      println("-->" + splitLine.tail.mkString(" "))
//      println("\n")
//      val doc = proc.annotateFromSentences(splitLine.tail.mkString(" ").split(". ")) //(splitLine.tail.mkString(" "))
//      for (d <- doc.sentences) {
//        d.lemmas.foreach(lemma => lemma.mkString(" "))
//      }
//      for {
//              sent <- doc.sentences
//              sentTok = sent.lemmas.head.mkString(" ").split(" ")
//              word <- sentTok
//            } println(word)

//      val uniLemmas = for {
//        sent <- doc.sentences
//        sentTok = sent.lemmas.head.mkString(" ").split(" ")
//        word <- sentTok
//      } yield word
//      //for (word <- uniLemmas) println(word + "\n")
//      titleBody += (splitLine.head -> uniLemmas.mkString(" "))

//for (tb <- titleBody) println(tb._1 + "\n")
//for (tb <- titleBody) println(tb._2 + "\n")
//
//    for (item <- titleBody) {
//      println(item._1 + "\n")
//      print(item._2 + "\n")
//
//    }

//    val unigrams = for {
//      sent <- doc.sentences
//      sentTok = sent.lemmas.head.mkString(" ").split(" ")
//      docId = sentTok(0)
//      word <- sentTok
//      if (sentTok.indexOf(word) != 0)
//
//    } yield word


//    val text = "hello i was doing homework"
//    val proc = new ShallowNLPProcessor()
//    val annotated = proc.annotate(text)
//    val lemmas = for {
//      doc <- annotated.sentences
//      lem <- doc.lemmas
//
//    } yield lem.mkString(" ")
//
//    for (x <- lemmas) println(x)

//
//    println(allData)
//    for (d <- data) println(d + "\n")

//    val source = Source.fromResource(collection)
//    println(source.length)
//    val docs = for (line <- source.getLines()) yield line
//    println("docs done")
//    val listDocs = docs.toList
//    println(listDocs)