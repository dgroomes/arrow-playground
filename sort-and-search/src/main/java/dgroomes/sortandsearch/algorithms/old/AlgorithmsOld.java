//package dgroomes.sortandsearch;
//
//import org.apache.arrow.algorithm.search.VectorRangeSearcher;
//import org.apache.arrow.algorithm.sort.IndexSorter;
//import org.apache.arrow.vector.IntVector;
//import org.apache.arrow.vector.VarCharVector;
//
//import java.util.Objects;
//import java.util.Optional;
//
//public class AlgorithmsOld {
//
//  /**
//   * WARNING: This implementation is abandoned. It became too complicated and I never got it fully working. I need to
//   * iterate a second solution. A "keep it simple" solution might be best, but I'm going to keep trying to use a "pattern matching"
//   * and typed approach though: sealed classes (as much as useful), enums (as mush as useful), etc.
//   * <p>
//   * Find the range of indices in a sorted index vector where the indexed values equal a given {@link String} value.
//   * This is a range of "indices of indices" (confusing, but that's the nature of this type of programming).
//   * <p>
//   * This is a binary search algorithm that operates on a sorted index vector, created from {@link IndexSorter}. The
//   * premise of this method is similar to {@link VectorRangeSearcher#getFirstMatch} and {@link VectorRangeSearcher#getLastMatch}
//   * but this API aims to be more ergonomic. Specifically, I don't know why the search methods in {@link VectorRangeSearcher}
//   * and {@link VectorRangeSearcher} ask for a vector and and index into that vector to represent the "value-under-test"
//   * instead of asking for the value itself. Maybe this is a quirk of a similar algorithm ported from the C++
//   * implementation where pointers are the norm?
//   * <p>
//   * This method does not handle nulls. This method is over-engineered; you should probably stick to primitive types for
//   * implementing an algorithm like this.
//   *
//   * @param sortedIndexVector a vector of indices, in sorted order, of "valueVector"
//   * @param valueVector       a vector of values
//   * @param target            the "target" value. The value we are searching for.
//   * @return an {@link Optional}-wrapped {@link dgroomes.sortandsearch.Range} of indices in the sorted index vector where each element of the
//   * index represents an index into the value vector where that element is equal to the "target". If the target was not
//   * found, an empty {@link Optional} is returned.
//   */
//  public static <V> Optional<dgroomes.sortandsearch.Range> searchContiguousRangeOfValue_abandonded(VarCharVector valueVector, IntVector sortedIndexVector, String target) {
//    Objects.requireNonNull(target, "The 'target' argument must not be null");
//    if (valueVector.getValueCount() != sortedIndexVector.getValueCount()) {
//      String msg = String.format("The given index vector must be the same size ('value count') as the given value vector, but found valueVector.getValueCount()=%d and sortedIndexVector.getValueCount()=%d", valueVector.getValueCount(), sortedIndexVector.getValueCount());
//      throw new IllegalStateException(msg);
//    }
//
//    var searcher = new VarCharRangeSearcher(valueVector, sortedIndexVector, target);
//    return searcher.search();
//  }
//
//  private static class VarCharRangeSearcher {
//
//    private final VarCharVector valueVector;
//    private final IntVector indexVector;
//    private final String target;
//
//    VarCharRangeSearcher(VarCharVector valueVector, IntVector indexVector, String target) {
//      this.valueVector = valueVector;
//      this.indexVector = indexVector;
//      this.target = target;
//    }
//
//    Optional<dgroomes.sortandsearch.Range> search() {
//      int valueCount = valueVector.getValueCount();
//      if (valueCount == 0) {
//        return Optional.empty();
//      }
//
//      Optional<Result> found = findEarliestOccurrence();
//      if (found.isEmpty()) return Optional.empty();
//      Result result = found.get();
//
//      Point latestOccurrence = findLatestOccurrence(result.latestKnownOccurrence());
//
//      dgroomes.sortandsearch.Range range = new dgroomes.sortandsearch.Range(result.earliestOccurrence, latestOccurrence.index());
//      return Optional.of(range);
//    }
//
//    /**
//     * How does the value at the given index compare to the "target".
//     */
//    private Comparison compareToTarget(Point point) {
//      var underTest = new String(valueVector.get(indexVector.get(point.index())));
//      int comparison = underTest.compareTo(target);
//      if (comparison < 0) return Comparison.LESS;
//      if (comparison > 0) return Comparison.GREATER;
//      return Comparison.EQUAL;
//    }
//
//    enum Comparison {LESS, EQUAL, GREATER}
//
//    record Result(int earliestOccurrence, int latestKnownOccurrence) {}
//
//    /**
//     * Find the earliest/leftmost occurrence of the "target" value using binary search. Also, indicate a latest/rightmost
//     * "known" occurrence of the "target" value to give a head start to the search for the "absolute" latest/rightmost
//     * occurrence.
//     *
//     * @return an empty {@link Optional} if no occurrences of "target" could be found
//     */
//    Optional<Result> findEarliestOccurrence() {
//      MatchPoint latestKnownOccurrence = null;
//
//      Point low = new UncheckedPoint(0);
//      Point high = new UncheckedPoint(valueVector.getValueCount() - 1);
//      Point mid = middleOrLower(low, high);
//
//      // Continue the search until it has "stalled out". Specifically, if the next point to check (represented by "mid")
//      // has already been checked, then we're not making progress anymore. We've "stalled out".
//      while (mid instanceof UncheckedPoint) {
//        Comparison comparison = compareToTarget(mid);
//
//        switch (comparison) {
//          // Found a string lower than the target. We need to search the upper half.
//          //
//          // We "raise the floor" by moving the "low" index up to the "mid" index.
//          case LESS -> low = mid.nonMatch();
//
//          // Found a string higher than the target. We need to search the lower half.
//          //
//          // We "lower the ceiling" by moving the "high" index down to the "mid" index.
//          case GREATER -> high = mid.nonMatch();
//
//          // Found a string equal to the target.
//          //
//          // We need to continue to search for earlier/leftmost occurrences. Again, we "lower the ceiling".
//          case EQUAL -> {
//            high = mid.match();
//
//            if (latestKnownOccurrence == null) {
//              // If this is the first occurrence we've found of the target, we can demarcate a latest/rightmost "known
//              // occurrence" to give us a head start in the search for the absolute latest/rightmost occurrence of the
//              // target value.
//              latestKnownOccurrence = mid.match();
//            }
//          }
//        }
//
//        mid = middleOrLower(low, high);
//      }
//
//      // We've searched the entirety of the vector.
//
//      if (mid instanceof MatchPoint) {
//        assert latestKnownOccurrence != null;
//        return Optional.of(new Result(mid.index(), latestKnownOccurrence.index()));
//      }
//
//      if (latestKnownOccurrence != null) {
//        return Optional.of(new Result(latestKnownOccurrence.index(), latestKnownOccurrence.index()));
//      }
//
//      return Optional.empty();
//    }
//
//    /**
//     * Find the latest/rightmost occurrence of the "target" value. This is similar to the other method so it is not
//     * commented as much.
//     */
//    MatchPoint findLatestOccurrence(int latestKnownOccurrence) {
//      Point low = new MatchPoint(latestKnownOccurrence);
//      Point high = new UncheckedPoint(valueVector.getValueCount() - 1);
//      Point mid = middleOrHigher(low, high);
//
//      while (mid instanceof UncheckedPoint) {
//        Comparison comparison = compareToTarget(mid);
//
//        switch (comparison) {
//          case LESS -> low = mid.nonMatch();
//          case GREATER -> high = mid.nonMatch();
//          case EQUAL -> high = mid.match();
//        }
//
//        mid = middleOrHigher(low, high);
//      }
//
//      if (mid instanceof MatchPoint midMatchPoint) {
//        return midMatchPoint;
//      }
//      return new MatchPoint(latestKnownOccurrence);
//    }
//
//    /**
//     * Compute a middle point between two bounds. If they are are adjacent, bias to the lower point.
//     */
//    private static Point middleOrLower(Point low, Point high) {
//      return middleOrElse(low, high, low);
//    }
//
//    /**
//     * Compute a middle point between two bounds. If they are are adjacent, bias to the higher point.
//     */
//    private static Point middleOrHigher(Point low, Point high) {
//      return middleOrElse(low, high, high);
//    }
//
//    private static Point middleOrElse(Point low, Point high, Point tieBreaker) {
//      int diff = high.index() - low.index();
//      assert diff >= 0;
//
//      // If the points are the same index, we coalesce their "match" quality. In other words, if one of them is a match
//      // or a non-match, then we can infer that the other is an "unchecked" point. It is an implementation error if the
//      // points contradict each other: one is a match and the other is a non-match.
//      if (diff == 0) {
//        if (low instanceof MatchPoint || high instanceof MatchPoint) {
//          return low.match();
//        }
//        if (low instanceof NonMatchPoint || high instanceof NonMatchPoint) {
//          return low.nonMatch();
//        }
//        return low;
//      }
//
//      // If the points are adjacent to each other or the same, there is no "middle".
//      if (diff == 1) {
//        return tieBreaker;
//      }
//
//      int middleIndex = diff / 2;
//      return (new
//
//              UncheckedPoint(middleIndex + low.index()));
//    }
//
//    private sealed interface Point permits UncheckedPoint, MatchPoint, NonMatchPoint {
//      int index();
//
//      default MatchPoint match() {
//        return new MatchPoint(index());
//      }
//
//      default NonMatchPoint nonMatch() {
//        return new NonMatchPoint(index());
//      }
//    }
//
//    private record UncheckedPoint(int index) implements Point {}
//
//    private record MatchPoint(int index) implements Point {}
//
//    private record NonMatchPoint(int index) implements Point {}
//  }
//
//  /**
//   * A simple binary search over a sorted vector of integers.
//   *
//   * @param vector a sorted vector of integers
//   * @param target the target value to search for
//   * @return an {@link Optional} containing the index of an occurrence of the target value in the vector. Or, an empty
//   * {@link Optional} if no occurrences could be found.
//   */
//  public static Optional<Integer> binarySearch(IntVector vector, int target) {
//    // continually partition the vector....
//    // and check for a match.
//    // if no match, continually split and repeat...
//
//    Span span = parseIndexSpan(0, vector.getValueCount() - 1);
//
//    Range range;
//
//    switch (span) {
//      case Range _range -> range = _range;
//      case Point(int index) -> {
//        return checkPoint(vector, target, index);
//      }
//      // The "definite-assignment" analysis is not working here. I have to throw an exception or else the compiler
//      // complains that "range" might not have been initialized, but Range and Point are exhaustive over Span.
//      default -> throw new IllegalStateException("Unexpected value: " + span);
//    }
//
//    // vector to partitio
//    //    Partition partition;
//
//    boolean found = false;
//
//    while (!found) {
//      Split split = split(range);
//      switch (split) {
//        case PointPair(int left, int right) -> {
//          Optional<Integer> leftCheck = checkPoint(vector, target, left);
//          if (leftCheck.isPresent()) {
//            return leftCheck;
//          }
//          Optional<Integer> rightCheck = checkPoint(vector, target, right);
//          if (rightCheck.isPresent()) {
//            return rightCheck;
//          }
//          return Optional.empty();
//        }
//      }
//
//    }
//
//
//    return Optional.empty();
//  }
//
//  private static Optional<Integer> checkPoint(IntVector vector, int target, int index) {
//    if (vector.get(index) == target) {
//      return Optional.of(index);
//    } else {
//      return Optional.empty();
//    }
//  }
//
//  private static Span parseIndexSpan(int start, int end) {
//    if (start == end) {
//      return new Point(start);
//    }
//    return new Range(start, end);
//  }
//
//  private sealed interface Span permits Range, Point {}
//
//  /**
//   * Partition a range into two components: a left side and a right side.
//   *
//   * The partition is created towards the middle of the range. If a perfect middle does not exist because the range is
//   * an even number, then the left side will be the larger of the two components.
//   */
//  private static Split split(Range range) {
//    var rangeSize = range.high - range.low;
//
//    int middle;
//    {
//      boolean isEven = rangeSize % 2 == 0;
//      if (isEven) {
//        middle = range.low + rangeSize / 2;
//      } else {
//        middle = range.low + rangeSize / 2 + 1;
//      }
//    }
//
//    if (middle == range.high) {
//      return new LeftRangeSplit(new Range(range.low, middle - 1), middle + 1);
//    }
//
//    return new FullRangeSplit(new Range(range.low, middle - 1), new Range(middle + 1, range.high));
//  }
//
//  private sealed interface Split permits FullRangeSplit, LeftRangeSplit, RightRangeSplit {
//
//    int splitPointIndex();
//  }
//
//  private record FullRangeSplit(Range left, Range right) implements Split {
//
//    @Override
//    public int splitPointIndex() {
//      return left().high + 1;
//    }
//  }
//
//  /**
//   * A split where the left side is a range and the right side is just a single value (not a range).
//   */
//  private record LeftRangeSplit(Range left, int right) implements Split {
//
//    @Override
//    public int splitPointIndex() {
//      return left().high + 1;
//    }
//  }
//
//  /**
//   * A split where the right side is a range and the left side is a single value (not a range).
//   */
//  private record RightRangeSplit(int left, Range right) implements Split {
//
//    @Override
//    public int splitPointIndex() {
//      return left + 1;
//    }
//  }
//
//  private record Point(int index) implements Span {}
//
//  private record PointPair(int left, int right) {}
//
//  /**
//   * A range over at least 3 indices from a low index to a high index. The range must "properly stretch" meaning the
//   * range must always be from a lower index to a higher index: for example "(1,3)". A range is never a single-valued
//   * index or a pair: for example "(1,1)" or "(1,2)".
//   *
//   * @param low
//   * @param high
//   */
//  private record Range(int low, int high) implements Span {
//    public Range {
//      var diff = high - low;
//      if (diff > 1) {
//        throw new IllegalArgumentException("The range must stretch at least three indices from its low-valued index to its higher-valued index:" + this);
//      }
//    }
//  }
//}
