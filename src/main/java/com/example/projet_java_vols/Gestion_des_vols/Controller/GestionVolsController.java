package com.example.projet_java_vols.Gestion_des_vols.Controller;

import com.example.projet_java_vols.ConnexionDB;
import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Reservation;
import com.example.projet_java_vols.Gestion_des_vols.Model.StatutVol;
import com.example.projet_java_vols.Gestion_des_vols.Model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GestionVolsController implements Initializable {

    @FXML private RadioButton rbInternational, rbNational;
    @FXML private TextField txtNumVol, txtHeureDepart, txtHeureArrivee;
    @FXML private TextField txtPrixBase, txtPaysDestination, txtRecherche;
    @FXML private TextField txtChampSpecifique1;
    @FXML private ComboBox<Aeroport> cmbAeroportDepart, cmbAeroportArrivee;
    @FXML private ComboBox<StatutVol> cmbStatut;
    @FXML private DatePicker dpDateDepart, dpDateArrivee;
    @FXML private Spinner<Integer> spinPlaces;
    @FXML private CheckBox chkExigenceVisa;
    @FXML private Label lblChampSpecifique1, lblChampSpecifique2;
    @FXML private Label lblPrixTotal, lblDetailsPrix, lblNombreVols;
    @FXML private Button btnAjouter, btnAnnuler, btnSupprimer, btnModifier;
    @FXML private TextField txtTerminal;
    @FXML private Label lblTerminal;
    @FXML private VBox containerSaisie;
    @FXML private Button btnMenuEmployes;

    @FXML private TableView<Vol> tableVols;
    @FXML private TableColumn<Vol, String> colNumVol, colType, colDepart, colArrivee;
    @FXML private TableColumn<Vol, String> colDateDepart, colHeureDepart, colStatut;
    @FXML private TableColumn<Vol, Integer> colPlaces;
    @FXML private TableColumn<Vol, Double> colPrix;
    @FXML private TableColumn<Vol, String> colTerminal;
    @FXML private TableColumn<Vol, String> colNumAutorisation;
    @FXML private TableColumn<Vol, String> colVisa;

    private ObservableList<Vol> listeVols = FXCollections.observableArrayList();
    private ObservableList<Aeroport> listeAeroports = FXCollections.observableArrayList();
    private ToggleGroup groupeTypeVol;
    private Vol volSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        groupeTypeVol = new ToggleGroup();
        rbInternational.setToggleGroup(groupeTypeVol);
        rbNational.setToggleGroup(groupeTypeVol);
        rbInternational.setSelected(true);

        cmbStatut.setItems(FXCollections.observableArrayList(StatutVol.values()));
        chargerAeroports();

        configurerTable();

        tableVols.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
                volSelectionne = newSelection;
            }
        });

        tableVols.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            volSelectionne = newSelection;
            btnModifier.setDisable(newSelection == null);
            btnSupprimer.setDisable(newSelection == null);
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            } else {
                viderFormulaire();
            }
        });
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerVols(newVal));
        onTypeVolChanged();
        actualiserListe();
        gererDroitsAcces();
    }

    private void gererDroitsAcces() {
        String role = UserSession.getRole();
        if (role == null) role = "AGENT_ENREG";

        if (role.equals("AGENT_ENREG")) {
            if (containerSaisie != null) {
                containerSaisie.setVisible(false);
                containerSaisie.setManaged(false);
            }
            btnAjouter.setVisible(false);
            btnAnnuler.setVisible(false);
            btnModifier.setVisible(false);
            btnSupprimer.setVisible(false);

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

    private void chargerAeroports() {
        listeAeroports.clear();
        try (Connection conn = ConnexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM aeroport")) {
            while (rs.next()) {
                Aeroport a = new Aeroport(
                        rs.getString("idAeroport"),
                        rs.getString("nom"),
                        rs.getString("ville"),
                        rs.getString("pays"));
                listeAeroports.add(a);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        cmbAeroportDepart.setItems(listeAeroports);
        cmbAeroportArrivee.setItems(listeAeroports);
    }

    public void actualiserListe() {
        listeVols.clear();
        try (Connection conn = ConnexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM vol")) {
            while (rs.next()) {
                String type = rs.getString("type");
                Aeroport aeroDep = getAeroportById(rs.getString("aeroportDepart"));
                Aeroport aeroArr = getAeroportById(rs.getString("aeroportArrivee"));

                ArrayList<Escale> escales = new ArrayList<>();
                HashMap<Integer, Reservation> reservations = new HashMap<>();

                java.sql.Date dateArrivee = parseSqlDate(rs.getObject("dateArrivee"));
                java.sql.Date dateDepart = parseSqlDate(rs.getObject("dateDepart"));
                LocalTime heureArrivee = parseSqlTime(rs.getObject("heureArrivee"));
                LocalTime heureDepart = parseSqlTime(rs.getObject("heureDepart"));

                double prixBase = rs.getDouble("prixBase");
                double prixVol = rs.getDouble("prixVol");

                Vol v;
                if ("International".equalsIgnoreCase(type)) {
                    v = new VolInternational(
                            rs.getString("numVol"),
                            rs.getInt("nbPlaces"),
                            prixBase,
                            prixVol,
                            StatutVol.valueOf(rs.getString("statut")),
                            rs.getString("paysDestination"),
                            escales,
                            reservations,
                            dateArrivee,
                            dateDepart,
                            heureArrivee,
                            aeroArr,
                            heureDepart,
                            aeroDep,
                            rs.getString("numeroAutorisation"),
                            rs.getBoolean("exigenceVisa")
                    );
                } else {
                    v = new VolNational(
                            rs.getString("numVol"),
                            rs.getInt("nbPlaces"),
                            prixBase,
                            prixVol,
                            StatutVol.valueOf(rs.getString("statut")),
                            rs.getString("paysDestination"),
                            escales,
                            reservations,
                            dateArrivee,
                            dateDepart,
                            heureArrivee,
                            aeroArr,
                            heureDepart,
                            aeroDep,
                            rs.getString("numeroAutorisation"),
                            rs.getString("terminal")
                    );
                }
                listeVols.add(v);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tableVols.setItems(listeVols);
        mettreAJourCompteur();
    }

    private java.sql.Date parseSqlDate(Object rawDate) {
        if (rawDate == null) return null;
        if (rawDate instanceof Number) {
            return new java.sql.Date(((Number) rawDate).longValue());
        }
        try {
            return java.sql.Date.valueOf(rawDate.toString());
        } catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
            return LocalTime.now();
        }
    }

    private Aeroport getAeroportById(String id) {
        for (Aeroport a : listeAeroports) {
            if (a.getIdAeroport().equals(id)) return a;
        }
        return null;
    }

    private void configurerTable() {
        colNumVol.setCellValueFactory(new PropertyValueFactory<>("numVol"));

        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue() instanceof VolInternational ? "International" : "National")
        );

        colDepart.setCellValueFactory(cellData -> {
            Aeroport aeroport = cellData.getValue().getAeroportDepart();
            String nom = (aeroport != null) ? aeroport.getNom() : "N/A";
            return new SimpleStringProperty(nom);
        });

        colArrivee.setCellValueFactory(cellData -> {
            Aeroport aeroport = cellData.getValue().getAeroportArrivee();
            String nom = (aeroport != null) ? aeroport.getNom() : "N/A";
            return new SimpleStringProperty(nom);
        });

        colDateDepart.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDateDepart();
            String dateStr = (date != null) ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(date) : "N/A";
            return new SimpleStringProperty(dateStr);
        });

        colHeureDepart.setCellValueFactory(cellData -> {
            LocalTime heure = cellData.getValue().getHeureDepart();
            String heureStr = (heure != null) ? heure.format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";
            return new SimpleStringProperty(heureStr);
        });

        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nbPlacesDisponibles"));

        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixVol"));

        colStatut.setCellValueFactory(cellData -> {
            StatutVol statut = cellData.getValue().getStatutVol();
            return new SimpleStringProperty(statut != null ? statut.toString() : "N/A");
        });

        colNumAutorisation.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof VolInternational) {
                return new SimpleStringProperty(((VolInternational)cellData.getValue()).getNumeroAutorisationInternationale());
            } else if (cellData.getValue() instanceof VolNational) {
                return new SimpleStringProperty(((VolNational)cellData.getValue()).getNumeroAutorisationNationale());
            } else {
                return new SimpleStringProperty("");
            }
        });

        colVisa.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof VolInternational) {
                VolInternational v = (VolInternational) cellData.getValue();
                if (v.isExigenceVisa()) {
                    return new SimpleStringProperty("50 €");
                } else {
                    return new SimpleStringProperty("non");
                }
            } else {
                return new SimpleStringProperty("non");
            }
        });

        colTerminal.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof VolNational) {
                return new SimpleStringProperty(((VolNational)cellData.getValue()).getTerminalDomestique());
            } else {
                return new SimpleStringProperty("");
            }
        });

        tableVols.setItems(listeVols);
    }

    @FXML
    private void onTypeVolChanged() {
        boolean isInternational = rbInternational.isSelected();

        if (isInternational) {
            lblChampSpecifique1.setText("N° Autorisation Inter.:");
            txtChampSpecifique1.setPromptText("Numéro d'autorisation internationale");
            lblChampSpecifique1.setVisible(true);
            txtChampSpecifique1.setVisible(true);

            lblChampSpecifique2.setText("Exigence Visa:");
            chkExigenceVisa.setVisible(true);

            lblTerminal.setVisible(false);
            txtTerminal.setVisible(false);

        } else {
            lblChampSpecifique1.setText("N° Autorisation Nat.:");
            txtChampSpecifique1.setPromptText("Numéro d'autorisation nationale");
            lblChampSpecifique1.setVisible(true);
            txtChampSpecifique1.setVisible(true);

            lblChampSpecifique2.setText("");
            chkExigenceVisa.setVisible(false);

            lblTerminal.setVisible(true);
            txtTerminal.setVisible(true);
        }
    }

    @FXML
    private void calculerPrixTotal() {
        try {
            double prixBase = txtPrixBase.getText().isEmpty() ? 0 : Double.parseDouble(txtPrixBase.getText());
            boolean isInternational = rbInternational.isSelected();
            double prixTotal;
            double taxes;

            if (isInternational) {
                double tarifSpecifique = prixBase * 1.15;
                taxes = prixBase * 0.20;
                double fraisVisa = chkExigenceVisa.isSelected() ? 50.0 : 0.0;
                prixTotal = tarifSpecifique + taxes + fraisVisa;

                lblDetailsPrix.setText(String.format("Base: %.2f€ | Tarif: %.2f€ | Taxes: %.2f€ | Visa: %.2f€",
                        prixBase, tarifSpecifique, taxes, fraisVisa));
            } else {
                double tarifSpecifique = prixBase * 0.95;
                taxes = prixBase * 0.10;
                prixTotal = tarifSpecifique + taxes;

                lblDetailsPrix.setText(String.format("Base: %.2f€ | Tarif: %.2f€ | Taxes domestiques: %.2f€",
                        prixBase, tarifSpecifique, taxes));
            }

            lblPrixTotal.setText(String.format("%.2f €", prixTotal));

        } catch (NumberFormatException e) {
            lblPrixTotal.setText("0.00 €");
            lblDetailsPrix.setText("Base: 0€ | Taxes: 0€");
        }
    }

    @FXML
    private void ajouterVol() {
        if (!validerChamps()) return;

        double prixBase = Double.parseDouble(txtPrixBase.getText().trim());
        double prixTotal;

        boolean isInternational = rbInternational.isSelected();
        if (isInternational) {
            double tarifSpecifique = prixBase * 1.15;
            double taxes = prixBase * 0.20;
            double fraisVisa = chkExigenceVisa.isSelected() ? 50.0 : 0.0;
            prixTotal = tarifSpecifique + taxes + fraisVisa;
        } else {
            double tarifSpecifique = prixBase * 0.95;
            double taxes = prixBase * 0.10;
            prixTotal = tarifSpecifique + taxes;
        }

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO vol (numVol, nbPlaces, statut, paysDestination, aeroportDepart, aeroportArrivee, dateDepart, dateArrivee, heureDepart, heureArrivee, prixBase, prixVol, type, numeroAutorisation, exigenceVisa, terminal) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, txtNumVol.getText().trim());
            stmt.setInt(2, spinPlaces.getValue());
            stmt.setString(3, cmbStatut.getValue().toString());
            stmt.setString(4, txtPaysDestination.getText().trim());
            stmt.setString(5, cmbAeroportDepart.getValue().getIdAeroport());
            stmt.setString(6, cmbAeroportArrivee.getValue().getIdAeroport());
            stmt.setDate(7, java.sql.Date.valueOf(dpDateDepart.getValue()));
            stmt.setDate(8, java.sql.Date.valueOf(dpDateArrivee.getValue()));

            String heureDepart = txtHeureDepart.getText().trim();
            if (!heureDepart.contains(":")) heureDepart += ":00:00";
            else if (heureDepart.split(":").length == 2) heureDepart += ":00";
            stmt.setString(9, heureDepart);

            String heureArrivee = txtHeureArrivee.getText().trim();
            if (!heureArrivee.contains(":")) heureArrivee += ":00:00";
            else if (heureArrivee.split(":").length == 2) heureArrivee += ":00";
            stmt.setString(10, heureArrivee);

            stmt.setDouble(11, prixBase);
            stmt.setDouble(12, prixTotal);

            if (isInternational) {
                stmt.setString(13, "International");
                stmt.setString(14, txtChampSpecifique1.getText().trim());
                stmt.setBoolean(15, chkExigenceVisa.isSelected());
                stmt.setNull(16, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(13, "National");
                stmt.setString(14, txtChampSpecifique1.getText().trim());
                stmt.setBoolean(15, false);
                stmt.setString(16, txtTerminal.getText().trim());
            }

            stmt.executeUpdate();
            actualiserListe();
            afficherSucces("Vol ajouté avec succès !");
            viderFormulaire();

        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ajout en base : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierVol() {
        if (volSelectionne == null || !validerChamps()) {
            afficherErreur("Veuillez sélectionner un vol à modifier et remplir tous les champs.");
            return;
        }
        double prixBase = Double.parseDouble(txtPrixBase.getText().trim());
        double prixTotal;

        boolean isInternational = rbInternational.isSelected();
        if (isInternational) {
            double tarifSpecifique = prixBase * 1.15;
            double taxes = prixBase * 0.20;
            double fraisVisa = chkExigenceVisa.isSelected() ? 50.0 : 0.0;
            prixTotal = tarifSpecifique + taxes + fraisVisa;
        } else {
            double tarifSpecifique = prixBase * 0.95;
            double taxes = prixBase * 0.10;
            prixTotal = tarifSpecifique + taxes;
        }

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE vol SET nbPlaces=?, statut=?, paysDestination=?, aeroportDepart=?, aeroportArrivee=?, dateDepart=?, dateArrivee=?, heureDepart=?, heureArrivee=?, prixBase=?, prixVol=?, type=?, numeroAutorisation=?, exigenceVisa=?, terminal=? WHERE numVol=?")) {

            stmt.setInt(1, spinPlaces.getValue());
            stmt.setString(2, cmbStatut.getValue().toString());
            stmt.setString(3, txtPaysDestination.getText().trim());
            stmt.setString(4, cmbAeroportDepart.getValue().getIdAeroport());
            stmt.setString(5, cmbAeroportArrivee.getValue().getIdAeroport());
            stmt.setDate(6, java.sql.Date.valueOf(dpDateDepart.getValue()));
            stmt.setDate(7, java.sql.Date.valueOf(dpDateArrivee.getValue()));

            String heureDepart = txtHeureDepart.getText().trim();
            if (!heureDepart.contains(":")) heureDepart += ":00:00";
            else if (heureDepart.split(":").length == 2) heureDepart += ":00";
            stmt.setString(8, heureDepart);

            String heureArrivee = txtHeureArrivee.getText().trim();
            if (!heureArrivee.contains(":")) heureArrivee += ":00:00";
            else if (heureArrivee.split(":").length == 2) heureArrivee += ":00";
            stmt.setString(9, heureArrivee);

            stmt.setDouble(10, prixBase);
            stmt.setDouble(11, prixTotal);

            if (isInternational) {
                stmt.setString(12, "International");
                stmt.setString(13, txtChampSpecifique1.getText().trim());
                stmt.setBoolean(14, chkExigenceVisa.isSelected());
                stmt.setNull(15, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(12, "National");
                stmt.setString(13, txtChampSpecifique1.getText().trim());
                stmt.setBoolean(14, false);
                stmt.setString(15, txtTerminal.getText().trim());
            }

            stmt.setString(16, txtNumVol.getText().trim());

            stmt.executeUpdate();
            actualiserListe();
            afficherSucces("Vol modifié avec succès !");
            viderFormulaire();
        } catch (Exception e) {
            afficherErreur("Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean hasVolDependencies(String numVol) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE numVol = ?";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numVol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("🔍 Vol " + numVol + " a " + count + " réservation(s)");
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
    private void supprimerVol() {
        if (volSelectionne == null) {
            afficherErreur("Veuillez sélectionner un vol à supprimer.");
            return;
        }

        if (hasVolDependencies(volSelectionne.getNumVol())) {

            afficherErreur("Impossible de supprimer ce vol.\n" +
                    "Il est associé à des réservations.\n" +
                    "Veuillez d'abord supprimer les réservations liées à ce vol.");
            return;
        }


        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le vol " + volSelectionne.getNumVol());
        confirmation.setContentText("Êtes-vous sûr ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM vol WHERE numVol = ?")) {
                stmt.setString(1, volSelectionne.getNumVol());
                stmt.executeUpdate();

                actualiserListe();
                afficherSucces("Vol supprimé avec succès !");
                viderFormulaire();
                volSelectionne = null;
            } catch (Exception e) {
                afficherErreur("Erreur lors de la suppression : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void annulerAction() {
        viderFormulaire();
        volSelectionne = null;
    }

    private void filtrerVols(String recherche) {
        if (recherche == null || recherche.isEmpty()) {
            tableVols.setItems(listeVols);
        } else {
            ObservableList<Vol> volsFiltres = listeVols.filtered(vol ->
                    vol.getNumVol().toLowerCase().contains(recherche.toLowerCase()) ||
                            (vol.getAeroportDepart() != null && vol.getAeroportDepart().getNom().toLowerCase().contains(recherche.toLowerCase())) ||
                            (vol.getAeroportArrivee() != null && vol.getAeroportArrivee().getNom().toLowerCase().contains(recherche.toLowerCase()))
            );
            tableVols.setItems(volsFiltres);
        }
    }

    private boolean validerChamps() {
        if (txtNumVol.getText() == null || txtNumVol.getText().trim().isEmpty()) {
            afficherErreur("Le numéro de vol est obligatoire");
            txtNumVol.requestFocus();
            return false;
        }

        if (cmbAeroportDepart.getValue() == null) {
            afficherErreur("Veuillez sélectionner un aéroport de départ");
            cmbAeroportDepart.requestFocus();
            return false;
        }
        if (cmbAeroportArrivee.getValue() == null) {
            afficherErreur("Veuillez sélectionner un aéroport d'arrivée");
            cmbAeroportArrivee.requestFocus();
            return false;
        }
        if (cmbAeroportDepart.getValue().getIdAeroport()
                .equals(cmbAeroportArrivee.getValue().getIdAeroport())) {
            afficherErreur("L'aéroport de départ et d'arrivée doivent être différents");
            return false;
        }

        if (dpDateDepart.getValue() == null) {
            afficherErreur("La date de départ est obligatoire");
            dpDateDepart.requestFocus();
            return false;
        }
        if (dpDateArrivee.getValue() == null) {
            afficherErreur("La date d'arrivée est obligatoire");
            dpDateArrivee.requestFocus();
            return false;
        }
        if (dpDateDepart.getValue().isAfter(dpDateArrivee.getValue())) {
            afficherErreur("La date de départ doit être antérieure ou égale à la date d'arrivée.");
            return false;
        }

        if (txtHeureDepart.getText() == null || txtHeureDepart.getText().trim().isEmpty()) {
            afficherErreur("L'heure de départ est obligatoire");
            txtHeureDepart.requestFocus();
            return false;
        }
        if (txtHeureArrivee.getText() == null || txtHeureArrivee.getText().trim().isEmpty()) {
            afficherErreur("L'heure d'arrivée est obligatoire");
            txtHeureArrivee.requestFocus();
            return false;
        }

        LocalTime heureDep;
        LocalTime heureArr;
        try {
            heureDep = LocalTime.parse(txtHeureDepart.getText().trim());
            heureArr = LocalTime.parse(txtHeureArrivee.getText().trim());
        } catch (Exception e) {
            afficherErreur("Format d'heure invalide. Utilisez HH:mm (ex: 15:30)");
            txtHeureDepart.requestFocus();
            return false;
        }

        if (dpDateDepart.getValue().equals(dpDateArrivee.getValue())) {
            if (!heureDep.isBefore(heureArr)) {
                afficherErreur("L'heure de départ doit être strictement avant l'heure d'arrivée pour le même jour.");
                txtHeureDepart.requestFocus();
                return false;
            }
        }

        if (txtPrixBase.getText() == null || txtPrixBase.getText().trim().isEmpty()) {
            afficherErreur("Le prix de base est obligatoire");
            txtPrixBase.requestFocus();
            return false;
        }
        try {
            double prix = Double.parseDouble(txtPrixBase.getText().trim());
            if (prix <= 0) {
                afficherErreur("Le prix doit être supérieur à 0");
                return false;
            }
        } catch (NumberFormatException e) {
            afficherErreur("Le prix doit être un nombre valide");
            txtPrixBase.requestFocus();
            return false;
        }

        if (cmbStatut.getValue() == null) {
            afficherErreur("Veuillez sélectionner un statut");
            cmbStatut.requestFocus();
            return false;
        }

        if (txtPaysDestination.getText() == null || txtPaysDestination.getText().trim().isEmpty()) {
            afficherErreur("Le pays de destination est obligatoire");
            txtPaysDestination.requestFocus();
            return false;
        }

        if (txtChampSpecifique1.getText() == null || txtChampSpecifique1.getText().trim().isEmpty()) {
            String typeVol = rbInternational.isSelected() ? "internationale" : "nationale";
            afficherErreur("Le numéro d'autorisation " + typeVol + " est obligatoire");
            txtChampSpecifique1.requestFocus();
            return false;
        }
        return true;
    }

    private void viderFormulaire() {
        txtNumVol.clear();
        txtPrixBase.clear();
        txtPaysDestination.clear();
        txtHeureDepart.clear();
        txtHeureArrivee.clear();
        txtChampSpecifique1.clear();
        cmbAeroportDepart.setValue(null);
        cmbAeroportArrivee.setValue(null);
        cmbStatut.setValue(null);
        dpDateDepart.setValue(null);
        dpDateArrivee.setValue(null);
        spinPlaces.getValueFactory().setValue(150);
        chkExigenceVisa.setSelected(false);
        rbInternational.setSelected(true);
        lblPrixTotal.setText("0.00 €");
        lblDetailsPrix.setText("Base: 0€ | Taxes: 0€");
        tableVols.getSelectionModel().clearSelection();
    }

    private void mettreAJourCompteur() {
        int nb = 0;
        try (Connection conn = ConnexionDB.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM vol")) {
            if (rs.next()) nb = rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        lblNombreVols.setText("Total: " + nb);
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void remplirFormulaire(Vol vol) {
        if (vol == null) return;

        txtNumVol.setText(vol.getNumVol());
        spinPlaces.getValueFactory().setValue(vol.getNbPlacesDisponibles());
        cmbStatut.setValue(vol.getStatutVol());
        txtPaysDestination.setText(vol.getPaysDestination());

        cmbAeroportDepart.getItems().stream()
                .filter(a -> a.getIdAeroport().equals(vol.getAeroportDepart().getIdAeroport()))
                .findFirst().ifPresent(cmbAeroportDepart::setValue);

        cmbAeroportArrivee.getItems().stream()
                .filter(a -> a.getIdAeroport().equals(vol.getAeroportArrivee().getIdAeroport()))
                .findFirst().ifPresent(cmbAeroportArrivee::setValue);

        if (vol.getDateDepart() != null) {
            dpDateDepart.setValue(((java.sql.Date)vol.getDateDepart()).toLocalDate());
        }
        if (vol.getDateArrivee() != null) {
            dpDateArrivee.setValue(((java.sql.Date)vol.getDateArrivee()).toLocalDate());
        }

        if (vol.getHeureDepart() != null) {
            txtHeureDepart.setText(vol.getHeureDepart().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        if (vol.getHeureArrivee() != null) {
            txtHeureArrivee.setText(vol.getHeureArrivee().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        txtPrixBase.setText(String.valueOf(vol.getPrixBase()));

        if (vol instanceof VolInternational) {
            rbInternational.setSelected(true);
            VolInternational v = (VolInternational) vol;
            txtChampSpecifique1.setText(v.getNumeroAutorisationInternationale());
            chkExigenceVisa.setSelected(v.isExigenceVisa());
            txtTerminal.clear();
            lblPrixTotal.setText(String.format("%.2f €", v.getPrixVol()));
            lblDetailsPrix.setText(String.format(
                    "Base: %.2f€ | Tarif: %.2f€ | Taxes: %.2f€ | Visa: %.2f€",
                    v.getPrixBase(), v.calculerTarifSpecifique(), v.calculerTaxesInternationales(), v.calculerFraisVisa()
            ));
        } else if (vol instanceof VolNational) {
            rbNational.setSelected(true);
            VolNational v = (VolNational) vol;
            txtChampSpecifique1.setText(v.getNumeroAutorisationNationale());
            txtTerminal.setText(v.getTerminalDomestique()!=null ? v.getTerminalDomestique():"");
            chkExigenceVisa.setSelected(false);

            lblPrixTotal.setText(String.format("%.2f €", v.getPrixVol()));
            lblDetailsPrix.setText(String.format(
                    "Base: %.2f€ | Tarif: %.2f€ | Taxes : %.2f€",
                    v.getPrixBase(), v.calculerTarifSpecifique(), v.calculerTaxesInternationales()
            ));
        }

        onTypeVolChanged();
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
    public void allerEmploye() {  try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_employes.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestion des Employés");
    } catch (Exception e) {
        e.printStackTrace();
    }

    }
    public void allerStats(ActionEvent actionEvent) {  try {
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
    private void suggérerHoraire() {
        if (cmbAeroportDepart.getValue() == null || dpDateDepart.getValue() == null) {
            afficherInfo("Choisissez d'abord l'aéroport de départ et la date.");
            return;
        }

        String aeroportDepId = cmbAeroportDepart.getValue().getIdAeroport();
        String terminal = txtTerminal.getText().trim();
        LocalTime heureProposee = LocalTime.of(8, 0);

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT heureDepart, heureArrivee FROM vol " +
                             "WHERE aeroportDepart = ? AND dateDepart = ? " +
                             "AND (terminal = ? OR ? = '')"
             )) {

            stmt.setString(1, aeroportDepId);
            stmt.setDate(2, java.sql.Date.valueOf(dpDateDepart.getValue()));
            stmt.setString(3, terminal);
            stmt.setString(4, terminal);

            ResultSet rs = stmt.executeQuery();

            List<LocalTime[]> intervalles = new ArrayList<>();
            while (rs.next()) {
                LocalTime hd = parseSqlTime(rs.getObject("heureDepart"));
                LocalTime ha = parseSqlTime(rs.getObject("heureArrivee"));
                if(hd != null && ha != null) {
                    intervalles.add(new LocalTime[]{hd, ha});
                }
            }

            int dureeMinutes = 120;
            boolean trouve = false;
            LocalTime finJournee = LocalTime.of(22, 0);

            while (!trouve && !heureProposee.plusMinutes(dureeMinutes).isAfter(finJournee)) {
                LocalTime finProposee = heureProposee.plusMinutes(dureeMinutes);
                boolean conflit = false;

                for (LocalTime[] intervalle : intervalles) {
                    LocalTime hd = intervalle[0];
                    LocalTime ha = intervalle[1];

                    if (!(finProposee.isBefore(hd) || heureProposee.isAfter(ha))) {
                        conflit = true;
                        break;
                    }
                }

                if (!conflit) {
                    trouve = true;
                } else {
                    heureProposee = heureProposee.plusMinutes(15);
                }
            }

            if (trouve) {
                txtHeureDepart.setText(heureProposee.toString().substring(0,5));
                afficherInfo("Créneau recommandé: " + heureProposee + " (2h, sans conflit détecté).");
            } else {
                afficherInfo("Aucun créneau de 2h libre trouvé entre 08:00 et 22:00.");
            }

        } catch (Exception e) {
            afficherErreur("Erreur lors du calcul de l'horaire: " + e.getMessage());
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
