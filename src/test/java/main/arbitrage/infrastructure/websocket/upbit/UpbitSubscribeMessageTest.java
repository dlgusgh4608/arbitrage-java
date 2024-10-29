package main.arbitrage.infrastructure.websocket.upbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpbitSubscribeMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createSubscribeMessage_ShouldCreateValidFormat() throws Exception {
        // given
        String[] symbols = {"btc"};
        String uniqueTicket = "test_ticket";

        // when
        List<UpbitSubscribeMessage> messages = UpbitSubscribeMessage.createSubscribeMessage(uniqueTicket, symbols);
        String jsonString = objectMapper.writeValueAsString(messages);

        // then
        assertThat(messages).hasSize(2);
        assertThat(jsonString).contains("test_ticket");
        assertThat(jsonString).contains("trade");
        assertThat(jsonString).contains("KRW-BTC");
    }

    @Test
    void formatSymbols_ShouldFormatCorrectly() {
        // given
        String[] symbols = {"btc", "eth"};
        String uniqueTicket = "test_ticket";

        // when
        List<UpbitSubscribeMessage> messages = UpbitSubscribeMessage.createSubscribeMessage(uniqueTicket, symbols);

        // then
        UpbitSubscribeMessage tradeMessage = messages.get(1);
        assertThat(tradeMessage.getCodes()).containsExactly("KRW-BTC", "KRW-ETH");
    }
}