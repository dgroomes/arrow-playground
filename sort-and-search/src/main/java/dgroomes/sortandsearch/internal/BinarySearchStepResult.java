package dgroomes.sortandsearch.internal;

/**
 * This represents the result of a single step of the binary search algorithm. As a reminder, the binary search
 * algorithm picks a "middle" point from a sorted list of elements, inspects the element at that point, and determines
 * if it's a match, or if it's too low, or if it's too high.
 */
sealed interface BinarySearchStepResult {

  /**
   * This result indicates that the target value was found. In this case, elements on the left side may also be a match
   * and elements on the right side may also be a match. These sides remain unsearched.
   */
  record Found(int index, Unsearched unsearched) implements BinarySearchStepResult {}

  /**
   * This result indicates that the target value was found and there is no space left unsearched.
   *
   * @param index
   */
  record FoundExhausted(int index) implements BinarySearchStepResult {}

  /**
   * This result indicates that the element-under-test was too low. In this case, all elements on the left side of the
   * split are also too low so we can discard the left side. The right side remains unsearched.
   * searched.
   *
   * @param unsearched
   */
  record TooLow(Range unsearched) implements BinarySearchStepResult {}

  /**
   * This result indicates that the element-under-test was too high. In this case, all elements on the right side of the
   * split are also too high so we can discard the right side. The left side remains unsearched.
   */
  record TooHigh(Range unsearched) implements BinarySearchStepResult {}

  /**
   * This result indicates that the target value was not found and there is no space left unsearched.
   */
  record NotFoundExhausted() implements BinarySearchStepResult {}

  sealed interface Unsearched {
    record OneSide(Range unsearched) implements Unsearched {}

    record TwoSided(Range leftUnsearched, Range rightUnsearched) implements Unsearched {}
  }
}
