package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;

/**
 * This algorithm uses binary search to iteratively break down and search ranges of the input vector. Java's sealed
 * classes, record classes, and pattern matching for switch (a language preview feature) are featured in the
 * implementation.
 */
public class BinaryRangeIntSearcher extends AbstractBinaryRangeSearcher<Integer> {

  private final IntVector intVector;
  private final int target;

  public BinaryRangeIntSearcher(IntVector vector, int target) {
    super(vector);
    this.intVector = vector;
    this.target = target;
  }

  @Override
  Integer lookup(int index) {
    return intVector.get(index);
  }

  @Override
  int compare(Integer valueUnderTest) {
    return target - valueUnderTest;
  }
}
