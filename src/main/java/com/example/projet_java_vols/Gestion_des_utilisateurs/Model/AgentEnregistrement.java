package com.example.projet_java_vols.Gestion_des_utilisateurs.Model;

import com.example.projet_java_vols.Gestion_des_vols.Model.Vol;

import java.util.ArrayList;


public final class AgentEnregistrement extends Employe {
    private String bureauEnregistrement;
    private int comptoir;

    public AgentEnregistrement(int idEmploye, String nom, String prenom, String email,
                               String telephone, String motDePasse,
                               String bureauEnregistrement, int comptoir) {
        super(idEmploye, nom, prenom, email, telephone, motDePasse);
        this.bureauEnregistrement = bureauEnregistrement;
        this.comptoir = comptoir;
    }

    public String getBureauEnregistrement() {
        return bureauEnregistrement;
    }

    public void setBureauEnregistrement(String bureauEnregistrement) {
        this.bureauEnregistrement = bureauEnregistrement;
    }

    public int getComptoir() {
        return comptoir;
    }

    public void setComptoir(int comptoir) {
        this.comptoir = comptoir;
    }

    public void reserverVol(Vol vol, int numeroPasseport, int numeroPassager) throws Exception {
        if (vol.getNbPlacesDisponibles() <= 0) {
            throw new Exception("Aucune place disponible sur le vol " + vol.getNumVol());
        }

        System.out.println("Réservation effectuée par " + getNom() +
                " pour le vol " + vol.getNumVol() +
                " - Passeport: " + numeroPasseport +
                ", Passager: " + numeroPassager);
    }

    public void annulerReservation(int idReservation) throws Exception {
        System.out.println("Réservation #" + idReservation + " annulée par " + getNom());
    }

    @Override
    public void afficherRole() {
        System.out.println("Rôle: Agent d'Enregistrement - Bureau: " + bureauEnregistrement + ", Comptoir: " + comptoir);
    }

    @Override
    public String toString() {
        return "AgentEnregistrement{" +
                "idEmploye=" + getIdEmploye() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", bureau='" + bureauEnregistrement + '\'' +
                ", comptoir=" + comptoir +
                '}';
    }
}