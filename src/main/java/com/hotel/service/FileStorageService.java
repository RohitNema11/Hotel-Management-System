package com.hotel.service;

import com.hotel.model.Bill;
import com.hotel.model.Room;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorageService {

    private static final String ROOMS_FILE = "data/rooms.dat";
    private static final String BILLS_LOG  = "data/bills_log.txt";

    public FileStorageService() {
        new File("data").mkdirs();
    }

    public void saveRooms(List<Room> rooms) {
        try (FileOutputStream fos = new FileOutputStream(ROOMS_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(new ArrayList<>(rooms));
            System.out.println("[Storage] Rooms saved successfully.");

        } catch (IOException e) {
            System.err.println("[Storage] Error saving rooms: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Room> loadRooms() {
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(ROOMS_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            List<Room> rooms = (List<Room>) ois.readObject();
            System.out.println("[Storage] Loaded " + rooms.size() + " rooms.");
            return rooms;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Storage] Error loading rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public void saveBillToLog(Bill bill) {
        try (FileWriter fw = new FileWriter(BILLS_LOG, true)) {
            fw.write(bill.generateReceipt());
            fw.write("\n");
            System.out.println("[Storage] Bill #" + bill.getBillId() + " logged.");
        } catch (IOException e) {
            System.err.println("[Storage] Error writing bill log: " + e.getMessage());
        }
    }

    public String readBillsLog() {
        File file = new File(BILLS_LOG);
        if (!file.exists()) return "No billing records found.";

        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(BILLS_LOG)) {
            int ch;
            while ((ch = fr.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            return "Error reading billing log: " + e.getMessage();
        }
        return sb.isEmpty() ? "No billing records found." : sb.toString();
    }
}
