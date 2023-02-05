# sort-and-search

Sorting and searching vectors with Apache Arrow.


## Overview

This is a runnable example project that showcases how to sort and search data in the Java implementation of Apache
Arrow. In general, this is a "getting started" project for working with and manipulating data in the Arrow format for
Java programmers.


## Instructions

Follow these instructions to build and run the example program.

1. Use Java 19
2. Build and run the example program
    * ```shell
      ./gradlew run
      ```
    * The program output will look something like this:
    * ```text
      [main] INFO dgroomes.sortandsearch.Runner - Reading ZIP code data from the local file ...
      [main] INFO dgroomes.sortandsearch.Runner - Read 29,353 ZIP codes from the local file and into Java objects.
      ... omitted ...
      [main] INFO dgroomes.sortandsearch.Runner - Loaded 29,353 ZIP codes into Apache Arrow vectors (arrays)
      [main] INFO dgroomes.sortandsearch.Runner - The highest population ZIP code is 60623 (CHICAGO, IL) with a population of 112,047.
      [main] INFO dgroomes.sortandsearch.Runner - California ZIP code entries are indexed in the range 1,610-3,125 in the state code index.
      [main] INFO dgroomes.sortandsearch.Runner - The population of California is 41,616,474.
      [main] INFO dgroomes.sortandsearch.Runner - Minnesota ZIP code entries are indexed in the range 12,646-13,527 in the state code index.
      [main] INFO dgroomes.sortandsearch.Runner - The population of Minnesota is 14,721,903.
      [main] INFO dgroomes.sortandsearch.Runner - Wyoming ZIP code entries are indexed in the range 29,213-29,352 in the state code index.
      [main] INFO dgroomes.sortandsearch.Runner - The population of Wyoming is 3,573,430.
      ```
3. Run the tests
    * ```shell
      ./gradlew test
      ``` 


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Sort the vector (well, index-sort it) by population
* [x] DONE Search the data (binary search)
* [ ] It would be really cool to see the memory usage when the data is in-memory in the Java objects, then after it's been
  transferred into the Arrow vectors. I'll have to add some manual garbage collection calls and then maybe include some
  screenshots of visualvm. Is there a better way to do it? I'm not an expert. The data is only 3MB on disk so it might
  be to multiply into 100 parallel universes so we get closer to 300MB. We need to overshadow the normal Java memory
  usage which is very roughly 50MB??
* [ ] Do something with a dictionary. Can I use a dictionary, keyed on the state codes?
* [ ] Delete the city names from the example. It's a bit crowded. In my next subproject I'll add them back and model them
  in Apache Arrow's table abstractions. That's a better fit.
* [ ] Remove most of the fancy type-based search algorithm code to a different project. I got carried away (and quite lost)
  while implementing these algorithms and I'm happy with the result. But this codebase is not appropriate to use Java
  language preview features.
