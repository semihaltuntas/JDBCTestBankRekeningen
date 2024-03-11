package be.vdab.exceptions;

import java.sql.SQLException;

public class OverschrijvingException extends SQLException {
    public OverschrijvingException(String reason) {
        super(reason);
    }
}
