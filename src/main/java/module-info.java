module org.example.hellofirefly {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.hellofirefly to javafx.fxml;
    exports org.example.hellofirefly;
}