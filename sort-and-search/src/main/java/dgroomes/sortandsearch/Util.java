package dgroomes.sortandsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Util {

  private static final Logger log = LoggerFactory.getLogger(Util.class);

  /**
   * Benchmark a function.
   *
   * @param name        a descriptive name for the benchmark
   * @param benchmarkFn the function to benchmark
   * @param times       the number of times to repeat the benchmark function
   */
  public static void benchmark(String name, Supplier<String> benchmarkFn, int times) {
    Instant start = Instant.now();

    // Repeat the benchmark function the specified number of times, and hash the result each time.
    byte[] resultHashBytes = IntStream.range(0, times).mapToObj(i -> benchmarkFn.get()).map(String::getBytes).reduce(new byte[]{}, (a, b) -> {
      byte[] concatenated = concat(a, b);
      return hash(concatenated);
    });

    Instant end = Instant.now();

    String hashString = bytesToAbbreviatedHexString(resultHashBytes);
    Duration elapsed = Duration.between(start, end);
    log.info("Benchmark '{}' evaluated {} times, produced a result hash of '{}', and took {}", name, formatInteger(times), hashString.substring(0, 8), elapsed);
  }

  /**
   * Concatenates two byte arrays.
   */
  public static byte[] concat(byte[] a, byte[] b) {
    byte[] concatenated = new byte[a.length + b.length];
    System.arraycopy(a, 0, concatenated, 0, a.length);
    System.arraycopy(b, 0, concatenated, a.length, b.length);
    return concatenated;
  }

  /**
   * Hash some bytes using SHA-256.
   */
  public static byte[] hash(byte[] bytes) {
    return digest.digest(bytes);
  }

  /**
   * Formats an integer value with commas.
   * <p>
   * For example, 1234567 becomes "1,234,567".
   */
  public static String formatInteger(int value) {
    return NumberFormat.getNumberInstance(Locale.US).format(value);
  }

  /**
   * Convert some bytes to an abbreviated hex string.
   *
   * @param bytes a byte array
   * @return an abbreviated hex string, for example: "e9747909"
   */
  public static String bytesToAbbreviatedHexString(byte[] bytes) {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < bytes.length && i < 8; i++) {
      byte b = bytes[i];
      var hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) builder.append('0');
      builder.append(hex);
    }
    return builder.toString();
  }

  private final static MessageDigest digest;

  static {
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}

