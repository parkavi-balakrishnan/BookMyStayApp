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

    public synchronized void addRequest(Reservation r) {
        queue.add(r);
    }

    public synchronized Reservation getNextRequest() {
        return queue.poll();
    }

    public synchronized boolean hasRequests() {
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

    public synchronized int getAvailability(String type) throws InvalidBookingException {
        if (!availability.containsKey(type)) {
            throw new InvalidBookingException("Invalid room type: " + type);
        }
        return availability.get(type);
    }

    public synchronized void decrement(String type) throws InvalidBookingException {

        int count = getAvailability(type);

        if (count <= 0) {
            throw new InvalidBookingException("No rooms available for " + type);
        }

        availability.put(type, count - 1);
    }

    public synchronized void increment(String type) throws InvalidBookingException {

        if (!availability.containsKey(type)) {
            throw new InvalidBookingException("Invalid room type: " + type);
        }

        availability.put(type, availability.get(type) + 1);
    }
}

class BookingHistory {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    public synchronized void addReservation(Reservation r) {
        confirmedBookings.add(r);
    }

    public synchronized List<Reservation> getReservations() {
        return confirmedBookings;
    }
}

class BookingService {

    private InventoryService inventory;
    private BookingHistory history;

    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, String> reservationRoomMap = new HashMap<>();

    public BookingService(InventoryService inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public synchronized void processReservation(Reservation reservation) {

        try {

            if (reservation == null) {
                throw new InvalidBookingException("Reservation cannot be null");
            }

            if (reservation.getGuestName() == null || reservation.getGuestName().isEmpty()) {
                throw new InvalidBookingException("Guest name is required");
            }

            String type = reservation.getRoom().getType();

            inventory.decrement(type);

            String roomId;

            do {
                roomId = type.substring(0,1) + (100 + allocatedRoomIds.size());
            } while (allocatedRoomIds.contains(roomId));

            allocatedRoomIds.add(roomId);

            reservationRoomMap.put(reservation.getReservationId(), roomId);

            history.addReservation(reservation);

            System.out.println(Thread.currentThread().getName() +
                    " confirmed reservation " + reservation.getReservationId() +
                    " for " + reservation.getGuestName() +
                    " | Room ID: " + roomId);

        } catch (InvalidBookingException e) {
            System.out.println(Thread.currentThread().getName() +
                    " failed booking: " + e.getMessage());
        }
    }
}

class ConcurrentBookingProcessor implements Runnable {

    private BookingRequestQueue queue;
    private BookingService bookingService;

    public ConcurrentBookingProcessor(BookingRequestQueue queue, BookingService bookingService) {
        this.queue = queue;
        this.bookingService = bookingService;
    }

    public void run() {

        while (true) {

            Reservation r;

            synchronized (queue) {
                if (!queue.hasRequests()) {
                    break;
                }
                r = queue.getNextRequest();
            }

            if (r != null) {
                bookingService.processReservation(r);
            }
        }
    }
}

class BookingReportService {

    public void printBookingHistory(List<Reservation> reservations) {

        System.out.println("\nFinal Booking History\n");

        for (Reservation r : reservations) {
            System.out.println(
                    "Reservation ID: " + r.getReservationId() +
                            " | Guest: " + r.getGuestName() +
                            " | Room: " + r.getRoom().getType()
            );
        }

        System.out.println("\nTotal Confirmed Bookings: " + reservations.size());
    }
}

public class BookMyStayApp {

    public static void main(String[] args) throws InterruptedException {

        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("R1", "Alice", single));
        queue.addRequest(new Reservation("R2", "Bob", dbl));
        queue.addRequest(new Reservation("R3", "Charlie", single));
        queue.addRequest(new Reservation("R4", "David", dbl));
        queue.addRequest(new Reservation("R5", "Eva", single));

        InventoryService inventory = new InventoryService();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(inventory, history);

        Thread t1 = new Thread(new ConcurrentBookingProcessor(queue, bookingService), "Thread-1");
        Thread t2 = new Thread(new ConcurrentBookingProcessor(queue, bookingService), "Thread-2");
        Thread t3 = new Thread(new ConcurrentBookingProcessor(queue, bookingService), "Thread-3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        BookingReportService reportService = new BookingReportService();
        reportService.printBookingHistory(history.getReservations());
    }
}