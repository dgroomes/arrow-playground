package dgroomes.sortandsearch.algorithms;

import dgroomes.sortandsearch.algorithms.BinarySearchStepResult.*;
import dgroomes.sortandsearch.algorithms.BinarySearchStepResult.Unsearched.OneSide;

import java.util.function.Function;

import static dgroomes.sortandsearch.algorithms.BinarySearchStepResult.Unsearched.TwoSided;
import static dgroomes.sortandsearch.algorithms.Range.*;

public class BinarySearch {

  /**
   * Perform a single step of the binary search algorithm.
   * <p>
   * Note: This method in combination with the related types ({@link Range}, {@link Comparison} and {@link BinarySearchStepResult})
   * and "exhaustive checking" for switch expressions/statements has proven to be a good fundamental building block to
   * build other binary search methods on top of. My goal was to learn more Java features, brush up on my algorithm
   * skills, and also to reduce the pain of off-by-one errors.
   * <p>
   * This implementation is somewhat silly, but it's great for learning.
   */
  public static <T> BinarySearchStepResult binarySearchStep(Range range, Function<Integer, T> lookup, TypedComparator<T> typedComparator, T target) {
    Split split = split(range);

    return switch (split) {
      case Split.Point(int index) -> {
        T valueUnderTest = lookup.apply(index);
        var comparison = typedComparator.compare(target, valueUnderTest);
        yield switch (comparison) {
          case EQUAL_TO -> new FoundExhausted(index);
          case LESS_THAN, GREATER_THAN -> new NotFoundExhausted();
        };
      }
      case Split.PointPair(int left, int right) -> {
        // Note to self: while Lisp-like languages are fun, and writing wide code instead of tall code is fun, I am continually
        // reminded that having local variables is a big enabler for understanding the code while in the debugger.
        // In this particular case, I like to see "leftValueUnderTest" and "leftComparison even though it means that I
        // have to name the later local variables "rightValueUnderTest" and "rightComparison", which is quite verbose.
        T leftValueUnderTest = lookup.apply(left);
        var leftComparison = typedComparator.compare(target, leftValueUnderTest);
        yield switch (leftComparison) {
          case EQUAL_TO -> new Found(left, new OneSide(new Point(right)));
          case LESS_THAN, GREATER_THAN -> {
            T rightValueUnderTest = lookup.apply(right);
            var rightComparison = typedComparator.compare(target, rightValueUnderTest);
            yield switch (rightComparison) {
              case EQUAL_TO -> new FoundExhausted(right);
              case LESS_THAN, GREATER_THAN -> new NotFoundExhausted();
            };
          }
        };
      }
      case Split.TrueSplit(var left, int middle, var right) -> {
        T valueUnderTest = lookup.apply(middle);
        var comparison = typedComparator.compare(target, valueUnderTest);
        yield switch (comparison) {
          case EQUAL_TO -> new Found(middle, new TwoSided(left, right));
          case LESS_THAN -> new TooHigh(left);
          case GREATER_THAN -> new TooLow(right);
        };
      }
    };
  }

  /**
   * I'm struggling between calling this a "partition" or a "split" because partition is a strong, well-understood term
   * but I really want to model the "middle point" and its two neighbors (left and right). And that's not really a partition
   * right?
   */
  private sealed interface Split {

    record Point(int index) implements Split {}

    record PointPair(int left, int right) implements Split {}

    record TrueSplit(Range left, int middle, Range right) implements Split {}
  }

  /**
   * Split a range on a "middle" point. The result is three parts: a left side, a middle, and a right side.
   * <p>
   * Usefully, this method accommodates ranges without a middle (i.e. a point or a point pair).
   */
  private static Split split(Range range) {
    return switch (range) {
      // A point is not splittable.
      case Point(int index) -> new Split.Point(index);
      // A pair of points are not splittable.
      case PointPair(int left, int right) -> new Split.PointPair(left, right);

      case StretchRange(int low, int high) -> {
        int middle = low + (high - low) / 2;
        Range left = of(low, middle - 1);
        Range right = of(middle + 1, high);
        yield new Split.TrueSplit(left, middle, right);
      }
    };
  }
}
