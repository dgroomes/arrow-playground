package dgroomes.sortandsearch.algorithms;

import java.util.Comparator;

import static dgroomes.sortandsearch.algorithms.Comparison.*;

/**
 * This is a typed alternative to the classic {@link Comparator} interface.
 * @param <T>
 */
@FunctionalInterface
public interface TypedComparator<T> {

  /**
   * Compare a value against another value.
   *
   * @return {@link Comparison#LESS_THAN} if the first value is less than the other one, {@link Comparison#EQUAL_TO} if
   * the values are equal, and {@link Comparison#GREATER_THAN} if the first value is greater than the other one.
   */
  Comparison compare(T value, T other);

  /**
   * Turn a classic {@link Comparator} into a {@link TypedComparator}.
   */
  static <T> TypedComparator<T> from(Comparator<T> comparator) {
    return (value, other) -> {
      var comparison = comparator.compare(value, other);
      if (comparison == 0) {
        return EQUAL_TO;
      } else if (comparison < 0) {
        return LESS_THAN;
      } else {
        return GREATER_THAN;
      }
    };
  }

  TypedComparator<Integer> INT_COMPARATOR = from(Integer::compareTo);

  TypedComparator<String> STRING_COMPARATOR = from(String::compareTo);
}
