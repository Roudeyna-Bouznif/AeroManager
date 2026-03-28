package com.example.projet_java_vols.Gestion_des_vols.Controller;

import com.example.projet_java_vols.ConnexionDB;
import com.example.projet_java_vols.Gestion_des_vols.Model.Aeroport;
import com.example.projet_java_vols.Gestion_des_vols.Model.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class GestionAeroportsController {
    @FXML private TableView<Aeroport> tableAeroports;
    @FXML private TableColumn<Aeroport,String> colCode;
    @FXML private TableColumn<Aeroport,String> colNom;
    @FXML private TableColumn<Aeroport,String> colVille;
    @FXML private TableColumn<Aeroport,String> colPays;
    @FXML private Button btnMenuEmployes;
    @FXML private TextField txtCodeAeroport;
    @FXML private TextField txtNom;
    @FXML private TextField txtVille;
    @FXML private TextField txtPays;
    @FXML private Label nbrAeroports;
    @FXML private VBox containerSaisie;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;
    @FXML private TextField txtRecherche;

    private final ObservableList<Aeroport> listeAeroports = FXCollections.observableArrayList();
    private Aeroport aeroportSelectionee = null;

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<Aeroport, String>("idAeroport"));
        colNom.setCellValueFactory(new PropertyValueFactory<Aeroport, String>("nom"));
        colVille.setCellValueFactory(new PropertyValueFactory<Aeroport, String>("ville"));
        colPays.setCellValueFactory(new PropertyValueFactory<Aeroport, String>("pays"));
        chargerAeroportsDepuisBD();
        tableAeroports.setItems(listeAeroports);
        updateCompteur();

        tableAeroports.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            aeroportSelectionee = newSelection;
            btnSupprimer.setDisable(newSelection == null);
            if (newSelection != null) {
                remplirChamps(newSelection);
            } else {
                effacerChamps();
            }
        });

        btnSupprimer.setDisable(true);
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerAeroports(newVal));
        gererDroitsAcces();
    }

    private void gererDroitsAcces() {
        String role = UserSession.getRole();
        if (role == null) role = "AGENT_ENREG";

        if (role.equals("AGENT_ENREG")) {
            containerSaisie.setVisible(false);
            containerSaisie.setManaged(false);
            btnSupprimer.setVisible(false);
            btnSupprimer.setManaged(false);
            if (btnMenuEmployes != null) {
                btnMenuEmployes.setVisible(false);
                btnMenuEmployes.setManaged(false);
            }
            System.out.println("🔒 Mode Lecture Seule activé (Agent Enregistrement)");
        } else if (role.equals("AGENT_VOL")) {
            if (btnMenuEmployes != null) {
                btnMenuEmployes.setVisible(false);
                btnMenuEmployes.setManaged(false);
            }
        }
    }

    private void chargerAeroportsDepuisBD() {
        listeAeroports.clear();
        try (Connection conn = ConnexionDB.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM aeroport")) {
            while (rs.next()) {
                String code = rs.getString("idAeroport");
                String nom = rs.getString("nom");
                String ville = rs.getString("ville");
                String pays = rs.getString("pays");
                listeAeroports.add(new Aeroport(code, nom, ville, pays));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void remplirChamps(Aeroport a) {
        txtCodeAeroport.setText(a.getIdAeroport());
        txtNom.setText(a.getNom());
        txtVille.setText(a.getVille());
        txtPays.setText(a.getPays());
    }

    @FXML
    private void ajouterAeroport() {
        String code = txtCodeAeroport.getText().trim();
        String nom = txtNom.getText().trim();
        String ville = txtVille.getText().trim();
        String pays = txtPays.getText().trim();

        if (!validerChamps()) return;

        Aeroport a = new Aeroport(code, nom, ville, pays);
        try (Connection conn = ConnexionDB.getConnection();
             var stmt = conn.prepareStatement("INSERT INTO aeroport VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, code);
            stmt.setString(2, nom);
            stmt.setString(3, ville);
            stmt.setString(4, pays);
            stmt.executeUpdate();
            listeAeroports.add(a);
            updateCompteur();
            afficherSucces("Aéroport ajouté avec succès !");
            effacerChamps();
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Code aéroport dupliqué",
                        "Le code '" + code + "' existe déjà. Veuillez choisir un autre code.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur Base de Données",
                        "Erreur lors de l'ajout", ex.getMessage());
            }
            ex.printStackTrace();
        }
    }

    private boolean validerChamps() {
        if (txtCodeAeroport.getText() == null || txtCodeAeroport.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur saisie", null, "Le code de l'aéroport est obligatoire.");
            txtCodeAeroport.requestFocus();
            return false;
        }

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur saisie", null, "Le nom de l'aéroport est obligatoire.");
            txtNom.requestFocus();
            return false;
        }

        if (txtVille.getText() == null || txtVille.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur saisie", null, "La ville de l'aéroport est obligatoire.");
            txtVille.requestFocus();
            return false;
        }

        if (txtPays.getText() == null || txtPays.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur saisie", null, "Le pays de l'aéroport est obligatoire.");
            txtPays.requestFocus();
            return false;
        }

        return true;
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void supprimerAeroport() {
        if (aeroportSelectionee != null) {
            try {

                if (hasAeroportDependencies(aeroportSelectionee.getIdAeroport())) {

                    showForceDeletionDialog(aeroportSelectionee);
                } else {

                    SuppressionDetecte(aeroportSelectionee, false);
                }
            } catch (Exception ex) {

            }
        }
    }

    private boolean hasAeroportDependencies(String aeroportCode) throws SQLException {
        try (Connection conn = ConnexionDB.getConnection();
             var stmt = conn.prepareStatement(
                     "SELECT COUNT(*) as count FROM escale WHERE idAeroportDepart = ? OR idAeroportArrivee = ?")) {
            stmt.setString(1, aeroportCode);
            stmt.setString(2, aeroportCode);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }
        return false;
    }

    private void showForceDeletionDialog(Aeroport aeroport) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Suppression avec dépendances");
        alert.setHeaderText("Cet aéroport a des escales associées");
        alert.setContentText(
                "L'aéroport '" + aeroport.getNom() + "' est utilisé dans des escales.\n\n" +
                        "Voulez-vous :\n" +
                        "• OK : Forcer la suppression (supprimer aussi les escales)\n" +
                        "• Annuler : Ne pas supprimer"
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SuppressionDetecte(aeroport, true);
        }
    }

    private void SuppressionDetecte(Aeroport aeroport, boolean forceDelete) {
        try (Connection conn = ConnexionDB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (forceDelete) {

                    try (var stmt = conn.prepareStatement(
                            "DELETE FROM escale WHERE idAeroportDepart = ? OR idAeroportArrivee = ?")) {
                        stmt.setString(1, aeroport.getIdAeroport());
                        stmt.setString(2, aeroport.getIdAeroport());
                        int deletedEscales = stmt.executeUpdate();
                        System.out.println("✓ " + deletedEscales + " escale(s) supprimée(s)");
                    }
                }


                try (var stmt = conn.prepareStatement("DELETE FROM aeroport WHERE idAeroport = ?")) {
                    stmt.setString(1, aeroport.getIdAeroport());
                    stmt.executeUpdate();
                }

                conn.commit();
                listeAeroports.remove(aeroport);
                updateCompteur();
                effacerChamps();
                aeroportSelectionee = null;

                String message = forceDelete ?
                        "Aéroport et ses escales supprimés avec succès !" :
                        "Aéroport supprimé avec succès !";
                afficherSucces(message);

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {

            ex.printStackTrace();
        }
    }

    @FXML
    private void annulerAction() {
        effacerChamps();
        aeroportSelectionee = null;
        tableAeroports.getSelectionModel().clearSelection();
    }

    private void filtrerAeroports(String filtre) {
        if (filtre == null || filtre.isEmpty()) {
            tableAeroports.setItems(listeAeroports);
        } else {
            ObservableList<Aeroport> filtrés = FXCollections.observableArrayList();
            for (Aeroport a : listeAeroports) {
                if (a.getIdAeroport().toLowerCase().contains(filtre.toLowerCase()) ||
                        a.getNom().toLowerCase().contains(filtre.toLowerCase()) ||
                        a.getVille().toLowerCase().contains(filtre.toLowerCase()) ||
                        a.getPays().toLowerCase().contains(filtre.toLowerCase())) {
                    filtrés.add(a);
                }
            }
            tableAeroports.setItems(filtrés);
        }
    }

    private void effacerChamps() {
        txtCodeAeroport.clear();
        txtNom.clear();
        txtVille.clear();
        txtPays.clear();
    }

    private void updateCompteur() {
        int nb = 0;
        try (Connection conn = ConnexionDB.getConnection();
             var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM aeroport")) {
            if (rs.next()) nb = rs.getInt(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        nbrAeroports.setText("Total: " + nb + " aéroport(s)");
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void allerAeroports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Aeroport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Aéroports");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void allerEscales() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Escales.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Escales");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void allerVols() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Vols.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Vols");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void allerReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Reservations.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Réservations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void allerEmploye() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_employes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Employés");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void allerStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e40af; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 8 16; -fx-background-radius: 8; " +
                "-fx-border-color: transparent; -fx-cursor: hand;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #475569; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 8 16; -fx-background-radius: 8; " +
                "-fx-border-color: transparent; -fx-cursor: hand;");
    }

    @FXML
    private void quitter() {
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
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
