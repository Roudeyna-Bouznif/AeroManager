package com.example.projet_java_vols.Gestion_des_utilisateurs.Controller;

import com.example.projet_java_vols.ConnexionDB;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Administrateur;
import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.AgentEnregistrement;
import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.AgentVol;
import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Employe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GestionEmployesController {

    @FXML private ComboBox<String> cmbTypeEmploye;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private PasswordField txtMotDePasse;
    @FXML private ComboBox<String> cmbStatut;

    @FXML private VBox vboxAgentVol;
    @FXML private TextField txtCodeCompagnie;
    @FXML private TextField txtNomCompagnie;
    @FXML private TextField txtPaysOrigine;
    @FXML private TextField txtFlotte;

    @FXML private VBox vboxAgentEnreg;
    @FXML private TextField txtBureau;
    @FXML private TextField txtComptoir;

    @FXML private TableView<Employe> tableEmployes;
    @FXML private TableColumn<Employe, Integer> colIdEmploye;
    @FXML private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colPrenom;
    @FXML private TableColumn<Employe, String> colEmail;
    @FXML private TableColumn<Employe, String> colTelephone;
    @FXML private TableColumn<Employe, String> colType;
    @FXML private TableColumn<Employe, String> colActif;
    @FXML private TableColumn<Employe, String> colCodeCompagnie;
    @FXML private TableColumn<Employe, String> colNomCompagnie;
    @FXML private TableColumn<Employe, String> colPaysOrigine;
    @FXML private TableColumn<Employe, Integer> colFlotte;
    @FXML private TableColumn<Employe, String> colBureau;
    @FXML private TableColumn<Employe, Integer> colComptoir;

    @FXML private Label lblAdminInfo;
    @FXML private Label lblTotalEmployes;
    @FXML private Label lblEmployesActifs;
    @FXML private Label lblAgentsVol;
    @FXML private Label lblAgentsEnreg;

    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private Administrateur administrateur;
    private ObservableList<Employe> employesObservableList;

    @FXML
    public void initialize() {
        System.out.println("🔧 Initialisation GestionEmployesController...");

        ObservableList<String> types = FXCollections.observableArrayList(
                "Agent de Vol",
                "Agent d'Enregistrement"
        );
        cmbTypeEmploye.setItems(types);

        cmbTypeEmploye.setOnAction(event -> {
            String type = cmbTypeEmploye.getValue();
            if (type == null) return;
            if (type.equals("Agent de Vol")) {
                vboxAgentVol.setVisible(true);
                vboxAgentVol.setManaged(true);
                vboxAgentEnreg.setVisible(false);
                vboxAgentEnreg.setManaged(false);
            } else {
                vboxAgentEnreg.setVisible(true);
                vboxAgentEnreg.setManaged(true);
                vboxAgentVol.setVisible(false);
                vboxAgentVol.setManaged(false);
            }
        });

        cmbStatut.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        cmbStatut.setValue("Actif");

        colIdEmploye.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getIdEmploye()).asObject());
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colTelephone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelephone()));

        colCodeCompagnie.setCellValueFactory(c -> {
            Employe e = c.getValue();
            if (e instanceof AgentVol av) {
                return new SimpleStringProperty(av.getCodeCompagnie());
            }
            return new SimpleStringProperty("");
        });

        colNomCompagnie.setCellValueFactory(c -> {
            Employe e = c.getValue();
            if (e instanceof AgentVol av) {
                return new SimpleStringProperty(av.getNomCompagnie());
            }
            return new SimpleStringProperty("");
        });

        colPaysOrigine.setCellValueFactory(c -> {
            Employe e = c.getValue();
            if (e instanceof AgentVol av) {
                return new SimpleStringProperty(av.getPaysOrigine());
            }
            return new SimpleStringProperty("");
        });

        colFlotte.setCellValueFactory(c -> {
            Employe e = c.getValue();
            if (e instanceof AgentVol av) {
                return new SimpleIntegerProperty(av.getFlotte()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject();
        });


        colBureau.setCellValueFactory(c -> {
            Employe e = c.getValue();
            if (e instanceof AgentEnregistrement ae) {
                return new SimpleStringProperty(ae.getBureauEnregistrement());
            }
            return new SimpleStringProperty("");
        });

        colComptoir.setCellValueFactory(c -> {
            Employe e = c.getValue();
            if (e instanceof AgentEnregistrement ae) {
                return new SimpleIntegerProperty(ae.getComptoir()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject();
        });


        colType.setCellValueFactory(c -> {
            Employe e = c.getValue();
            String type = (e instanceof AgentVol) ? "Agent de Vol" : "Agent d'Enregistrement";
            return new SimpleStringProperty(type);
        });
        colActif.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isActif() ? "Oui" : "Non"));

        employesObservableList = FXCollections.observableArrayList();
        tableEmployes.setItems(employesObservableList);

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        tableEmployes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean has = newSel != null;

            btnModifier.setDisable(!has);
            btnSupprimer.setDisable(!has);

            if (!has) {
                effacerFormulaire();
                return;
            }

            cmbTypeEmploye.setValue(newSel instanceof AgentVol ? "Agent de Vol" : "Agent d'Enregistrement");
            txtNom.setText(newSel.getNom());
            txtPrenom.setText(newSel.getPrenom());
            txtEmail.setText(newSel.getEmail());
            txtTelephone.setText(newSel.getTelephone());
            txtMotDePasse.setText(newSel.getMotDePasse());
            cmbStatut.setValue(newSel.isActif() ? "Actif" : "Inactif");

            if (newSel instanceof AgentVol av) {
                vboxAgentVol.setVisible(true);
                vboxAgentVol.setManaged(true);
                vboxAgentEnreg.setVisible(false);
                vboxAgentEnreg.setManaged(false);
                txtCodeCompagnie.setText(av.getCodeCompagnie());
                txtNomCompagnie.setText(av.getNomCompagnie());
                txtPaysOrigine.setText(av.getPaysOrigine());
                txtFlotte.setText(String.valueOf(av.getFlotte()));
            } else if (newSel instanceof AgentEnregistrement ae) {
                vboxAgentEnreg.setVisible(true);
                vboxAgentEnreg.setManaged(true);
                vboxAgentVol.setVisible(false);
                vboxAgentVol.setManaged(false);
                txtBureau.setText(ae.getBureauEnregistrement());
                txtComptoir.setText(String.valueOf(ae.getComptoir()));
            }
        });

        administrateur = new Administrateur("Admin", "Principal", "admin123");
        lblAdminInfo.setText("Administrateur: " + administrateur.getNom() + " " + administrateur.getPrenom());

        chargerEmployesDepuisBD();
        System.out.println("✅ Initialisation terminée.");
    }

    private void chargerEmployesDepuisBD() {
        if (administrateur == null) return;
        administrateur.getEmployes().clear();

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM employe");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("idEmploye");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");
                String tel = rs.getString("telephone");
                String mdp = rs.getString("motDePasse");
                boolean actif = rs.getBoolean("actif");
                String type = rs.getString("typeEmploye");

                Employe emp;
                if ("AGENT_VOL".equals(type)) {
                    String codeComp = rs.getString("codeCompagnie");
                    String nomComp = rs.getString("nomCompagnie");
                    String pays = rs.getString("paysOrigine");
                    int flotte = rs.getInt("flotte");
                    emp = new AgentVol(id, nom, prenom, email, tel, mdp,
                            codeComp, nomComp, pays, flotte);
                } else {
                    String bureau = rs.getString("bureauEnreg");
                    int comptoir = rs.getInt("comptoir");
                    emp = new AgentEnregistrement(id, nom, prenom, email, tel, mdp,
                            bureau, comptoir);
                }
                emp.setActif(actif);
                administrateur.getEmployes().put(String.valueOf(id), emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur chargement employés : " + e.getMessage());
        }

        rafraichir();
    }

    private int genererNouvelIdEmploye() {
        int max = 0;
        if (administrateur != null && administrateur.getEmployes() != null) {
            for (Employe e : administrateur.getEmployes().values()) {
                if (e.getIdEmploye() > max) {
                    max = e.getIdEmploye();
                }
            }
        }
        return max + 1;
    }

    @FXML
    private void ajouterEmploye() {
        try {
            if (cmbTypeEmploye.getValue() == null ||
                    txtNom.getText().isEmpty() ||
                    txtPrenom.getText().isEmpty() ||
                    txtEmail.getText().isEmpty() ||
                    txtTelephone.getText().isEmpty() ||
                    txtMotDePasse.getText().isEmpty() ||
                    cmbStatut.getValue() == null) {
                afficherErreur("Tous les champs sont obligatoires !");
                return;
            }

            String typeSel = cmbTypeEmploye.getValue();

            if (typeSel.equals("Agent de Vol")) {
                if (txtCodeCompagnie.getText().isEmpty() ||
                        txtNomCompagnie.getText().isEmpty() ||
                        txtPaysOrigine.getText().isEmpty() ||
                        txtFlotte.getText().isEmpty()) {
                    afficherErreur("Tous les champs Agent de Vol sont obligatoires !");
                    return;
                }
            } else if (typeSel.equals("Agent d'Enregistrement")) {
                if (txtBureau.getText().isEmpty() ||
                        txtComptoir.getText().isEmpty()) {
                    afficherErreur("Tous les champs Agent d'Enregistrement sont obligatoires !");
                    return;
                }
            }

            int idEmploye = genererNouvelIdEmploye();

            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String telephone = txtTelephone.getText().trim();
            String motDePasse = txtMotDePasse.getText().trim();
            boolean actif = "Actif".equals(cmbStatut.getValue());

            Employe nouvelEmploye;

            if (typeSel.equals("Agent de Vol")) {
                String codeCompagnie = txtCodeCompagnie.getText().trim();
                String nomCompagnie = txtNomCompagnie.getText().trim();
                String paysOrigine = txtPaysOrigine.getText().trim();
                int flotte = Integer.parseInt(txtFlotte.getText().trim());

                nouvelEmploye = new AgentVol(idEmploye, nom, prenom, email, telephone,
                        motDePasse, codeCompagnie, nomCompagnie, paysOrigine, flotte);

            } else {
                String bureau = txtBureau.getText().trim();
                int comptoir = Integer.parseInt(txtComptoir.getText().trim());

                nouvelEmploye = new AgentEnregistrement(idEmploye, nom, prenom, email,
                        telephone, motDePasse, bureau, comptoir);
            }

            nouvelEmploye.setActif(actif);

            administrateur.ajouterEmploye(nouvelEmploye);

            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO employe (idEmploye, nom, prenom, email, telephone, motDePasse, actif, typeEmploye, " +
                                 "codeCompagnie, nomCompagnie, paysOrigine, flotte, bureauEnreg, comptoir) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                ps.setInt(1, nouvelEmploye.getIdEmploye());
                ps.setString(2, nouvelEmploye.getNom());
                ps.setString(3, nouvelEmploye.getPrenom());
                ps.setString(4, nouvelEmploye.getEmail());
                ps.setString(5, nouvelEmploye.getTelephone());
                ps.setString(6, nouvelEmploye.getMotDePasse());
                ps.setBoolean(7, nouvelEmploye.isActif());

                if (nouvelEmploye instanceof AgentVol av) {
                    ps.setString(8, "AGENT_VOL");
                    ps.setString(9, av.getCodeCompagnie());
                    ps.setString(10, av.getNomCompagnie());
                    ps.setString(11, av.getPaysOrigine());
                    ps.setInt(12, av.getFlotte());
                    ps.setNull(13, java.sql.Types.VARCHAR);
                    ps.setNull(14, java.sql.Types.INTEGER);
                } else if (nouvelEmploye instanceof AgentEnregistrement ae) {
                    ps.setString(8, "AGENT_ENREG");
                    ps.setNull(9, java.sql.Types.VARCHAR);
                    ps.setNull(10, java.sql.Types.VARCHAR);
                    ps.setNull(11, java.sql.Types.VARCHAR);
                    ps.setNull(12, java.sql.Types.INTEGER);
                    ps.setString(13, ae.getBureauEnregistrement());
                    ps.setInt(14, ae.getComptoir());
                }

                ps.executeUpdate();
            }

            rafraichir();
            effacerFormulaire();
            afficherSucces("Employé ajouté avec succès !");

        } catch (NumberFormatException e) {
            afficherErreur("Les champs numériques (flotte, comptoir) doivent être des nombres.");
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void effacerFormulaire() {
        cmbTypeEmploye.setValue(null);
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtMotDePasse.clear();
        cmbStatut.setValue("Actif");

        txtCodeCompagnie.clear();
        txtNomCompagnie.clear();
        txtPaysOrigine.clear();
        txtFlotte.clear();

        txtBureau.clear();
        txtComptoir.clear();

        vboxAgentVol.setVisible(false);
        vboxAgentVol.setManaged(false);
        vboxAgentEnreg.setVisible(false);
        vboxAgentEnreg.setManaged(false);
    }





    @FXML
    private void supprimerEmployeSelection() {
        Employe sel = tableEmployes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherErreur("Veuillez sélectionner un employé à supprimer.");
            return;
        }


        if (hasEmployeDependencies(sel.getIdEmploye())) {

            afficherErreur("Impossible de supprimer cet employé.\n" +
                    "Il est associé à des réservations.\n" +
                    "Veuillez d'abord supprimer ou réassigner ses réservations.");
            return;
        }


        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'employé " + sel.getNom() + " " + sel.getPrenom());
        confirmation.setContentText("Êtes-vous sûr ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM employe WHERE idEmploye = ?")) {
                ps.setInt(1, sel.getIdEmploye());
                ps.executeUpdate();

                administrateur.supprimerEmploye(sel.getIdEmploye());
                rafraichir();
                effacerFormulaire();
                afficherSucces("Employé supprimé avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                afficherErreur("Erreur suppression : " + e.getMessage());
            }
        }
    }



    private boolean hasEmployeDependencies(int idEmploye) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE idEmploye = ?";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmploye);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("🔍 Employé " + idEmploye + " a " + count + " réservation(s)");
                    return count > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur vérification dépendances : " + e.getMessage());
        }
        return false;
    }


    @FXML
    private void modifierEmployeSelection() {
        Employe sel = tableEmployes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherErreur("Veuillez sélectionner un employé à modifier.");
            return;
        }

        try {
            if (cmbTypeEmploye.getValue() == null ||
                    txtNom.getText().isEmpty() ||
                    txtPrenom.getText().isEmpty() ||
                    txtEmail.getText().isEmpty() ||
                    txtTelephone.getText().isEmpty() ||
                    txtMotDePasse.getText().isEmpty() ||
                    cmbStatut.getValue() == null) {
                afficherErreur("Tous les champs sont obligatoires pour la modification !");
                return;
            }

            String typeSel = cmbTypeEmploye.getValue();
            boolean actif = "Actif".equals(cmbStatut.getValue());

            sel.setNom(txtNom.getText().trim());
            sel.setPrenom(txtPrenom.getText().trim());
            sel.setEmail(txtEmail.getText().trim());
            sel.setTelephone(txtTelephone.getText().trim());
            sel.setMotDePasse(txtMotDePasse.getText().trim());
            sel.setActif(actif);

            String codeCompagnie = null, nomCompagnie = null, paysOrigine = null;
            Integer flotte = null;
            String bureau = null;
            Integer comptoir = null;

            if (typeSel.equals("Agent de Vol")) {
                if (txtCodeCompagnie.getText().isEmpty() ||
                        txtNomCompagnie.getText().isEmpty() ||
                        txtPaysOrigine.getText().isEmpty() ||
                        txtFlotte.getText().isEmpty()) {
                    afficherErreur("Tous les champs Agent de Vol sont obligatoires !");
                    return;
                }

                codeCompagnie = txtCodeCompagnie.getText().trim();
                nomCompagnie = txtNomCompagnie.getText().trim();
                paysOrigine = txtPaysOrigine.getText().trim();
                flotte = Integer.parseInt(txtFlotte.getText().trim());

                if (sel instanceof AgentVol av) {
                    av.setCodeCompagnie(codeCompagnie);
                    av.setNomCompagnie(nomCompagnie);
                    av.setPaysOrigine(paysOrigine);
                    av.setFlotte(flotte);
                }
            } else {
                if (txtBureau.getText().isEmpty() ||
                        txtComptoir.getText().isEmpty()) {
                    afficherErreur("Tous les champs Agent d'Enregistrement sont obligatoires !");
                    return;
                }

                bureau = txtBureau.getText().trim();
                comptoir = Integer.parseInt(txtComptoir.getText().trim());

                if (sel instanceof AgentEnregistrement ae) {
                    ae.setBureauEnregistrement(bureau);
                    ae.setComptoir(comptoir);
                }
            }

            administrateur.getEmployes().put(String.valueOf(sel.getIdEmploye()), sel);

            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE employe SET nom=?, prenom=?, email=?, telephone=?, motDePasse=?, actif=?, " +
                                 "typeEmploye=?, codeCompagnie=?, nomCompagnie=?, paysOrigine=?, flotte=?, " +
                                 "bureauEnreg=?, comptoir=? WHERE idEmploye=?")) {

                ps.setString(1, sel.getNom());
                ps.setString(2, sel.getPrenom());
                ps.setString(3, sel.getEmail());
                ps.setString(4, sel.getTelephone());
                ps.setString(5, sel.getMotDePasse());
                ps.setBoolean(6, sel.isActif());

                if (typeSel.equals("Agent de Vol")) {
                    ps.setString(7, "AGENT_VOL");
                    ps.setString(8, codeCompagnie);
                    ps.setString(9, nomCompagnie);
                    ps.setString(10, paysOrigine);
                    ps.setObject(11, flotte, java.sql.Types.INTEGER);
                    ps.setNull(12, java.sql.Types.VARCHAR);
                    ps.setNull(13, java.sql.Types.INTEGER);
                } else {
                    ps.setString(7, "AGENT_ENREG");
                    ps.setNull(8, java.sql.Types.VARCHAR);
                    ps.setNull(9, java.sql.Types.VARCHAR);
                    ps.setNull(10, java.sql.Types.VARCHAR);
                    ps.setNull(11, java.sql.Types.INTEGER);
                    ps.setString(12, bureau);
                    ps.setObject(13, comptoir, java.sql.Types.INTEGER);
                }

                ps.setInt(14, sel.getIdEmploye());
                ps.executeUpdate();
            }

            rafraichir();
            afficherSucces("Employé modifié avec succès.");

        } catch (NumberFormatException e) {
            afficherErreur("Les champs numériques (flotte, comptoir) doivent être des nombres.");
        } catch (Exception e) {
            afficherErreur("Erreur modification : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void rafraichir() {
        if (administrateur == null) {
            System.out.println("⚠️ administrateur null");
            return;
        }

        employesObservableList.clear();
        employesObservableList.addAll(administrateur.getEmployes().values());

        long nbAgentsVol = administrateur.getEmployes().values().stream()
                .filter(e -> e instanceof AgentVol).count();
        long nbAgentsEnreg = administrateur.getEmployes().values().stream()
                .filter(e -> e instanceof AgentEnregistrement).count();
    }

    @FXML
    private void quitter() {
        Stage stage = (Stage) tableEmployes.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void allerAeroports() {
        changerScene("/com/example/projet_java_vols/Gestion_Aeroport.fxml", "Gestion des Aéroports");
    }

    @FXML
    private void allerVols() {
        changerScene("/com/example/projet_java_vols/Gestion_Vols.fxml", "Gestion des Vols");
    }

    @FXML
    private void allerEscales() {
        changerScene("/com/example/projet_java_vols/Gestion_Escales.fxml", "Gestion des Escales");
    }

    @FXML
    private void allerReservations() {
        changerScene("/com/example/projet_java_vols/Gestion_Reservations.fxml", "Gestion des Réservations");
    }
    @FXML
    private void allerStats() {
        changerScene("/com/example/projet_java_vols/Gestion_Statistiques.fxml", "Statistiques");
    }

    private void changerScene(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tableEmployes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle() + "-fx-background-color: #e0e7ff;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle().replace("-fx-background-color: #e0e7ff;", ""));
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableEmployes.getScene().getWindow();

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
