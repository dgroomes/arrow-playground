package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static dgroomes.sortandsearch.internal.BinarySearchStepResult.*;
import static dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.OneSide;
import static dgroomes.sortandsearch.internal.BinarySearchStepResult.Unsearched.TwoSided;
import static dgroomes.sortandsearch.internal.Comparison.*;

/**
 * This algorithm uses binary search to iteratively break down and search ranges of the input vector. Java's sealed
 * classes, record classes, and pattern matching for switch (a language preview feature) are featured in the
 * implementation.
 */
public class BinaryRangeIntSearcher {
  private final IntVector vector;
  private final int target;
  private final Queue<Range> unsearchedRanges = new LinkedList<>();
  private Range matchRange;

  public BinaryRangeIntSearcher(IntVector vector, int target) {
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
      BinarySearchStepResult stepResult = BinarySearch.binarySearchStep(toSearch, this::targetComparedToElementAt);

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

  private void addUnsearchedRange(Range range) {
    if (matchRange != null && matchRange.contains(range)) {
      return;
    }
    unsearchedRanges.add(range);
  }

  protected Comparison targetComparedToElementAt(int index) {
    int valueUnderTest = vector.get(index);
    int comparison = target - valueUnderTest;
    if (comparison == 0) {
      return EQUAL_TO;
    } else if (comparison < 0) {
      return LESS_THAN;
    } else {
      return GREATER_THAN;
    }
  }
}
