package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

/**
 * This algorithm uses binary search to iteratively break down and search ranges of the input vector. Java's sealed
 * classes, record classes, and pattern matching for switch (a language preview feature) are featured in the
 * implementation.
 */
public class BinaryRangeIntSearcher extends AbstractBinaryRangeSearcher<Integer> {

  private final IntVector vector;

  public BinaryRangeIntSearcher(IntVector vector, int target) {
    super(vector, target);
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
