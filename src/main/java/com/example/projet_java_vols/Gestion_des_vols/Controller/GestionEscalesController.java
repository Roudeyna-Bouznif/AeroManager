package com.example.projet_java_vols.Gestion_des_vols.Controller;

import com.example.projet_java_vols.ConnexionDB;
import com.example.projet_java_vols.Gestion_des_vols.Model.StatutVol;
import com.example.projet_java_vols.Gestion_des_vols.Model.*;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static com.example.projet_java_vols.Gestion_des_vols.Model.Escale.formaterDuree;

public class GestionEscalesController {

    // Champs FXML
    @FXML private TextField txtIdEscale;
    @FXML private Spinner<Integer> spinOrdre;
    @FXML private ComboBox<Aeroport> cmbAeroportDepart;
    @FXML private ComboBox<Aeroport> cmbAeroportArrivee;
    @FXML private DatePicker dpDateDepart;
    @FXML private DatePicker dpDateArrivee;
    @FXML private TextField txtHeureDepart;
    @FXML private TextField txtHeureArrivee;
    @FXML private Label lblDuree;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;
    @FXML private Button btnSupprimer;
    @FXML private TextField txtRecherche;
    @FXML private Label lblNombreEscales;
    @FXML private VBox containerSaisie;
    @FXML private Button btnMenuEmployes;

    @FXML private TableView<Escale> tableEscales;
    @FXML private TableColumn<Escale,Integer> colId;
    @FXML private TableColumn<Escale, String> colNumVol;
    @FXML private TableColumn<Escale, Integer> colOrdre;
    @FXML private TableColumn<Escale, String> colAeroportDepart;
    @FXML private TableColumn<Escale, String> colAeroportArrivee;
    @FXML private TableColumn<Escale, Date> colDateDepart;
    @FXML private TableColumn<Escale, String> colHeureDepart;
    @FXML private TableColumn<Escale, Date> colDateArrivee;
    @FXML private TableColumn<Escale, String> colHeureArrivee;
    @FXML private TableColumn<Escale, Long> colDuree;
    @FXML private ComboBox<Vol> cmbVol;

    private ObservableList<Vol> listeVols = FXCollections.observableArrayList();
    private ObservableList<Escale> listeEscales= FXCollections.observableArrayList();
    private ObservableList<Aeroport> listeAeroports=FXCollections.observableArrayList();

    private Escale escaleSelectionnee = null;
    private final DateTimeFormatter HEURE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Initialisation sécurisée des composants
        if (spinOrdre != null) {
            spinOrdre.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
            spinOrdre.setEditable(true);
        }

        chargerAeroportsDepuisBD();
        if (cmbAeroportDepart != null) cmbAeroportDepart.setItems(listeAeroports);
        if (cmbAeroportArrivee != null) cmbAeroportArrivee.setItems(listeAeroports);

        // Initialisation des colonnes
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idEscale"));
        if (colOrdre != null) colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        if (colNumVol != null) colNumVol.setCellValueFactory(new PropertyValueFactory<>("numVol"));
        if (colAeroportDepart != null) colAeroportDepart.setCellValueFactory(new PropertyValueFactory<>("aeroportDepart"));
        if (colAeroportArrivee != null) colAeroportArrivee.setCellValueFactory(new PropertyValueFactory<>("aeroportArrivee"));
        if (colDateDepart != null) colDateDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        if (colDateArrivee != null) colDateArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        if (colHeureDepart != null) colHeureDepart.setCellValueFactory(new PropertyValueFactory<>("heureDepart"));
        if (colHeureArrivee != null) colHeureArrivee.setCellValueFactory(new PropertyValueFactory<>("heureArrivee"));

        chargerVolsDepuisBD();
        if (cmbVol != null) cmbVol.setItems(listeVols);

