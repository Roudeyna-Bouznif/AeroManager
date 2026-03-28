package com.example.projet_java_vols.Gestion_des_vols.Model;

import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Reservation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public sealed abstract class Vol permits VolInternational, VolNational {

    protected String numVol;
    protected int nbPlacesDisponibles;
    protected double prixVol;
    protected StatutVol statutVol;
    protected String paysDestination;
    protected ArrayList<Escale> listeEscales;
    protected HashMap<Integer, Reservation> reservations;
    protected Date dateArrivee;
    protected Date dateDepart;
    protected LocalTime heureArrivee;
    protected LocalTime heureDepart;
    protected Aeroport aeroportArrivee;
    protected Aeroport aeroportDepart;
    protected double prixBase;

    @FunctionalInterface
    public interface CalculateurVol {
        double appliquer(Vol vol);
    }

    public Vol(String numVol, int nbPlacesDisponibles,double prixBase, double prixVol, StatutVol statutVol, String paysDestination,
               ArrayList<Escale> listeEscales, HashMap<Integer, Reservation> reservations, Date dateArrivee,
               Date dateDepart, LocalTime heureArrivee, Aeroport aeroportArrivee, LocalTime heureDepart, Aeroport aeroportDepart) {
        this(numVol, nbPlacesDisponibles,  prixBase ,statutVol, paysDestination, listeEscales, reservations, dateArrivee,
                dateDepart, heureArrivee, aeroportArrivee, heureDepart, aeroportDepart);
        this.prixVol = prixVol;
    }

    public Vol(String numVol, int nbPlacesDisponibles, double prixBase, StatutVol statutVol, String paysDestination,
               ArrayList<Escale> listeEscales, HashMap<Integer, Reservation> reservations, Date dateArrivee,
               Date dateDepart, LocalTime heureArrivee, Aeroport aeroportArrivee, LocalTime heureDepart, Aeroport aeroportDepart) {
        this.numVol = numVol;
        this.nbPlacesDisponibles = nbPlacesDisponibles;
        this.prixBase = prixBase;
        this.prixVol = prixVol;
        this.statutVol = statutVol;
        this.paysDestination = paysDestination;
        this.listeEscales = listeEscales == null ? new ArrayList<>() : listeEscales;
        this.reservations = reservations == null ? new HashMap<>() : reservations;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.heureArrivee = heureArrivee;
        this.aeroportArrivee = aeroportArrivee;
        this.heureDepart = heureDepart;
        this.aeroportDepart = aeroportDepart;
    }

    public double appliquerCalcul(CalculateurVol calc) {
        return calc.appliquer(this);
    }

    public List<Escale> filtrerEscales(Predicate<Escale> condition) {
        return listeEscales.stream().filter(condition).collect(Collectors.toList());
    }

    public long nombreEscalesVers(String nomAeroport) {
        return listeEscales.stream()
                .filter(e -> e.getAeroportArrivee()!=null && nomAeroport.equals(e.getAeroportArrivee().getNom()))
                .count();
    }

    public List<Date> datesEscalesTriees() {
        return listeEscales.stream()
                .map(Escale::getDateDepart)
                .sorted()
                .collect(Collectors.toList());
    }

    public String getNumVol() { return numVol; }
    public int getNbPlacesDisponibles() { return nbPlacesDisponibles; }
    public double getPrixVol() { return prixVol; }
    public StatutVol getStatutVol() { return statutVol; }
    public String getPaysDestination() { return paysDestination; }
    public ArrayList<Escale> getListeEscales() { return listeEscales; }
    public HashMap<Integer, Reservation> getReservations() { return reservations; }
    public Date getDateArrivee() { return dateArrivee; }
    public Date getDateDepart() { return dateDepart; }
    public LocalTime getHeureArrivee() { return heureArrivee; }
    public LocalTime getHeureDepart() { return heureDepart; }
    public Aeroport getAeroportArrivee() { return aeroportArrivee; }
    public Aeroport getAeroportDepart() { return aeroportDepart; }

    public void ajouterEscale(Escale escale) { listeEscales.add(escale); }
    public void supprimerEscale(int idEscale) throws Exception {}

    public boolean verifierDisponibilite(int nbPlaces) {
        return this.nbPlacesDisponibles - nbPlaces > 0;
    }

    public void reserver(int numPass, Reservation reservation) throws Exception {
        if (nbPlacesDisponibles <= 0)
            throw new Exception("Aucune place disponible sur ce vol");
        if (reservations.containsKey(reservation.getIdReservation()))
            throw new Exception("Réservation #" + reservation.getIdReservation() + " existe déjà");
        reservations.put(reservation.getIdReservation(), reservation);
    }

    public Reservation obtenirReservation(int numero) throws Exception {
        Reservation res = reservations.get(numero);
        if (res == null)
            throw new Exception("Réservation #" + numero + " introuvable");
        return res;
    }

    public void annuler(int numeroReservation) throws Exception {}
    public void listerReservationsVol() {}

    public abstract double calculerTarifSpecifique();
    public abstract double calculerTaxesInternationales();
    public abstract double calculerFraisVisa();

    @Override
    public String toString() {
        return numVol;
    }
    public double getPrixBase() { return prixBase; }
    public void setPrixBase(double prixBase) { this.prixBase = prixBase;}

    public Vol setStatutVol(StatutVol statutVol) {
        this.statutVol = statutVol;
        return null;
    }
}
