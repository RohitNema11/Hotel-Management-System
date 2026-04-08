package com.hotel.model;

import java.io.Serializable;

public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    // Private data members - Encapsulation
    private int roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private boolean isAvailable;
    private boolean isUnderMaintenance;
    private String guestName;
    private String guestContact;
    private int daysBooked;

    // Enum for Room Types with constructor/methods
    public enum RoomType {
        STANDARD("Standard", 2000),
        DELUXE("Deluxe", 4000),
        SUITE("Suite", 7000);

        private final String displayName;
        private final double basePrice;

        // Enum constructor
        RoomType(String displayName, double basePrice) {
            this.displayName = displayName;
            this.basePrice = basePrice;
        }

        public String getDisplayName() { return displayName; }
        public double getBasePrice()   { return basePrice; }

        // Enum method - calculate total cost
        public double calculateCost(int nights) {
            return basePrice * nights;
        }

        @Override
        public String toString() { return displayName; }
    }

    // Default constructor
    public Room() {
        this.isAvailable        = true;
        this.isUnderMaintenance = false;
        this.guestName          = "";
        this.guestContact       = "";
        this.daysBooked         = 0;
    }

    // Parameterised constructor
    public Room(int roomNumber, RoomType roomType) {
        this();
        this.roomNumber    = roomNumber;
        this.roomType      = roomType;
        this.pricePerNight = roomType.getBasePrice();
    }

    // Full constructor 
    public Room(int roomNumber, RoomType roomType, double pricePerNight) {
        this(roomNumber, roomType);
        this.pricePerNight = pricePerNight;
    }

    // Getters
    public int      getRoomNumber()        { return roomNumber; }
    public RoomType getRoomType()          { return roomType; }
    public double   getPricePerNight()     { return pricePerNight; }
    public boolean  isAvailable()          { return isAvailable; }
    public boolean  isUnderMaintenance()   { return isUnderMaintenance; }
    public String   getGuestName()         { return guestName; }
    public String   getGuestContact()      { return guestContact; }
    public int      getDaysBooked()        { return daysBooked; }

    // Setters with validation
    public void setRoomNumber(int roomNumber) {
        if (roomNumber > 0) this.roomNumber = roomNumber;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public void setPricePerNight(double price) {
        if (price > 0) this.pricePerNight = price;
        else throw new IllegalArgumentException("Price must be positive.");
    }

    public void setAvailable(boolean available) { this.isAvailable = available; }

    public void setUnderMaintenance(boolean maintenance) {
        this.isUnderMaintenance = maintenance;
        if (maintenance) {
            this.isAvailable = false;   
        } else {
            this.isAvailable = true;
        }
    }

    public void setGuestName(String guestName)       { this.guestName    = guestName; }
    public void setGuestContact(String guestContact) { this.guestContact = guestContact; }

    public void setDaysBooked(int days) {
        if (days >= 0) this.daysBooked = days;
    }

    /** Calculate total bill  */
    public Double calculateBill() {
        Double price  = pricePerNight;  
        Integer days  = daysBooked;     
        return price * days;            
    }

    /** Book room for a guest */
    public void book(String guestName, String guestContact, int days) {
        this.guestName    = guestName;
        this.guestContact = guestContact;
        this.daysBooked   = days;
        this.isAvailable  = false;
    }

    /** Release / checkout this room.*/
    public void checkout() {
        this.guestName    = "";
        this.guestContact = "";
        this.daysBooked   = 0;
        this.isAvailable  = !isUnderMaintenance; // stays unavailable if maintenance is ON
    }

    /** Extend the current booking by additional nights. */
    public void extendBooking(int extraDays) {
        if (extraDays <= 0) {
            throw new IllegalArgumentException("Extra days must be positive.");
        }
        this.daysBooked += extraDays;
    }

    public String getStatusText() {
        if (isUnderMaintenance) return "Maintenance";
        return isAvailable ? "Available" : "Occupied";
    }

    @Override
    public String toString() {
        return String.format("Room %d | %s | ₹%.0f/night | %s",
                roomNumber, roomType, pricePerNight, getStatusText());
    }
}