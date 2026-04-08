module com.hotel {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;

    opens com.hotel to javafx.fxml;
    opens com.hotel.model to javafx.base;
    opens com.hotel.ui to javafx.fxml;

    exports com.hotel;
    exports com.hotel.model;
    exports com.hotel.service;
    exports com.hotel.ui;
}
