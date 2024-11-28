package main.arbitrage.infrastructure.email.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailMessageDto {
    private String to;
    private String subject;
    private String message;
}