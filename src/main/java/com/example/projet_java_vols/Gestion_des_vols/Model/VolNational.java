package com.example.projet_java_vols.Gestion_des_vols.Model;

import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Reservation;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalTime;

public final class VolNational extends Vol {
    private String numeroAutorisationNationale;
    private String terminalDomestique;

    public VolNational(String numVol, int nbPlacesDisponibles, double prixVol, StatutVol statutVol, String paysDestination,
                       ArrayList<Escale> listeEscales, HashMap<Integer, Reservation> reservations, Date dateArrivee,
                       Date dateDepart, LocalTime heureArrivee, Aeroport aeroportArrivee, LocalTime heureDepart, Aeroport aeroportDepart,
                       String numeroAutorisationNationale, String terminal) {
        super(numVol, nbPlacesDisponibles, prixVol, statutVol, paysDestination, listeEscales, reservations, dateArrivee,
                dateDepart, heureArrivee, aeroportArrivee, heureDepart, aeroportDepart);
        this.numeroAutorisationNationale = numeroAutorisationNationale;
        this.terminalDomestique = terminal;
    }


    public VolNational(
            String numVol, int nbPlacesDisponibles, double prixBase, double prixVol, StatutVol statutVol,
            String paysDestination, ArrayList<Escale> listeEscales, HashMap<Integer, Reservation> reservations,
            Date dateArrivee, Date dateDepart, LocalTime heureArrivee, Aeroport aeroportArrivee,
            LocalTime heureDepart, Aeroport aeroportDepart,
            String numeroAutorisationNationale, String terminalDomestique
    ) {
        super(numVol, nbPlacesDisponibles, prixBase, prixVol, statutVol, paysDestination, listeEscales, reservations,
                dateArrivee, dateDepart, heureArrivee, aeroportArrivee, heureDepart, aeroportDepart);
        this.numeroAutorisationNationale = numeroAutorisationNationale;
        this.terminalDomestique = terminalDomestique;
    }

    public double tarifAvecTaxesDomestiques() {
        CalculateurVol calc = v -> v.getPrixVol() * 0.95 + calculerTaxesDomestiques();
        return appliquerCalcul(calc);
    }

    @Override
    public double calculerTarifSpecifique() {
        return prixVol * 0.95;
    }

    @Override
    public double calculerTaxesInternationales() {
        return 0;
    }

    @Override
    public double calculerFraisVisa() {
        return 0;
    }

    public double calculerTaxesDomestiques() {
        return prixVol * 0.10;
    }

    public String getTerminalDomestique() {
        return terminalDomestique;
    }

    public String getNumeroAutorisationNationale() {
        return numeroAutorisationNationale;
    }
}
