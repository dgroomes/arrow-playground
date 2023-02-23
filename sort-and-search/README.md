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
      22:54:38 [main] INFO dgroomes.sortandsearch.Runner - Reading ZIP code data from the local file ...
      22:54:38 [main] INFO dgroomes.sortandsearch.Runner - Read 29,353 ZIP codes from the local file and into Java objects.
      22:54:40 [main] INFO dgroomes.sortandsearch.Runner - Multiplied the ZIP code data by 100 to get 2,964,653 parallel universe ZIP codes.
      22:54:40 [main] INFO dgroomes.sortandsearch.Runner - Loaded 2,964,653 ZIP codes into Apache Arrow vectors (arrays)
      22:54:41 [main] INFO dgroomes.sortandsearch.Runner - Sorting the population data ...
      22:54:43 [main] INFO dgroomes.sortandsearch.Runner - Done sorting the population data.
      22:54:43 [main] INFO dgroomes.sortandsearch.Runner - The highest population ZIP code is 10060623 (100CHICAGO, 100IL) with a population of 11,204,700.
      22:54:43 [main] INFO dgroomes.sortandsearch.Runner - Sorting the state codes ...
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - Done sorting the state codes.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - California ZIP code entries are indexed in the range 2,936,910-2,938,425 in the state code index.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - The population of California is 29,754,890.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - Minnesota ZIP code entries are indexed in the range 2,947,946-2,948,827 in the state code index.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - The population of Minnesota is 4,372,982.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - Wyoming ZIP code entries are indexed in the range 2,964,513-2,964,652 in the state code index.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - The population of Wyoming is 453,528.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - Found 21 ZIP codes in Minneapolis.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - Found 21 ZIP codes in Minneapolis.
      22:55:08 [main] INFO dgroomes.sortandsearch.Runner - Found 1,551,163 ZIP codes with population greater than 100,000.
      22:55:09 [main] INFO dgroomes.sortandsearch.Runner - Found 1,551,163 ZIP codes with population greater than 100,000.
      22:55:10 [main] INFO dgroomes.sortandsearch.Util - Benchmark 'POJO city scan' evaluated 100 times, produced a result hash of '04a623a4', and took PT1.041348S
      22:55:15 [main] INFO dgroomes.sortandsearch.Util - Benchmark 'Arrow VarCharVector city scan' evaluated 100 times, produced a result hash of '04a623a4', and took PT5.160639S
      22:55:16 [main] INFO dgroomes.sortandsearch.Util - Benchmark 'POJO population scan' evaluated 100 times, produced a result hash of '15410aeb', and took PT1.727438S
      22:55:18 [main] INFO dgroomes.sortandsearch.Util - Benchmark 'Arrow IntVector population scan' evaluated 100 times, produced a result hash of '15410aeb', and took PT1.143581S
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
* [x] DONE Multiply the data into "parallel universes" so we can better see the effect of Arrow (spoiler: you pay an overhead
  for Arrow from Java so I see slower performance than just in-memory Java objects. My observations are naive though.
  This is only for my own discovery and learning.)
* [x] DONE Benchmark "full data set scan" performance. Compare scanning of the POJO data to Arrow data. Results: POJO is much
  faster for strings (a JVM optimization? Nice) and Arrow is slightly faster for integers.
* [x] DONE (Done but not with much intelligence) Do something with a dictionary. Can I use a dictionary, keyed on the state codes?
   * Reference: <https://arrow.apache.org/docs/java/vector.html#dictionary-encoding>
   * > Dictionary encoding is a form of compression where values of one type are replaced by values of a smaller type:
     > an array of ints replacing an array of strings is a common example. The mapping between the original values and the replacements is held in a ‘dictionary’.
* [x] DONE (done; that's a nice effect. "Read-only-one-shot-data + dictionaries + vectors" means our dictionaries can be sorted)
  I think the dictionary vector itself can be sorted. That should speed up the sorting regression introduced in
  this commit. 
* [x] OBSOLETE (Update: maybe not, because I used them to show the speed of varchar scanning) Delete the city names from the example. It's a bit crowded. In my next subproject I'll add them back and model them
  in Apache Arrow's table abstractions. That's a better fit.
* [ ] IN PROGRESS Remove most of the fancy type-based search algorithm code to a different project. I got carried away (and quite lost)
  while implementing these algorithms and I'm happy with the result. But this codebase is not appropriate to use Java
  language preview features.
   * DONE use Arrow's built-in search algorithms
   * Delete the custom algorithms and move back to Java 17 and without preview features.
