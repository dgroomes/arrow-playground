package dgroomes.sortandsearch;

import dgroomes.sortandsearch.internal.*;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;

import java.util.Optional;

/**
 * Basic algorithms for searching vectors.
 */
public class Algorithms {

  /**
   * Binary search over a sorted vector of integers.
   *
   * @param vector a sorted vector of integers
   * @param target the target value to search for
   * @return an {@link Optional} containing the index of an occurrence of the target value in the vector. Or, an empty
   * {@link Optional} if no occurrences could be found.
   */
  public static Optional<Integer> binarySearch(IntVector vector, int target) {
    return new BinaryIntSearcher(vector, target).search();
  }

  /**
   * Binary search over a sorted collection of integers. The "values" vector itself does not contain elements in a
   * sorted order but its accompanying index vector indicates a sorted order of the indices of the elements in
   * the "value" vector.
   */
  public static Optional<Integer> binarySearch(IntVector values, IntVector index, int target) {
    return new BinaryIntSearcherOverIndex(values, index, target).search();
  }

  /**
   * Binary search over a sorted collection of strings. The "values" vector itself does not contain elements in a
   * sorted order but its accompanying index vector indicates a sorted order of the indices of the elements in
   * the "value" vector.
   */
  public static Optional<Integer> binarySearch(VarCharVector vector, IntVector index, String target) {
    return new BinaryVarCharSearcherOverIndex(vector, index, target).search();
  }
}
