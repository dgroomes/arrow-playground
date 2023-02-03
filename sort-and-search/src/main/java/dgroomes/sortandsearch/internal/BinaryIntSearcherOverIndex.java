package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

public class BinaryIntSearcherOverIndex extends AbstractBinarySearcher<Integer> {
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
  Integer lookup(int indexOfIndex) {
    int indexOfValue = this.index.get(indexOfIndex);
    return values.get(indexOfValue);
  }

  @Override
  int compare(Integer valueUnderTest) {
    return target - valueUnderTest;
  }
}
