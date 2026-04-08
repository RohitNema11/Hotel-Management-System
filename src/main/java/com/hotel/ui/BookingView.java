package com.hotel.ui;

import com.hotel.model.Bill;
import com.hotel.model.Room;
import com.hotel.service.HotelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BookingView {

    private final HotelService service;
    private TableView<Room> occupiedTable;
    private ObservableList<Room> occupiedData;
    private Label msgLabel;
    private TextArea receiptArea;
    private ComboBox<Room> checkoutCombo;
    private ComboBox<Room> extendCombo;

    public BookingView(HotelService service) {
        this.service = service;
    }

    public ScrollPane build() {
        VBox root = new VBox(22);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(30, 35, 30, 35));

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildDivider());

        HBox columns = new HBox(20);
        columns.getChildren().addAll(buildBookingForm(), buildActionPanel());
        HBox.setHgrow(columns.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(columns.getChildren().get(1), Priority.ALWAYS);

        root.getChildren().add(columns);
        root.getChildren().add(buildOccupiedTable());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #0d1b2a;");
        return scroll;
    }

    private VBox buildHeader() {
        VBox h = new VBox(4);
        Label brand = new Label("✦  RESERVATIONS");
        brand.getStyleClass().add("hotel-brand");
        Label title = new Label("Booking & Checkout");
        title.getStyleClass().add("page-title");
        h.getChildren().addAll(brand, title);
        return h;
    }

    private Region buildDivider() {
        Region d = new Region();
        d.getStyleClass().add("gold-divider");
        return d;
    }

    private VBox buildBookingForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("🛎  Book a Room");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);

        Label lRoom    = lbl("Available Room");
        Label lGuest   = lbl("Guest Name");
        Label lContact = lbl("Contact No.");
        Label lDays    = lbl("No. of Days");

        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.setItems(FXCollections.observableArrayList(service.getAvailableRooms()));
        roomCombo.setPromptText("Select room");
        roomCombo.getStyleClass().add("form-combo");
        roomCombo.setPrefWidth(230);
        roomCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null :
                    "Room " + r.getRoomNumber() + " — " +
                    r.getRoomType().getDisplayName() +
                    " (₹" + String.format("%.0f", r.getPricePerNight()) + "/night)");
            }
        });
        roomCombo.setButtonCell(roomCombo.getCellFactory().call(null));

        TextField fGuest   = field("e.g. Anjali Sharma");
        TextField fContact = field("e.g. 9876543210");
        TextField fDays    = field("e.g. 3");

        msgLabel = new Label("");
        msgLabel.setWrapText(true);

        Button bookBtn = new Button("Confirm Booking");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setOnAction(e -> {
            Room selected = roomCombo.getValue();
            if (selected == null) { showMsg("Please select a room.", false); return; }

            String validationError = validateInputs(
                fGuest.getText(), fContact.getText(), fDays.getText()
            );
            if (validationError != null) { showMsg(validationError, false); return; }

            String result = service.bookRoom(
                selected.getRoomNumber(),
                fGuest.getText(),
                fContact.getText(),
                parseDays(fDays.getText())
            );
            if (result.startsWith("SUCCESS")) {
                showMsg(result.substring(8), true);
                fGuest.clear(); fContact.clear(); fDays.clear();
                roomCombo.setItems(FXCollections.observableArrayList(service.getAvailableRooms()));
                roomCombo.setValue(null);
                refreshOccupied();
            } else {
                showMsg(result.substring(6), false);
            }
        });

        grid.add(lRoom,    0, 0); grid.add(roomCombo, 1, 0);
        grid.add(lGuest,   0, 1); grid.add(fGuest,    1, 1);
        grid.add(lContact, 0, 2); grid.add(fContact,  1, 2);
        grid.add(lDays,    0, 3); grid.add(fDays,     1, 3);

        card.getChildren().addAll(title, grid, new HBox(bookBtn), msgLabel);
        return card;
    }

    private VBox buildActionPanel() {
        VBox wrapper = new VBox(16);
        wrapper.getChildren().addAll(buildCheckoutPanel(), buildExtendStayPanel());
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
    }

    private VBox buildExtendStayPanel() {
        VBox card = new VBox(16);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("🕒  Extend Stay");
        title.getStyleClass().add("card-title");

        Label lRoom = lbl("Occupied Room");
        Label lExtraDays = lbl("Extra Nights");

        extendCombo = new ComboBox<>();
        extendCombo.setItems(FXCollections.observableArrayList(service.getOccupiedRooms()));
        extendCombo.setPromptText("Select room to extend");
        extendCombo.getStyleClass().add("form-combo");
        extendCombo.setPrefWidth(230);
        extendCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null :
                    "Room " + r.getRoomNumber() + " — " + r.getGuestName() +
                    " (" + r.getDaysBooked() + " nights)");
            }
        });
        extendCombo.setButtonCell(extendCombo.getCellFactory().call(null));

        TextField fExtraDays = field("e.g. 2");

        Button extendBtn = new Button("Confirm Extension");
        extendBtn.getStyleClass().add("btn-secondary");
        extendBtn.setOnAction(e -> {
            Room selected = extendCombo.getValue();
            if (selected == null) { showMsg("Please select an occupied room.", false); return; }

            int extraDays = parseDays(fExtraDays.getText());
            if (extraDays <= 0) {
                showMsg("Extra nights must be at least 1.", false);
                return;
            }

            String result = service.extendStay(selected.getRoomNumber(), extraDays);
            if (result.startsWith("SUCCESS")) {
                showMsg(result.substring(8), true);
                fExtraDays.clear();
                extendCombo.setItems(FXCollections.observableArrayList(service.getOccupiedRooms()));
                extendCombo.setValue(null);
                checkoutCombo.setItems(FXCollections.observableArrayList(service.getOccupiedRooms()));
                refreshOccupied();
            } else {
                showMsg(result.substring(6), false);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.add(lRoom, 0, 0); grid.add(extendCombo, 1, 0);
        grid.add(lExtraDays, 0, 1); grid.add(fExtraDays, 1, 1);

        card.getChildren().addAll(title, grid, new HBox(extendBtn));
        return card;
    }

    private VBox buildCheckoutPanel() {
        VBox card = new VBox(16);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("🔓  Checkout Guest");
        title.getStyleClass().add("card-title");

        Label lRoom = lbl("Room to Checkout");

        checkoutCombo = new ComboBox<>();
        checkoutCombo.setItems(FXCollections.observableArrayList(service.getOccupiedRooms()));
        checkoutCombo.setPromptText("Select occupied room");
        checkoutCombo.getStyleClass().add("form-combo");
        checkoutCombo.setPrefWidth(230);
        checkoutCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null :
                    "Room " + r.getRoomNumber() + " — " + r.getGuestName());
            }
        });
        checkoutCombo.setButtonCell(checkoutCombo.getCellFactory().call(null));

        Label receiptTitle = new Label("Bill Receipt:");
        receiptTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12px; -fx-font-weight: bold;");

        receiptArea = new TextArea();
        receiptArea.getStyleClass().add("receipt-area");
        receiptArea.setEditable(false);
        receiptArea.setPrefHeight(220);
        receiptArea.setPromptText("Bill will appear here after checkout...");

        Button checkoutBtn = new Button("Checkout & Generate Bill");
        checkoutBtn.getStyleClass().add("btn-danger");
        checkoutBtn.setOnAction(e -> {
            Room selected = checkoutCombo.getValue();
            if (selected == null) return;

            Bill bill = service.checkout(selected.getRoomNumber());
            if (bill != null) {
                receiptArea.setText(bill.generateReceipt());
                checkoutCombo.setValue(null);
                refreshOccupied();
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.add(lRoom, 0, 0); grid.add(checkoutCombo, 1, 0);

        card.getChildren().addAll(title, grid,
            new HBox(checkoutBtn), receiptTitle, receiptArea);
        return card;
    }

    private VBox buildOccupiedTable() {
        VBox section = new VBox(12);

        Label heading = new Label("Currently Occupied Rooms");
        heading.setStyle("-fx-text-fill: #e8c96f; -fx-font-size: 15px; -fx-font-weight: bold;");

        occupiedTable = new TableView<>();
        occupiedTable.getStyleClass().add("custom-table");
        occupiedTable.setPrefHeight(220);
        occupiedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Room, String> c1 = col("Room", c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getRoomNumber())));
        TableColumn<Room, String> c2 = col("Type", c ->
            new SimpleStringProperty(c.getValue().getRoomType().getDisplayName()));
        TableColumn<Room, String> c3 = col("Guest", c ->
            new SimpleStringProperty(c.getValue().getGuestName()));
        TableColumn<Room, String> c4 = col("Contact", c ->
            new SimpleStringProperty(c.getValue().getGuestContact()));
        TableColumn<Room, String> c5 = col("Days", c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getDaysBooked())));
        TableColumn<Room, String> c6 = col("Estimated Bill", c -> {
            Double bill = c.getValue().calculateBill();
            return new SimpleStringProperty("₹" + String.format("%.2f", bill));
        });

        TableColumn<Room, String> c7 = new TableColumn<>("Action");
        c7.setPrefWidth(110);
        c7.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("✕  Cancel");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                cancelBtn.setStyle(
                    "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                    "-fx-background-radius: 4px; -fx-font-size: 11px;" +
                    "-fx-cursor: hand;");

                cancelBtn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Cancel Booking");
                    alert.setHeaderText("Cancel booking for Room " + room.getRoomNumber() + "?");
                    alert.setContentText(
                        "Guest   : " + room.getGuestName() + "\n" +
                        "Contact : " + room.getGuestContact() + "\n\n" +
                        "Full amount will be refunded.");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            boolean success = service.cancelBooking(room.getRoomNumber());
                            if (success) {
                                showMsg("Booking for Room " + room.getRoomNumber()
                                    + " cancelled. Full amount refunded.", true);
                                refreshOccupied();
                            }
                        }
                    });
                });

                setGraphic(cancelBtn);
                setText(null);
            }
        });

        occupiedTable.getColumns().addAll(c1, c2, c3, c4, c5, c6, c7);
        occupiedData = FXCollections.observableArrayList(service.getOccupiedRooms());
        occupiedTable.setItems(occupiedData);
        occupiedTable.setPlaceholder(new Label("No rooms are currently occupied."));

        section.getChildren().addAll(heading, occupiedTable);
        return section;
    }


    private void refreshOccupied() {
        if (occupiedData != null)
            occupiedData.setAll(service.getOccupiedRooms());
        if (checkoutCombo != null)
            checkoutCombo.setItems(
                FXCollections.observableArrayList(service.getOccupiedRooms()));
        if (extendCombo != null)
            extendCombo.setItems(
                FXCollections.observableArrayList(service.getOccupiedRooms()));
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        return l;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("form-field");
        tf.setPrefWidth(230);
        return tf;
    }

    private int parseDays(String text) {
        try { return Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private void showMsg(String msg, boolean success) {
        msgLabel.setText(msg);
        msgLabel.getStyleClass().removeAll("msg-success", "msg-error");
        msgLabel.getStyleClass().add(success ? "msg-success" : "msg-error");
    }

    private String validateInputs(String guestName, String contact, String daysText) {
        
        if (guestName.isBlank())
            return "Guest name cannot be empty.";
        if (!guestName.trim().matches("[a-zA-Z ]+"))
            return "Guest name must contain only alphabets.";

        
        if (contact.isBlank())
            return "Contact number cannot be empty.";
        if (!contact.trim().matches("\\d{10}"))
            return "Contact must contain exactly 10 digits.";

        
        if (daysText.isBlank())
            return "Number of days cannot be empty.";
        if (!daysText.trim().matches("\\d+"))
            return "Number of days must be a valid number.";
        if (Integer.parseInt(daysText.trim()) <= 0)
            return "Number of days must be at least 1.";

        return null; 
    }

    private <S> TableColumn<S, String> col(String title,
            javafx.util.Callback<TableColumn.CellDataFeatures<S, String>,
            javafx.beans.value.ObservableValue<String>> factory) {
        TableColumn<S, String> c = new TableColumn<>(title);
        c.setCellValueFactory(factory);
        return c;
    }
}