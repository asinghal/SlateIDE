package net.slate.editor.tools

import scala.actors.Actor._

object ScalaTags {

  import scala.io.Source
  import util.matching.Regex
  import net.slate.util.FileUtils

  private lazy val regex = Array("def", "class", "object", "trait").map { "\\b" + _ + "\\b[\\s]*" }.foldLeft("") { (x, y) => x + "|" + y }.substring(1)
  private lazy val overall: Regex = ("(" + regex + ")([^\\s\\(\\=]*)").r

  private var running = false
  
  def tagAll(project: String) = {
    FileUtils.findAllFiles(project, ".scala").foreach(tag)
  }

  def tag(path: String) {
    val source = Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val indexer = new TagsIndexer(net.slate.ExecutionContext.currentProjectName(path))

    var pos = 0

    val a = actor {
      if (!running) {
        running = true
        val matcher = overall.pattern.matcher(lines)
        while (matcher.find()) {
          val cType = matcher.group(1).trim
          val tag = matcher.group(2).trim
          if (tag != "" && !indexer.exists(cType, tag, path)) {
            indexer.addTag(cType, tag, path)
          }
        }
        running = false
      }
    }
  }
  
  def lookup(tag: String, path: String) = {
    val indexer = new TagsIndexer(net.slate.ExecutionContext.currentProjectName(path))
    indexer.find(tag)
  }
}

class TagsIndexer(project: String) {
  import java.io.File
  import java.io.IOException
  import org.apache.lucene.analysis.standard.StandardAnalyzer
  import org.apache.lucene.document._
  import org.apache.lucene.index._
  import org.apache.lucene.queryParser._
  import org.apache.lucene.search._
  import org.apache.lucene.store._
  import org.apache.lucene.util.Version

  val INDEX_DIR = new File(project + File.separator + ".slate_tags" + File.separator + "index")
  var ready = false
  val analyzer = new StandardAnalyzer(Version.LUCENE_33)
  val index_ = FSDirectory.open(INDEX_DIR)

  def addTag(cType: String, tag: String, path: String) = {
    val config = new IndexWriterConfig(Version.LUCENE_33,
      analyzer)

    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)

    val w = new IndexWriter(index_, config)

    addDoc(w, cType, tag, path)

    ready = true
    w.close()
  }

  def find(tag: String) = {
    val keyword = tag

    val q = new QueryParser(Version.LUCENE_33, "tag", analyzer)
      .parse(keyword)
    search(q, tag)
  }

  def exists(cType: String, tag: String, path: String) = {
    val keyword = tag
    val query = "cType:\"" + cType + "\" AND path:\"" + path + "\" AND \"" + keyword + "\""

    val q = new QueryParser(Version.LUCENE_33, "tag", analyzer)
      .parse(keyword)
    search(q, tag).length != 0
  }

  private def search(q: Query, tag: String) = {
    try {
      val hitsPerPage = 100
      val searcher = new IndexSearcher(index_, true)
      val collector = TopScoreDocCollector.create(
        hitsPerPage, true)
      searcher.search(q, collector)
      val hits = collector.topDocs().scoreDocs

      val results = new Array[AnyRef](hits.length)

      var skipped = 0

      // prepare results
      for (i <- 0 to (hits.length - 1)) {
        val docId = hits(i).doc
        val d = searcher.doc(docId)

        if (d.get("tag") == tag) {
          results(i - skipped) = d.get("tag")
        } else {
          skipped += 1
        }
      }

      // searcher can only be closed when there
      // is no need to access the documents any more.
      searcher.close()

      results
    } catch {
      case e: Throwable => Array[AnyRef]()
    }
  }

  private def addDoc(w: IndexWriter, cType: String, tag: String, path: String) = {
    val doc = new Document()
    doc.add(new Field("cType", cType, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("tag", tag, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("path", path, Field.Store.YES, Field.Index.ANALYZED))
    w.addDocument(doc)
  }
}