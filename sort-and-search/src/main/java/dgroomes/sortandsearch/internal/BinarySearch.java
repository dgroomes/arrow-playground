package dgroomes.sortandsearch.internal;

import dgroomes.sortandsearch.internal.BinarySearchStepResult.*;
import dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.OneSide;

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

  interface IndexComparator {
    Comparison compare(int index);
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
  static BinarySearchStepResult binarySearchStep(Range range, IndexComparator indexComparator) {
    Split split = split(range);

    return switch (split) {
      case SplitPoint(Point(int index)) -> {
        var comparison = indexComparator.compare(index);
        yield switch (comparison) {
          case EQUAL_TO -> new FoundExhausted(index);
          case LESS_THAN, GREATER_THAN -> new NotFoundExhausted();
        };
      }
      case SplitPointPair(PointPair(int left, int right)) -> {
        var comparison = indexComparator.compare(left);
        yield switch (comparison) {
          case EQUAL_TO -> new Found(left, new OneSide(new Point(right)));
          case LESS_THAN, GREATER_THAN -> switch (indexComparator.compare(right)) {
            case EQUAL_TO -> new FoundExhausted(right);
            case LESS_THAN, GREATER_THAN -> new NotFoundExhausted();
          };
        };
      }
      case TrueSplit(var left, int middle, var right) -> {
        var comparison = indexComparator.compare(middle);
        yield switch (comparison) {
          case EQUAL_TO -> new Found(middle, new TwoSided(left, right));
          case LESS_THAN -> new TooHigh(left);
          case GREATER_THAN -> new TooLow(right);
        };
      }
    };
  }
}
