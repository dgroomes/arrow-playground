package dgroomes.sortandsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dgroomes.sortandsearch.algorithms.Algorithms;
import org.apache.arrow.algorithm.sort.DefaultVectorComparators;
import org.apache.arrow.algorithm.sort.IndexSorter;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Please see the README for more information.
 */
public class Runner {

  public static final int PARALLEL_UNIVERSES = 10;
  private static final Logger log = LoggerFactory.getLogger(Runner.class);

  public static void main(String[] args) {
    log.info("Reading ZIP code data from the local file ...");
    record Zip(String zipCode, String cityName, String stateCode, int population) {}

    // Read the ZIP code data from the local JSON file.
    List<Zip> zips;
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

      log.info("Multiplied the ZIP code data by {} to get {} parallel universe ZIP codes.", PARALLEL_UNIVERSES, Util.formatInteger(zips.size()));
    }

    // Load the in-memory ZIP and city data from Java objects into Apache Arrow's in-memory data structures.
    try (BufferAllocator allocator = new RootAllocator();
         IntVector zipCodeVector = new IntVector("zip-codes", allocator);
         VarCharVector cityNameVector = new VarCharVector("city-names", allocator);
         VarCharVector stateCodeVector = new VarCharVector("state-codes", allocator);
         IntVector populationVector = new IntVector("populations", allocator);
         IntVector populationSortedIndexVector = new IntVector("population-index", allocator);
         IntVector stateCodesSortedIndexVector = new IntVector("state-codes-index", allocator)) {

      int zipValuesSize = zips.size();

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

      // Sort the population data.
      //
      // We're not going to actually mutate the population vector itself but we're going to create an "index vector"
      // which is a sorted representation of the population vector. By way of example, the first element of the index
      // vector might be 123, which means that the 123th element of the population vector has the smallest population.
      log.info("Sorting the population data ...");
      IndexSorter<IntVector> populationIndexer = new IndexSorter<>();
      populationSortedIndexVector.setValueCount(zipValuesSize);
      populationIndexer.sort(populationVector, populationSortedIndexVector, new DefaultVectorComparators.IntComparator());
      log.info("Done sorting the population data.");

      {
        int idx = populationSortedIndexVector.get(zipValuesSize - 1);
        int zip = zipCodeVector.get(idx);
        String cityName = new String(cityNameVector.get(idx));
        String stateCode = new String(stateCodeVector.get(idx));
        int population = populationVector.get(idx);
        log.info("The highest population ZIP code is {} ({}, {}) with a population of {}.", zip, cityName, stateCode, Util.formatInteger(population));
      }

      // Sort the state codes names.
      log.info("Sorting the state codes ...");
      IndexSorter<VarCharVector> stateCodeIndexer = new IndexSorter<>();
      stateCodesSortedIndexVector.setValueCount(zipValuesSize);
      stateCodeIndexer.sort(stateCodeVector, stateCodesSortedIndexVector, DefaultVectorComparators.createDefaultComparator(stateCodeVector));
      log.info("Done sorting the state codes.");

      // Summarize the population of a few states.
      summarizeStatePopulation(stateCodeVector, populationVector, stateCodesSortedIndexVector, "CA", "California");
      summarizeStatePopulation(stateCodeVector, populationVector, stateCodesSortedIndexVector, "MN", "Minnesota");
      summarizeStatePopulation(stateCodeVector, populationVector, stateCodesSortedIndexVector, "WY", "Wyoming");
    }
  }

  private static void summarizeStatePopulation(VarCharVector stateCodeVector, IntVector populationVector, IntVector stateCodesSortedIndexVector, String stateCode, String state) {
    // Get the range of state ZIP codes.
    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(stateCodeVector, stateCodesSortedIndexVector, stateCode);

    if (found.isEmpty()) {
      log.info("No ZIP codes were found for the state of {} ('{}').", state, stateCode);
      return;
    }

    Algorithms.Range range = found.get();
    log.info("{} ZIP code entries are indexed in the range {}-{} in the state code index.", state, Util.formatInteger(range.low()), Util.formatInteger(range.high()));

    // Sum up the population of all the ZIP codes in the state.
    int population = IntStream.rangeClosed(range.low(), range.high())
            .map(stateCodesSortedIndexVector::get)
            .map(populationVector::get)
            .sum();

    String populationFormatted = Util.formatInteger(population);
    log.info("The population of {} is {}.", state, populationFormatted);
  }
}
