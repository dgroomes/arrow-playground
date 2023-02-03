package dgroomes.sortandsearch.internal;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.Comparison.*;
import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

/**
 * A generic binary search implementation.
 *
 * @param <T>
 */
public abstract class AbstractBinarySearcher<T> {

  private final int size;

  public AbstractBinarySearcher(int size) {
    this.size = size;
  }

  public Optional<Integer> search() {
    if (size == 0) return Optional.empty();

    Range range = of(0, size - 1);

    while (true) {
      Split split = BinarySearch.split(range);
      switch (split) {
        case SplitPoint(Point(int index)) -> {
          return checkPoint(index);
        }
        case SplitPointPair(PointPair(int left, int right)) -> {
          return checkPoint(left).or(() -> checkPoint(right));
        }
        case TrueSplit(var left, int middle, var right) -> {
          switch (targetComparedToElementAt(middle)) {
            // The target is less than the middle point. We need to search the lower half.
            case LESS_THAN -> range = left;
            // The target is greater than the middle point. We need to search the upper half.
            case GREATER_THAN -> range = right;
            // We found it!
            case EQUAL_TO -> {
              return Optional.of(middle);
            }
          }
        }
      }
    }
  }

  /**
   * Get the value of the element at the given index. This value is going to be compared to the "target" value and drive
   * the binary search in the right direction.
   *
   * @param index the "index-under-test
   * @return the "value-under-test"
   */
  abstract T lookup(int index);

  /**
   * In the style of {@link java.util.Comparator#compare}, return an integer that represents the comparison integer when
   * comparing "target" to the "value-under-test".
   * <p>
   * For example, for a "target" of 3 and "value-under-test" of 5, the comparison yields -2.
   */
  abstract int compare(T valueUnderTest);

  private Optional<Integer> checkPoint(int index) {
    if (targetComparedToElementAt(index) == EQUAL_TO) {
      return Optional.of(index);
    } else {
      return Optional.empty();
    }
  }

  private Comparison targetComparedToElementAt(int index) {
    // Note to self: while Lisp-like languages are fun, and writing wide code instead of tall code is fun, I am continually
    // reminded that having local variables is a big enabler for understanding the code while in the debugger.
    var valueUnderTest = lookup(index);
    int comparison = compare(valueUnderTest);
    if (comparison == 0) {
      return EQUAL_TO;
    } else if (comparison < 0) {
      return LESS_THAN;
    } else {
      return GREATER_THAN;
    }
  }
}
