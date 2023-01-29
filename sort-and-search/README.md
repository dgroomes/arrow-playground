# sort-and-search

NOT YET IMPLEMENTED

Sorting and searching vectors with Apache Arrow.


## Instructions

Follow these instructions to build and run the example program.

1. Use Java 17
2. Build and run the example program
    * ```shell
      ./gradlew run
      ```
    * The program output will look something like this:
    * ```text
      TODO
      ```

## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [ ] Sort the vector by population and query (binary search) for ZIP codes with a population in a given range.
* [ ] Search the data (binary search)
* [ ] It would be really cool to see the memory usage when the data is in-memory in the Java objects, then after it's been
  transferred into the Arrow vectors. I'll have to add some manual garbage collection calls and then maybe include some
  screenshots of visualvm. Is there a better way to do it? I'm not an expert. The data is only 3MB on disk so it might
  be to multiply into 100 parallel universes so we get closer to 300MB. We need to overshadow the normal Java memory
  usage which is very roughly 50MB??
* [ ] Do something with a dictionary. Can I use a dictionary, keyed on the state codes?
