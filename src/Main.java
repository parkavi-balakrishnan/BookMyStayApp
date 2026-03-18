import java.io.*;
import java.util.*;

abstract class Room implements Serializable {

    private String type;
    private int beds;
    private int size;
    private double price;

    public Room(String type, int beds, int size, double price) {
        this.type = type;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single", 1, 180, 80);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double", 2, 300, 120);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite", 3, 500, 250);
    }
}

class Reservation implements Serializable {

    private String reservationId;
    private String guestName;
    private Room room;

    public Reservation(String reservationId, String guestName, Room room) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.room = room;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public Room getRoom() {
        return room;
    }
}

class InventoryService implements Serializable {

    private Map<String, Integer> availability = new HashMap<>();

    public InventoryService() {
        availability.put("Single", 2);
        availability.put("Double", 2);
        availability.put("Suite", 1);
    }

    public synchronized int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public synchronized void decrement(String type) {
        availability.put(type, availability.get(type) - 1);
    }

    public synchronized void increment(String type) {
        availability.put(type, availability.get(type) + 1);
    }

    public Map<String, Integer> getAvailabilityMap() {
        return availability;
    }
}

class BookingHistory implements Serializable {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    public synchronized void addReservation(Reservation r) {
        confirmedBookings.add(r);
    }

    public synchronized List<Reservation> getReservations() {
        return confirmedBookings;
    }
}

class SystemState implements Serializable {

    private InventoryService inventory;
    private BookingHistory history;

    public SystemState(InventoryService inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public InventoryService getInventory() {
        return inventory;
    }

    public BookingHistory getHistory() {
        return history;
    }
}

class PersistenceService {

    private static final String FILE_NAME = "system_state.dat";

    public void save(SystemState state) {

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(state);
            System.out.println("System state saved successfully.");

        } catch (IOException e) {
            System.out.println("Failed to save system state.");
        }
    }

    public SystemState load() {

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            SystemState state = (SystemState) in.readObject();
            System.out.println("System state restored successfully.");
            return state;

        } catch (Exception e) {
            System.out.println("No valid persistence file found. Starting fresh.");
            return null;
        }
    }
}

class BookingService {

    private InventoryService inventory;
    private BookingHistory history;

    public BookingService(InventoryService inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public synchronized void processReservation(Reservation reservation) {

        String type = reservation.getRoom().getType();

        if (inventory.getAvailability(type) <= 0) {
            System.out.println("No rooms available for " + type);
            return;
        }

        inventory.decrement(type);

        history.addReservation(reservation);

        System.out.println("Reservation Confirmed: " +
                reservation.getReservationId() +
                " | Guest: " + reservation.getGuestName() +
                " | Room: " + type);
    }
}

class BookingReportService {

    public void printReport(BookingHistory history, InventoryService inventory) {

        System.out.println("\n--- Booking History ---");

        for (Reservation r : history.getReservations()) {
            System.out.println(
                    r.getReservationId() +
                            " | " + r.getGuestName() +
                            " | " + r.getRoom().getType()
            );
        }

        System.out.println("\n--- Inventory State ---");

        for (Map.Entry<String, Integer> entry :
                inventory.getAvailabilityMap().entrySet()) {

            System.out.println(entry.getKey() + " available: " + entry.getValue());
        }
    }
}

public class Main {

    public static void main(String[] args) {

        PersistenceService persistence = new PersistenceService();

        SystemState state = persistence.load();

        InventoryService inventory;
        BookingHistory history;

        if (state != null) {
            inventory = state.getInventory();
            history = state.getHistory();
        } else {
            inventory = new InventoryService();
            history = new BookingHistory();
        }

        BookingService bookingService = new BookingService(inventory, history);

        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();

        bookingService.processReservation(new Reservation("R1", "Alice", single));
        bookingService.processReservation(new Reservation("R2", "Bob", dbl));

        BookingReportService reportService = new BookingReportService();
        reportService.printReport(history, inventory);

        SystemState newState = new SystemState(inventory, history);
        persistence.save(newState);
    }
}