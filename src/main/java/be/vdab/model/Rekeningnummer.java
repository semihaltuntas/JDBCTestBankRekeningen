package be.vdab.model;

import be.vdab.exceptions.RekeningNummerException;

public class Rekeningnummer {
    private String rekeningnummer;

    public Rekeningnummer(String rekeningnummer) throws RekeningNummerException {
        setRekeningnummer(rekeningnummer);
        this.rekeningnummer = rekeningnummer;
    }

    public String getRekeningnummer() {
        return rekeningnummer;
    }

    public void setRekeningnummer(String rekeningnummer) throws RekeningNummerException {
        if (rekeningnummer.length() != 16 || !rekeningnummer.substring(0, 2).equals("BE")) {
            throw new RekeningNummerException("Ooops Ongeldige rekening nummer! De lengte van rekeningnummer moet 16 zijn en beginnen met 'BE'!");
        }

        int controleGetal = Integer.parseInt(rekeningnummer.substring(2, 4));
        if (controleGetal < 2 || controleGetal > 98) {
            throw new RekeningNummerException("Ongeldig controle Getal!");
        }
        String validatieString = rekeningnummer.substring(4) + "1114" + controleGetal;
        long resultVanModulo = Long.parseLong(validatieString) % 97;

        if (resultVanModulo != 1) {
            throw new RekeningNummerException("Ongeldig modulo!");
        }
    }
}
