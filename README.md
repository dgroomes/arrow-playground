# arrow-playground

📚 Learning and exploring Apache Arrow.

> Apache Arrow is a cross-language development platform for in-memory analytics.
> 
> -- <cite>[Apache Arrow](https://arrow.apache.org/)</cite>


## Overview

This project is a playground for me to learn and explore Apache Arrow with executable code and my own verbose in-line
comments and notes.


## Standalone sub-projects

This repository illustrates different concepts, patterns and examples via standalone sub-projects. Each sub-project is
completely independent of the others and do not depend on the root project. This _standalone sub-project constraint_
forces the sub-projects to be complete and maximizes the reader's chances of successfully running, understanding, and
re-using the code.

The sub-projects include:


### `basic/`

A runnable "hello world" program featuring Apache Arrow.

See the README in [basic/](basic/).


### `sort-and-search/`

Sorting and searching vectors with Apache Arrow.

See the README in [sort-and-search/](sort-and-search/).


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Model ZIP code data in a small hardcoded vector. I like using ZIP code data to explore data products, like I do in
  [my other projects](https://github.com/dgroomes/cypher-playground#overview)
* [x] DONE Split into subprojects
* [x] DONE Incorporate a full copy of ZIP code data. 
* [ ] (stretch goal) Model cyclic graphs in the data using the ["state adjacencies" of my cypher-playground](https://github.com/dgroomes/cypher-playground/blob/dc836b1ac934175394ece264c443bfae47465cd6/postgres-init/2-init-states-data.sql#L1)
  and do a query by something like "find states adjacent to states that have at least a ZIP code with a population of 1,000,000"
  (or a more illustrative query if you can think of one)
* [ ] (stretch goal) Create a generic graph API plus a (overtly simple) query execution engine. The graph API only
  supports schema-ful graphs (does this matter?). The query execution engine should prune the vector lists (i can't find
  words for this right now). 


## Reference

* [Apache Arrow official website](https://arrow.apache.org/)
