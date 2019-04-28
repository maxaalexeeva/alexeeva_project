//
//package edu.arizona.cs
//import org.apache.lucene.analysis.standard.StandardAnalyzer
//import org.apache.lucene.analysis.core.WhitespaceAnalyzer
//import java.io._
//
//import org.apache.lucene.search.similarities.Similarity
//import org.apache.lucene.search.similarities.TFIDFSimilarity
//import org.apache.lucene.search.similarities.ClassicSimilarity
//import org.apache.lucene.document.Document
//import org.apache.lucene.document.Field
//import org.apache.lucene.document.StringField
//import org.apache.lucene.document.TextField
//import org.apache.lucene.index.DirectoryReader
//import org.apache.lucene.index.IndexReader
//import org.apache.lucene.index.IndexWriter
//import org.apache.lucene.index.IndexWriterConfig
//import org.apache.lucene.queryparser.classic.ParseException
//import org.apache.lucene.queryparser.classic.QueryParser
//import org.apache.lucene.search.IndexSearcher
//import org.apache.lucene.search.Query
//import org.apache.lucene.search.ScoreDoc
//import org.apache.lucene.search.TopDocs
//import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
//import org.apache.lucene.store._
//
//import scala.collection.mutable.ListBuffer
//import java.io.File
//import java.io.IOException
//import java.nio.charset.StandardCharsets
//import java.nio.file.Paths
//import java.util.Scanner
//
//import QueryEngine._
//
//import scala.collection.JavaConversions._
//import scala.io.Source
//import scala.collection.mutable.{ArrayBuffer, ListBuffer}
//import scala.collection.immutable
//import org.apache.lucene.index.DirectoryReader
//import org.apache.lucene.index.IndexReader
//import org.apache.lucene.search.IndexSearcher
//import org.apache.lucene.search.ScoreDoc
//import org.apache.lucene.search.TopDocs
//import org.apache.lucene.util.BytesRef
//
//
//object QueryEngine {
//
//  def main(args: Array[String]): Unit = {
//    val lemmatized = true
////    try {
//      //val fileName: String = "input.txt"
//    val pathToDocs = if (lemmatized == true) "./src/main/resources/lemmatized" else "./src/main/resources/non-lemmatized"
//    val pathOfIndex = if (lemmatized == true) "./src/main/resources/lemmIndDir" else "./src/main/resources/IndDir"
//
//    val indexFile = new File(pathOfIndex)
//    if (!indexFile.exists())
//      indexFile.mkdirs()
//      println("********Welcome to the Project!")
//    val query13a: List[String] = List("Joker", "winner")//, "order")
//    val objQueryEngine: QueryEngine = new QueryEngine(pathToDocs, pathOfIndex, lemmatized)
////    println("Question 1a")
//    val ans1: ListBuffer[ResultClass] = objQueryEngine.runQ1(query13a)
////    println("\n")
//
//
////    } catch {
////      case ex: Exception => println("Error")
////    }
//  }
//
//}
//
//class QueryEngine(pathToDocs: String, pathToIndex: String, lemmatized: Boolean) {
//
//
//
//  val analyzer = if (lemmatized == true) new WhitespaceAnalyzer else new StandardAnalyzer
//  val config = new IndexWriterConfig(analyzer)
//  val index = FSDirectory.open(Paths.get(pathToIndex))
//  val writer = new IndexWriter(index, config)
//  var indexExists = false
//
//
//
//
//  val files = getListOfFiles(pathToDocs)
//
//  //for (file <- files) println("file names: " + file.toString)
//
////  for (fileName <- files) {
////    val lines = Source.fromFile(fileName).getLines()
////    for (line <- lines) println(line)
////  }
//
//  def buildIndex(): Unit = {
//
//    //val source = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream(input_file))
//    //val lines = source.getLines().toList
////    // build one doc in your Lucene index from each line in the input file
//
//    for (fileName <- files) {
//      //val source = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream(fileName.toString))
//      //val lines = source.getLines().toList
//      val lines = Source.fromFile(fileName).getLines()
//      for (line <- lines) {
//        val splitLine = line.split(" ")
//        //println("head " + splitLine.head)
//        val docId = splitLine.head
//        val text = splitLine.tail.mkString(" ")
//        addDoc(writer, docId, text)
//      } //println("line " + line)
//
////      for(line <- lines if line.nonEmpty) {
////        println("LINE " + line)
////        val docId = line.split("\t").head
////        println("-->" + docId)
////        val text = line.split("\t").tail.mkString(" ")
////        println("text" + text)
////        addDoc(writer, docId, text)
////      }
//      //fileName.close()
//    }
//    println("building something")
//    writer.close()
//     //.close()
//    indexExists = true
//  }
//
//  def getListOfFiles(dir: String): List[File] = {
//    val d = new File(dir)
//    if (d.exists && d.isDirectory) {
//      d.listFiles.filter(_.isFile).toList
//    } else {
//      List[File]()
//    }
//  }
//  @throws[IOException]
//  private def addDoc(w: IndexWriter, docid: String, text: String): Unit = {
//    val doc = new Document()
//    doc.add(new StringField("docid", docid, Field.Store.YES))
//    doc.add(new TextField("text", text, Field.Store.YES))
//    w.addDocument(doc)
//    //println(w.numDocs + " document(s) indexed.")
//  }
//
//  def runQ1(query: List[String]): ListBuffer[ResultClass] = {
//    if(! indexExists) buildIndex()
//
//    val query_to_parse = query.mkString(" ")
//    val q = new QueryParser("text", analyzer).parse(query_to_parse)
//    val doc_score_list = getDocScoreList(q)
//    return doc_score_list
//
//  }
//
//  def getDocScoreList(q: Query): ListBuffer[ResultClass] = {
//    var doc_score_list = new ListBuffer[ResultClass]()
//    val hitsPerPage = 10
//    val reader = DirectoryReader.open(index)
//    val searcher = new IndexSearcher(reader)
//    val docs = searcher.search(q, hitsPerPage)
//    val hits = docs.scoreDocs
//    for (hit <- hits) {
//      //println ("-->", hit.doc)
//      val docId = hit.doc
//      val d = searcher.doc(docId)
//      //println(d.get("docid"))
//      val objResultClass: ResultClass = new ResultClass()
//      objResultClass.DocName = d
//      objResultClass.doc_score = hit.score
//      doc_score_list += (objResultClass)
//
//    }
//    displayResults(hits, searcher)
//    doc_score_list
//
//  }
//
//
//  def getDocScoreListDiffSimilarity(q: Query): ListBuffer[ResultClass] = {
//    var doc_score_list = new ListBuffer[ResultClass]()
//    val hitsPerPage = 10
//    val reader = DirectoryReader.open(index)
//    val similarity = new ClassicSimilarity
//    val searcher = new IndexSearcher(reader)
//    searcher.setSimilarity(similarity)
//    val docs = searcher.search(q, hitsPerPage)
//    val hits = docs.scoreDocs
//    for (hit <- hits) {
//      //println ("-->", hit.doc)
//      val docId = hit.doc
//      val d = searcher.doc(docId)
//      //println(d.get("docid"))
//      val objResultClass: ResultClass = new ResultClass()
//      objResultClass.DocName = d
//      objResultClass.doc_score = hit.score
//      doc_score_list += (objResultClass)
//
//    }
//    displayResults(hits, searcher)
//    doc_score_list
//
//  }
//
//  def runQ13a(query: List[String]): ListBuffer[ResultClass] = {
//    if(! indexExists) buildIndex()
//    val query_to_parse = "'" + query(0) + "'" + " AND " + "'"+query(1) + "'"
//    val q = new QueryParser("text", analyzer).parse(query_to_parse)
//    val doc_score_list = getDocScoreList(q)
//    return doc_score_list
//  }
//
//  def runQ13b(query: List[String]): ListBuffer[ResultClass] = {
//    if(! indexExists) buildIndex()
//    val query_to_parse = "'" + query(0) + "'" + " NOT " + "'"+query(1) + "'"
//    //println("query with NOT: " + query_to_parse)
//    val q = new QueryParser("text", analyzer).parse(query_to_parse)
//    val doc_score_list = getDocScoreList(q)
//    return doc_score_list
//  }
//
//  def runQ13c(query: List[String]): ListBuffer[ResultClass] = {
//    if(! indexExists) buildIndex()
//    val query_to_parse = '"' + query(0) + " AND " +query(1) + '"' + "~1"
//    //println("query with proximity: " + query_to_parse)
//    val q = new QueryParser("text", analyzer).parse(query_to_parse)
//    val doc_score_list = getDocScoreList(q)
//    return doc_score_list
//  }
//
//  def runQ14(query: List[String]): ListBuffer[ResultClass] = {
//    if(! indexExists) buildIndex()
//    val query_to_parse = "'" + query.mkString(" ")
//    val q = new QueryParser("text", analyzer).parse(query_to_parse)
//    val doc_score_list = getDocScoreListDiffSimilarity(q)
//    return doc_score_list
//  }
//
//
//  private def returnDummyResults(): ListBuffer[ResultClass] = {
//    var doc_score_list = new ListBuffer[ResultClass]()
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
//
//
//  def displayResults(hits: Array[ScoreDoc], searcher: IndexSearcher): Unit = {
//    System.out.println("Found " + hits.length + " hits.")
//    var i = 0
//    while ( {
//      i < hits.length
//    }) {
//      val docId = hits(i).doc
//      val d = searcher.doc(docId)
//      System.out.println("Hit: " + (i + 1) + ". " + d.get("docid") + " Score: " + hits(i).score)
//      i += 1; i
//    }
//  }
//
//
//
//}
