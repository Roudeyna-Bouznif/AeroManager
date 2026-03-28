package com.example.projet_java_vols.Gestion_des_vols.Model;

import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Reservation;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalTime;

public final class VolInternational extends Vol {
    private String numeroAutorisationInternationale;
    private boolean exigenceVisa;


    public VolInternational(String numVol, int nbPlacesDisponibles, double prixVol, StatutVol statutVol, String paysDestination,
                            ArrayList<Escale> listeEscales, HashMap<Integer, Reservation> reservations, Date dateArrivee,
                            Date dateDepart, LocalTime heureArrivee, Aeroport aeroportArrivee, LocalTime heureDepart, Aeroport aeroportDepart,
                            String numeroAutorisationInternationale, boolean exigenceVisa) {
        super(numVol, nbPlacesDisponibles, prixVol, statutVol, paysDestination, listeEscales, reservations, dateArrivee,
                dateDepart, heureArrivee, aeroportArrivee, heureDepart, aeroportDepart);
        this.numeroAutorisationInternationale = numeroAutorisationInternationale;
        this.exigenceVisa = exigenceVisa;
    }


    public VolInternational(
            String numVol, int nbPlacesDisponibles, double prixBase, double prixVol, StatutVol statutVol,
            String paysDestination, ArrayList<Escale> listeEscales, HashMap<Integer, Reservation> reservations,
            Date dateArrivee, Date dateDepart, LocalTime heureArrivee, Aeroport aeroportArrivee,
            LocalTime heureDepart, Aeroport aeroportDepart,
            String numeroAutorisationInternationale, boolean exigenceVisa
    ) {
        super(numVol, nbPlacesDisponibles, prixBase, prixVol, statutVol, paysDestination, listeEscales, reservations,
                dateArrivee, dateDepart, heureArrivee, aeroportArrivee, heureDepart, aeroportDepart);
        this.numeroAutorisationInternationale = numeroAutorisationInternationale;
        this.exigenceVisa = exigenceVisa;
    }

    public double tarifCompletAvecTaxesEtVisa() {
        CalculateurVol calc = v -> v.getPrixVol() * 1.15 + calculerTaxesInternationales() + calculerFraisVisa();
        return appliquerCalcul(calc);
    }


    @Override
    public double calculerTarifSpecifique() {
        return prixVol * 1.15;
    }

    @Override
    public double calculerTaxesInternationales() {
        return prixVol * 0.20;
    }

    @Override
    public double calculerFraisVisa() {
        return exigenceVisa ? 50.0 : 0.0;
    }

    public String getNumeroAutorisationInternationale() {
        return numeroAutorisationInternationale;
    }

    public boolean isExigenceVisa() {
        return exigenceVisa;
    }
}
