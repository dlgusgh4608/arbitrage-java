package main.arbitrage.global.util.aes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class AESCrypto {
  private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

  @Value("${mail.secret}")
  private String SECRET_KEY;

  public String encrypt(byte[] textBytes) throws Exception {
    // 키 생성
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] keyBytes = digest.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

    // IV 생성 (16바이트)
    byte[] iv = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(iv);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    // 암호화
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
    byte[] encrypted = cipher.doFinal(textBytes);

    // IV와 암호화된 데이터 결합
    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

    return Base64.getEncoder().encodeToString(combined);
  }

  public String decrypt(String encryptedText) throws Exception {
    byte[] combined = Base64.getDecoder().decode(encryptedText);

    // IV 추출
    byte[] iv = new byte[16];
    System.arraycopy(combined, 0, iv, 0, iv.length);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    // 암호화된 데이터 추출
    byte[] encrypted = new byte[combined.length - iv.length];
    System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

    // 키 생성
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] keyBytes = digest.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

    // 복호화
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
    byte[] decrypted = cipher.doFinal(encrypted);

    return new String(decrypted);
  }
}
