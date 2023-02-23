package dgroomes.sortandsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.arrow.algorithm.search.VectorRangeSearcher;
import org.apache.arrow.algorithm.search.VectorSearcher;
import org.apache.arrow.algorithm.sort.DefaultVectorComparators;
import org.apache.arrow.algorithm.sort.IndexSorter;
import org.apache.arrow.algorithm.sort.VectorValueComparator;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BaseIntVector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.dictionary.Dictionary;
import org.apache.arrow.vector.dictionary.DictionaryEncoder;
import org.apache.arrow.vector.types.pojo.DictionaryEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Please see the README for more information.
 *
 * WARNING: This is some gnarly code. I haven't been able to fully grok the Apache Arrow Java APIs yet. I prioritized
 * "learning by doing" so that's why the code is written in a mostly top-down procedural fashion and without much thought
 * in encapsulation, variable naming, and the order of operations.
 */
public class Runner implements AutoCloseable {

  public static final int PARALLEL_UNIVERSES = 100;
  private static final Logger log = LoggerFactory.getLogger(Runner.class);
  private List<Zip> zips;

  private final BufferAllocator allocator;
  private final IntVector zipCodeVector;
  private final VarCharVector cityNameVector;
  private final VarCharVector stateCodeVector;
  private final IntVector populationVector;
  private final IntVector populationSortedIndexVector;
  private final IntVector stateCodesSortedIndexVector;
  private final VarCharVector stateCodeUniqueVector;
  private BaseIntVector stateCodesEncodedVector;

  public Runner(BufferAllocator allocator,
                IntVector zipCodeVector,
                VarCharVector cityNameVector,
                VarCharVector stateCodeVector,
                IntVector populationVector,
                IntVector populationSortedIndexVector,
                IntVector stateCodesSortedIndexVector,
                VarCharVector stateCodeUniqueVector) {
    this.allocator = allocator;
    this.zipCodeVector = zipCodeVector;
    this.cityNameVector = cityNameVector;
    this.stateCodeVector = stateCodeVector;
    this.populationVector = populationVector;
    this.populationSortedIndexVector = populationSortedIndexVector;
    this.stateCodesSortedIndexVector = stateCodesSortedIndexVector;
    this.stateCodeUniqueVector = stateCodeUniqueVector;
  }

  @Override
  public void close() {
    Optional.ofNullable(stateCodesEncodedVector).ifPresent(ValueVector::close);
  }

  private record Zip(String zipCode, String cityName, String stateCode, int population) {}

  public static void main(String[] args) {

    try (BufferAllocator allocator = new RootAllocator();
         IntVector zipCodeVector = new IntVector("zip-codes", allocator);
         VarCharVector cityNameVector = new VarCharVector("city-names", allocator);

         // We're going to apply a dictionary to the state codes data.
         //
         // The 'state-codes' vector represents an unencoded version of the state codes for each ZIP entry. For example,
         // there will be many entries for the same value (like 'MN', 'CA', 'NY') because there are many ZIP codes in
         // each state. So this should be a good candidate for dictionary encoding. From this unencoded vector, we'll
         // create a "dictionary" vector that contains only the unique values. Then we'll create an encoded version of
         // the unencoded vector. Then, we should be able to drop the unencoded vector and save on memory!
         VarCharVector stateCodeVector = new VarCharVector("state-codes", allocator);
         VarCharVector stateCodeUniqueVector = new VarCharVector("state-codes-unique", allocator);

         IntVector populationVector = new IntVector("populations", allocator);
         IntVector populationSortedIndexVector = new IntVector("population-index", allocator);
         IntVector stateCodesSortedIndexVector = new IntVector("state-codes-index", allocator)) {

      try (var runner = new Runner(allocator,
              zipCodeVector,
              cityNameVector,
              stateCodeVector,
              populationVector,
              populationSortedIndexVector,
              stateCodesSortedIndexVector,
              stateCodeUniqueVector)) {

        runner.execute();
      }
    }
  }

