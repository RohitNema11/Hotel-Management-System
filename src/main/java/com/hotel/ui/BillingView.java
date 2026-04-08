package com.hotel.ui;

import com.hotel.model.Bill;
import com.hotel.service.HotelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BillingView {

    private final HotelService service;

    public BillingView(HotelService service) {
        this.service = service;
    }

    public ScrollPane build() {
        VBox root = new VBox(22);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(30, 35, 30, 35));

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildDivider());
        root.getChildren().add(buildSessionBills());
        root.getChildren().add(buildFileLog());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #0d1b2a;");
        return scroll;
    }

    private VBox buildHeader() {
        VBox h = new VBox(4);
        Label brand = new Label("✦  BILLING");
        brand.getStyleClass().add("hotel-brand");
        Label title = new Label("Revenue & Bills");
        title.getStyleClass().add("page-title");
        h.getChildren().addAll(brand, title);
        return h;
    }

    private Region buildDivider() {
        Region d = new Region(); d.getStyleClass().add("gold-divider"); return d;
    }

    private VBox buildSessionBills() {
        VBox section = new VBox(12);

        Label heading = new Label("Session Bills");
        heading.setStyle("-fx-text-fill: #e8c96f; -fx-font-size: 15px; -fx-font-weight: bold;");

        TableView<Bill> table = new TableView<>();
        table.getStyleClass().add("custom-table");
        table.setPrefHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Bill, String> c1 = col("Bill #", c ->
            new SimpleStringProperty("#" + c.getValue().getBillId()));
        TableColumn<Bill, String> c2 = col("Room", c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getRoomNumber())));
        TableColumn<Bill, String> c3 = col("Guest", c ->
            new SimpleStringProperty(c.getValue().getGuestName()));
        TableColumn<Bill, String> c4 = col("Type", c ->
            new SimpleStringProperty(c.getValue().getRoomType()));
        TableColumn<Bill, String> c5 = col("Days", c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getDaysStayed())));
        TableColumn<Bill, String> c6 = col("Sub-Total", c ->
            new SimpleStringProperty("₹" + String.format("%.2f", c.getValue().getTotalAmount())));
        TableColumn<Bill, String> c7 = col("GST (18%)", c ->
            new SimpleStringProperty("₹" + String.format("%.2f", c.getValue().getTaxAmount())));
        TableColumn<Bill, String> c8 = col("Grand Total", c ->
            new SimpleStringProperty("₹" + String.format("%.2f", c.getValue().getGrandTotal())));

        table.getColumns().addAll(c1, c2, c3, c4, c5, c6, c7, c8);
        table.setItems(FXCollections.observableArrayList(service.getAllBills()));
        table.setPlaceholder(new Label("No bills generated this session."));

        double rev = service.getTotalRevenue();
        Label revLabel = new Label("Session Revenue (incl. GST):  ₹" +
            String.format("%.2f", rev));
        revLabel.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 14px; -fx-font-weight: bold;");

        section.getChildren().addAll(heading, table, revLabel);
        return section;
    }

    private VBox buildFileLog() {
        VBox section = new VBox(12);

        Label heading = new Label("Persistent Bills Log ");
        heading.setStyle("-fx-text-fill: #e8c96f; -fx-font-size: 15px; -fx-font-weight: bold;");

        TextArea logArea = new TextArea();
        logArea.getStyleClass().add("receipt-area");
        logArea.setEditable(false);
        logArea.setPrefHeight(260);

        Button refreshBtn = new Button("🔄  Refresh Log from File");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> logArea.setText(service.getBillingLog()));

        logArea.setText(service.getBillingLog());

        section.getChildren().addAll(heading, refreshBtn, logArea);
        return section;
    }

    private <S> TableColumn<S, String> col(String title,
            javafx.util.Callback<TableColumn.CellDataFeatures<S, String>,
            javafx.beans.value.ObservableValue<String>> factory) {
        TableColumn<S, String> c = new TableColumn<>(title);
        c.setCellValueFactory(factory);
        return c;
    }
}
