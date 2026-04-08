package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.service.HotelService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

public class RoomCleaningView {

    private final HotelService service;
    private TableView<Room> tableView;
    private ObservableList<Room> roomData;
    private Timeline refreshTimeline;

    public RoomCleaningView(HotelService service) {
        this.service = service;
    }

    public ScrollPane build() {
        VBox root = new VBox(22);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(30, 35, 30, 35));

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildDivider());
        root.getChildren().add(buildFilterBar());
        root.getChildren().add(buildTable());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #0d1b2a;");
        return scroll;
    }

    private VBox buildHeader() {
        VBox h = new VBox(4);
        Label brand = new Label("✦  HOUSEKEEPING");
        brand.getStyleClass().add("hotel-brand");
        Label title = new Label("Room Cleaning");
        title.getStyleClass().add("page-title");
        h.getChildren().addAll(brand, title);
        return h;
    }

    private Region buildDivider() {
        Region d = new Region();
        d.getStyleClass().add("gold-divider");
        return d;
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        Button all = filterBtn("All Rooms", () -> refreshTable(service.getAllRooms()));
        bar.getChildren().add(all);
        return bar;
    }

    private Button filterBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-secondary");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        tableView = new TableView<>();
        tableView.getStyleClass().add("custom-table");
        tableView.setPrefHeight(420);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Room, Integer> colNum = new TableColumn<>("Room No.");
        colNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colNum.setPrefWidth(90);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getRoomType().getDisplayName()));

        TableColumn<Room, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatusText()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add(
                    "Available".equals(item)   ? "badge-available"   :
                    "Maintenance".equals(item) ? "badge-maintenance" :
                                                  "badge-occupied");

                // Add cleaning indicator
                Room room = getTableView().getItems().get(getIndex());
                if (service.isRoomBeingCleaned(room.getRoomNumber())) {
                    badge.setText(item);
                    badge.setStyle("-fx-background-color: rgba(255,193,7,0.2); -fx-text-fill: #ffc107;");
                } else {
                    badge.setText(item);
                    badge.setStyle("");
                }

                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<Room, String> colClean = new TableColumn<>("Clean Room");
        colClean.setPrefWidth(120);
        colClean.setCellValueFactory(c -> new SimpleStringProperty("Clean"));
        colClean.setCellFactory(col -> new TableCell<>() {
            private final Button cleanBtn = new Button("Clean");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }

                Room room = getTableView().getItems().get(getIndex());
                boolean isBeingCleaned = service.isRoomBeingCleaned(room.getRoomNumber());

                cleanBtn.setDisable(false);
                cleanBtn.setText("Clean");
                cleanBtn.getStyleClass().setAll("btn-secondary");
                cleanBtn.setStyle("");
                cleanBtn.setOnAction(null);

                if (isBeingCleaned) {
                    cleanBtn.setText("Cleaning...");
                    cleanBtn.setDisable(true);
                    cleanBtn.setStyle("-fx-background-color: rgba(255,193,7,0.2); -fx-text-fill: #ffc107;");
                } else {
                    cleanBtn.setOnAction(e -> {
                        service.startRoomCleaning(room.getRoomNumber());
                        refreshTableSilently();
                        startAutoRefresh();
                    });
                }

                setGraphic(cleanBtn);
                setText(null);
            }
        });

        tableView.getColumns().addAll(colNum, colType, colStatus, colClean);

        roomData = FXCollections.observableArrayList(service.getAllRooms());
        tableView.setItems(roomData);
        tableView.setPlaceholder(new Label("No rooms found."));

        // Start auto-refresh timeline to update cleaning status
        startAutoRefresh();

        return new VBox(tableView);
    }

    private void startAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> refreshTableSilently())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    public void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    private void refreshTableSilently() {
        // Refresh the table data without changing the current filter
        roomData.setAll(service.getAllRooms());
        if (tableView != null) {
            tableView.refresh();
        }
    }

    private void refreshTable(List<Room> rooms) {
        roomData.setAll(rooms);
        if (tableView != null) {
            tableView.refresh();
        }
    }
}