package main.arbitrage.global.util.aes;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class AESCryptoTest {

  private AESCrypto aesCrypto;

  @BeforeEach
  void setUp() {
    aesCrypto = new AESCrypto();
    ReflectionTestUtils.setField(aesCrypto, "SECRET_KEY", "testSecretKey");
  }

  @Test
  @DisplayName("암호화/복호화 테스트")
  void testConsistency() {
    // given
    String text = "helloWorld";

    // when
    String encryptStr = aesCrypto.encrypt(text.getBytes());
    String decryptStr = aesCrypto.decrypt(encryptStr);

    // then
    assertThat(text).isEqualTo(decryptStr);
    assertThat(text).isNotEqualTo(encryptStr);
  }
}
