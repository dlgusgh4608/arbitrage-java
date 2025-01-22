package main.arbitrage.global.util.aes;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import main.arbitrage.global.util.aes.exception.CryptoErrorCode;
import main.arbitrage.global.util.aes.exception.CryptoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class AESCrypto {
  private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

  @Value("${mail.secret}")
  private String SECRET_KEY;

  public String encrypt(byte[] textBytes) {
    if (textBytes == null || textBytes.length == 0)
      throw new CryptoException(CryptoErrorCode.INVALID_INPUT, "암호화할 데이터가 비어있습니다.");

    try {
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

    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_ALGORITHM, "암호화 알고리즘 초기화 실패", e);
    } catch (InvalidKeyException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_KEY, "유효하지 않은 암호화 키", e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_IV, "유효하지 않은 IV 값", e);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new CryptoException(CryptoErrorCode.ENCRYPTION_FAILED, "암호화 처리 실패", e);
    } catch (NoSuchPaddingException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_PADDING, "패딩 처리 실패", e);
    } catch (Exception e) {
      throw new CryptoException(CryptoErrorCode.ENCRYPTION_FAILED, "예상치 못한 암호화 오류", e);
    }
  }

  public String decrypt(String encryptedText) {
    if (encryptedText == null || encryptedText.isEmpty())
      throw new CryptoException(CryptoErrorCode.INVALID_INPUT, "복호화할 데이터가 비어있습니다");

    try {
      byte[] combined = Base64.getDecoder().decode(encryptedText);

      if (combined.length < 16)
        throw new CryptoException(CryptoErrorCode.INVALID_INPUT, "유효하지 않은 암호화 데이터 형식");

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
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_ALGORITHM, "복호화 알고리즘 초기화 실패", e);
    } catch (InvalidKeyException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_KEY, "유효하지 않은 복호화 키", e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_IV, "유효하지 않은 IV 값", e);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new CryptoException(CryptoErrorCode.DECRYPTION_FAILED, "복호화 처리 실패", e);
    } catch (NoSuchPaddingException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_PADDING, "패딩 처리 실패", e);
    } catch (IllegalArgumentException e) {
      throw new CryptoException(CryptoErrorCode.INVALID_INPUT, "유효하지 않은 Base64 인코딩", e);
    } catch (Exception e) {
      throw new CryptoException(CryptoErrorCode.DECRYPTION_FAILED, "예상치 못한 복호화 오류", e);
    }
  }

  public boolean check(String encrypted, String decrypted) {
    return decrypt(encrypted).equals(decrypted);
  }
}
