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

    public int getPrice() {
        return (int) price;
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

class BookingService {

    private InventoryService inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> roomAllocations = new HashMap<>();

    public BookingService(InventoryService inventory) {
        this.inventory = inventory;
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

        System.out.println("Reservation Confirmed");
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Guest: " + reservation.getGuestName());
        System.out.println("Room Type: " + type);
        System.out.println("Assigned Room ID: " + roomId);
        System.out.println();
    }
}

class Service {

    private String name;
    private double cost;

    public Service(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public String getName() {
        return name;
    }
}

class AddOnServiceManager {

    private Map<String, List<Service>> reservationServices = new HashMap<>();

    public void addService(String reservationId, Service service) {
        reservationServices
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);
    }

    public double calculateTotalServiceCost(String reservationId) {

        double total = 0;

        List<Service> services = reservationServices.get(reservationId);

        if (services != null) {
            for (Service s : services) {
                total += s.getCost();
            }
        }

        return total;
    }

    public void displayServices(String reservationId) {

        List<Service> services = reservationServices.get(reservationId);

        if (services == null) {
            System.out.println("No services selected.");
            return;
        }

        System.out.println("Services for Reservation " + reservationId + ":");

        for (Service s : services) {
            System.out.println(s.getName() + " - $" + s.getCost());
        }

        System.out.println("Total Add-On Cost: $" + calculateTotalServiceCost(reservationId));
        System.out.println();
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
        BookingService bookingService = new BookingService(inventory);

        List<Reservation> confirmedReservations = new ArrayList<>();

        while (queue.hasRequests()) {
            Reservation r = queue.getNextRequest();
            bookingService.processReservation(r);
            confirmedReservations.add(r);
        }

        AddOnServiceManager serviceManager = new AddOnServiceManager();

        Service breakfast = new Service("Breakfast", 20);
        Service airportPickup = new Service("Airport Pickup", 40);
        Service spa = new Service("Spa Access", 60);

        serviceManager.addService("R1", breakfast);
        serviceManager.addService("R1", airportPickup);
        serviceManager.addService("R2", spa);

        serviceManager.displayServices("R1");
        serviceManager.displayServices("R2");
    }
}