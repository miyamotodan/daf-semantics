
semantic_repository
====================

The Semantic Repository is a component designed to provide basic functionalities for managing ontologies/vocabularies and eventually data on an underlying triplestore, using a standard abstract interface, based on the well-know [RDF4J](http://rdf4j.org/) abstraction.

This microservice reiles on the underlying `kbaselib` library, for operations on generic RDF data, and ontology/vocabulary management.
A similar microservice, designed to support ontology catalog navigation, has been created with the temporary name `kataLOD` in a pecific repository.

The default triplestore is currently in-memory, but [Virtuoso](http://vos.openlinksw.com/owiki/wiki/VOS) was tested too.
**NOTE**: The support for [Blazegraph](https://www.blazegraph.com) is planned but not already tested, waiting for the [full support](https://github.com/blazegraph/database/issues/40).

![semantic_repository component inside the semantic_manager architecture](./docs/daf-semantics.png)
**TODO**: update the diagram to the current architecture and modules


**NOTE**: this is an alpha version, still work-in-progress.

* * *

## HTTP API

+ entrypoint for the play application (swagger-ui)
[http://localhost:8777](http://localhost:8777)

+ swagger definition
[http://localhost:8777/spec/semantic_repository.yaml](http://localhost:8777/spec/semantic_repository.yaml)



### adding an ontology

An ontology can be added using the endpoint provided at `/kb/v1/ontologies`, for example using a CURL command similar to the following one:

```bash
curl -X POST 'http://localhost:8777/kb/v1/ontologies' \ 
	-H 'Content-Type: multipart/form-data' -H 'Accept: application/json' \ 
	-F 'fileName=my_rdf_file.rdf' -F 'rdfDocument=@/some/path/my_rdf_file.rdf' \ 
	-F 'prefix=my_prefix' -F 'context=http://my_context/'
```

By convention at the moment each ontology will be published under an assigned context (which may coincide with its base URI), and it must have a prefix too. The idea behind this choice is to have only the last version of an ontology loaded, using a conventional prefix/context pair, and this pair must be unique.
This assumption only affects the external HTTP API (not actually the underlying engine) and may change in the future.
Vocabularies are handled as ontology, at the moment: however it is not useful to provide a prefix for a vocabulary, and we could imagine adding specific endpoints with different parameters and a similar behaviour under `/kb/v1/vocabularies/` in the next future.


### deleting an ontology, by its context

Currently an ontology can be deleted using the endpoint at `/kb/v1/ontologies/remove` providing the conventional context in which it was published.
This can be done for example with the following CURL command:

```bash
curl -X DELETE 'http://localhost:8777/kb/v1/ontologies/remove?context=http://my_context' \
	-H 'Accept: application/json'
```

We could think about removing ontologies by prefix, too.


### exploring contexts

A list of all the available contexts can be obtained by the endpoint `/kb/v1/contexts` .

For example using the following CURL command:
```bash
curl -X GET 'http://localhost:8777/kb/v1/contexts' \ 
	-H  "accept: application/json" -H  "content-type: application/json"
```

### exploring prefixes

A list of all the available prefix/namespace pair can be obtained by the endpoint `/kb/v1/prefixes` .

For example using the following CURL command:
```
curl -X GET 'http://localhost:8777/kb/v1/prefixes' \ 
	-H  "accept: application/json" -H  "content-type: application/json"
```

The namespace related to a choosen prefix can be obtained by the endpoint `/kb/v1/prefixes/lookup` .

For example using the following CURL command:
```bash
curl -X GET http://localhost:8777/kb/v1/prefixes/lookup?prefix=my_prefix \
	-H  "accept: application/json" -H  "content-type: application/json"
```

The namespace corresponds to a context, at the moment.


The namespace related to a choosen prefix can be obtained by the endpoint `/kb/v1/prefixes/reverse` .

For example using the following CURL command:
```bash
curl -X GET http://localhost:8777/kb/v1/prefixes/reverse?namespace=http://my_namespace/ \ 
	-H  "accept: application/json" -H  "content-type: application/json"
```

The namespace corresponds to a context, at the moment.

### counting triples

The total amount of triples can be obtained by the endpoint `/kb/v1/triples` .

For example using the following CURL command:
```bash
curl -X GET http://localhost:8777/kb/v1/triples -H  "accept: application/json" \ 
	-H  "content-type: application/json"
```

The total amount of triples for the prefix `{prefix}` can be obtained by the endpoint `/kb/v1/triples/{prefix}` .

For example using the following CURL command:
```bash
curl -X GET http://localhost:8777/kb/v1/{prefix} -H  "accept: application/json" \
	-H  "content-type: application/json"
```



## instructions

1. local publishing of dependencies

	1.1 virtuoso JDBC / RDF4J jar

	The dependencies for virtuoso integration are currently not yet published on the maven central, so they are linked using the convetional `lib` folder in the sbt project:

	```bash
	[semantic_repository]
	├───/lib
	│   ├───virtjdbc4_2.jar
	│   └───virt_rdf4j.jar
	```

	We could avoid this if/when the libraries will be published, or by publishing them, for example on a private nexus.

2. compile / package

	```bash
	$ sbt clean package
	```

3. run

	```bash
	$ sbt run
	```

4. (local, manual) deploy

	```bash
	$ sbt clean dist
	$ unzip -o -d  target/universal/ target/universal/semantic_repository-0.1.0.zip
	$ cd target/universal/semantic_repository-0.1.0
	$ bin/semantic-repository -Dconfig.file=./conf/application.conf
	```

	**NOTE**: if the application crashed, the pid file whould be deleted before attempting re-run

	```bash
	$ rm target/universal/semantic_repository-0.1.0/RUNNING_PID 
	```

5. release

	working draft: [0.1.0](https://github.com/seralf/semantic_repository/releases/tag/0.1.0)

6. preparing docker image with sbt (manually)

	```bash
	$ sbt docker:publishLocal 
	```

	after this command, will be generated an image including the deployed application, and published on the local docker system.
	The generated image should be used for starting a new container, exposing the ports with a command similar to the following one:

	```bash
	$ sbt docker run -d -p 8777:9000 {docker-image-id}
	```


### prerequisites: kbaselib

This project uses the library [`kbaselib`](https://bitbucket.org/awodata/kbaselib) as a dependency:
```bash
libraryDependencies += "it.almawave.linkeddata.kb" % "kbaselib" % "0.0.2"
```

The library should be available on the local maven repository, so if it is not yet installed, please install it, using a command like:

```bash
mvn install \
	-DgroupId=it.almawave.linkeddata.kb -DartifactId=kbaselib -Dversion=0.0.2 \
	-Dfile=target/kbaselib-0.0.1.jar -Dpackaging=jar2
```

*NOTE*: for compatibility reason, please ensure that the library `kbaselib/0.0.1/kbaselib-0.0.1.jar` is also available as `kbaselib_2.11.8/0.0.1/kbaselib-0.0.1.jar`.




* * *

### adapters for RDF4J collections

The object `it.almawave.kb.utils.RDF4JAdapters` contains some adapters which may be useful for working with RDF4J collections in a simpler way.

+ `StringContextAdapter` can be used for writing conversions like:
`val context:Resource = "http://graph".toIRI`
+ `StringContextListAdapter` it's the same on collections, and can be used for writing conversions like:
`val contexts:Array[Resource] = Array("http://graph").toIRIList`
+ `RepositoryResultIterator` and `TupleResultIterator` are useful for handling results from query as Scala collections, without having to write `while(...)` code.
+ `BindingSetMapAdapter` adds a `toMap` conversion method, useful for writings things like:
```scala
val bs: BindingSet = ...
val map: Map[String, Object] = bs.toMap()
```

### Try + transaction handling + error messages

It's possible to simplify the code used for interacting with the underling RDF4J Repository instance, focusing on the actual code, using the `RepositoryAction` construct like in the following example:

```scala
import it.almawave.kb.utils.Handlers._

...

implicit val logger = LoggerFactory.getLogger(this.getClass)

val repo:Repository = ...

// a method clearing all the triples!
def clear_all() {

	RepositoryAction(repo) { conn =>

	// some code using connection object, like for example:
	conn.clear()

	}(s"cannot clear all the triples for some reason!}")

}

```

**NOTE**:
+ the open/close connection actions are handled by the `RepositoryAction` itself 
+ the provided error message will be used internally both for logging on the chosen logger, and for handling Exception in the usual `Try/Failure` way.

**SEE***: [kbaselib]() library for further details


* * *

## TODO

- [x] switch to new name conventions: `semantic_*`, merge into main daf.
- [x] NOTE: consider using `git subtree` for the local fork
- [x] add `kbaselib` dependency on sbt - move core to external library
- [x] publish `kbaselib` (changing name conventions) on github / bitbucket or as sub-module
- [x] add `RDF4J` dependencies on sbt
- [x] add `virtoso` dependencies on sbt
- [x] add `virtoso` jar on sbt/lib
- [x] refactoring JUnit tests for engine part: memory
- [x] refactoring JUnit tests for engine part: virtuoso wrapper
- [ ] more test coverage for simple example HTTP requests (specs2)
- [ ] review / refactor the response from services, using more meaningful data structures

* * *

SEE: [teamdigitale/daf](https://github.com/italia/daf) 

