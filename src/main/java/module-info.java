module com.aypak.filetimecheck {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.aypak.filetimecheck to javafx.fxml;
    exports com.aypak.filetimecheck;
}