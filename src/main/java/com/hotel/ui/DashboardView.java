package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.service.HotelService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class DashboardView {

    private final HotelService service;

    public DashboardView(HotelService service) {
        this.service = service;
    }

    public ScrollPane build() {
        VBox root = new VBox(24);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(30, 35, 30, 35));

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildDivider());

        root.getChildren().add(buildStatsRow());
        root.getChildren().add(buildRecentRooms());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("custom-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #0d1b2a;");
        return scroll;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        Label brand = new Label("✦  OVERVIEW");
        brand.getStyleClass().add("hotel-brand");

        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        header.getChildren().addAll(brand, title);
        return header;
    }

    private Region buildDivider() {
        Region div = new Region();
        div.getStyleClass().add("gold-divider");
        return div;
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        int total     = service.getTotalRooms();
        int available = service.getAvailableCount();
        int occupied  = service.getOccupiedCount();
        double revenue = service.getTotalRevenue();

        row.getChildren().addAll(
            statCard("🏨", String.valueOf(total),    "Total Rooms",     false),
            statCard("✅", String.valueOf(available), "Available",       false),
            statCard("🔑", String.valueOf(occupied),  "Occupied",        false),
            statCard("₹",  String.format("%.0f", revenue), "Revenue (Session)", true)
        );

        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(3), Priority.ALWAYS);

        return row;
    }

    private VBox statCard(String icon, String value, String label, boolean gold) {
        VBox card = new VBox(6);
        card.getStyleClass().add(gold ? "stat-card-gold" : "stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        Label ico = new Label(icon + "  " + value);
        ico.getStyleClass().add("stat-number");

        Label lbl = new Label(label.toUpperCase());
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(ico, lbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox buildRecentRooms() {
        VBox section = new VBox(14);

        Label heading = new Label("All Rooms at a Glance");
        heading.setStyle("-fx-text-fill: #e8c96f; -fx-font-size: 16px; -fx-font-weight: bold;");

        FlowPane grid = new FlowPane();
        grid.setHgap(10);
        grid.setVgap(10);

        List<Room> rooms = service.getAllRooms();
        for (Room room : rooms) {
            grid.getChildren().add(buildRoomTile(room));
        }

        section.getChildren().addAll(heading, grid);
        return section;
    }

    private VBox buildRoomTile(Room room) {
    VBox tile = new VBox(6);
    tile.setPrefWidth(160);
    tile.setPrefHeight(110);
    tile.setPadding(new Insets(14, 16, 14, 16));
    tile.setAlignment(Pos.TOP_LEFT);

    boolean maintenance = room.isUnderMaintenance();
    boolean avail = room.isAvailable();

    String bgColor;
    String borderColor;

    if (maintenance) {
        bgColor = "#1a1325";
        borderColor = "#9b59b6";
    } else if (avail) {
        bgColor = "#162438";
        borderColor = "#27ae60";
    } else {
        bgColor = "#1e1224";
        borderColor = "#c0392b";
    }

    tile.setStyle("-fx-background-color: " + bgColor + ";" +
                  "-fx-background-radius: 8px;" +
                  "-fx-border-color: " + borderColor + ";" +
                  "-fx-border-width: 1;" +
                  "-fx-border-radius: 8px;");

    Label num = new Label("Room " + room.getRoomNumber());
    num.setStyle("-fx-text-fill: #f5f0e8; -fx-font-weight: bold; -fx-font-size: 14px;");

    Label type = new Label(room.getRoomType().getDisplayName());
    type.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 11px;");

    Label price = new Label("₹" + String.format("%.0f", room.getPricePerNight()) + "/night");
    price.setStyle("-fx-text-fill: #8a9bb5; -fx-font-size: 11px;");

    Label status;
    if (maintenance) {
        status = new Label("● Maintenance");
        status.setStyle("-fx-text-fill: #9b59b6; -fx-font-size: 11px; -fx-font-weight: bold;");
    } else if (avail) {
        status = new Label("● Available");
        status.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px; -fx-font-weight: bold;");
    } else {
        status = new Label("● Occupied");
        status.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-weight: bold;");
    }

    if (!avail && !maintenance) {
        Label guest = new Label(room.getGuestName());
        guest.setStyle("-fx-text-fill: #8a9bb5; -fx-font-size: 10px; -fx-font-style: italic;");
        tile.getChildren().addAll(num, type, price, status, guest);
    } else {
        tile.getChildren().addAll(num, type, price, status);
    }

    return tile;
}
}