  public void execute() {
    log.info("Reading ZIP code data from the local file ...");

    // Read the ZIP code data from the local JSON file.
    {
      JsonMapper jsonMapper = JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();

      File zipsFile = new File("zips.jsonl");
      if (!zipsFile.exists()) {
        String msg = "The 'zips.jsonl' file could not be found (%s). You need to run this program from the root of the 'sort-and-search' project.".formatted(zipsFile.getAbsolutePath());
        throw new RuntimeException(msg);
      }

      try (Stream<String> zipsJsonLines = Files.lines(zipsFile.toPath())) {
        zips = zipsJsonLines.map(zipJson -> {
          JsonNode zipNode;
          try {
            zipNode = jsonMapper.readTree(zipJson);
          } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize the JSON representing a ZIP code", e);
          }

          return new Zip(zipNode.get("_id").asText(), zipNode.get("city").asText(), zipNode.get("state").asText(), zipNode.get("pop").asInt());
        }).toList();
      } catch (IOException e) {
        throw new RuntimeException("There was an error while reading the ZIP data from the file.", e);
      }

      log.info("Read {} ZIP codes from the local file and into Java objects.", Util.formatInteger(zips.size()));
    }

    // Multiplying the data for bigger effect
    int zipValuesSize;
    {
      ArrayList<Zip> multipliedZips = new ArrayList<>(zips);

      for (int parallelUniverse = 1; parallelUniverse < PARALLEL_UNIVERSES + 1; parallelUniverse++) {
        for (Zip zip : zips) {
          String parallelZipCode = "%d%s".formatted(parallelUniverse, zip.zipCode());
          String parallelCityName = "%d%s".formatted(parallelUniverse, zip.cityName());
          String parallelStateCode = "%d%s".formatted(parallelUniverse, zip.stateCode());
          int parallelPopulation = zip.population() * parallelUniverse;

          var parallelZip = new Zip(parallelZipCode, parallelCityName, parallelStateCode, parallelPopulation);
          multipliedZips.add(parallelZip);
        }
      }

      zips = multipliedZips;
      zipValuesSize = zips.size();

      log.info("Multiplied the ZIP code data by {} to get {} parallel universe ZIP codes.", PARALLEL_UNIVERSES, Util.formatInteger(zips.size()));
    }

    // Load the in-memory ZIP and city data from Java objects into Apache Arrow's in-memory data structures.
    {
      zipCodeVector.allocateNew(zipValuesSize);
      // Notice that we don't set the size because we don't know how many bytes the city names and state codes are going
      // to take. This is the nature of using a variable-sized data type, like "var char".
      cityNameVector.allocateNew();
      stateCodeVector.allocateNew();
      populationVector.allocateNew(zipValuesSize);

      for (int i = 0; i < zipValuesSize; i++) {
        Zip zip = zips.get(i);
        zipCodeVector.set(i, Integer.parseInt(zip.zipCode));
        // The "safe" version of the set method automatically grows the vector if it's not big enough to hold the new value.
        cityNameVector.setSafe(i, zip.cityName.getBytes());
        stateCodeVector.setSafe(i, zip.stateCode.getBytes());
        populationVector.set(i, zip.population);
      }

      // Necessary boilerplate to tell Apache Arrow that we're done adding values to the vectors, and to restate the
      // number of values in each vector.
      zipCodeVector.setValueCount(zipValuesSize);
      cityNameVector.setValueCount(zipValuesSize);
      stateCodeVector.setValueCount(zipValuesSize);
      populationVector.setValueCount(zipValuesSize);

      log.info("Loaded {} ZIP codes into Apache Arrow vectors (arrays)", Util.formatInteger(zipValuesSize));
    }

    // Encode the "state codes" column of the ZIP code data.
    // The encoding has a nice compression effect, for example "AK" becomes the number 0.
    //
    // There are a few parts to this procedure. Read carefully.
    {
      // The first part is to populate a vector with the unique state codes.
      {
        List<String> uniqueSortedStateCodes = IntStream.range(0, zipValuesSize).mapToObj(stateCodeVector::get)
                .map(String::new)
                .distinct()
                .sorted()
                .toList();

        for (int i = 0; i < uniqueSortedStateCodes.size(); i++) {
          stateCodeUniqueVector.setSafe(i, uniqueSortedStateCodes.get(i).getBytes());
        }

        stateCodeUniqueVector.setValueCount(uniqueSortedStateCodes.size());
      }

      // Next we use the Apache Arrow Java types like "Dictionary" and "DictionaryEncoding". It's hard to understand
      // this intuitively so I suggest you read the official docs.
      {
        var encoding = new DictionaryEncoding(1L, true, null);
        var dictionary = new Dictionary(stateCodeUniqueVector, encoding);
        var encoder = new DictionaryEncoder(dictionary, allocator);
        stateCodesEncodedVector = (BaseIntVector) encoder.encode(stateCodeVector);
      }

      // At this point, we can free the memory used by the "stateCodeVector" because we have the encoded version and the
      // the "stateCodeUniqueVector" dictionary vector.  encoded it. Unfortunately, for this example program, I still need the original unencoded vector because we
      // do a binary search over it and my binary search algorithm doesn't support
       stateCodeVector.close();
    }

