package brave.internal;

import brave.propagation.TraceContext;

// code originally imported from zipkin.Util
public final class HexCodec {

  /**
   * Parses a 1 to 32 character lower-hex string with no prefix into an unsigned long, tossing any
   * bits higher than 64.
   */
  public static long lowerHexToUnsignedLong(CharSequence lowerHex) {
    int length = lowerHex.length();
    if (length < 1 || length > 32) throw isntLowerHexLong(lowerHex);

    // trim off any high bits
    int beginIndex = length > 16 ? length - 16 : 0;

    return lowerHexToUnsignedLong(lowerHex, beginIndex);
  }

  /**
   * Parses a 16 character lower-hex string with no prefix into an unsigned long, starting at the
   * specified index.
   */
  public static long lowerHexToUnsignedLong(CharSequence lowerHex, int index) {
    int endIndex = Math.min(index + 16, lowerHex.length());
    long result = lenientLowerHexToUnsignedLong(lowerHex, index, endIndex);
    if (result == 0) throw isntLowerHexLong(lowerHex);
    return result;
  }

  /** Like {@link #lowerHexToUnsignedLong(CharSequence, int)}, but returns zero on invalid input */
  public static long lenientLowerHexToUnsignedLong(CharSequence lowerHex, int index, int endIndex) {
    long result = 0;
    while (index < endIndex) {
      char c = lowerHex.charAt(index++);
      result <<= 4;
      if (c >= '0' && c <= '9') {
        result |= c - '0';
      } else if (c >= 'a' && c <= 'f') {
        result |= c - 'a' + 10;
      } else {
        return 0;
      }
    }
    return result;
  }

  static NumberFormatException isntLowerHexLong(CharSequence lowerHex) {
    throw new NumberFormatException(
        lowerHex + " should be a 1 to 32 character lower-hex string with no prefix");
  }

  /** Returns 16 or 32 character hex string depending on if {@code high} is zero. */
  public static String toLowerHex(long high, long low) {
    char[] result = new char[high != 0 ? 32 : 16];
    int pos = 0;
    if (high != 0) {
      writeHexLong(result, pos, high);
      pos += 16;
    }
    writeHexLong(result, pos, low);
    return new String(result);
  }

  public static boolean lowerHexEqualsUnsignedLong(CharSequence lowerHex, long unsigned) {
    if (lowerHex == null || lowerHex.length() != 16) return false;
    return lowerHexEqualsUnsignedLong(lowerHex, 0, unsigned);
  }

  public static boolean lowerHexEqualsTraceId(CharSequence lowerHex, TraceContext context) {
    if (lowerHex == null) return false;
    int length = lowerHex.length();
    long high = context.traceIdHigh(), low = context.traceId();

    int pos = 0;
    if (length == 32) {
      if (!lowerHexEqualsUnsignedLong(lowerHex, pos, high)) return false;
      pos += 16;
    } else if (length != 16 || high != 0L) {
      return false;
    }
    return lowerHexEqualsUnsignedLong(lowerHex, pos, low);
  }

  /** Inspired by {@code okio.Buffer.writeLong} */
  public static String toLowerHex(long v) {
    char[] data = new char[16];
    writeHexLong(data, 0, v);
    return new String(data);
  }

  /** Inspired by {@code okio.Buffer.writeLong} */
  public static void writeHexLong(char[] data, int pos, long v) {
    writeHexByte(data, pos + 0, (byte) ((v >>> 56L) & 0xff));
    writeHexByte(data, pos + 2, (byte) ((v >>> 48L) & 0xff));
    writeHexByte(data, pos + 4, (byte) ((v >>> 40L) & 0xff));
    writeHexByte(data, pos + 6, (byte) ((v >>> 32L) & 0xff));
    writeHexByte(data, pos + 8, (byte) ((v >>> 24L) & 0xff));
    writeHexByte(data, pos + 10, (byte) ((v >>> 16L) & 0xff));
    writeHexByte(data, pos + 12, (byte) ((v >>> 8L) & 0xff));
    writeHexByte(data, pos + 14, (byte) (v & 0xff));
  }

  static final char[] HEX_DIGITS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  public static void writeHexByte(char[] data, int pos, byte b) {
    data[pos + 0] = HEX_DIGITS[(b >> 4) & 0xf];
    data[pos + 1] = HEX_DIGITS[b & 0xf];
  }

  static boolean lowerHexEqualsUnsignedLong(CharSequence data, int pos, long v) {
    if (!hexEqualsByte(data, pos + 0, (byte) ((v >>> 56L) & 0xff))) return false;
    if (!hexEqualsByte(data, pos + 2, (byte) ((v >>> 48L) & 0xff))) return false;
    if (!hexEqualsByte(data, pos + 4, (byte) ((v >>> 40L) & 0xff))) return false;
    if (!hexEqualsByte(data, pos + 6, (byte) ((v >>> 32L) & 0xff))) return false;
    if (!hexEqualsByte(data, pos + 8, (byte) ((v >>> 24L) & 0xff))) return false;
    if (!hexEqualsByte(data, pos + 10, (byte) ((v >>> 16L) & 0xff))) return false;
    if (!hexEqualsByte(data, pos + 12, (byte) ((v >>> 8L) & 0xff))) return false;
    return hexEqualsByte(data, pos + 14, (byte) (v & 0xff));
  }

  static boolean hexEqualsByte(CharSequence data, int pos, byte b) {
    return data.charAt(pos + 0) == HEX_DIGITS[(b >> 4) & 0xf] &&
        data.charAt(pos + 1) == HEX_DIGITS[b & 0xf];
  }

  HexCodec() {
  }
}
