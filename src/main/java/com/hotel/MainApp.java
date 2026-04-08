package com.hotel;

import com.hotel.service.HotelService;
import com.hotel.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    private HotelService hotelService;
    private StackPane contentPane;

    private Button activeNav = null;

    @Override
    public void start(Stage primaryStage) {
        hotelService = new HotelService();

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());

        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #0d1b2a;");
        root.setCenter(contentPane);

        // Load dashboard by default
        showDashboard();

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(
            getClass().getResource("/com/hotel/styles/hotel.css").toExternalForm());

        primaryStage.setTitle("Hotel Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // Save on close
        primaryStage.setOnCloseRequest(e -> hotelService.saveData());

        primaryStage.show();
    }

    // Sidebar
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");

        sidebar.getChildren().add(buildBrand());

        sidebar.getChildren().add(sectionLabel("MAIN"));
        sidebar.getChildren().add(navBtn("🏠  Dashboard",    this::showDashboard));
        sidebar.getChildren().add(navBtn("🛏  Rooms",        this::showRooms));
        sidebar.getChildren().add(navBtn("📋  Booking",      this::showBooking));

        sidebar.getChildren().add(sectionLabel("FINANCE"));
        sidebar.getChildren().add(navBtn("💰  Billing",      this::showBilling));

        sidebar.getChildren().add(sectionLabel("SERVICE"));
        sidebar.getChildren().add(navBtn("🧹  Room Cleaning", this::showRoomCleaning));

        return sidebar;
    }

    private VBox buildBrand() {
        VBox brand = new VBox(4);
        brand.getStyleClass().add("sidebar-header");

        Label star = new Label("✦ OOSDL");
        star.getStyleClass().add("hotel-brand");

        Label name = new Label("Hotel Management");
        name.getStyleClass().add("hotel-name");

        Label tag = new Label("JavaFX Application");
        tag.getStyleClass().add("hotel-tagline");

        brand.getChildren().addAll(star, name, tag);
        return brand;
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("nav-section-label");
        return lbl;
    }

    private Button navBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            action.run();
            setActiveNav(btn);
        });
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNav != null) {
            activeNav.getStyleClass().remove("nav-btn-active");
        }
        btn.getStyleClass().add("nav-btn-active");
        activeNav = btn;
    }

    // View Switching
    private void showDashboard() {
        contentPane.getChildren().setAll(
            new DashboardView(hotelService).build());
    }

    private void showRooms() {
        contentPane.getChildren().setAll(
            new RoomManagementView(hotelService).build());
    }

    private void showRoomCleaning() {
        contentPane.getChildren().setAll(
            new RoomCleaningView(hotelService).build());
    }

    private void showBooking() {
        contentPane.getChildren().setAll(
            new BookingView(hotelService).build());
    }

    private void showBilling() {
        contentPane.getChildren().setAll(
            new BillingView(hotelService).build());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
