package com.hotel.service;

import com.hotel.model.Bill;
import com.hotel.model.Room;

import java.util.*;

public class HotelService {

    private final List<Room> rooms = new ArrayList<>();

    private final Map<Integer, Room> roomMap = new HashMap<>();

    private final List<Bill> bills = new ArrayList<>();

    private final FileStorageService storage;

    public HotelService() {
        this.storage = new FileStorageService();
        loadData();
    }

    private void loadData() {
        List<Room> loaded = storage.loadRooms();
        if (loaded.isEmpty()) {
            initializeDefaultRooms();
        } else {
            loaded.forEach(this::registerRoom);
        }
    }

    public void saveData() {
        storage.saveRooms(rooms);
    }

    private void initializeDefaultRooms() {
        addRoom(new Room(101, Room.RoomType.STANDARD));
        addRoom(new Room(102, Room.RoomType.STANDARD));
        addRoom(new Room(201, Room.RoomType.DELUXE));
        addRoom(new Room(202, Room.RoomType.DELUXE));
        addRoom(new Room(301, Room.RoomType.SUITE));
        addRoom(new Room(302, Room.RoomType.SUITE));
        saveData();
    }

    // Room Management

    /** Add a new room. Synchronized for thread safety*/
    public synchronized boolean addRoom(Room room) {
        if (roomMap.containsKey(room.getRoomNumber())) {
            return false; // Room already exists
        }
        registerRoom(room);
        saveData();
        return true;
    }

    private void registerRoom(Room room) {
        rooms.add(room);
        roomMap.put(room.getRoomNumber(), room);
    }

