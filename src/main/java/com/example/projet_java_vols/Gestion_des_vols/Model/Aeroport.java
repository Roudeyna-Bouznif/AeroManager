package com.example.projet_java_vols.Gestion_des_vols.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public record Aeroport(
        String  idAeroport,
        String  nom,
        String  ville,
        String  pays
) {

    public String getIdAeroport() { return idAeroport; }
    public String getNom() { return nom; }
    public String getVille() { return ville; }
    public String getPays() { return pays; }

    @Override
    public String toString() {
        return nom;
    }

}

