
package edu.arizona.cs
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.core.WhitespaceAnalyzer
import java.io._

import org.apache.lucene.search.similarities._
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
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
import org.apache.lucene.store._

import scala.collection.mutable.ListBuffer
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Scanner

import QueryEngine._

import scala.collection.JavaConversions._
import scala.io.Source
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.immutable
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.util.BytesRef


object QueryEngine {

  def main(args: Array[String]): Unit = {
    val lemmatized = true //true for lemmarized index and also for the neither stemmed nor lemmatized bc the "neither" one uses the same whitespace analyzer

    //comment out the next two lines if want to train the index with no stemming and no lemmatization
    val pathToDocs = if (lemmatized == true) "./src/main/resources/lemmatized" else "./src/main/resources/non-lemmatized"
    val pathToIndex = if (lemmatized == true) "./src/main/resources/lemmIndDir" else "./src/main/resources/IndDir"

    //uncomment to build an index with no stemming and not lemmatization
//    val pathToDocs = "./src/main/resources/non-lemmatized"
//    val pathToIndex = "./src/main/resources/neitherIndexDir"

    val indexFile = new File(pathToIndex)
    if (!indexFile.exists())
      indexFile.mkdirs()
      println("********Welcome to the Project!")
    val query13a: List[String] = List("GOLDEN", "GLOBE", "WINNERS", "In", "2010", ":", "As", "Sherlock", "Holmes", "on", "film")//, "order")
    val objQueryEngine: QueryEngine = new QueryEngine(pathToDocs, pathToIndex, lemmatized)
    val ans1: ListBuffer[ResultClass] = objQueryEngine.runQ1(query13a)


  }

}

class QueryEngine(pathToDocs: String, pathToIndex: String, lemmatized: Boolean) {



  val analyzer = if (lemmatized == false) new StandardAnalyzer else new WhitespaceAnalyzer
  val config = new IndexWriterConfig(analyzer)
  val index = FSDirectory.open(Paths.get(pathToIndex))
  val writer = new IndexWriter(index, config)
  var indexExists = false




  val files = getListOfFiles(pathToDocs)


  def buildIndex(): Unit = {
    println("building something")

   // build one doc in your Lucene index from each line in the input file

    for (fileName <- files) {
      //val source = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream(fileName.toString))
      //val lines = source.getLines().toList
      val lines = Source.fromFile(fileName).getLines().toList
     // val lines1 = lines.slice(1,lines.length)
      for (line <- lines.slice(1, lines.length) if !line.startsWith("Image") && !line.startsWith("File") && !line.startsWith("Media")) {
        val splitLine = line.split("\t")
        val docId = splitLine.head
        val text = splitLine.tail.mkString(" ")
        addDoc(writer, docId, text)
      }

    }

    writer.close()
     //.close()
    indexExists = true
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
  @throws[IOException]
  private def addDoc(w: IndexWriter, docid: String, text: String): Unit = {
    val doc = new Document()
    doc.add(new StringField("docid", docid, Field.Store.YES))
    doc.add(new TextField("text", text, Field.Store.YES))
    w.addDocument(doc)
    //println(w.numDocs + " document(s) indexed.")
  }

  def runQ1(query: List[String]): ListBuffer[ResultClass] = {
    //if(! indexExists) buildIndex() //UNCOMMENT IF INDEX DOES NOT EXIST

    val query_to_parse = query.mkString(" ")
    val q = new QueryParser("text", analyzer).parse(query_to_parse)
    val doc_score_list = getDocScoreList(q) //change to getDocScoreListDiffSimilarity to run with different similarity
    return doc_score_list

  }

  def getDocScoreList(q: Query): ListBuffer[ResultClass] = {
    var doc_score_list = new ListBuffer[ResultClass]()
    val hitsPerPage = 10
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    val docs = searcher.search(q, hitsPerPage)
    val hits = docs.scoreDocs
    for (hit <- hits) {
      //println ("-->", hit.doc)
      val docId = hit.doc
      val d = searcher.doc(docId)
      //println(d.get("docid"))
      val objResultClass: ResultClass = new ResultClass()
      objResultClass.DocName = d
      objResultClass.doc_score = hit.score
      doc_score_list += (objResultClass)

    }
    displayResults(hits, searcher)
    doc_score_list

  }


  def getDocScoreListDiffSimilarity(q: Query): ListBuffer[ResultClass] = {
    var doc_score_list = new ListBuffer[ResultClass]()
    val hitsPerPage = 10
    val reader = DirectoryReader.open(index)
    val similarity = new ClassicSimilarity
    //val similarity = new BooleanSimilarity
    val searcher = new IndexSearcher(reader)
    searcher.setSimilarity(similarity)
    val docs = searcher.search(q, hitsPerPage)
    val hits = docs.scoreDocs
    for (hit <- hits) {
      //println ("-->", hit.doc)
      val docId = hit.doc
      val d = searcher.doc(docId)
      //println(d.get("docid"))
      val objResultClass: ResultClass = new ResultClass()
      objResultClass.DocName = d
      objResultClass.doc_score = hit.score
      doc_score_list += (objResultClass)

    }
    displayResults(hits, searcher)
    doc_score_list

  }



  def displayResults(hits: Array[ScoreDoc], searcher: IndexSearcher): Unit = {
    System.out.println("Found " + hits.length + " hits.")
    var i = 0
    while ( {
      i < hits.length
    }) {
      val docId = hits(i).doc
      val d = searcher.doc(docId)
      System.out.println("Hit: " + (i + 1) + ". " + d.get("docid") + " Score: " + hits(i).score)
      i += 1; i
    }
  }



}
