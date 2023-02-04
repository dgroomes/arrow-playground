package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;


public final class BinaryRangeIntSearcherOverIndex extends AbstractBinaryRangeSearcher<Integer> {

  private final IntVector values;
  private final IntVector index;

  public BinaryRangeIntSearcherOverIndex(IntVector values, IntVector index, int target) {
    super(values, target);
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
