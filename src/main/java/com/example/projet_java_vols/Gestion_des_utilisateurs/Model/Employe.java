package com.example.projet_java_vols.Gestion_des_utilisateurs.Model;

// Sealed class - seuls AgentVol et AgentEnregistrement peuvent hériter
public abstract sealed class Employe permits AgentVol, AgentEnregistrement {
    private int idEmploye;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String motDePasse;
    private boolean actif;

    public Employe(int idEmploye, String nom, String prenom, String email,
                   String telephone, String motDePasse) {
        this.idEmploye = idEmploye;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.motDePasse = motDePasse;
        this.actif = true;
    }

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void modifierProfil(String nom, String prenom, String email, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        System.out.println("Profil modifié pour " + nom + " " + prenom);
    }

    public boolean connecter(int id, String motDePasse) {
        if (this.idEmploye == id && this.motDePasse.equals(motDePasse)) {
            System.out.println("Connexion réussie pour " + nom + " " + prenom);
            return true;
        }
        System.out.println("Échec de connexion - ID ou mot de passe incorrect");
        return false;
    }

    public void deconnecter() {
        System.out.println("Employé " + nom + " " + prenom + " déconnecté.");
    }

    // Méthode abstraite que les classes filles doivent implémenter
    public abstract void afficherRole();

    @Override
    public String toString() {
        return "Employe{" +
                "idEmploye=" + idEmploye +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", actif=" + actif +
                '}';
    }
}
