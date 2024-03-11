package be.vdab;

import be.vdab.exceptions.OnvoldoendeSaldoException;
import be.vdab.exceptions.OverschrijvingException;
import be.vdab.exceptions.RekeningNietGevondenException;
import be.vdab.exceptions.RekeningNummerException;
import be.vdab.model.BankRekening;
import be.vdab.repository.BankRekeningRepository;
import be.vdab.model.Rekeningnummer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        BankRekeningRepository repository = new BankRekeningRepository();

        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice != 4) {
            System.out.println("1.Nieuwe rekening toevoegen" + "\n" + "2.Toon saldo" + "\n" + "3.Geld overschrijving" + "\n" + "4.EXİT");
            System.out.println("Maak een Keuze Aub!: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1 -> {
                    Rekeningnummer newRekeningNummer = getRekeningnummer("Nieuwe rekening :");
                    BigDecimal beginBalance = getPositiveBigDecimal("Eerste saldo (Optional) : ");
                    BankRekening newRekening = new BankRekening(newRekeningNummer.getRekeningnummer(), Optional.ofNullable(beginBalance));

                    try {
                        repository.addNieuweRekening(newRekening);
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                case 2 -> {

                    Rekeningnummer rekeningNummer2 = getRekeningnummer("Type Rekening Nummer:");

                    try {
                        Rekeningnummer bankrekeningNummer = new Rekeningnummer(rekeningNummer2.getRekeningnummer());
                        Optional<BigDecimal> saldo = repository.getSaldo(bankrekeningNummer);
                        saldo.ifPresent(bigDecimal -> System.out.println("Saldo: " + bigDecimal));

                    } catch (RekeningNietGevondenException ex) {
                        System.err.println(ex.getMessage());
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    } catch (RekeningNummerException e) {
                        e.printStackTrace(System.err);
                    }
                }
                case 3 -> {
                    Rekeningnummer vanNummer = null;
                    Rekeningnummer naarNummer = null;
                    while (true) {
                        vanNummer = getRekeningnummer("Van Rekening Nummer :");
                        naarNummer = getRekeningnummer("Naar Rekening Nummer :");
                        if (!vanNummer.getRekeningnummer().equals(naarNummer.getRekeningnummer())) {
                            break;
                        }
                        System.out.println("vanRekening en NaarRekening nummers mag niet hetzelfde zijn!");
                    }
                    BigDecimal bedrag = getPositiveBigDecimal("Bedrag: ");

                    try {
                        if (repository.overschrijven(vanNummer.getRekeningnummer(), naarNummer.getRekeningnummer(), bedrag)) {
                            System.out.println("De geldoverdracht is succesvol uitgevoerd.");
                        } else {
                            System.out.println("Helaas,opnieuw probeer!");
                        }

                    } catch (OverschrijvingException | OnvoldoendeSaldoException | RekeningNietGevondenException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                case 4 -> {
                    System.out.println("Het programma afsluiten");
                }

                default -> System.out.println("Ongeldige verkiezing");
            }
        }
    }

    private static Rekeningnummer getRekeningnummer(String message) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(message);
            try {
                return new Rekeningnummer(scanner.nextLine());
            } catch (RekeningNummerException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private static BigDecimal getPositiveBigDecimal(String message) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(message);
            try {
                BigDecimal input = scanner.nextBigDecimal();
                if (input.compareTo(BigDecimal.ZERO) > 0) {
                    return input;
                }
                System.err.println("Type positive getal!");
            } catch (Exception e) {
                scanner.nextLine();
                System.err.println("Type positive getal!");
            }
        }
    }
}