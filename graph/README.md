# graph

NOT YET IMPLEMENTED

An ambitious application of Apache Arrow to model data as an object graph and implement search algorithms with some optimization.


## Overview

I want to model and search over a a cyclic structured set of data (think "car models" made by "car makers" influenced by
the design and success of other car models and car makers). I also really want to do this in-memory. Apache Arrow is the
natural choice for modeling in-memory data in 2023 and it has strong reference implementations for Java, which is my
go-to language. The Java Arrow implementation also offers table-like data modeling which goes a long way to making the
developer experience pretty good for modeling the aforementioned "car makers" and "car models" data. But the convenience
stops there. The Java Arrow implementation does not offer entity-to-entity relationships and it offers only very basic
implementations of algorithms: specifically binary search and sorting. I want an actual basic query engine.


## Instructions

Follow these instructions to build and run the example program:

1. Use Java 17
2. Build and run the program:
   * ```shell
     ./gradlew run
     ```


## Notes

I'm using the word "graph" in the same way that so-called graph databases like Neo4J use the word "graph" but maybe I
mean ["object databases"](https://en.wikipedia.org/wiki/Object_database), which are somewhat obscure but actually there
is one modern one called [Realm](https://en.wikipedia.org/wiki/Realm_(database)) which is popular on mobile. Also I've
been inspired by [Kuzu](https://github.com/kuzudb/kuzu) which is a property graph database but it has schemas (which I
like) but doesn't that make it a traditional object database? I tried build Kuzu from source but had issues (it's
extremely new; so that's ok) so maybe I'll try Realm (although it's also C++ so I'm scared). 


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Model the data in Apache Arrow's table abstractions. Use `Table` even knowing it is experimental.
* [ ] IN PROGRESS Model cyclic graphs in the data using the ["state adjacencies" of my cypher-playground](https://github.com/dgroomes/cypher-playground/blob/dc836b1ac934175394ece264c443bfae47465cd6/postgres-init/2-init-states-data.sql#L1)
  and do a query by something like "find states adjacent to states that have at least a ZIP code with a population of 1,000,000"
  (or a more illustrative query if you can think of one)
  * DONE Define the adjacencies data.
  * IN PROGRESS Incorporate it into the Arrow data model.
* [ ] Create a generic graph API plus a (overtly simple) query execution engine. The graph API only
  supports schema-ful graphs (does this matter?). The query execution engine should prune the vector lists (i can't find
  words for this right now).
* [ ] Consider loading data using Arrow's own (Java) APIs? It dosn't really matter, it's very easy to just load the data
  with some hand-coded Jackson/file calls, but I generally want to learn any/all aspects of Arrow, unlimited.
* [ ] Consider renaming the project to something like "object-query-engine" or something more specific/descriptive.


## Reference

* [Apache Arrow: *Tabular Data*](https://arrow.apache.org/docs/java/vector_schema_root.html)
  * The Arrow Java implementation has `VectorSchemaRoot` for modelling table-like data and that's pretty good. It has an
    experimental `Table` type which seems like a better fit but I prefer to stick with stable types. Although
    `VectorSchemaRoot` is pretty primitive, it's almost like it exists for data loading but not for data querying.
