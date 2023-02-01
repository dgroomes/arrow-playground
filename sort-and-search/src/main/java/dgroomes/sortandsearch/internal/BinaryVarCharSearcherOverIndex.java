package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;

import java.util.Optional;

import static dgroomes.sortandsearch.internal.BinaryVarCharSearcherOverIndex.Comparison.EQUAL_TO;
import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

public class BinaryVarCharSearcherOverIndex {
  private final VarCharVector values;
  private final IntVector index;
  private final String target;

  public BinaryVarCharSearcherOverIndex(VarCharVector values, IntVector index, String target) {
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
          switch (targetComparedToMiddle(middle)) {
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
    String valueUnderTest = vectorValueAtIndex(index);
    if (valueUnderTest.equals(target)) {
      return Optional.of(index);
    } else {
      return Optional.empty();
    }
  }

  private String vectorValueAtIndex(int index) {
    return new String(values.get(this.index.get(index)));
  }

  enum Comparison {
    LESS_THAN, EQUAL_TO, GREATER_THAN
  }

  private Comparison targetComparedToMiddle(int index) {
    String valueUnderTest = vectorValueAtIndex(index);
    int comparison = target.compareTo(valueUnderTest);
    if (comparison == 0) {
      return EQUAL_TO;
    } else if (comparison < 0) {
      return Comparison.LESS_THAN;
    } else {
      return Comparison.GREATER_THAN;
    }
  }
}
