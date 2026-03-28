package com.example.projet_java_vols.Gestion_des_utilisateurs.Model;

public class Reservation {
    private int idReservation;
    private String numVol;
    private int passeport_pasasager;
    private double prixTotal;
    private ClasseVol classe;
    private Reservable calculateur;

    public Reservation(int numeroReservation, double prixBase, ClasseVol classe, int passeport_pasasager ,String numVol) {
        this.idReservation = numeroReservation;
        this.classe = classe;
        this.passeport_pasasager = passeport_pasasager;
this.numVol=numVol;

        this.calculateur = (base, c) -> {
            switch (c) {
                case "Eco":      return base;
                case "Business": return base * 1.5;
                case "First":    return base * 2;
                default:         return base;
            }
        };


        this.prixTotal = calculateur.calculer(prixBase, classe.name());
    }

    public int getIdReservation() {
        return idReservation;
    }

    public int getPasseport_pasasager() {
        return passeport_pasasager;
    }


    public double getPrixTotal() {
        return prixTotal;
    }

    public ClasseVol getClasse() {
        return classe;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }



    public void envoyerConfirmation() {
        System.out.println("Confirmation envoyée pour la réservation #" + idReservation);
    }

    public String getNumVol() {
        return numVol;
    }


    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", passeport_pasasager=" + passeport_pasasager +
                ", prixTotal=" + prixTotal +
                ", classe=" + classe +
                '}';
    }
}
