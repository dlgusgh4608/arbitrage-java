package main.arbitrage.global.util.aes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AESCryptoTest {

    private AESCrypto aesCrypto;

    @BeforeEach
    void setUp() {
        aesCrypto = new AESCrypto();
        ReflectionTestUtils.setField(aesCrypto, "SECRET_KEY", "testSecretKey");
    }

    @Test
    @DisplayName("암호화/복호화 테스트")
    void testConsistency() throws Exception {
        // given
        String text = "helloWorld";

        // when
        String encryptStr = aesCrypto.encrypt(text);
        String[] encryptStrs = encryptStr.split(":");

        String encryptedStr = encryptStrs[0];
        String originStr = encryptStrs[1];

        String decryptStr = aesCrypto.decrypt(encryptedStr);


        // then
        assertThat(text).isEqualTo(decryptStr);
        assertThat(text).isEqualTo(originStr);
        assertThat(text).isNotEqualTo(encryptStr);
        assertThat(text).isNotEqualTo(encryptedStr);
    }
}
