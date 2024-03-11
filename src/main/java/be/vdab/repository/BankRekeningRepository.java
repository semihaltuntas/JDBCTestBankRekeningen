package be.vdab.repository;

import be.vdab.exceptions.OnvoldoendeSaldoException;
import be.vdab.exceptions.OverschrijvingException;
import be.vdab.exceptions.RekeningBestaatAlException;
import be.vdab.exceptions.RekeningNietGevondenException;
import be.vdab.model.BankRekening;
import be.vdab.model.Rekeningnummer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BankRekeningRepository extends AbstractRepository {


    public void addNieuweRekening(BankRekening bankrekening) throws SQLException {
        try {
            String sql = """ 
                    insert into rekeningen(nummer, saldo) 
                    values (?,?) """;
            try (Connection connection = super.getConnection()) {
                try (PreparedStatement statementInsert = connection.prepareStatement(sql)) {
                    statementInsert.setString(1, bankrekening.bankregeningNummer());
                    if (bankrekening.saldo().isPresent()) {
                        statementInsert.setBigDecimal(2, bankrekening.saldo().get());
                    }

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
                            throw new RekeningBestaatAlException("Rekening bestaat al.");
                        }
                        connection.rollback();
                    } catch (RekeningBestaatAlException e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
        }
    }


    public Optional<BigDecimal> getSaldo(Rekeningnummer bankrekeningNummer) throws SQLException, RekeningNietGevondenException {

        String selectSql = """
                select saldo 
                from rekeningen
                where nummer = ?
                """;
        try (Connection connection = super.getConnection(); PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, bankrekeningNummer.getRekeningnummer());
            ResultSet result = selectStatement.executeQuery();
            if (result.next()){
               return Optional.of(result.getBigDecimal("saldo"));
            }else{
                throw new RekeningNietGevondenException("Rekening niet gevonden");
            }
        }
    }

    public boolean overschrijven(String vanNummer, String naarNummer, BigDecimal bedrag)
            throws OverschrijvingException, OnvoldoendeSaldoException, RekeningNietGevondenException {

        String updateVanSql = """
                update rekeningen 
                set saldo = saldo - ? 
                where nummer = ? and saldo >= ?
                """;
        String updateNaarSql = """
                update rekeningen
                set saldo = saldo + ?
                where nummer = ?       
                """;
        String checkVanRekeningSql = """
                select count(*) as aantal
                from rekeningen
                where nummer = ?
                """;

        try (Connection connection = super.getConnection();
             PreparedStatement updateVanStatement = connection.prepareStatement(updateVanSql);
             PreparedStatement updateNaarStatement = connection.prepareStatement(updateNaarSql);
             PreparedStatement checkVanRekeningStatement = connection.prepareStatement(checkVanRekeningSql)) {

            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            updateVanStatement.setBigDecimal(1, bedrag);
            updateVanStatement.setString(2, vanNummer);
            updateVanStatement.setBigDecimal(3, bedrag);

            int updatedRowsVan = updateVanStatement.executeUpdate();

            // Controleer of de rekening bestaat en er voldoende saldo is
            checkVanRekeningStatement.setString(1, vanNummer);
            ResultSet result = checkVanRekeningStatement.executeQuery();
            result.next();
            int countVanRekening = result.getInt("aantal");

            if (updatedRowsVan == 1 && countVanRekening == 1) {
                // Tweede rekening bijwerken
                updateNaarStatement.setBigDecimal(1, bedrag);
                updateNaarStatement.setString(2, naarNummer);
                int updatedRowsNaar = updateNaarStatement.executeUpdate();

                if (updatedRowsNaar == 1) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            } else if (updatedRowsVan == 0 && countVanRekening == 1) {
                connection.rollback();
                throw new OnvoldoendeSaldoException("Onvoldoende saldo op de van-rekening!");
            }
            else {
                throw new RekeningNietGevondenException("Rekening niet gevonden!");
            }
        } catch (SQLException ex) {
            throw new OverschrijvingException("Er is een fout opgetreden tijdens het overschrijven.");
        }
    }
}

