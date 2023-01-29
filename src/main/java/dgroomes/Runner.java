package dgroomes;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.IntVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

  private static final Logger log = LoggerFactory.getLogger(Runner.class);

  /**
   * Please see the README for more information.
   */
  public static void main(String[] args) {
    log.info("Let's learn about Apache Arrow!");

    // Let's create two vectors. The first is an integer vector representing the ZIP codes and the second is another
    // integer vector representing the population of each ZIP code.

    // Let's represent these ZIP codes:
    //
    // ZIP Code | Population | City
    // 90210    | 20,700     | Beverly Hills
    // 19106    | 7,043      | Philadelphia (this is where the Liberty Bell is)
    // 82190    | 443        | Fishing Bridge (this is in Yellowstone National Park)

    try (BufferAllocator allocator = new RootAllocator();
         IntVector zipCodeVector = new IntVector("zip-codes", allocator);
         IntVector populationVector = new IntVector("populations", allocator)) {

      zipCodeVector.allocateNew(3);
      populationVector.allocateNew(3);

      // Beverly Hills
      {
        zipCodeVector.set(0, 90210);
        populationVector.set(0, 20_700);
      }

      // The Liberty Bell
      {
        zipCodeVector.set(1, 19106);
        populationVector.set(1, 7_043);
      }

      // Yellowstone National Park
      {
        zipCodeVector.set(2, 82190);
        populationVector.set(2, 443);
      }

      zipCodeVector.setValueCount(3);
      populationVector.setValueCount(3);

      log.info("ZIP code vector: {}", zipCodeVector);
      log.info("Population vector: {}", populationVector);
    }
  }
}
