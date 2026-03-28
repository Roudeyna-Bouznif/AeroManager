package com.example.projet_java_vols;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnexionDB {

    // URL de connexion SQLite
    static String url = "jdbc:sqlite:gestionvols.db";

    public static Connection getConnection() {
        try {
            // Chargement explicite du driver SQLite
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur de connexion SQLite : " + e.getMessage());
        }
    }

    public static void initialiserBDD() {
        // --- Définition des structures de tables ---

        // Table EMPLOYE
        String sqlEmploye = """
            CREATE TABLE IF NOT EXISTS employe (
                idEmploye INTEGER PRIMARY KEY,
                nom TEXT NOT NULL,
                prenom TEXT,
                email TEXT,
                telephone TEXT,
                motDePasse TEXT NOT NULL,
                actif INTEGER DEFAULT 1,
                typeEmploye TEXT,
                codeCompagnie TEXT,
                nomCompagnie TEXT,
                paysOrigine TEXT,
                flotte INTEGER,
                bureauEnreg TEXT,
                comptoir INTEGER
            );
        """;

        // Table AEROPORT
        String sqlAeroport = """
            CREATE TABLE IF NOT EXISTS aeroport (
                idAeroport TEXT PRIMARY KEY,
                nom TEXT NOT NULL,
                ville TEXT NOT NULL,
                pays TEXT NOT NULL
            );
        """;

        // Table ESCALE (Définition complète et correcte)
        String sqlEscale = "CREATE TABLE IF NOT EXISTS escale (" +
                "idEscale INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ordre INTEGER NOT NULL DEFAULT 1, " +
                "numVol TEXT, " +
                "idAeroportArrivee TEXT, " +
                "idAeroportDepart TEXT, " +
                "dateArrivee DATE, " +
                "dateDepart DATE, " +
                "heureArrivee TEXT, " +
                "heureDepart TEXT, " +
                "FOREIGN KEY(numVol) REFERENCES vol(numVol) ON DELETE CASCADE, " +
                "FOREIGN KEY(idAeroportArrivee) REFERENCES aeroport(idAeroport), " +
                "FOREIGN KEY(idAeroportDepart) REFERENCES aeroport(idAeroport)" +
                ")";

        // Table VOL
        String sqlVol = """
            CREATE TABLE IF NOT EXISTS vol (
                numVol TEXT PRIMARY KEY,
                type TEXT,
                nbPlaces INTEGER,
                prixBase REAL,
                prixVol REAL,
                statut TEXT,
                paysDestination TEXT,
                dateDepart TEXT,
                dateArrivee TEXT,
                heureDepart TEXT,
                heureArrivee TEXT,
                aeroportDepart TEXT,
                aeroportArrivee TEXT,
                numeroAutorisation TEXT,
                exigenceVisa INTEGER,
                terminal TEXT
            );
        """;

        // Table RESERVATION
        String sqlReservation = """
            CREATE TABLE IF NOT EXISTS reservation (
                idReservation INTEGER PRIMARY KEY AUTOINCREMENT,
                numVol TEXT,
                passeport_pasasager INTEGER,
                prixTotal REAL,
                classeVol TEXT,
                idEmploye INTEGER
            );
        """;

        // Insertion Admin par défaut
        String sqlInsertAdmin = """
            INSERT OR IGNORE INTO employe (idEmploye, nom, prenom, motDePasse, typeEmploye, actif)
            VALUES (1, 'malek', 'Admin', '123', 'ADMIN', 1);
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Exécution pour les tables standard
            stmt.execute(sqlEmploye);
            stmt.execute(sqlAeroport);
            stmt.execute(sqlVol);
            stmt.execute(sqlReservation);
            stmt.execute(sqlInsertAdmin);

            // 2. Logique de réparation automatique pour la table ESCALE
            // On vérifie si la table est obsolète (manque de colonnes)
            boolean recréerTableEscale = false;
            try {
                // Test simple : est-ce que la colonne 'dateDepart' existe ?
                stmt.executeQuery("SELECT dateDepart FROM escale LIMIT 1");
            } catch (SQLException e) {
                // Si une exception se produit ici, c'est que la colonne n'existe pas ou la table n'existe pas
                // Dans les deux cas, on veut s'assurer qu'elle est créée correctement.
                // Si l'erreur est "no such table", le CREATE IF NOT EXISTS suffirait.
                // Si l'erreur est "no such column", il FAUT supprimer et recréer.
                if (e.getMessage().contains("no such column") || e.getMessage().contains("missing database")) {
                    recréerTableEscale = true;
                }
            }

            if (recréerTableEscale) {
                System.out.println("⚠️ Structure de la table 'escale' incorrecte détectée. Réparation en cours...");
                stmt.execute("DROP TABLE IF EXISTS escale"); // Suppression propre
                stmt.execute(sqlEscale); // Recréation propre
                System.out.println("✅ Table 'escale' réparée avec succès !");
            } else {
                // Création normale si elle n'existe pas encore
                stmt.execute(sqlEscale);
            }

            System.out.println("✅ Base de données SQLite complète vérifiée/initialisée.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'initialisation de la DB : " + e.getMessage());
        }
    }
}
