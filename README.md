
lod_manager
===============

SEE: [teamdigitale/daf](https://github.com/teamdigitale/daf) 

## instructions

1. local publish of dependencies (`kb-core`)

At the moment the wrapper for the triplestore(s) is published as a jar dependency, using the `lib` folder:

```
[lod_manager]
├───/lib
│   ├───eclipse-rdf4j-2.2.1-onejar.jar
│   └───kb-core-0.0.1.jar
```
Copy the library under `/lib` before compilation (NOTE: the libraries should be managed too by sbt).

2. compile / package

```
$ sbt clean package
```

3. run

```
$ sbt run 
```


* * * 

## TODO 

- [ ] publish `kb-core` on github / bitbucket or as sub-module
- [ ] add `kb-core` dependency on sbt 
- [ ] add `RDF4J` dependencies on sbt (if needed)
- [ ] proper configuration of JUnit for sbt
- [ ] more test coverage for simple example HTTP requests
- [ ] RDF4J connection pool, with implicit connection as curry method


### RDF4J connections (IDEA)

An idea could be rewriting methods as curried, in order to add implicit connection, such as:

```
def action(params...)(implicit conn:RepositoryConnection){
	...
}
```

this way we could avoid open/close a connection every time, instead maintaining a connection pool

