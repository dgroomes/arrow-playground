package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

public class BinaryIntSearcher {
  private final IntVector vector;
  private final int target;

  public BinaryIntSearcher(IntVector vector, int target) {
    this.vector = vector;
    this.target = target;
  }

  public Optional<Integer> search() {
    if (vector.getValueCount() == 0) return Optional.empty();

    Range range = of(0, vector.getValueCount() - 1);

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
          var comparison = vector.get(middle) - target;
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
    if (vector.get(index) == target) {
      return Optional.of(index);
    } else {
      return Optional.empty();
    }
  }
}
