package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.ValueVector;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static dgroomes.sortandsearch.internal.BinarySearchStepResult.*;
import static dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.OneSide;
import static dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.TwoSided;

/**
 * A generic binary search implementation to find all matches (a range) of a target value in a vector.
 *
 * @param <T>
 */
public abstract class AbstractBinaryRangeSearcher<T> {

  private final ValueVector vector;
  private final T target;
  private final Queue<Range> unsearchedRanges = new LinkedList<>();
  private Range matchRange;

  public AbstractBinaryRangeSearcher(ValueVector vector, T target) {
    this.vector = vector;
    this.target = target;
  }

  public Optional<Range> search() {
    int size = vector.getValueCount();
    if (size == 0) {
      return Optional.empty();
    }

    {
      Range initialRange = Range.of(0, size - 1);
      unsearchedRanges.add(initialRange);
    }

    while (!unsearchedRanges.isEmpty()) {
      Range toSearch = unsearchedRanges.remove();
      BinarySearchStepResult stepResult = BinarySearch.binarySearchStep(toSearch, this::lookup, this::compare, target);

      switch (stepResult) {
        case Found(int index, Unsearched unsearched) -> {
          matchRange = Range.extend(matchRange, index);
          switch (unsearched) {
            case TwoSided(var left, var right) -> {
              addUnsearchedRange(left);
              addUnsearchedRange(right);
            }
            case OneSide(var range) -> addUnsearchedRange(range);
          }
        }
        case FoundExhausted(int index) -> matchRange = Range.extend(matchRange, index);
        case NotFoundExhausted() -> {
        }
        case TooHigh(var range) -> addUnsearchedRange(range);
        case TooLow(var range) -> addUnsearchedRange(range);
      }
    }

    return Optional.ofNullable(matchRange);
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

  private void addUnsearchedRange(Range range) {
    if (matchRange != null && matchRange.contains(range)) {
      return;
    }
    unsearchedRanges.add(range);
  }
}
