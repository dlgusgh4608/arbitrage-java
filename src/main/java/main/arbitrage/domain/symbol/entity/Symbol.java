package main.arbitrage.domain.symbol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import main.arbitrage.domain.symbolPrice.entity.SymbolPrice;

@Entity
@Table(name = "symbol")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Symbol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long symbol_id;

    @Column(name = "name", nullable = false, length = 5)
    private String name;

    @Column(name = "domestic", nullable = false)
    private String domestic;

    @Column(name = "overseas", nullable = false)
    private String overseas;

    @OneToMany(mappedBy = "symbol", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SymbolPrice> symbolPrices = new ArrayList<>();
}