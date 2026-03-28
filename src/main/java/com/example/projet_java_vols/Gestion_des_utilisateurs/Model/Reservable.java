/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.projet_java_vols.Gestion_des_utilisateurs.Model;



@FunctionalInterface
public interface Reservable {
    double calculer(double prixBase, String classe);
}