package dgroomes.sortandsearch.algorithms.internal;

import dgroomes.sortandsearch.algorithms.BinarySearch;
import dgroomes.sortandsearch.algorithms.Comparison;
import dgroomes.sortandsearch.algorithms.Range;
import dgroomes.sortandsearch.algorithms.TypedComparator;

import java.util.Optional;

import static dgroomes.sortandsearch.algorithms.Range.of;

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

    return BinarySearch.binarySearch(range, this::lookup, this::compare, target);
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
