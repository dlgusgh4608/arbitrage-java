package main.arbitrage.domain.symbol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "symbol")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Symbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symbol_id", nullable = false)
    private Long symbolId;

    @Column(name = "symbol", nullable = false)
    private String name;

    @Column(name = "use", nullable = false)
    private boolean use;

    @Builder
    public Symbol(String name, boolean use) {
        this.name = name;
        this.use = use;
    }
}