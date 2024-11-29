package main.arbitrage.infrastructure.oauthValidator.google;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleValidatorTest {

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GoogleApiClient googleApiClient;

    private static final String TEST_ACCESS_TOKEN = "accessToken";
    private static final String TEST_SUB = "oAuthUserProviderId";
    private static final String TEST_EMAIL = "userEmail";

    @Test
    @DisplayName("Google AccessToken을 이용한 User검증 성공")
    void validatorSuccess() throws Exception {
        // given
        GoogleUserInfoDto expectedUserInfo = new GoogleUserInfoDto();
        expectedUserInfo.setSub(TEST_SUB);
        expectedUserInfo.setEmail(TEST_EMAIL);

        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("{}");
        when(objectMapper.readValue("{}", GoogleUserInfoDto.class)).thenReturn(expectedUserInfo);

        // when
        GoogleUserInfoDto result = googleApiClient.validateTokenAndGetUserInfo(TEST_ACCESS_TOKEN);

        System.out.println(TEST_EMAIL);
        System.out.println(TEST_SUB);
        System.out.println(result.getSub());
        System.out.println(result.getEmail());

        // then
        assertThat(result.getSub()).isEqualTo(TEST_SUB);
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Google API 응답 실패")
    void validateTokenAndGetUserInfo_실패_응답실패() throws Exception {
        // given
        Call call = mock(Call.class);
        Response response = mock(Response.class);

        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> googleApiClient.validateTokenAndGetUserInfo(TEST_ACCESS_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failure Google API request");
    }
}