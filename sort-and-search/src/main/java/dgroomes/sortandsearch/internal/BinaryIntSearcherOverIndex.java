package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

import static dgroomes.sortandsearch.internal.Comparison.*;

public class BinaryIntSearcherOverIndex extends AbstractBinarySearcher {
  private final IntVector values;
  private final IntVector index;
  private final int target;

  public BinaryIntSearcherOverIndex(IntVector values, IntVector index, int target) {
    super(values.getValueCount());
    this.values = values;
    this.index = index;
    this.target = target;
  }

  @Override
  protected Comparison targetComparedToElementAt(int index) {
    int valueUnderTest = values.get(this.index.get(index));
    int diff = target - valueUnderTest;
    if (diff == 0) {
      return EQUAL_TO;
    } else if (diff < 0) {
      return LESS_THAN;
    } else {
      return GREATER_THAN;
    }
  }
}
