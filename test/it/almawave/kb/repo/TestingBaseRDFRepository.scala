package it.almawave.kb.repo

import java.io.FileInputStream
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert
import java.io.StringReader
import java.io.File
import org.junit.Assume

abstract class TestingBaseRDFRepository {

  val base_uri = "http://local/graph/"

  val doc_example = """
    @prefix foaf: <http://xmlns.com/foaf/0.1/> .
    @prefix ex: <http://example.org/> .
    ex:john a foaf:Person ;
      foaf:name "John" ;
      foaf:age 42 ;
      foaf:mbox "john@example.org" 
      .
  """

  val vf = SimpleValueFactory.getInstance
  val ctxs = List(vf.createIRI("http://localhost"), vf.createIRI("http://graph"))

  val docWithContexts = Rio.parse(new StringReader(doc_example), base_uri, RDFFormat.TURTLE, ctxs: _*)
  val docNoContexts = Rio.parse(new StringReader(doc_example), base_uri, RDFFormat.TURTLE)

  val dir_base = new File("dist/data/ontologies/").getAbsoluteFile

  // ------------

  // TODO: find a way for executing both
  val mock: RDFRepositoryBase = RDFRepository.memory()
  //  var mock = RDFRepository.virtuoso()

  @Before()
  def before() {

    val up = mock.check()
    println("ASSUME? " + up)

    Assume.assumeTrue(false)
    mock.start()

    mock.store.clear()
  }

  @After()
  def after() {
    mock.stop()
  }

  @Test
  def size_when_empty() {

    mock.store.clear()
    val _size = mock.store.size()
    Assert.assertEquals(0, _size)

  }

  @Test
  def size_when_docWithContexts_added_with_contexts() {

    val size_before = mock.store.size()
    mock.store.add(docWithContexts, ctxs: _*)
    val size_after = mock.store.size(ctxs: _*)
    Assert.assertEquals(docWithContexts.size(), size_after - size_before)

  }

  @Test
  def size_when_docWithNoContext_added_with_contexts() {

    val size_before = mock.store.size()
    mock.store.add(docNoContexts, ctxs: _*)
    val size_after = mock.store.size(ctxs: _*)
    Assert.assertEquals(docNoContexts.size() * ctxs.size, size_after - size_before)

  }

  @Test
  def size_when_docWithNoContexts_added_no_contexts() {

    val size_before = mock.store.size()
    mock.store.add(docNoContexts)
    val size_after = mock.store.size()
    Assert.assertEquals(docNoContexts.size(), size_after - size_before)

  }

  @Test
  def size_when_docWithContexts_added_no_contexts() {

    val size_before = mock.store.size()
    mock.store.add(docWithContexts)
    val size_after = mock.store.size(ctxs: _*)
    Assert.assertEquals(docWithContexts.size(), size_after - size_before)

  }

  @Test
  def contexts_list() {

    val expected_list = Array("http://graph", "http://localhost")

    mock.store.add(docWithContexts, ctxs: _*)

    val contexts_list = mock.store.contexts()

    Assert.assertEquals(expected_list.size, contexts_list.size)

    contexts_list.foreach { ctx =>
      Assert.assertTrue(expected_list.contains(ctx))
    }

  }

  @Test
  def sparql_query() {

    mock.store.add(docNoContexts, ctxs: _*)

    println(s"added ${docNoContexts.size()} triples from document\n${doc_example}\nto contexts [ ${ctxs.mkString(" | ")} ]\n")

    // TODO: externalize
    val query = s"""
      SELECT * 
      FROM NAMED <${ctxs(0)}>
      FROM NAMED <${ctxs(1)}>
      WHERE {
        GRAPH ?g {
          ?s ?p ?o
        }
      }
    """

    println(s"SPARQL> executing query:\n${query}")
    val results = mock.sparql.query(query)
    // TODO: extends test on triples content
    val size = results.size
    println(s"SPARQL> ${size} TOTAL SPARQL results")

    Assert.assertEquals(mock.store.size(ctxs: _*), mock.sparql.query(query).size)

  }

  @Test
  def test_import() {

    mock.helper.importFrom(dir_base.toString())
    val foaf_context = SimpleValueFactory.getInstance.createIRI("http://xmlns.com/foaf/0.1/")
    val foaf_size = mock.store.size(foaf_context)
    Assert.assertEquals(631, foaf_size)

  }

  @Test
  def remove_from_context() {

    mock.store.add(docNoContexts, ctxs(0))
    mock.store.remove(docNoContexts, ctxs(0))
    Assert.assertEquals(0, mock.store.size(ctxs(0)))

    mock.store.clear()
    mock.store.add(docNoContexts, ctxs(0))
    mock.store.clear(ctxs(0))
    Assert.assertEquals(0, mock.store.size(ctxs(0)))
  }

}