    // Sort the population data.
    {
      // We're not going to actually mutate the population vector itself but we're going to create an "index vector"
      // which is a sorted representation of the population vector. By way of example, the first element of the index
      // vector might be 123, which means that the 123th element of the population vector has the smallest population.
      log.info("Sorting the population data ...");
      IndexSorter<IntVector> populationIndexer = new IndexSorter<>();
      populationSortedIndexVector.setValueCount(zipValuesSize);
      populationIndexer.sort(populationVector, populationSortedIndexVector, new DefaultVectorComparators.IntComparator());
      log.info("Done sorting the population data.");
    }

    {
      int idx = populationSortedIndexVector.get(zipValuesSize - 1);
      int zip = zipCodeVector.get(idx);
      String cityName = new String(cityNameVector.get(idx));
      long stateCodeEncodedIdx = stateCodesEncodedVector.getValueAsLong(idx);
      String stateCode = new String(stateCodeUniqueVector.get((int) stateCodeEncodedIdx));
      int population = populationVector.get(idx);
      log.info("The highest population ZIP code is {} ({}, {}) with a population of {}.", zip, cityName, stateCode, Util.formatInteger(population));
    }

    // Sort the "state codes" column. Because we encoded the original state codes data, we have to sort the encoded
    // column. How do we do that?
    {
      log.info("Sorting the state codes ...");
      IndexSorter<ValueVector> stateCodeIndexer = new IndexSorter<>();
      stateCodesSortedIndexVector.setValueCount(zipValuesSize);
      stateCodeIndexer.sort(stateCodesEncodedVector, stateCodesSortedIndexVector, new VectorValueComparator<>() {

        @Override
        public int compareNotNull(int index1, int index2) {
          var a = stateCodesEncodedVector.getValueAsLong(index1);
          var b = stateCodesEncodedVector.getValueAsLong(index2);

          return Long.compare(a, b);
        }

        @Override
        public VectorValueComparator<ValueVector> createNew() {
          return null;
        }
      });
      log.info("Done sorting the state codes.");
    }

    // Summarize the population of a few states.
    {
      summarizeStatePopulation("CA", "California");
      summarizeStatePopulation("MN", "Minnesota");
      summarizeStatePopulation("WY", "Wyoming");
    }

    // Let's try scanning the data and see how fast it executes...
    {
      log.info(scanForMinneapolisZips_pojos());
      log.info(scanForMinneapolisZips_arrow());
      log.info(scanFor100kZips_pojos());
      log.info(scanFor100kZips_arrow());
    }

