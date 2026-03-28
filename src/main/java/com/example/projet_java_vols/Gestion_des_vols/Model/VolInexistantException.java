package com.example.projet_java_vols.Gestion_des_vols.Model;

public class VolInexistantException extends Exception {
    public VolInexistantException(String message) {
        super(message);
    }
}
