package com.example.projet_java_vols.Gestion_des_vols.Controller;

import com.example.projet_java_vols.ConnexionDB;
import com.example.projet_java_vols.Gestion_des_vols.Model.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class GestionStatsController implements Initializable {

    @FXML private Label lblVolsJour;
    @FXML private Label lblTauxOccupation;
    @FXML private Label lblRevenusJour;
    @FXML private Label lblDerniereMaj;

    @FXML private BarChart<String, Number> barVolsDestination;
    @FXML private PieChart pieTypeVol;

    @FXML private Button btnMenuEmployes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lancerThreadStats();
        gererDroitsAcces();
    }

    private void gererDroitsAcces() {
        String role = UserSession.getRole();
        if (role == null) role = "INVITE";

        boolean isAdmin = role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ADMINISTRATEUR");

        if (btnMenuEmployes != null) {
            btnMenuEmployes.setVisible(isAdmin);
            btnMenuEmployes.setManaged(isAdmin);
        }
    }

    @FXML
    private void relancerRefresh() {
        lancerThreadStats();
    }

    private void lancerThreadStats() {

        Task<Void> task = new Task<>() {

            int volsJour = 0;
            double tauxOcc = 0.0;
            double revenusJour = 0.0;

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            XYChart.Series<String, Number> serieDestination = new XYChart.Series<>();

            @Override
            protected Void call() {

                LocalDate today = LocalDate.now();

                try (Connection conn = ConnexionDB.getConnection()) {

                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) FROM vol WHERE dateDepart = ?")) {
                        ps.setDate(1, java.sql.Date.valueOf(today));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) volsJour = rs.getInt(1);
                    }

                    int totalPlaces = 0;
                    int occupees = 0;

                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT SUM(nbPlaces) AS totalPlaces FROM vol WHERE dateDepart = ?")) {
                        ps.setDate(1, java.sql.Date.valueOf(today));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) totalPlaces = rs.getInt("totalPlaces");
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) AS occupees " +
                                    "FROM reservation r " +
                                    "JOIN vol v ON r.numVol = v.numVol " +
                                    "WHERE v.dateDepart = ?")) {
                        ps.setDate(1, java.sql.Date.valueOf(today));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) occupees = rs.getInt("occupees");
                    }

                    if (totalPlaces > 0) {
                        tauxOcc = (occupees * 100.0) / totalPlaces;
                    } else {
                        tauxOcc = 0.0;
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT SUM(prixVol) FROM vol WHERE dateDepart = ?")) {
                        ps.setDate(1, java.sql.Date.valueOf(today));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) revenusJour = rs.getDouble(1);
                    }

                    int nbNat = 0, nbInt = 0;

                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT type, COUNT(*) FROM vol WHERE dateDepart = ? GROUP BY type")) {
                        ps.setDate(1, java.sql.Date.valueOf(today));
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            String type = rs.getString(1);
                            int count = rs.getInt(2);

                            if ("National".equalsIgnoreCase(type)) nbNat = count;
                            else nbInt = count;
                        }
                    }

                    pieData.add(new PieChart.Data("National", nbNat));
                    pieData.add(new PieChart.Data("International", nbInt));

                    serieDestination.setName("Vols du " + today.format(DateTimeFormatter.ofPattern("dd/MM")));

                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT paysDestination, COUNT(*) " +
                                    "FROM vol WHERE dateDepart = ? GROUP BY paysDestination")) {
                        ps.setDate(1, java.sql.Date.valueOf(today));
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            String dest = rs.getString(1);
                            int count = rs.getInt(2);
                            serieDestination.getData().add(new XYChart.Data<>(dest, count));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    lblVolsJour.setText(String.valueOf(volsJour));
                    lblTauxOccupation.setText(String.format("%.1f %%", tauxOcc));
                    lblRevenusJour.setText(String.format("%.2f €", revenusJour));

                    pieTypeVol.setData(pieData);

                    barVolsDestination.getData().clear();
                    barVolsDestination.getData().add(serieDestination);

                    lblDerniereMaj.setText(
                            "Dernière mise à jour : " +
                                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    );
                });

                return null;
            }
        };

        new Thread(task).start();
    }


    @FXML
    private void retourVols() {
        nav("/com/example/projet_java_vols/Gestion_Vols.fxml", "Gestion des Vols");
    }

    @FXML private void allerAeroports() { nav("/com/example/projet_java_vols/Gestion_Aeroport.fxml", "Gestion des Aéroports"); }
    @FXML private void allerVols() { nav("/com/example/projet_java_vols/Gestion_Vols.fxml", "Gestion des Vols"); }
    @FXML private void allerEscales() { nav("/com/example/projet_java_vols/Gestion_Escales.fxml", "Gestion des Escales"); }
    @FXML private void allerReservations() { nav("/com/example/projet_java_vols/Gestion_Reservations.fxml", "Gestion des Réservations"); }
    @FXML public void allerEmploye(ActionEvent actionEvent) { nav("/com/example/projet_java_vols/Gestion_employes.fxml", "Gestion des Employés"); }
    @FXML public void allerStats(ActionEvent actionEvent) { nav("/com/example/projet_java_vols/Gestion_Statistiques.fxml", "Statistiques"); }

    private void nav(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) lblVolsJour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle(btn.getStyle() + "-fx-background-color: #e0e7ff;");
        }
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle(btn.getStyle().replace("-fx-background-color: #e0e7ff;", ""));
        }
    }

    @FXML
    private void quitter() {
        Stage stage = (Stage) lblVolsJour.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblVolsJour.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion - AeroManager");

            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur : Impossible de charger la vue Login.fxml");
        }
    }
}
