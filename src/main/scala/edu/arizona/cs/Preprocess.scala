
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

import org.apache.commons.lang3.StringUtils.stripAccents
import org.clulab.processors.clu.CluProcessor
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
  }

}

class Preprocess() {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def train(collection: String): Unit = {

    val files = getListOfFiles("./src/main/resources/wiki")
    val proc = new CluProcessor()

    val data = for {
      file <- files
      fileText = "\n\n" + Source.fromFile(file).getLines().mkString("\n\n").replaceAll("http.*? |\\[tpl\\]|\\[/tpl\\]|\\|.*?\\}|\\[ tpl \\]|\\[\\[File.*?\\]\\]|\\[\\[Image.*?\\]\\]|^\n|\\[\\[Media.*?\\]\\]|url.*? ", "")

    }  yield (file, fileText)

    //home/masha/alexeeva_project/src/main/resources/lemmatized

    for (i <- data) {
      val currentFileName = i._1.toString.split("/")//.slice(0, 5).mkString(" ")
      val newFileName = "./src/main/resources/lemmatized/" + currentFileName.slice(5, currentFileName.length).mkString("/")
      val newFileNameNonLemmatized = "./src/main/resources/non-lemmatized/" + currentFileName.slice(5, currentFileName.length).mkString("/")
      //println(newFileName)
      val fw = new FileWriter(newFileName, true)
      val fwNonLem = new FileWriter(newFileNameNonLemmatized, true)
      val allData = i._2.split("\n\n\\[\\[(?!File|Image|Media)") //split on [[ to separate entries
      for (line <- allData) {
        val splitLine = line.split("\\]\\]\n\n") //split on ]] to separate title from body
        fw.write(splitLine.head.replaceAll("\n*", "") + "\t")
        fwNonLem.write(splitLine.head.replaceAll("\n*", "") + "\t")
        val textToLemmatize = splitLine.tail.mkString(" ").replaceAll("  +|\n", "")//.split("\\. ").filter(m => m.length > 0) //break body up into sentences
        fwNonLem.write(stripAccents(removeDiacritics(textToLemmatize)))

        val doc = proc.mkDocument(stripAccents(removeDiacritics(textToLemmatize)).replaceAll("[^\\x1F-\\x7F]", ""))//.mkString(". "))   //
        try {
          proc.lemmatize(doc)
        }

        for {
          sent <- doc.sentences
          lemmas <- sent.lemmas

        } fw.write(lemmas.mkString(" "))
                fw.write("\n")
                fwNonLem.write("\n")

      }
        fwNonLem.close()
       fw.close()

    }

    println("tah-dah")
  }

  def removeDiacritics(str: String): String = {
    import Character.UnicodeBlock._
    val diacriticBlocks = List(COMBINING_DIACRITICAL_MARKS,
      //COMBINING_DIACRITICAL_MARKS_EXTENDED, //For some reason not in Java.
      COMBINING_DIACRITICAL_MARKS_SUPPLEMENT,
      COMBINING_MARKS_FOR_SYMBOLS,
      COMBINING_HALF_MARKS)

    import java.text.Normalizer
    val normald = Normalizer.normalize(str, Normalizer.Form.NFD)
    normald.filterNot(ch => diacriticBlocks.contains(Character.UnicodeBlock.of(ch)))
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