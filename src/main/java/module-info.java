module com.example.projet_java_vols {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.base;


    opens com.example.projet_java_vols to javafx.fxml;
    opens com.example.projet_java_vols.Gestion_des_vols.Controller to javafx.fxml;
    opens com.example.projet_java_vols.Gestion_des_vols.Model to javafx.base, javafx.fxml;
    opens com.example.projet_java_vols.Gestion_des_utilisateurs.Controller to javafx.fxml;
    opens com.example.projet_java_vols.Gestion_des_utilisateurs.Model to javafx.base, javafx.fxml;
    exports com.example.projet_java_vols;


}