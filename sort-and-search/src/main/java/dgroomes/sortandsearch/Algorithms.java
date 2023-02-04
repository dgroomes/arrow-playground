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

  /**
   * Binary search over a sorted vector of integers to produce a range of indices that all match the given "target"
   * integer.
   * <p>
   * In other words, the sorted vector is expected to allow duplicates and you want to find the target value and all its
   * duplicate neighbors (if they exist). This forms a range. For example, given the vector [1, 2, 2, 3], a search for
   * 2 would return the range of indices [1, 2].
   * <p>
   * The range is inclusive and zero-indexed.
   *
   * @param vector a sorted vector of integers
   * @param target the target value to search for
   * @return an {@link Optional} containing the index of an occurrence of the target value in the vector. Or, an empty
   * {@link Optional} if no occurrences could be found.
   */
  public static Optional<Range> binaryRangeSearch(IntVector vector, int target) {
    var searcher = new BinaryRangeIntSearcher(vector, target);
    var result = searcher.search();
    return result.map(internalRange -> new Range(internalRange.low(), internalRange.high()));
  }

  /**
   * Binary search over a sorted vector of strings to produce a range of indices that all match the given "target"
   * string.
   * <p>
   * In other words, the sorted vector is expected to allow duplicates and you want to find the target value and all its
   * duplicate neighbors (if they exist). This forms a range. For example, given the vector ["a", "b", "b", "c"], a search
   * for "b" would return the range of indices [1, 2].
   * <p>
   * The range is inclusive and zero-indexed.
   *
   * @param vector a sorted vector of strings
   * @param target the target value to search for
   * @return an {@link Optional} containing the index of an occurrence of the target value in the vector. Or, an empty
   * {@link Optional} if no occurrences could be found.
   */
  public static Optional<Range> binaryRangeSearch(VarCharVector vector, String target) {
    var searcher = new BinaryRangeStringSearcher(vector, target);
    var result = searcher.search();
    return result.map(internalRange -> new Range(internalRange.low(), internalRange.high()));
  }

  /**
   * Binary search over a sorted vector of integers to produce a range of indices that all match the given "target"
   * integer. The "values" vector itself does not contain elements in a sorted order but its accompanying index vector
   * indicates a sorted order of the indices of the elements in the "value" vector.
   */
  public static Optional<Range> binaryRangeSearch(IntVector values, IntVector index, int target) {
    BinaryRangeIntSearcherOverIndex searcher = new BinaryRangeIntSearcherOverIndex(values, index, target);
    return searcher.search().map(internalRange -> new Range(internalRange.low(), internalRange.high()));
  }

  public record Range(int low, int high) {}
}
