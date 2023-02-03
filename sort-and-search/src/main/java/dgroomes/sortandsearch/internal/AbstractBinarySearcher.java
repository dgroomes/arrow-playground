package dgroomes.sortandsearch.internal;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.Comparison.EQUAL_TO;
import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

/**
 * A generic binary search implementation.
 *
 * @param <T>
 */
public abstract class AbstractBinarySearcher<T> {

  private final int size;
  private final T target;

  public AbstractBinarySearcher(int size, T target) {
    this.size = size;
    this.target = target;
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
          // Note to self: while Lisp-like languages are fun, and writing wide code instead of tall code is fun, I am continually
          // reminded that having local variables is a big enabler for understanding the code while in the debugger.
          Comparison comparison = targetComparedToElementAt(middle);
          switch (comparison) {
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
  protected abstract T lookup(int index);

  /**
   * In the style of {@link TypedComparator}, return a {@link java.util.Comparator} that represents the comparison
   * result when comparing "target" to the "value-under-test".
   * <p>
   * For example, for a "target" of 3 and "value-under-test" of 5, the comparison yields {@link Comparison#LESS_THAN}.
   */
  protected abstract Comparison compare(T target, T valueUnderTest);

  private Optional<Integer> checkPoint(int index) {
    if (targetComparedToElementAt(index) == EQUAL_TO) {
      return Optional.of(index);
    } else {
      return Optional.empty();
    }
  }

  private Comparison targetComparedToElementAt(int index) {
    var valueUnderTest = lookup(index);
    return compare(target, valueUnderTest);
  }
}
