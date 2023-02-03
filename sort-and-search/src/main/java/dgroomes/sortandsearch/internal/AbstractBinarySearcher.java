package dgroomes.sortandsearch.internal;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.BinarySearchStepResult.*;
import static dgroomes.sortandsearch.internal.Range.of;

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
      BinarySearchStepResult stepResult = BinarySearch.binarySearchStep(range, this::lookup, this::compare, target);

      switch (stepResult) {
        case Found(int index, Unsearched ignored) -> {
          return Optional.of(index);
        }
        case FoundExhausted(int index) -> {
          return Optional.of(index);
        }
        case NotFoundExhausted() -> {
          return Optional.empty();
        }
        case TooHigh(var unsearched) -> range = unsearched;
        case TooLow(var unsearched) -> range = unsearched;
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
}