        chargerEscalesDepuisBD();
        if (tableEscales != null) {
            tableEscales.setItems(listeEscales);
            tableEscales.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                escaleSelectionnee = newSelection;
                if (btnModifier != null) btnModifier.setDisable(newSelection == null);
                if (btnSupprimer != null) btnSupprimer.setDisable(newSelection == null);
                if (newSelection != null) {
                    remplirChamps(newSelection);
                } else {
                    effacerChamps();
                }
            });
        }

        if (txtHeureDepart != null) txtHeureDepart.textProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        if (txtHeureArrivee != null) txtHeureArrivee.textProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        if (txtRecherche != null) txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerEscales(newVal));

        if (btnModifier != null) btnModifier.setDisable(true);
        if (btnSupprimer != null) btnSupprimer.setDisable(true);

        updateCompteurEscales();
        gererDroitsAcces();
    }

    // Méthodes utilitaires pour parser les dates SQL correctement
    private java.sql.Date parseSqlDate(Object rawDate) {
        if (rawDate == null) return null;
        if (rawDate instanceof Number) {
            return new java.sql.Date(((Number) rawDate).longValue());
        }
        try {
            return java.sql.Date.valueOf(rawDate.toString());
        } catch (Exception e) {
            return new java.sql.Date(System.currentTimeMillis());
        }
    }

    private LocalTime parseSqlTime(Object rawTime) {
        if (rawTime == null) return null;
        if (rawTime instanceof Number) {
            return new java.sql.Time(((Number) rawTime).longValue()).toLocalTime();
        }
        try {
            String t = rawTime.toString();
            if (t.length() == 5) t += ":00";
            return java.sql.Time.valueOf(t).toLocalTime();
        } catch (Exception e) {
            return LocalTime.now();
        }
    }

    private void chargerAeroportsDepuisBD() {
        listeAeroports.clear();
        try (Connection conn = ConnexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM aeroport")) {
            while (rs.next()) {
                listeAeroports.add(new Aeroport(
                        rs.getString("idAeroport"),
                        rs.getString("nom"),
                        rs.getString("ville"),
                        rs.getString("pays")
                ));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void chargerVolsDepuisBD() {
        listeVols.clear();
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vol");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("type");

                java.sql.Date dArr = parseSqlDate(rs.getObject("dateArrivee"));
                java.sql.Date dDep = parseSqlDate(rs.getObject("dateDepart"));
                LocalTime hArr = parseSqlTime(rs.getObject("heureArrivee"));
                LocalTime hDep = parseSqlTime(rs.getObject("heureDepart"));

                Vol v;
                if ("International".equalsIgnoreCase(type)) {
                    v = new VolInternational(
                            rs.getString("numVol"),
                            rs.getInt("nbPlaces"),
                            rs.getDouble("prixBase"),
                            rs.getDouble("prixVol"),
                            StatutVol.valueOf(rs.getString("statut")),
                            rs.getString("paysDestination"),
                            new ArrayList<>(),
                            new HashMap<>(),
                            dArr, dDep, hArr,
                            chargerAeroport(rs.getString("aeroportArrivee")),
                            hDep,
                            chargerAeroport(rs.getString("aeroportDepart")),
                            rs.getString("numeroAutorisation"),
                            rs.getBoolean("exigenceVisa")
                    );
                } else {
                    v = new VolNational(
                            rs.getString("numVol"),
                            rs.getInt("nbPlaces"),
                            rs.getDouble("prixBase"),
                            rs.getDouble("prixVol"),
                            StatutVol.valueOf(rs.getString("statut")),
                            rs.getString("paysDestination"),
                            new ArrayList<>(),
                            new HashMap<>(),
                            dArr, dDep, hArr,
                            chargerAeroport(rs.getString("aeroportArrivee")),
                            hDep,
                            chargerAeroport(rs.getString("aeroportDepart")),
                            rs.getString("numeroAutorisation"),
                            rs.getString("terminal")
                    );
                }
                listeVols.add(v);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void chargerEscalesDepuisBD() {
        listeEscales.clear();
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM escale");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.sql.Date dArr = parseSqlDate(rs.getObject("dateArrivee"));
                java.sql.Date dDep = parseSqlDate(rs.getObject("dateDepart"));
                LocalTime hArr = parseSqlTime(rs.getObject("heureArrivee"));
                LocalTime hDep = parseSqlTime(rs.getObject("heureDepart"));

                int ordre = 1;
                try { ordre = rs.getInt("ordre"); } catch (SQLException ignored) {}

                Escale e = new Escale(
                        rs.getInt("idEscale"),
                        ordre,
                        chargerAeroport(rs.getString("idAeroportArrivee")),
                        chargerAeroport(rs.getString("idAeroportDepart")),
                        dArr, dDep, hArr, hDep,
                        rs.getString("numVol")
                );
                listeEscales.add(e);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        if (tableEscales != null) tableEscales.setItems(listeEscales);
        updateCompteurEscales();
    }

    private Aeroport chargerAeroport(String idAeroport) {
        if (idAeroport == null) return null;
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM aeroport WHERE idAeroport = ?")) {
            stmt.setString(1, idAeroport);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Aeroport(idAeroport, rs.getString("nom"), rs.getString("ville"), rs.getString("pays"));
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private void remplirChamps(Escale e) {
        if (e == null) return;

        if (txtIdEscale != null) txtIdEscale.setText(String.valueOf(e.getIdEscale()));
        if (spinOrdre != null) spinOrdre.getValueFactory().setValue(e.getOrdre());

        if (cmbVol != null && e.getNumVol() != null) {
            for (Vol v : cmbVol.getItems()) {
                if (v.getNumVol().equals(e.getNumVol())) {
                    cmbVol.setValue(v);
                    break;
                }
            }
        }

        if (cmbAeroportDepart != null && e.getAeroportDepart() != null) {
            for (Aeroport a : cmbAeroportDepart.getItems()) {
                if (a.getIdAeroport().equals(e.getAeroportDepart().getIdAeroport())) {
                    cmbAeroportDepart.setValue(a);
                    break;
                }
            }
        }

        if (cmbAeroportArrivee != null && e.getAeroportArrivee() != null) {
            for (Aeroport a : cmbAeroportArrivee.getItems()) {
                if (a.getIdAeroport().equals(e.getAeroportArrivee().getIdAeroport())) {
                    cmbAeroportArrivee.setValue(a);
                    break;
                }
            }
        }

        if (dpDateDepart != null) dpDateDepart.setValue(convertToLocalDate(e.getDateDepart()));
        if (dpDateArrivee != null) dpDateArrivee.setValue(convertToLocalDate(e.getDateArrivee()));
        if (txtHeureDepart != null) txtHeureDepart.setText(formatHeure(e.getHeureDepart()));
        if (txtHeureArrivee != null) txtHeureArrivee.setText(formatHeure(e.getHeureArrivee()));

        calculerDuree();
    }


    @FXML
    public void ajouterEscale() {
        if (!validerChamps()) return;

        Escale e = construireEscale(false);
        Vol vol = (cmbVol != null) ? cmbVol.getValue() : null;

        if (vol == null) {
            showAlert("Sélection du vol", "Veuillez sélectionner un numéro de vol.");
            return;
        }

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO escale (ordre, idAeroportDepart, idAeroportArrivee, dateDepart, dateArrivee, heureDepart, heureArrivee, numVol) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, e.getOrdre());
            stmt.setString(2, e.getAeroportDepart().getIdAeroport());
            stmt.setString(3, e.getAeroportArrivee().getIdAeroport());
            stmt.setDate(4, new java.sql.Date(e.getDateDepart().getTime()));
            stmt.setDate(5, new java.sql.Date(e.getDateArrivee().getTime()));

            String hDep = e.getHeureDepart().toString();
            if(hDep.length() == 5) hDep += ":00";
            stmt.setString(6, hDep);

            String hArr = e.getHeureArrivee().toString();
            if(hArr.length() == 5) hArr += ":00";
            stmt.setString(7, hArr);

            stmt.setString(8, vol.getNumVol());
            stmt.executeUpdate();
            afficherSucces("Escale ajoutée avec succès !");

        } catch (Exception ex) {
            ex.printStackTrace();
            afficherErreur("Erreur lors de l'ajout: " + ex.getMessage());
        }
        chargerEscalesDepuisBD();
        effacerChamps();
    }

    @FXML
    private void modifierEscale() {
        if (escaleSelectionnee == null || !validerChamps()) return;

        // On récupère les valeurs du formulaire
        Escale maj = construireEscale(true);
        Vol volSelectionne = (cmbVol != null) ? cmbVol.getValue() : null;

        // Sécurité supplémentaire
        if (volSelectionne == null) {
            afficherErreur("Veuillez sélectionner un vol valide.");
            return;
        }

        try (Connection conn = ConnexionDB.getConnection();
             // AJOUT DE "numVol=?" DANS LA REQUÊTE SQL
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE escale SET ordre=?, idAeroportDepart=?, idAeroportArrivee=?, dateDepart=?, dateArrivee=?, heureDepart=?, heureArrivee=?, numVol=? WHERE idEscale=?")) {

            stmt.setInt(1, maj.getOrdre());
            stmt.setString(2, maj.getAeroportDepart().getIdAeroport());
            stmt.setString(3, maj.getAeroportArrivee().getIdAeroport());
            stmt.setDate(4, new java.sql.Date(maj.getDateDepart().getTime()));
            stmt.setDate(5, new java.sql.Date(maj.getDateArrivee().getTime()));

            String hDep = maj.getHeureDepart().toString();
            if(hDep.length() == 5) hDep += ":00";
            stmt.setString(6, hDep);

            String hArr = maj.getHeureArrivee().toString();
            if(hArr.length() == 5) hArr += ":00";
            stmt.setString(7, hArr);

            // ICI : On met à jour le numéro de vol
            stmt.setString(8, volSelectionne.getNumVol());

            // WHERE idEscale = ...
            stmt.setInt(9, escaleSelectionnee.getIdEscale());

            stmt.executeUpdate();
            afficherSucces("Escale modifiée avec succès !");

        } catch (Exception ex) {
            ex.printStackTrace();
            afficherErreur("Erreur lors de la modification : " + ex.getMessage());
        }

        chargerEscalesDepuisBD();
        effacerChamps();
        if(tableEscales != null) tableEscales.getSelectionModel().clearSelection();
    }


    @FXML
    private void supprimerEscale() {
        if (escaleSelectionnee == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Suppression");
        alert.setContentText("Confirmer la suppression ?");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM escale WHERE idEscale=?")) {
                stmt.setInt(1, escaleSelectionnee.getIdEscale());
                stmt.executeUpdate();
            } catch (Exception ex) { ex.printStackTrace(); }
            chargerEscalesDepuisBD();
            effacerChamps();
            if(tableEscales != null) tableEscales.getSelectionModel().clearSelection();
            escaleSelectionnee = null;
        }
    }

    @FXML
    private void annulerAction() {
        effacerChamps();
        if(tableEscales != null) tableEscales.getSelectionModel().clearSelection();
        escaleSelectionnee = null;
    }

    private void filtrerEscales(String filtre) {
        if (tableEscales == null) return;

        if (filtre == null || filtre.isEmpty()) {
            tableEscales.setItems(listeEscales);
        } else {
            String recherche = filtre.toLowerCase();
            ObservableList<Escale> filtrees = FXCollections.observableArrayList();
            for (Escale e : listeEscales) {
                if (
                        (e.getNumVol() != null && e.getNumVol().toLowerCase().contains(recherche)) ||
                                (e.getAeroportDepart() != null && e.getAeroportDepart().getNom().toLowerCase().contains(recherche)) ||
                                (e.getAeroportArrivee() != null && e.getAeroportArrivee().getNom().toLowerCase().contains(recherche)) ||
                                String.valueOf(e.getOrdre()).contains(recherche)
                ) {
                    filtrees.add(e);
                }
            }
            tableEscales.setItems(filtrees);
        }
        updateCompteurEscales();
    }

    private void effacerChamps() {
        if (txtIdEscale != null) txtIdEscale.clear();
        if (spinOrdre != null) spinOrdre.getValueFactory().setValue(1);
        if (cmbVol != null) cmbVol.getSelectionModel().clearSelection();
        if (cmbAeroportDepart != null) cmbAeroportDepart.getSelectionModel().clearSelection();
        if (cmbAeroportArrivee != null) cmbAeroportArrivee.getSelectionModel().clearSelection();
        if (dpDateDepart != null) dpDateDepart.setValue(null);
        if (dpDateArrivee != null) dpDateArrivee.setValue(null);
        if (txtHeureDepart != null) txtHeureDepart.clear();
        if (txtHeureArrivee != null) txtHeureArrivee.clear();
        if (lblDuree != null) lblDuree.setText("-- min");
        if (btnModifier != null) btnModifier.setDisable(true);
        if (btnSupprimer != null) btnSupprimer.setDisable(true);
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validerChamps() {
        if (cmbVol != null && cmbVol.getValue() == null) {
            afficherErreur("Veuillez sélectionner un vol.");
            cmbVol.requestFocus();
            return false;
        }

        int ordre = (spinOrdre != null) ? spinOrdre.getValue() : 1;
        Vol vol = (cmbVol != null) ? cmbVol.getValue() : null;

        if (vol != null) {
            for (Escale esc : listeEscales) {
                if (esc.getNumVol().equals(vol.getNumVol()) && esc.getOrdre() == ordre) {
                    if (escaleSelectionnee == null || esc.getIdEscale() != escaleSelectionnee.getIdEscale()) {
                        afficherErreur("Ce vol contient déjà une escale d'ordre " + ordre + ".");
                        return false;
                    }
                }
            }
        }

        Aeroport dep = (cmbAeroportDepart != null) ? cmbAeroportDepart.getValue() : null;
        Aeroport arr = (cmbAeroportArrivee != null) ? cmbAeroportArrivee.getValue() : null;

        if (dep == null || arr == null) {
            afficherErreur("Veuillez sélectionner les aéroports.");
            return false;
        }
        if (dep.getIdAeroport().equals(arr.getIdAeroport())) {
            afficherErreur("Départ et arrivée doivent être différents.");
            return false;
        }

        if (dpDateDepart != null && dpDateArrivee != null) {
            if (dpDateDepart.getValue() == null || dpDateArrivee.getValue() == null) {
                afficherErreur("Veuillez saisir les dates.");
                return false;
            }
            if (dpDateDepart.getValue().isBefore(dpDateArrivee.getValue()) == false && dpDateDepart.getValue().equals(dpDateArrivee.getValue()) == false) {
                afficherErreur("La date de départ ne peut pas être après l'arrivée.");
                return false;
            }
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

    private Escale construireEscale(boolean garderId) {
        int id = 0;
        if (garderId && txtIdEscale != null && !txtIdEscale.getText().isEmpty()) {
            try { id = Integer.parseInt(txtIdEscale.getText()); } catch(NumberFormatException e){}
        } else if (garderId && escaleSelectionnee != null) {
            id = escaleSelectionnee.getIdEscale();
        }

        int ordre = (spinOrdre != null) ? spinOrdre.getValue() : 1;
        Aeroport dep = (cmbAeroportDepart != null) ? cmbAeroportDepart.getValue() : null;
        Aeroport arr = (cmbAeroportArrivee != null) ? cmbAeroportArrivee.getValue() : null;
        Date dDep = (dpDateDepart != null) ? convertToDate(dpDateDepart.getValue()) : null;
        Date dArr = (dpDateArrivee != null) ? convertToDate(dpDateArrivee.getValue()) : null;
        LocalTime hDep = (txtHeureDepart != null) ? parseHeure(txtHeureDepart.getText()) : null;
        LocalTime hArr = (txtHeureArrivee != null) ? parseHeure(txtHeureArrivee.getText()) : null;
        String numVol = (cmbVol != null && cmbVol.getValue() != null) ? cmbVol.getValue().getNumVol() : "";

        return new Escale(id, ordre, arr, dep, dArr, dDep, hArr, hDep, numVol);
    }

    private void calculerDuree() {
        if (dpDateDepart == null || dpDateArrivee == null || txtHeureDepart == null || txtHeureArrivee == null) return;

        LocalDate dateDep = dpDateDepart.getValue();
        LocalDate dateArr = dpDateArrivee.getValue();
        LocalTime hDep = parseHeure(txtHeureDepart.getText());
        LocalTime hArr = parseHeure(txtHeureArrivee.getText());
        String duree = formaterDuree(dateDep, hDep, dateArr, hArr);
        if (lblDuree != null) lblDuree.setText(duree);
    }

    private String formatHeure(LocalTime time) {
        return time == null ? "" : time.format(HEURE_FORMATTER);
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return convertToLocalDate(date).format(DATE_FORMATTER);
    }

    private LocalTime parseHeure(String text) {
        if (text == null) return null;
        try { return LocalTime.parse(text, HEURE_FORMATTER); }
        catch (Exception e) { return null; }
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return java.sql.Date.valueOf(localDate);
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateCompteurEscales() {
        if (lblNombreEscales == null) return;
        int nb = 0;
        try (Connection conn = ConnexionDB.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM escale")) {
            if (rs.next()) nb = rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        lblNombreEscales.setText("Total: " + nb + " escale(s)");
    }

    private void gererDroitsAcces() {
        String role = UserSession.getRole();
        if (role == null) role = "AGENT_ENREG";

        if (role.equals("AGENT_ENREG")) {
            if (containerSaisie != null) {
                containerSaisie.setVisible(false);
                containerSaisie.setManaged(false);
            }
            if (btnAjouter != null) btnAjouter.setVisible(false);
            if (btnAnnuler != null) btnAnnuler.setVisible(false);
            if (btnModifier != null) btnModifier.setVisible(false);
            if (btnSupprimer != null) btnSupprimer.setVisible(false);
            if (btnMenuEmployes != null) {
                btnMenuEmployes.setVisible(false);
                btnMenuEmployes.setManaged(false);
            }
        }
        else if (role.equals("AGENT_VOL")) {
            if (btnMenuEmployes != null) {
                btnMenuEmployes.setVisible(false);
                btnMenuEmployes.setManaged(false);
            }
        }
    }

    // --- Navigation ---
    @FXML private void allerAeroports() { nav("/com/example/projet_java_vols/Gestion_Aeroport.fxml", "Gestion Aéroports"); }
    @FXML private void allerEscales() { nav("/com/example/projet_java_vols/Gestion_Escales.fxml", "Gestion Escales"); }
    @FXML private void allerVols() { nav("/com/example/projet_java_vols/Gestion_Vols.fxml", "Gestion Vols"); }
    @FXML private void allerReservations() { nav("/com/example/projet_java_vols/Gestion_Reservations.fxml", "Gestion Réservations"); }
    @FXML public void allerEmploye() { nav("/com/example/projet_java_vols/Gestion_employes.fxml", "Gestion Employés"); }
    @FXML public void allerStats(ActionEvent actionEvent) { nav("/com/example/projet_java_vols/Gestion_Statistiques.fxml", "Statistiques"); }

    private void nav(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((tableEscales != null) ? tableEscales.getScene().getWindow() : btnMenuEmployes.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e40af; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: transparent; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: transparent; -fx-cursor: hand;");
        }
    }

    @FXML
    private void quitter() {
        if (tableEscales != null) {
            Stage stage = (Stage) tableEscales.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void deconnexion() {
        nav("/com/example/projet_java_vols/Login.fxml", "Connexion - AeroManager");
    }
}
