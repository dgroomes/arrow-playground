package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

public class BinaryIntSearcher extends AbstractBinarySearcher<Integer> {
  private final IntVector vector;
  private final int target;

  public BinaryIntSearcher(IntVector vector, int target) {
    super(vector.getValueCount());
    this.vector = vector;
    this.target = target;
  }

  @Override
  Integer lookup(int index) {
    return vector.get(index);
  }

  @Override
  int compare(Integer valueUnderTest) {
    return target - valueUnderTest;
  }
}
