# sort-and-search

Sorting and searching vectors with Apache Arrow.


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
      [main] INFO dgroomes.sortandsearch.Runner - Read 29353 ZIP codes from the local file and into Java objects.
      [main] INFO org.apache.arrow.memory.BaseAllocator - Debug mode disabled.
      [main] INFO org.apache.arrow.memory.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
      [main] INFO org.apache.arrow.memory.CheckAllocator - Using DefaultAllocationManager at memory-netty/11.0.0/4e427a070f21efaffe6009faf6d97b260dbec36b/arrow-memory-netty-11.0.0.jar!/org/apache/arrow/memory/DefaultAllocationManagerFactory.class
      [main] INFO dgroomes.sortandsearch.Runner - Loaded 29353 ZIP codes into Apache Arrow vectors.
      [main] INFO dgroomes.sortandsearch.Runner - The lowest population ZIP code is 55450 (MINNEAPOLIS, MN) with a population of 0.
      [main] INFO dgroomes.sortandsearch.Runner - The highest population ZIP code is 60623 (CHICAGO, IL) with a population of 112047.
      ```
3. Run the tests
    * ```shell
      ./gradlew test
      ``` 


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Sort the vector (well, index-sort it) by population
* [ ] IN PROGRESS Search the data (binary search)
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
