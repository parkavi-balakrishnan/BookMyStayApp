import java.util.*;

abstract class Room {

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

class Reservation {

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

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.add(r);
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }

    public boolean hasRequests() {
        return !queue.isEmpty();
    }
}

class InventoryService {

    private Map<String, Integer> availability = new HashMap<>();

    public InventoryService() {
        availability.put("Single", 2);
        availability.put("Double", 2);
        availability.put("Suite", 1);
    }

    public int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public void decrement(String type) {
        availability.put(type, availability.get(type) - 1);
    }
}

class BookingHistory {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    public void addReservation(Reservation r) {
        confirmedBookings.add(r);
    }

    public List<Reservation> getReservations() {
        return confirmedBookings;
    }
}

class BookingService {

    private InventoryService inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> roomAllocations = new HashMap<>();
    private BookingHistory history;

    public BookingService(InventoryService inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void processReservation(Reservation reservation) {

        String type = reservation.getRoom().getType();

        if (inventory.getAvailability(type) <= 0) {
            System.out.println("No rooms available for " + type);
            return;
        }

        String roomId;
        do {
            roomId = type.substring(0,1) + (100 + allocatedRoomIds.size());
        } while (allocatedRoomIds.contains(roomId));

        allocatedRoomIds.add(roomId);

        roomAllocations
                .computeIfAbsent(type, k -> new HashSet<>())
                .add(roomId);

        inventory.decrement(type);

        history.addReservation(reservation);

        System.out.println("Reservation Confirmed");
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Guest: " + reservation.getGuestName());
        System.out.println("Room Type: " + type);
        System.out.println("Assigned Room ID: " + roomId);
        System.out.println();
    }
}

class BookingReportService {

    public void printBookingHistory(List<Reservation> reservations) {

        System.out.println("Booking History:\n");

        for (Reservation r : reservations) {
            System.out.println(
                    "Reservation ID: " + r.getReservationId() +
                            " | Guest: " + r.getGuestName() +
                            " | Room Type: " + r.getRoom().getType()
            );
        }

        System.out.println("\nTotal Bookings: " + reservations.size());
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("R1", "Alice", single));
        queue.addRequest(new Reservation("R2", "Bob", dbl));
        queue.addRequest(new Reservation("R3", "Charlie", suite));

        InventoryService inventory = new InventoryService();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(inventory, history);

        while (queue.hasRequests()) {
            Reservation r = queue.getNextRequest();
            bookingService.processReservation(r);
        }

        BookingReportService reportService = new BookingReportService();
        reportService.printBookingHistory(history.getReservations());
    }
}