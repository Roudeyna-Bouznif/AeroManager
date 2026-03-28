package com.example.projet_java_vols.Gestion_des_vols.Model;

public class UserSession {
    private static int userId;
    private static String roleActuel;
    private static String nomUtilisateur;



    public static void login( int Id,String role, String nom) {
        userId = Id;
        roleActuel = role;
        nomUtilisateur = nom;
    }
    public static int getUserId() {
        return userId;
    }
    public static String getRole() {
        return roleActuel;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleActuel) || "ADMINISTRATEUR".equalsIgnoreCase(roleActuel);
    }

    public static void logout() {
        userId = 0;
        roleActuel = null;
        nomUtilisateur = null;
    }
}
