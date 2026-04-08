package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bill implements Serializable {

    private static final long serialVersionUID = 2L;
    private static int counter = 1000;

    private final int    billId;
    private final int    roomNumber;
    private final String guestName;
    private final String guestContact;
    private final String roomType;
    private final int    daysStayed;
    private final Double pricePerNight;   // Wrapper class
    private final Double totalAmount;
    private final Double taxAmount;
    private final Double grandTotal;
    private final String checkoutTime;

    private static final double TAX_RATE = 0.18;

    public Bill(Room room) {
        this.billId        = ++counter;
        this.roomNumber    = room.getRoomNumber();
        this.guestName     = room.getGuestName();
        this.guestContact  = room.getGuestContact();
        this.roomType      = room.getRoomType().getDisplayName();
        this.daysStayed    = room.getDaysBooked();
        this.pricePerNight = room.getPricePerNight();   // autoboxing
        this.totalAmount   = room.calculateBill();
        this.taxAmount     = totalAmount * TAX_RATE;
        this.grandTotal    = totalAmount + taxAmount;
        this.checkoutTime  = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public int    getBillId()        { return billId; }
    public int    getRoomNumber()    { return roomNumber; }
    public String getGuestName()     { return guestName; }
    public String getGuestContact()  { return guestContact; }
    public String getRoomType()      { return roomType; }
    public int    getDaysStayed()    { return daysStayed; }
    public Double getPricePerNight() { return pricePerNight; }
    public Double getTotalAmount()   { return totalAmount; }
    public Double getTaxAmount()     { return taxAmount; }
    public Double getGrandTotal()    { return grandTotal; }
    public String getCheckoutTime()  { return checkoutTime; }

    public String generateReceipt() {
        return String.format(
            "╔══════════════════════════════════════╗\n" +
            "║              HOTEL                   ║\n" +
            "║         INVOICE / BILL               ║\n" +
            "╠══════════════════════════════════════╣\n" +
            "  Bill No    : #%d\n" +
            "  Date       : %s\n" +
            "╠══════════════════════════════════════╣\n" +
            "  Guest      : %s\n" +
            "  Contact    : %s\n" +
            "  Room No    : %d  (%s)\n" +
            "  Days Stayed: %d night(s)\n" +
            "╠══════════════════════════════════════╣\n" +
            "  Rate/Night : ₹%.2f\n" +
            "  Sub-Total  : ₹%.2f\n" +
            "  GST (18%%) : ₹%.2f\n" +
            "  ─────────────────────────────────────\n" +
            "  GRAND TOTAL: ₹%.2f\n" +
            "╚══════════════════════════════════════╝\n" +
            "    Thank you for staying with us!\n",
            billId, checkoutTime,
            guestName, guestContact,
            roomNumber, roomType,
            daysStayed,
            pricePerNight, totalAmount, taxAmount, grandTotal
        );
    }
}
