package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

import static dgroomes.sortandsearch.internal.Comparison.*;

public class BinaryIntSearcher extends AbstractBinarySearcher {
  private final IntVector vector;
  private final int target;

  public BinaryIntSearcher(IntVector vector, int target) {
    super(vector.getValueCount());
    this.vector = vector;
    this.target = target;
  }

  @Override
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
