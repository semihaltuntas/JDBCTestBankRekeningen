package be.vdab.test;

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
                    System.out.println("Nieuwe rekening : ");
                    String newRekeningNummer = scanner.nextLine();
                    System.out.println("Eerste saldo (Optional) : ");
                    BigDecimal beginBalance = scanner.nextBigDecimal();

                    BankRekening newRekening = new BankRekening(newRekeningNummer, Optional.ofNullable(beginBalance));

                    try {
                        repository.addNieuweRekening(newRekening);
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                case 2 -> {
                    System.out.println("Type Rekening Nummer: ");
                    String rekeningNummer2 = scanner.nextLine();

                    try {
                        Optional<BigDecimal> saldo = repository.getSaldo(rekeningNummer2);
                        System.out.println("Saldo: " + saldo.get());
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                case 3 -> {
                    System.out.println("Van Rekening Nummer: ");
                    String vanNummer = scanner.nextLine();
                    System.out.println("Naar Rekening Nummer: ");
                    String naarNummer = scanner.nextLine();
                    System.out.println("Bedrag :");
                    BigDecimal bedrag = scanner.nextBigDecimal();

                    try {
                        if (repository.overschrijven(vanNummer, naarNummer, bedrag)){
                            System.out.println("De geldoverdracht is succesvol uitgevoerd.");
                        }else{
                            System.out.println("Helaas,opnieuw probeer!");
                        }

                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                case 4 -> {
                    System.out.println("het programma afsluiten");
                }

                default -> System.out.println("Ongeldige verkiezing");
            }
        }
    }
}