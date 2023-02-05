# graph

NOT YET IMPLEMENTED

An ambitious application of Apache Arrow to model data as a graph and implement search algorithms with some optimization.


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


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [ ] Model the data in Apache Arrow's table abstractions. Try to use the Vector  


## Reference

* [Apache Arrow: *Tabular Data*](https://arrow.apache.org/docs/java/vector_schema_root.html)
  * The Arrow Java implementation has `VectorSchemaRoot` for modelling table-like data and that's pretty good. It has an
    experimental `Table` type which seems like a better fit but I prefer to stick with stable types.
