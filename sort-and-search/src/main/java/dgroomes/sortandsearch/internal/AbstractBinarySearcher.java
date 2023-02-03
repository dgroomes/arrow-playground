package dgroomes.sortandsearch.internal;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.Comparison.EQUAL_TO;
import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

public abstract class AbstractBinarySearcher {

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

  private Optional<Integer> checkPoint(int index) {
    if (targetComparedToElementAt(index) == EQUAL_TO) {
      return Optional.of(index);
    } else {
      return Optional.empty();
    }
  }

  protected abstract Comparison targetComparedToElementAt(int index);
}
