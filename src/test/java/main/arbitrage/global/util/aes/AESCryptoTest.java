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
        String decryptStr = aesCrypto.decrypt(encryptStr);


        // then
        assertThat(text).isEqualTo(decryptStr);
        assertThat(text).isNotEqualTo(encryptStr);
    }
}