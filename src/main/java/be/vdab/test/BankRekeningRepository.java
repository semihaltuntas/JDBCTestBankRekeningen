package be.vdab.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BankRekeningRepository extends AbstractRepository {


    public void addNieuweRekening(BankRekening bankrekening) throws SQLException {
        boolean isValid = isBankrekeningnummerValid(bankrekening.bankregeningNummer());
        if (!isValid) {
            throw new IllegalArgumentException("Ongeldig Bankrekening Nummer");
        }
        var sql = """ 
                insert into rekeningen(nummer) 
                values (?) """;
        try (var connection = super.getConnection()) {
            try (var statementInsert = connection.prepareStatement(sql)) {
                statementInsert.setString(1, bankrekening.bankregeningNummer());
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);
                statementInsert.executeUpdate();
                connection.commit();
                System.out.println("Dit rekeningnummer is toegevoegd aan de database.");
            } catch (SQLException ex) {
                var sqlSelect = """ 
                        select nummer 
                        from rekeningen 
                        where nummer = ? """;
                try (var statementSelect = connection.prepareStatement(sqlSelect)) {
                    statementSelect.setString(1, bankrekening.bankregeningNummer());
                    if (statementSelect.executeQuery().next()) {
                        connection.rollback();
                        System.err.println("Rekening bestaat al.");
                    }
                    connection.rollback();
                    // throw ex;
                }
            }
        }
    }
    private boolean isBankrekeningnummerValid(String nummer) {

        if (nummer.length() != 16 || !nummer.substring(0, 2).equals("BE")) {
            return false;
        }

        int controleGetal = Integer.parseInt(nummer.substring(2, 4));
        if (controleGetal < 2 || controleGetal > 98) {
            return false;
        }
        String validatieString = nummer.substring(4) + "1114" + controleGetal;
        long resultVanModulo = Long.parseLong(validatieString) % 97;

        return resultVanModulo == 1;
    }

    public Optional<BigDecimal> getSaldo(String bankrekeningNummer) throws SQLException {

        String selectSql = """
                select saldo 
                from rekeningen
                where nummer = ?
                """;
        try (Connection connection = super.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, bankrekeningNummer);
            ResultSet result = selectStatement.executeQuery();
            return result.next() ? Optional.of(result.getBigDecimal("saldo"))
                    : Optional.empty();
        }
    }

    public boolean overschrijven(String vanNummer, String naarNummer, BigDecimal bedrag) throws SQLException {

        Optional<BigDecimal> saldoVan = getSaldo(vanNummer);
        Optional<BigDecimal> saldoNaar = getSaldo(naarNummer);

        // System.out.println(saldoNaar);
        // System.out.println(saldoVan);

        if (!saldoNaar.isPresent()) {
            throw new IllegalArgumentException("De ontvangende rekening staat niet in de database.");
        }
        if (!saldoVan.isPresent()){
            throw new IllegalArgumentException("De rekening van de afzender staat niet in de database.");
        }

        if (vanNummer.equals(naarNummer)) {
            throw new IllegalArgumentException("Geen twee rekeningnummers kunnen hetzelfde zijn.");
        }
        if (bedrag.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ongeldige bedrag om te stoorten.");
        }
        if (saldoVan.get().compareTo(bedrag) < 0) {
            throw new IllegalArgumentException("Onvoldoende saldo.");
        }

        String updateSql = """
                update rekeningen 
                set saldo = ?
                where nummer = ?
                """;
        try (Connection connection = super.getConnection();
             PreparedStatement updateStatementVan = connection.prepareStatement(updateSql);
             PreparedStatement updateStatementNaar = connection.prepareStatement(updateSql)
        ) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            updateStatementVan.setBigDecimal(1, saldoVan.get().subtract(bedrag));
            updateStatementVan.setString(2, vanNummer);
            updateStatementVan.executeUpdate();

            updateStatementNaar.setBigDecimal(1, saldoNaar.get().add(bedrag));
            updateStatementNaar.setString(2, naarNummer);
            updateStatementNaar.executeUpdate();

            connection.commit();
            return true;
        }
    }
}