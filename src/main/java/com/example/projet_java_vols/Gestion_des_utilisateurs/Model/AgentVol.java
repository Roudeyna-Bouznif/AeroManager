package com.example.projet_java_vols.Gestion_des_utilisateurs.Model;

import com.example.projet_java_vols.Gestion_des_vols.Model.StatutVol;
import com.example.projet_java_vols.Gestion_des_vols.Model.Vol;

import java.util.ArrayList;

// Final car c'est une feuille de la sealed class
public final class AgentVol extends Employe {
    private String codeCompagnie;
    private String nomCompagnie;
    private String paysOrigine;
    private int flotte;
    private ArrayList<Vol> vols;

    public AgentVol(int idEmploye, String nom, String prenom, String email,
                    String telephone, String motDePasse, String codeCompagnie,
                    String nomCompagnie, String paysOrigine, int flotte) {
        super(idEmploye, nom, prenom, email, telephone, motDePasse);
        this.codeCompagnie = codeCompagnie;
        this.nomCompagnie = nomCompagnie;
        this.paysOrigine = paysOrigine;
        this.flotte = flotte;
        this.vols = new ArrayList<>();
    }

    public String getCodeCompagnie() {
        return codeCompagnie;
    }

    public void setCodeCompagnie(String codeCompagnie) {
        this.codeCompagnie = codeCompagnie;
    }

    public String getNomCompagnie() {
        return nomCompagnie;
    }

    public void setNomCompagnie(String nomCompagnie) {
        this.nomCompagnie = nomCompagnie;
    }

    public String getPaysOrigine() {
        return paysOrigine;
    }

    public void setPaysOrigine(String paysOrigine) {
        this.paysOrigine = paysOrigine;
    }

    public int getFlotte() {
        return flotte;
    }

    public void setFlotte(int flotte) {
        this.flotte = flotte;
    }

    public ArrayList<Vol> getVols() {
        return vols;
    }

    public void ajouterVol(Vol vol) {
        if (vol != null) {
            vols.add(vol);
            System.out.println("Vol " + vol.getNumVol() + " ajouté avec succès.");
        }
    }

    public void supprimerVol(String idVol) throws Exception {
        Vol volASupprimer = null;
        for (Vol v : vols) {
            if (v.getNumVol() == idVol) {
                volASupprimer = v;
                break;
            }
        }

        if (volASupprimer == null) {
            throw new Exception("Vol avec ID " + idVol + " introuvable!");
        }

        vols.remove(volASupprimer);
        System.out.println("Vol " + idVol + " supprimé avec succès.");
    }

    public void changerStatutVol(String idVol, StatutVol nouveauStatut) throws Exception {
        for (int i = 0; i < vols.size(); i++) {
            Vol v = vols.get(i);
            if (v.getNumVol() == idVol) {

                vols.set(i, v.setStatutVol(nouveauStatut));
                System.out.println("Statut du vol " + idVol + " changé en " + nouveauStatut);
                return;
            }
        }
        throw new Exception("Vol avec ID " + idVol + " introuvable!");
    }

    // STREAM + LAMBDA - chercher vol par numéro
    public Vol rechercherVolParNumero(String numeroVol) {
        return vols.stream()
                .filter(v -> v.getNumVol().equals(numeroVol))
                .findFirst()
                .orElse(null);
    }

    // STREAM + LAMBDA - filtrer vols disponibles
    public ArrayList<Vol> getVolsDisponibles() {
        return vols.stream()
                .filter(v -> v.getNbPlacesDisponibles() > 0)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    // STREAM + LAMBDA - compter total places
    public int getTotalPlaces() {
        return vols.stream()
                .mapToInt(Vol::getNbPlacesDisponibles)
                .sum();
    }

    public void afficherTousLesVols() {
        System.out.println("\n=== Vols gérés par " + getNom() + " ===");
        if (vols.isEmpty()) {
            System.out.println("Aucun vol");
        } else {
            vols.forEach(v -> System.out.println(v));
        }
    }

    @Override
    public void afficherRole() {
        System.out.println("Rôle: Agent de Vol - Compagnie: " + nomCompagnie);
    }

    @Override
    public String toString() {
        return "AgentVol{" +
                "idEmploye=" + getIdEmploye() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", compagnie='" + nomCompagnie + '\'' +
                ", nombreVols=" + vols.size() +
                '}';
    }
}