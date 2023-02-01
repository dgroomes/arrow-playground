package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

public class BinaryIntSearcherOverIndex {
  private final IntVector values;
  private final IntVector index;
  private final int target;

  public BinaryIntSearcherOverIndex(IntVector values, IntVector index, int target) {
    this.values = values;
    this.index = index;
    this.target = target;
  }

  public Optional<Integer> search() {
    if (values.getValueCount() == 0) return Optional.empty();

    Range range = of(0, values.getValueCount() - 1);

    while (true) {
      Split split = InternalAlgorithms.split(range);
      switch (split) {
        case SplitPoint(Point(int index)) -> {
          return checkPoint(index);
        }
        case SplitPointPair(PointPair(int left, int right)) -> {
          return checkPoint(left).or(() -> checkPoint(right));
        }
        case TrueSplit(var left, int middle, var right) -> {
          var comparison = vectorValueAtIndex(middle) - target;
          if (comparison > 0) {
            // Found a value higher than the target. We need to search the lower half.
            range = left;
          } else if (comparison < 0) {
            // Found a value lower than the target. We need to search the upper half.
            range = right;
          } else {
            // We found it!
            return Optional.of(middle);
          }
        }
      }
    }
  }

  private Optional<Integer> checkPoint(int index) {
    int valueUnderTest = vectorValueAtIndex(index);
    if (valueUnderTest == target) {
      return Optional.of(index);
    } else {
      return Optional.empty();
    }
  }

  private int vectorValueAtIndex(int index) {
    return values.get(this.index.get(index));
  }
}
