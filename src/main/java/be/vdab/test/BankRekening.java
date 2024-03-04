package be.vdab.test;


import java.math.BigDecimal;
import java.util.Optional;

record BankRekening(String bankregeningNummer, Optional<BigDecimal> saldo) {
}
