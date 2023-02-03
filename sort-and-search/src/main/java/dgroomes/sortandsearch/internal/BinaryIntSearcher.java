package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

public final class BinaryIntSearcher extends AbstractBinarySearcher<Integer> {
  private final IntVector vector;

  public BinaryIntSearcher(IntVector vector, int target) {
    super(vector.getValueCount(), target);
    this.vector = vector;
  }

  @Override
  protected Integer lookup(int index) {
    return vector.get(index);
  }

  @Override
  protected Comparison compare(Integer target, Integer valueUnderTest) {
    return TypedComparator.INT_COMPARATOR.compare(target, valueUnderTest);
  }
}
