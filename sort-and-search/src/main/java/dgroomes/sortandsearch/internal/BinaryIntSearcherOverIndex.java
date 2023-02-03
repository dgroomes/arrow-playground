package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

public class BinaryIntSearcherOverIndex extends AbstractBinarySearcher<Integer> {
  private final IntVector values;
  private final IntVector index;

  public BinaryIntSearcherOverIndex(IntVector values, IntVector index, int target) {
    super(values.getValueCount(), target);
    this.values = values;
    this.index = index;
  }

  @Override
  protected Integer lookup(int indexOfIndex) {
    int indexOfValue = this.index.get(indexOfIndex);
    return values.get(indexOfValue);
  }

  @Override
  protected Comparison compare(Integer target, Integer valueUnderTest) {
    return TypedComparator.INT_COMPARATOR.compare(target, valueUnderTest);
  }
}