    // Benchmark Arrow-based scans vs POJO-based scan...
    {
      int times = 100;
      Util.benchmark("POJO city scan", this::scanForMinneapolisZips_pojos, times);
      Util.benchmark("Arrow VarCharVector city scan", this::scanForMinneapolisZips_arrow, times);
      Util.benchmark("POJO population scan", this::scanFor100kZips_pojos, times);
      Util.benchmark("Arrow IntVector population scan", this::scanFor100kZips_arrow, times);
    }
  }

  private void summarizeStatePopulation(String stateCode, String state) {
    // Search for the range of state ZIP codes.
    //
    // This is a complicated procedure. We have to "search over a dictionary-encoded vector accompanied by a sorted
    // index vector". The quoted scenario is a good illustration of the levels-of-indirection that you need to deal with
    // when working with vector data. There is a certain amount of "essential complexity" that you need to deal with.
    // With some diligence, it's up to you to limit the amount of "accidental complexity" that you introduce.
    // Think about the domain data carefully, think about the data structures carefully, read the Arrow library code
    // carefully and read this code carefully.

    // First, search for the index of the target state code in the "unique state codes" vector.
    //
    // To contribute to the complexity is that the Arrow VectorSearch.binarySearch method doesn't take a parameter that
    // represents the "target" search value but instead takes a a whole vector paired with an index into that vector to
    // represent that target value to search for. I suppose that might have saved the allocation of one object (?) but
    // even then I don't think that's true. I could be wrong but I just don't get it. So, we have to create a temporary
    // one-element vector to do a search.
    int stateCodeIdx;
    try (var temp = new VarCharVector("stateCode", allocator)) {

      temp.allocateNew(1);
      temp.set(0, stateCode.getBytes());
      temp.setValueCount(1);

      VectorValueComparator<VarCharVector> comparator = DefaultVectorComparators.createDefaultComparator(stateCodeUniqueVector);
      stateCodeIdx = VectorSearcher.binarySearch(stateCodeUniqueVector, comparator, temp, 0);

      if (stateCodeIdx == -1) {
        var msg = "Did not find the state code '%s' in the unique state codes vector.".formatted(stateCode);
        throw new IllegalStateException(msg);
      }
    }

    // Now, we can start our search for the range of ZIP codes for the target state code. Usefully, Arrow provides
    // a 'VectorRangeSearcher' class but interestingly it doesn't provide a one-shot range search. Instead, it provides
    // a method to find the first occurrence of a value and a second method to find the last occurrence. With these, you
    // can find the complete range.
    var comparator = new VectorValueComparator<>() {

      @Override
      public int compareNotNull(int keyIndex, int targetIndex) {
        // This is a bit mind-bending.
        var targetStateCodeIndex = stateCodesEncodedVector.getValueAsLong((int) stateCodesSortedIndexVector.getValueAsLong(targetIndex));
        return Long.compare(keyIndex, targetStateCodeIndex);
      }

      @Override
      public VectorValueComparator<ValueVector> createNew() {
        return null;
      }
    };
    int first = VectorRangeSearcher.getFirstMatch(stateCodesSortedIndexVector, comparator, stateCodeUniqueVector, stateCodeIdx);
    int last = VectorRangeSearcher.getLastMatch(stateCodesSortedIndexVector, comparator, stateCodeUniqueVector, stateCodeIdx);

    if (first == -1 || last == -1) {
      log.info("No ZIP codes were found for the state of {} ('{}').", state, stateCode);
      return;
    }

    log.info("{} ZIP code entries are indexed in the range {}-{} in the state code index.", state, Util.formatInteger(first), Util.formatInteger(last));

    // Sum up the population of all the ZIP codes in the state.
    int population = IntStream.rangeClosed(first, last)
            .map(stateCodesSortedIndexVector::get)
            .map(populationVector::get)
            .sum();

    String populationFormatted = Util.formatInteger(population);
    log.info("The population of {} is {}.", state, populationFormatted);
  }

  /**
   * Scan for ZIP codes in Minneapolis from the Java POJO data.
   */
  private String scanForMinneapolisZips_pojos() {
    int minneapolisZips = 0;
    for (var zip : zips) {
      if ("MINNEAPOLIS".equals(zip.cityName())) {
        minneapolisZips++;
      }
    }

    return "Found %s ZIP codes in Minneapolis.".formatted(Util.formatInteger(minneapolisZips));
  }


  /**
   * Scan for ZIP codes in Minneapolis from the Apache Arrow vector. How much faster/slower is it than the POJO-based
   * scan? (It's about 3x to 4x slower I think because of the POJO-based scan is getting some JVM optimizations? Maybe
   * string interning?? No idea.).
   */
  private String scanForMinneapolisZips_arrow() {
    int minneapolisZips = 0;
    int valueCount = cityNameVector.getValueCount();
    byte[] minneapolisBytes = "MINNEAPOLIS".getBytes();

    for (int i = 0; i < valueCount; i++) {
      byte[] stateCode = cityNameVector.get(i);
      if (Arrays.equals(minneapolisBytes, stateCode)) {
        minneapolisZips++;
      }
    }

    return "Found %s ZIP codes in Minneapolis.".formatted(Util.formatInteger(minneapolisZips));
  }

  private String scanFor100kZips_pojos() {
    int count = 0;
    for (var zip : zips) {
      if (zip.population() >= 100_000) {
        count++;
      }
    }

    return "Found %s ZIP codes with population greater than 100,000.".formatted(Util.formatInteger(count));
  }

  /**
   * Apache Arrow is a bit faster than the POJO-based scan. I'm expecting (naively?) that because the Arrow data is laid
   * out "densely" (i.e. in a contiguous block of memory) that it's faster to scan through it.
   * <p>
   * <a href="https://openjdk.org/projects/valhalla/design-notes/state-of-valhalla/01-background#the-costs-of-indirection">inspiration</a>
   */
  private String scanFor100kZips_arrow() {
    int count = 0;
    int valueCount = populationVector.getValueCount();

    for (int i = 0; i < valueCount; i++) {
      int population = populationVector.get(i);
      if (population >= 100_000) {
        count++;
      }
    }

    return "Found %s ZIP codes with population greater than 100,000.".formatted(Util.formatInteger(count));
  }
}
