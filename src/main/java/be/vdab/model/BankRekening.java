package be.vdab.model;


import java.math.BigDecimal;
import java.util.Optional;

public record BankRekening(String bankregeningNummer, Optional<BigDecimal> saldo) {
}
