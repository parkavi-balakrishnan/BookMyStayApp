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

    public int getBeds() {
        return beds;
    }

    public int getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public void displayRoom(int availability) {
        System.out.println("Room Type: " + type);
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sq ft");
        System.out.println("Price per night: $" + price);
        System.out.println("Available Rooms: " + availability);
        System.out.println();
    }
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 180, 80);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 300, 120);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 500, 250);
    }
}

class Inventory {

    private int singleAvailability;
    private int doubleAvailability;
    private int suiteAvailability;

    public Inventory(int singleAvailability, int doubleAvailability, int suiteAvailability) {
        this.singleAvailability = singleAvailability;
        this.doubleAvailability = doubleAvailability;
        this.suiteAvailability = suiteAvailability;
    }

    public int getSingleAvailability() {
        return singleAvailability;
    }

    public int getDoubleAvailability() {
        return doubleAvailability;
    }

    public int getSuiteAvailability() {
        return suiteAvailability;
    }
}

class SearchService {

    public void searchRooms(Room single, Room dbl, Room suite, Inventory inventory) {

        System.out.println("Available Rooms:\n");

        if (inventory.getSingleAvailability() > 0) {
            single.displayRoom(inventory.getSingleAvailability());
        }

        if (inventory.getDoubleAvailability() > 0) {
            dbl.displayRoom(inventory.getDoubleAvailability());
        }

        if (inventory.getSuiteAvailability() > 0) {
            suite.displayRoom(inventory.getSuiteAvailability());
        }
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        Inventory inventory = new Inventory(5, 3, 0);

        SearchService searchService = new SearchService();
        searchService.searchRooms(single, dbl, suite, inventory);
    }
}