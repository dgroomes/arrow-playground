package dgroomes.sortandsearch.internal;

sealed interface Range {

  static Range of(int low, int high) {
    var diff = high - low;
    if (diff == 0) {
      return new Point(low);
    }

    if (diff == 1) {
      return new PointPair(low, high);
    }

    return new StretchRange(low, high);
  }

  record Point(int index) implements Range {}

  record PointPair(int left, int right) implements Range {}

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
