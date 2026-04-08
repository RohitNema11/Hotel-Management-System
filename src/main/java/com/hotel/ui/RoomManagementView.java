package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.service.HotelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.UnaryOperator;

public class RoomManagementView {

    private final HotelService service;
    private TableView<Room> tableView;
    private Label messageLabel;
    private ObservableList<Room> roomData;

    public RoomManagementView(HotelService service) {
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
        
        // Place both forms side by side with equal widths
        HBox formsContainer = new HBox(20);
        formsContainer.setFillHeight(false);
        
        VBox addRoomForm = buildAddRoomForm();
        VBox editPricesForm = buildEditPricesForm();
        
        HBox.setHgrow(addRoomForm, Priority.ALWAYS);
        HBox.setHgrow(editPricesForm, Priority.ALWAYS);
        
        formsContainer.getChildren().addAll(addRoomForm, editPricesForm);
        root.getChildren().add(formsContainer);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #0d1b2a;");
        return scroll;
    }

    private VBox buildHeader() {
        VBox h = new VBox(4);
        Label brand = new Label("✦  ROOM MANAGEMENT");
        brand.getStyleClass().add("hotel-brand");
        Label title = new Label("All Rooms");
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

        Button all        = filterBtn("All Rooms",      () -> refreshTable(service.getAllRooms()));
        Button avail      = filterBtn("Available",      () -> refreshTable(service.getAvailableRooms()));
        Button occ        = filterBtn("Occupied",       () -> refreshTable(service.getOccupiedRooms()));
        Button maint      = filterBtn("Maintenance",    () -> refreshTable(service.getMaintenanceRooms()));

        bar.getChildren().addAll(all, avail, occ, maint);
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
        tableView.setPrefHeight(300);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Room, Integer> colNum = new TableColumn<>("Room No.");
        colNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colNum.setPrefWidth(90);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getRoomType().getDisplayName()));

        TableColumn<Room, String> colPrice = new TableColumn<>("Price/Night");
        colPrice.setCellValueFactory(c ->
            new SimpleStringProperty("₹" + String.format("%.0f", c.getValue().getPricePerNight())));

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
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<Room, String> colGuest = new TableColumn<>("Guest");
        colGuest.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().isAvailable() ? "—" : c.getValue().getGuestName()));

        TableColumn<Room, String> colDays = new TableColumn<>("Days");
        colDays.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().isAvailable() ? "—" : String.valueOf(c.getValue().getDaysBooked())));
        colDays.setPrefWidth(60);

        TableColumn<Room, String> colMaint = new TableColumn<>("Maintenance");
        colMaint.setPrefWidth(120);
        colMaint.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().isUnderMaintenance() ? "ON" : "OFF"));
        colMaint.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }

                Room room = getTableView().getItems().get(getIndex());
                boolean underMaintenance = room.isUnderMaintenance();
                boolean occupiedByGuest  = !room.isAvailable() && !underMaintenance;

                if (occupiedByGuest) {
                    toggleBtn.setText("Occupied");
                    toggleBtn.setDisable(true);
                    toggleBtn.setStyle(
                        "-fx-background-color: rgba(192,57,43,0.2); -fx-text-fill: #e74c3c;" +
                        "-fx-border-color: #e74c3c; -fx-border-width: 1; -fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px; -fx-font-size: 11px; -fx-font-weight: bold;" + "-fx-opacity: 1.0;");
                } else {
                    toggleBtn.setText(underMaintenance ? "🔧 ON" : "OFF");
                    toggleBtn.setDisable(false);
                    toggleBtn.setStyle(underMaintenance
                        ? "-fx-background-color: rgba(230,169,34,0.2); -fx-text-fill: #e6a922;" +
                        "-fx-border-color: #e6a922; -fx-border-width: 1; -fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px; -fx-font-size: 11px; -fx-font-weight: bold;"
                        : "-fx-background-color: rgba(211,84,0,0.2); -fx-text-fill: #ce7629;" +
                        "-fx-border-color: #e67e22; -fx-border-width: 1; -fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px; -fx-font-size: 11px; -fx-font-weight: bold;");

                    toggleBtn.setOnAction(e -> {
                        boolean success = service.toggleMaintenance(room.getRoomNumber());
                        if (success) {
                            refreshTable(service.getAllRooms());
                        } else {
                            showMessage("Cannot toggle — room is occupied by a guest.", false);
                        }
                    });
                }
                setGraphic(toggleBtn);
                setText(null);
            }
        });

        TableColumn<Room, String> colEdit = new TableColumn<>("Edit");
        colEdit.setPrefWidth(100);
        colEdit.setCellValueFactory(c -> new SimpleStringProperty("Edit"));
        colEdit.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✎ Edit");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }

                Room room = getTableView().getItems().get(getIndex());
                editBtn.getStyleClass().clear();
                editBtn.getStyleClass().add("btn-secondary");
                editBtn.setStyle("-fx-font-size: 11px;");
                editBtn.setOnAction(e -> showEditRoomDialog(room));
                setGraphic(editBtn);
                setText(null);
            }
        });

        tableView.getColumns().addAll(colNum, colType, colPrice, colStatus, colGuest, colDays, colMaint, colEdit);

        roomData = FXCollections.observableArrayList(service.getAllRooms());
        tableView.setItems(roomData);
        tableView.setPlaceholder(new Label("No rooms found."));

        return new VBox(tableView);
    }

    private VBox buildAddRoomForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("form-card");

        Label title = new Label("➕  Add New Room");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        Label lNum  = new Label("Room Number"); lNum.getStyleClass().add("form-label");
        Label lType = new Label("Room Type");   lType.getStyleClass().add("form-label");

        TextField fNum = new TextField();
        fNum.setPromptText("e.g. 305");
        fNum.getStyleClass().add("form-field");

        ComboBox<Room.RoomType> fType = new ComboBox<>();
        fType.getItems().addAll(Room.RoomType.values());
        fType.setPromptText("Select type");
        fType.getStyleClass().add("form-combo");
        fType.setPrefWidth(200);

        Label priceHint = new Label("");
        priceHint.setStyle("-fx-text-fill: #8a9bb5; -fx-font-size: 11px;");
        fType.setOnAction(e -> {
            if (fType.getValue() != null) {
                priceHint.setText("Base price: ₹" + fType.getValue().getBasePrice() + "/night");
            }
        });

        messageLabel = new Label("");
        messageLabel.setWrapText(true);

        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            try {
                int num = Integer.parseInt(fNum.getText().trim());
                Room.RoomType type = fType.getValue();
                if (type == null) {
                    showMessage("Please select a room type.", false);
                    return;
                }
                Room room = new Room(num, type);
                boolean ok = service.addRoom(room);
                if (ok) {
                    showMessage("Room " + num + " added successfully!", true);
                    refreshTable(service.getAllRooms());
                    fNum.clear();
                    fType.setValue(null);
                    priceHint.setText("");
                } else {
                    showMessage("Room " + num + " already exists.", false);
                }
            } catch (NumberFormatException ex) {
                showMessage("Room number must be a valid integer.", false);
            }
        });

        grid.add(lNum,  0, 0); grid.add(fNum,  1, 0);
        grid.add(lType, 0, 1); grid.add(fType, 1, 1);
        grid.add(new Label(""), 0, 2); grid.add(priceHint, 1, 2);

        HBox btnRow = new HBox(addBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, grid, btnRow, messageLabel);
        return card;
    }

    private void refreshTable(List<Room> rooms) {
        roomData.setAll(rooms);
    }

    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("msg-success", "msg-error");
        messageLabel.getStyleClass().add(success ? "msg-success" : "msg-error");
    }

    private VBox buildEditPricesForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("form-card");

        Label title = new Label("💰  Update Room Type Prices");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        Label lType = new Label("Room Type"); lType.getStyleClass().add("form-label");
        Label lPrice = new Label("New Price/Night"); lPrice.getStyleClass().add("form-label");

        ComboBox<Room.RoomType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Room.RoomType.values());
        typeCombo.setPromptText("Select type");
        typeCombo.getStyleClass().add("form-combo");
        typeCombo.setPrefWidth(200);

        Label currentPrice = new Label("");
        currentPrice.setStyle("-fx-text-fill: #8a9bb5; -fx-font-size: 11px;");
        typeCombo.setOnAction(e -> {
            if (typeCombo.getValue() != null) {
                currentPrice.setText("Current base price: ₹" + typeCombo.getValue().getBasePrice());
            }
        });

        TextField priceField = new TextField();
        priceField.setPromptText("Enter new price");
        priceField.getStyleClass().add("form-field");
        priceField.setTextFormatter(new TextFormatter<>(getNumericFilter()));

        Label resultLabel = new Label("");
        resultLabel.setWrapText(true);

        Button updateBtn = new Button("Update All Rooms of Type");
        updateBtn.getStyleClass().add("btn-primary");
        updateBtn.setOnAction(e -> {
            try {
                Room.RoomType type = typeCombo.getValue();
                if (type == null) {
                    resultLabel.setText("Please select a room type.");
                    resultLabel.getStyleClass().clear();
                    resultLabel.getStyleClass().add("msg-error");
                    return;
                }
                double price = Double.parseDouble(priceField.getText().trim());
                String result = service.updateAllRoomsOfType(type, price);
                boolean success = result.startsWith("SUCCESS");
                resultLabel.setText(result.substring(result.indexOf(":") + 1));
                resultLabel.getStyleClass().clear();
                resultLabel.getStyleClass().add(success ? "msg-success" : "msg-error");
                if (success) {
                    refreshTable(service.getAllRooms());
                    priceField.clear();
                    typeCombo.setValue(null);
                    currentPrice.setText("");
                }
            } catch (NumberFormatException ex) {
                resultLabel.setText("Price must be a valid number.");
                resultLabel.getStyleClass().clear();
                resultLabel.getStyleClass().add("msg-error");
            }
        });

        grid.add(lType, 0, 0); grid.add(typeCombo, 1, 0);
        grid.add(new Label(""), 0, 1); grid.add(currentPrice, 1, 1);
        grid.add(lPrice, 0, 2); grid.add(priceField, 1, 2);

        HBox btnRow = new HBox(updateBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, grid, btnRow, resultLabel);
        return card;
    }

    private void showEditRoomDialog(Room room) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Room " + room.getRoomNumber());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/hotel/styles/hotel.css").toExternalForm());

        VBox content = new VBox(14);
        content.setPadding(new Insets(16));
        content.getStyleClass().add("dialog-content");

        Label typeLabel = new Label("Room Type:");
        typeLabel.getStyleClass().add("form-label");
        ComboBox<Room.RoomType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Room.RoomType.values());
        typeCombo.setValue(room.getRoomType());
        typeCombo.getStyleClass().add("form-combo");

        Label priceLabel = new Label("Price per Night:");
        priceLabel.getStyleClass().add("form-label");
        TextField priceField = new TextField(String.valueOf(room.getPricePerNight()));
        priceField.getStyleClass().add("form-field");
        priceField.setTextFormatter(new TextFormatter<>(getNumericFilter()));

        content.getChildren().addAll(
            typeLabel, typeCombo,
            priceLabel, priceField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return "OK";
            }
            return null;
        });

        if (dialog.showAndWait().isPresent()) {
            try {
                Room.RoomType newType = typeCombo.getValue();
                double newPrice = Double.parseDouble(priceField.getText().trim());

                if (newType == null) {
                    showMessage("Invalid room type selected.", false);
                    return;
                }

                // If type changed, use changeRoomType which sets it to enum's base price
                if (newType != room.getRoomType()) {
                    String result = service.changeRoomType(room.getRoomNumber(), newType);
                    boolean success = result.startsWith("SUCCESS");
                    showMessage(result.substring(result.indexOf(":") + 1), success);
                    if (success) {
                        refreshTable(service.getAllRooms());
                    }
                } else if (newPrice != room.getPricePerNight()) {
                    // Only price changed, not type
                    String result = service.setRoomPrice(room.getRoomNumber(), newPrice);
                    boolean success = result.startsWith("SUCCESS");
                    showMessage(result.substring(result.indexOf(":") + 1), success);
                    if (success) {
                        refreshTable(service.getAllRooms());
                    }
                } else {
                    showMessage("No changes made.", true);
                }
            } catch (NumberFormatException ex) {
                showMessage("Price must be a valid number.", false);
            }
        }
    }

    private UnaryOperator<TextFormatter.Change> getNumericFilter() {
        return change -> {
            String newText = change.getControlNewText();
            // Allow only digits and one decimal point
            if (newText.matches("^(\\d*\\.?\\d*)?$")) {
                return change;
            }
            return null; // Reject the change
        };
    }
}