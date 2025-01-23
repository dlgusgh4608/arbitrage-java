package main.arbitrage.global.util.regex;

import java.util.UUID;
import java.util.regex.Pattern;

public final class KeyGenUtil {
  private static final String KEY_REGEX =
      "^[a-f0-9]{8}A[a-f0-9]{4}R[a-f0-9]{4}B[a-f0-9]{4}T[a-f0-9]{12}$";
  private static final Pattern pattern = Pattern.compile(KEY_REGEX);

  public static String generate() {
    String[] uuidParts = UUID.randomUUID().toString().split("-");

    /**
     * Random UUID가 "550e8400-e29b-41d4-a716-446655440000"라면 "-"를 기준으로 나눠 "-"의 자리에 각각 ARBT를 넣어 재조합
     */
    return String.format(
        "%s%s%s%s%s%s%s%s%s",
        uuidParts[0], "A", uuidParts[1], "R", uuidParts[2], "B", uuidParts[3], "T", uuidParts[4]);
  }

  public static boolean validate(String key) {
    return pattern.matcher(key).matches();
  }
}