    /** Get all rooms. Uses Iterator */
    public List<Room> getAllRooms() {
        List<Room> copy = new ArrayList<>();
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            copy.add(it.next());
        }
        return copy;
    }

    /** Get only available rooms (excludes maintenance and occupied rooms) */
    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for (Room r : rooms) {
            if (r.isAvailable()) available.add(r);
        }
        return available;
    }

    /** Get only occupied rooms (guest is checked in, excludes maintenance-only rooms) */
    public List<Room> getOccupiedRooms() {
        List<Room> occupied = new ArrayList<>();
        for (Room r : rooms) {
            if (!r.isAvailable() && !r.isUnderMaintenance()) occupied.add(r);
        }
        return occupied;
    }

    /** Get only rooms under maintenance */
    public List<Room> getMaintenanceRooms() {
        List<Room> maintenance = new ArrayList<>();
        for (Room r : rooms) {
            if (r.isUnderMaintenance()) maintenance.add(r);
        }
        return maintenance;
    }

    /** Find room by room number (uses HashMap) */
    public Room findRoom(int roomNumber) {
        return roomMap.get(roomNumber);
    }

    /**Sort rooms by price using Collections.sort() */
    public List<Room> getRoomsSortedByPrice() {
        List<Room> sorted = new ArrayList<>(rooms);
        Collections.sort(sorted, Comparator.comparingDouble(Room::getPricePerNight));
        return sorted;
    }

    // Booking / Checkout

    /** Book a room. Synchronized to prevent race conditions */
    public synchronized String bookRoom(int roomNumber, String guestName,
                                        String guestContact, int days) {
        Room room = roomMap.get(roomNumber);
        if (room == null)                 return "ERROR:Room " + roomNumber + " does not exist.";
        if (room.isUnderMaintenance())    return "ERROR:Room " + roomNumber + " is under maintenance.";
        if (!room.isAvailable())          return "ERROR:Room " + roomNumber + " is already booked.";
        if (guestName.isBlank())          return "ERROR:Guest name cannot be empty.";
        if (guestContact.isBlank())       return "ERROR:Contact number cannot be empty.";
        if (days <= 0)                    return "ERROR:Number of days must be at least 1.";

        room.book(guestName.trim(), guestContact.trim(), days);
        saveData();
        return "SUCCESS:Room " + roomNumber + " booked for " + guestName
                + ". Total: ₹" + String.format("%.2f", room.calculateBill());
    }

    /** Checkout a room. Generates a Bill, saves to file*/
    public synchronized Bill checkout(int roomNumber) {
        Room room = roomMap.get(roomNumber);
        if (room == null || room.isAvailable() || room.isUnderMaintenance()) return null;

        Bill bill = new Bill(room);
        bills.add(bill);
        storage.saveBillToLog(bill);

        room.checkout();
        saveData();
        return bill;
    }

    /** Extend an occupied room booking by extra days. */
    public synchronized String extendStay(int roomNumber, int extraDays) {
        Room room = roomMap.get(roomNumber);
        if (room == null) return "ERROR:Room " + roomNumber + " does not exist.";
        if (room.isUnderMaintenance()) return "ERROR:Room " + roomNumber + " is under maintenance.";
        if (room.isAvailable()) return "ERROR:Room " + roomNumber + " is not currently occupied.";
        if (extraDays <= 0) return "ERROR:Extra days must be at least 1.";

        room.extendBooking(extraDays);
        saveData();
        return "SUCCESS:Stay extended by " + extraDays + " night(s). New total: ₹" +
            String.format("%.2f", room.calculateBill());
    }

    /** Cancel a booking without generating a bill - Room is reset and made available again (unless under maintenance) */
    public synchronized boolean cancelBooking(int roomNumber) {
        Room room = roomMap.get(roomNumber);
        if (room == null || room.isAvailable() || room.isUnderMaintenance()) return false;
        room.checkout();
        saveData();
        return true;
    }

    /** Toggle maintenance mode for a room. Cannot toggle if a guest is currently checked in*/
    public synchronized boolean toggleMaintenance(int roomNumber) {
        Room room = roomMap.get(roomNumber);
        if (room == null) return false;

        if (!room.isAvailable() && !room.isUnderMaintenance()) return false;

        room.setUnderMaintenance(!room.isUnderMaintenance());
        saveData();
        return true;
    }

    /** Change a room's type.Updates the room type and sets price to the new type's base price. */
    public synchronized String changeRoomType(int roomNumber, Room.RoomType newType) {
        Room room = roomMap.get(roomNumber);
        if (room == null) return "ERROR:Room " + roomNumber + " does not exist.";
        if (!room.isAvailable()) return "ERROR:Room " + roomNumber + " is currently occupied or under maintenance.";
        if (newType == null) return "ERROR:Invalid room type.";

        Room.RoomType oldType = room.getRoomType();
        room.setRoomType(newType);
        room.setPricePerNight(newType.getBasePrice());
        saveData();
        return "SUCCESS:Room " + roomNumber + " type changed from " + oldType.getDisplayName()
                + " to " + newType.getDisplayName() + ". Price updated to ₹" + newType.getBasePrice();
    }

    /** Update price for individual room */
    public synchronized String setRoomPrice(int roomNumber, double newPrice) {
        Room room = roomMap.get(roomNumber);
        if (room == null) return "ERROR:Room " + roomNumber + " does not exist.";
        if (newPrice <= 0) return "ERROR:Price must be positive.";

        double oldPrice = room.getPricePerNight();
        room.setPricePerNight(newPrice);
        saveData();
        return "SUCCESS:Room " + roomNumber + " price updated from ₹" + String.format("%.0f", oldPrice)
                + " to ₹" + String.format("%.0f", newPrice);
    }

    /** Update price for all rooms of a specific type. Synchronized for thread safety. */
    public synchronized String updateAllRoomsOfType(Room.RoomType type, double newPrice) {
        if (type == null) return "ERROR:Invalid room type.";
        if (newPrice <= 0) return "ERROR:Price must be positive.";

        int count = 0;
        for (Room room : rooms) {
            if (room.getRoomType() == type) {
                room.setPricePerNight(newPrice);
                count++;
            }
        }
        saveData();
        return "SUCCESS:" + count + " room(s) of type " + type.getDisplayName()
                + " updated to ₹" + String.format("%.0f", newPrice) + "/night.";
    }

    // Billing 

    public List<Bill> getAllBills() {
        return new ArrayList<>(bills);
    }

    public String getBillingLog() {
        return storage.readBillsLog();
    }

    /** Generic method to display any list */
    public <T> String listToString(List<T> items) {
        StringBuilder sb = new StringBuilder();
        for (T item : items) sb.append(item).append("\n");
        return sb.toString();
    }

    // Room Cleaning with Threading Demonstration

    private final Set<Integer> roomsBeingCleaned = new HashSet<>();
    private final Object cleaningLock = new Object();
    private Thread cleaningThread;
    private volatile boolean cleaningActive = false;

    /** Start cleaning a room using a single shared cleaning thread */
    public synchronized String startRoomCleaning(int roomNumber) {
        Room room = roomMap.get(roomNumber);
        if (room == null) return "ERROR:Room " + roomNumber + " does not exist.";

        if (roomsBeingCleaned.contains(roomNumber)) {
            return "ERROR:Room " + roomNumber + " is already being cleaned.";
        }

        // Add to cleaning queue
        roomsBeingCleaned.add(roomNumber);

        // Start the cleaning thread if not already running
        if (!cleaningActive) {
            cleaningActive = true;
            cleaningThread = new Thread(this::cleaningWorker);
            cleaningThread.setDaemon(true);
            cleaningThread.start();
        } else {
            // Notify the existing thread that there's work to do
            synchronized (cleaningLock) {
                cleaningLock.notify();
            }
        }

        return "SUCCESS:Room " + roomNumber + " added to cleaning queue.";
    }

    /** Worker method that runs in the cleaning thread */
    private void cleaningWorker() {
        while (cleaningActive) {
            Integer roomToClean = null;

            synchronized (this) {
                if (!roomsBeingCleaned.isEmpty()) {
                    roomToClean = roomsBeingCleaned.iterator().next();
                }
            }

            if (roomToClean != null) {
                try {
                    System.out.println("🧹 Started cleaning room " + roomToClean + " in thread: " + Thread.currentThread().getName());

                    // Simulate cleaning time (5 seconds)
                    Thread.sleep(5000);

                    // Mark room as cleaned
                    synchronized (this) {
                        Room room = roomMap.get(roomToClean);
                        if (room != null) {
                            if (room.isUnderMaintenance()) {
                                room.setUnderMaintenance(false);
                            }
                            roomsBeingCleaned.remove(roomToClean);
                            saveData();
                        }
                    }

                    System.out.println("✅ Finished cleaning room " + roomToClean + " in thread: " + Thread.currentThread().getName());

                } catch (InterruptedException e) {
                    System.out.println("❌ Cleaning interrupted for room " + roomToClean);
                    synchronized (this) {
                        roomsBeingCleaned.remove(roomToClean);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // No rooms to clean, wait for notification
                synchronized (cleaningLock) {
                    try {
                        cleaningLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    /** Check if a room is currently being cleaned */
    public synchronized boolean isRoomBeingCleaned(int roomNumber) {
        return roomsBeingCleaned.contains(roomNumber);
    }

    /** Get list of rooms currently being cleaned */
    public synchronized List<Integer> getRoomsBeingCleaned() {
        return new ArrayList<>(roomsBeingCleaned);
    }

    // Stats

    public int getTotalRooms()        { return rooms.size(); }
    public int getAvailableCount()    { return getAvailableRooms().size(); }
    public int getOccupiedCount()     { return getOccupiedRooms().size(); }
    public int getMaintenanceCount()  { return getMaintenanceRooms().size(); }

    public double getTotalRevenue() {
        Double total = 0.0;
        for (Bill b : bills) total += b.getGrandTotal();
        return total;
    }
}