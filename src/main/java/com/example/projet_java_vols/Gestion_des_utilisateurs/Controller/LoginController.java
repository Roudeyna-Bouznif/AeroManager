package com.example.projet_java_vols.Gestion_des_utilisateurs.Controller;

import com.example.projet_java_vols.ConnexionDB;
import com.example.projet_java_vols.Gestion_des_vols.Model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private HBox errorBox;
    @FXML private Label lblError;
    @FXML private StackPane loadingOverlay;
    @FXML private CheckBox chkRememberMe;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        if (loadingOverlay != null) loadingOverlay.setVisible(true);

        if (username.equals("malek") && password.equals("123")) {
            System.out.println("✅ Connexion Admin (SuperUser)");
            UserSession.login(1, "ADMIN", "malek");
            allerVersEmployes();
            return;
        }

        String roleBDD = recupererRoleDepuisBD(username, password);

        if (roleBDD != null) {
            int idEmploye = chercherIdDansBD(username, password);

            UserSession.login(idEmploye, roleBDD, username);

            System.out.println("✅ Connexion réussie ! ID: " + idEmploye + " Rôle : " + roleBDD);

            if (UserSession.isAdmin()) {
                allerVersEmployes();
            } else {
                allerVersAeroports();
            }
        } else {
            if (loadingOverlay != null) loadingOverlay.setVisible(false);
            afficherErreur("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }
    private String recupererRoleDepuisBD(String nom, String mdp) {
        String sql = "SELECT typeEmploye FROM employe WHERE nom = ? AND motDePasse = ? AND actif = 1";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, mdp);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("typeEmploye");


                    if ("AGENT_VOL".equalsIgnoreCase(type)) return "AGENT_VOL";
                    if ("AGENT_ENREG".equalsIgnoreCase(type)) return "AGENT_ENREG";
                    return "USER";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la récupération du rôle : " + e.getMessage());
        }

        return null;
    }

    private int chercherIdDansBD(String nom, String mdp) {
        String sql = "SELECT idEmploye FROM employe WHERE nom = ? AND motDePasse = ?";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, mdp);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idEmploye");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    private void allerVersEmployes() {
        chargerInterface("/com/example/projet_java_vols/Gestion_Employes.fxml", "Gestion des Employés");
    }

    private void allerVersAeroports() {
        chargerInterface("/com/example/projet_java_vols/Gestion_Aeroport.fxml", "Gestion des Aéroports");
    }

    private void chargerInterface(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de " + fxmlPath);
            e.printStackTrace();
            afficherErreur("Impossible de charger l'interface : " + titre);
            if (loadingOverlay != null) loadingOverlay.setVisible(false);
        }
    }

    private void afficherErreur(String message) {
        if (errorBox != null && lblError != null) {
            lblError.setText(message);
            errorBox.setVisible(true);
            errorBox.setManaged(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.show();
        }
    }
}
