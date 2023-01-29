package dgroomes.sortandsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.util.List;
import java.util.stream.Stream;

/**
 * Please see the README for more information.
 */
public class Runner {

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

      log.info("Read {} ZIP codes from the local file and into Java objects.", zips.size());
    }

    // Load the in-memory ZIP and city data from Java objects into Apache Arrow's in-memory data structures.
    try (BufferAllocator allocator = new RootAllocator();
         IntVector zipCodeVector = new IntVector("zip-codes", allocator);
         VarCharVector cityNameVector = new VarCharVector("city-names", allocator);
         VarCharVector stateCodeVector = new VarCharVector("state-codes", allocator);
         IntVector populationVector = new IntVector("populations", allocator);
         IntVector populationIndexVector = new IntVector("population-index", allocator)) {

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

      zipCodeVector.setValueCount(zipValuesSize);
      cityNameVector.setValueCount(zipValuesSize);
      stateCodeVector.setValueCount(zipValuesSize);
      populationVector.setValueCount(zipValuesSize);

      log.info("Loaded {} ZIP codes into Apache Arrow vectors.", zipValuesSize);

      // Sort the population data.
      //
      // We're not going to actually mutate the population vector itself but we're going to create an "index vector"
      // which is a sorted representation of the population vector. By way of example, the first element of the index
      // vector might be 123, which means that the 123th element of the population vector has the smallest population.
      IndexSorter<IntVector> populationIndexer = new IndexSorter<>();
      populationIndexVector.setValueCount(zipValuesSize);
      populationIndexer.sort(populationVector, populationIndexVector, new DefaultVectorComparators.IntComparator());

      {
        int idx = populationIndexVector.get(0);
        int zip = zipCodeVector.get(idx);
        String cityName = new String(cityNameVector.get(idx));
        String stateCode = new String(stateCodeVector.get(idx));
        int population = populationVector.get(idx);
        log.info("The lowest population ZIP code is {} ({}, {}) with a population of {}.", zip, cityName, stateCode, population);
      }

      {
        int idx = populationIndexVector.get(zipValuesSize - 1);
        int zip = zipCodeVector.get(idx);
        String cityName = new String(cityNameVector.get(idx));
        String stateCode = new String(stateCodeVector.get(idx));
        int population = populationVector.get(idx);
        log.info("The highest population ZIP code is {} ({}, {}) with a population of {}.", zip, cityName, stateCode, population);
      }

      // TODO Search
    }
  }
}
