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

class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
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

    public int getAvailability(String type) throws InvalidBookingException {
        if (!availability.containsKey(type)) {
            throw new InvalidBookingException("Invalid room type requested: " + type);
        }
        return availability.get(type);
    }

    public void decrement(String type) throws InvalidBookingException {

        if (!availability.containsKey(type)) {
            throw new InvalidBookingException("Room type does not exist: " + type);
        }

        int count = availability.get(type);

        if (count <= 0) {
            throw new InvalidBookingException("No available rooms for type: " + type);
        }

        availability.put(type, count - 1);
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

    public void processReservation(Reservation reservation) throws InvalidBookingException {

        if (reservation == null) {
            throw new InvalidBookingException("Reservation request cannot be null.");
        }

        if (reservation.getGuestName() == null || reservation.getGuestName().isEmpty()) {
            throw new InvalidBookingException("Guest name is required.");
        }

        if (reservation.getRoom() == null) {
            throw new InvalidBookingException("Room selection is required.");
        }

        String type = reservation.getRoom().getType();

        inventory.getAvailability(type);

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
        queue.addRequest(new Reservation("R4", "", suite));

        InventoryService inventory = new InventoryService();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(inventory, history);

        while (queue.hasRequests()) {

            Reservation r = queue.getNextRequest();

            try {
                bookingService.processReservation(r);
            } catch (InvalidBookingException e) {
                System.out.println("Booking Failed: " + e.getMessage());
                System.out.println();
            }
        }

        BookingReportService reportService = new BookingReportService();
        reportService.printBookingHistory(history.getReservations());
    }
}