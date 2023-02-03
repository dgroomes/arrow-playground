package dgroomes.sortandsearch.internal;

import dgroomes.sortandsearch.internal.BinarySearchStepResult.*;
import dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.OneSide;

import java.util.function.Function;

import static dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.TwoSided;
import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

public class BinarySearch {

  /**
   * Split a range on a "middle" point. The result is three parts: a left side, a middle, and a right side.
   * <p>
   * Usefully, this method accommodates ranges without a middle (i.e. a point or a point pair).
   */
  public static Split split(Range range) {
    return switch (range) {
      // A point is not splittable.
      case Point point -> new SplitPoint(point);
      // A pair of points are not splittable.
      case PointPair pointPair -> new SplitPointPair(pointPair);

      case StretchRange(int low, int high) -> {
        int middle = low + (high - low) / 2;
        Range left = of(low, middle - 1);
        Range right = of(middle + 1, high);
        yield new TrueSplit(left, middle, right);
      }
    };
  }

  /**
   * Perform a single step of the binary search algorithm.
   * <p>
   * Note: This method in combination with the related types ({@link Range}, {@link Split}, {@link Comparison} and
   * {@link BinarySearchStepResult}) and "exhaustive checking" for switch expressions/statements has proven to be a good
   * fundamental building block to build other binary search methods on top of. My goal was to learn more Java features,
   * brush up on my algorithm skills, and also to reduce the pain of off-by-one errors.
   * <p>
   * This implementation is somewhat silly, but it's great for learning.
   */
  static <T> BinarySearchStepResult binarySearchStep(Range range, Function<Integer, T> lookup, TypedComparator<T> typedComparator, T target) {
    Split split = split(range);

    return switch (split) {
      case SplitPoint(Point(int index)) -> {
        T valueUnderTest = lookup.apply(index);
        var comparison = typedComparator.compare(target, valueUnderTest);
        yield switch (comparison) {
          case EQUAL_TO -> new FoundExhausted(index);
          case LESS_THAN, GREATER_THAN -> new NotFoundExhausted();
        };
      }
      case SplitPointPair(PointPair(int left, int right)) -> {
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
      case TrueSplit(var left, int middle, var right) -> {
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
}
