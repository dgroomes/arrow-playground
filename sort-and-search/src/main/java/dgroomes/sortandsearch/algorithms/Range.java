package dgroomes.sortandsearch.algorithms;

sealed public interface Range {

  int low();

  int high();

  default boolean contains(Range range) {
    assert range != null;

    if (range.low() < this.low()) {
      return false;
    }

    return range.high() <= this.high();
  }

  static Range of(int low, int high) {
    assert high >= low;

    var diff = high - low;
    if (diff == 0) {
      return new Point(low);
    }

    if (diff == 1) {
      return new PointPair(low, high);
    }

    return new StretchRange(low, high);
  }

  /**
   * This method extends the given range to include the given index. It is an error to call this method with an index
   * that is already contained in the range. This method does not re-use existing methods.
   */
  static Range extend(Range range, int index) {
    if (range == null) {
      return new Point(index);
    }

    if (index < range.low()) {
      // Extend to the left.
      return Range.of(index, range.high());
    } else if (index > range.high()) {
      // Extend to the right.
      return Range.of(range.low(), index);
    } else {
      throw new IllegalArgumentException("The index %d is already contained in the range %s".formatted(index, range));
    }
  }


  record Point(int index) implements Range {
    @Override
    public int low() {
      return index;
    }

    @Override
    public int high() {
      return index;
    }
  }

  record PointPair(int left, int right) implements Range {
    @Override
    public int low() {
      return left;
    }

    @Override
    public int high() {
      return right;
    }
  }

  /**
   * A range covering at least three indices. For example: "(1,3)".
   *
   * @param low
   * @param high
   */
  record StretchRange(int low, int high) implements Range {
    public StretchRange {
      var diff = high - low;
      if (diff < 2) {
        throw new IllegalArgumentException("The range must stretch at least three indices from its low-valued index to its higher-valued index:" + this);
      }
    }
  }
}
