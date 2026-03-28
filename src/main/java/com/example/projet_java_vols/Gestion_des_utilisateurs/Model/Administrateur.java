package com.example.projet_java_vols.Gestion_des_utilisateurs.Model;

import java.util.Map;
import java.util.HashMap;

public class Administrateur {
    private String nom;
    private String prenom;
    private String motDePasse;
    private Map<String, Employe> employes;

    public Administrateur(String nom, String prenom, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.motDePasse = motDePasse;
        this.employes = new HashMap<>();
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Map<String, Employe> getEmployes() {
        return employes;
    }

    public void ajouterEmploye(Employe employe) throws Exception {
        if (employe == null) {
            throw new Exception("Employé ne peut pas être null!");
        }
        if (employes.containsKey(String.valueOf(employe.getIdEmploye()))) {
            throw new Exception("Un employé avec cet ID existe déjà!");
        }
        employes.put(String.valueOf(employe.getIdEmploye()), employe);
        System.out.println("Employé " + employe.getNom() + " " + employe.getPrenom() + " ajouté avec succès.");
    }

    public void supprimerEmploye(int idEmploye) throws Exception {
        if (!employes.containsKey(String.valueOf(idEmploye))) {
            throw new Exception("Employé avec ID " + idEmploye + " introuvable!");
        }
        Employe employe = employes.remove(String.valueOf(idEmploye));
        System.out.println("Employé " + employe.getNom() + " " + employe.getPrenom() + " supprimé avec succès.");
    }

    public void activerEmploye(int idEmploye) throws Exception {
        Employe employe = employes.get(String.valueOf(idEmploye));
        if (employe == null) {
            throw new Exception("Employé avec ID " + idEmploye + " introuvable!");
        }
        employe.setActif(true);
        System.out.println("Employé " + employe.getNom() + " activé.");
    }

    public void desactiverEmploye(int idEmploye) throws Exception {
        Employe employe = employes.get(String.valueOf(idEmploye));
        if (employe == null) {
            throw new Exception("Employé avec ID " + idEmploye + " introuvable!");
        }
        employe.setActif(false);
        System.out.println("Employé " + employe.getNom() + " désactivé.");
    }

    // STREAM + LAMBDA - compter employés actifs
    public long compterEmployesActifs() {
        return employes.values().stream()
                .filter(Employe::isActif)
                .count();
    }

    // STREAM + LAMBDA - afficher employés avec forEach
    public void afficherTousLesEmployes() {
        System.out.println("\n=== Liste des employés ===");
        if (employes.isEmpty()) {
            System.out.println("Aucun employé");
        } else {
            employes.values().forEach(e -> {
                System.out.println(e);
                e.afficherRole();
            });
        }
    }

    @Override
    public String toString() {
        return "Administrateur{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", nombreEmployes=" + employes.size() +
                '}';
    }
}