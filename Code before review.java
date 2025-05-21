git commit -m "Initial unreviewed code"
import java.time.*;
import java.util.*;

class StaffMember {
    private String name;
    private String role;
    private List<LocalDate> unavailableDates = new ArrayList<>();
    private Map<LocalDate, Shift> assignedShifts = new HashMap<>();

    public StaffMember(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getName() { return name; }
    public String getRole() { return role; }

    public void setUnavailableDates(List<LocalDate> dates) {
        unavailableDates = dates;
    }

    public List<LocalDate> getUnavailableDates() {
        return unavailableDates;
    }

    public boolean isAvailable(LocalDate date) {
        return !unavailableDates.contains(date) && !assignedShifts.containsKey(date);
    }

    public void assignShift(Shift shift) {
        assignedShifts.put(shift.getDate(), shift);
    }

    public Map<LocalDate, Shift> getAssignedShifts() {
        return assignedShifts;
    }

    public void removeShift(LocalDate date) {
        assignedShifts.remove(date);
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}

class Shift {
    private LocalDate date;
    private String shiftType;  // e.g. Morning, Evening, Night
    private StaffMember staff;

    public Shift(LocalDate date, String shiftType, StaffMember staff) {
        this.date = date;
        this.shiftType = shiftType;
        this.staff = staff;
    }

    public LocalDate getDate() { return date; }
    public String getShiftType() { return shiftType; }
    public StaffMember getStaff() { return staff; }

    @Override
    public String toString() {
        return date + " - " + shiftType + " shift - " + staff.getName();
    }
}

class TimeOffRequest {
    private StaffMember staff;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private boolean approved = false;
    private boolean reviewed = false;

    public TimeOffRequest(StaffMember staff, LocalDate start, LocalDate end, String reason) {
        this.staff = staff;
        this.startDate = start;
        this.endDate = end;
        this.reason = reason;
    }

    public StaffMember getStaff() { return staff; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public boolean isApproved() { return approved; }
    public boolean isReviewed() { return reviewed; }

    public void approve() {
        approved = true;
        reviewed = true;
    }

    public void reject() {
        approved = false;
        reviewed = true;
    }

    @Override
    public String toString() {
        return "Request by " + staff.getName() + " from " + startDate + " to " + endDate +
               " Reason: " + reason + " Approved: " + approved;
    }
}

public class StaffSchedulingSystem {
    private static List<StaffMember> staffList = new ArrayList<>();
    private static List<TimeOffRequest> timeOffRequests = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Staff Scheduling System ===");
        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect option:");
            System.out.println("1. Add New Staff Member");
            System.out.println("2. Update Staff Availability");
            System.out.println("3. Assign Shifts");
            System.out.println("4. View Staff Schedule");
            System.out.println("5. Request Time Off");
            System.out.println("6. Approve/Reject Time-Off Requests");
            System.out.println("7. Shift Swapping");
            System.out.println("8. Schedule Conflict Detection");
            System.out.println("9. Emergency Shift Replacement");
            System.out.println("10. Track Staff Attendance (Placeholder)");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch(choice) {
                case 1: addNewStaffMember(); break;
                case 2: updateStaffAvailability(); break;
                case 3: assignShifts(); break;
                case 4: viewStaffSchedule(); break;
                case 5: requestTimeOff(); break;
                case 6: reviewTimeOffRequests(); break;
                case 7: shiftSwapping(); break;
                case 8: detectScheduleConflicts(); break;
                case 9: emergencyShiftReplacement(); break;
                case 10: trackAttendance(); break;
                case 0: exit = true; break;
                default: System.out.println("Invalid choice."); break;
            }
        }
        System.out.println("Exiting system.");
    }

    // 1. Add New Staff Member
    private static void addNewStaffMember() {
        System.out.print("Enter staff name: ");
        String name = scanner.nextLine();
        System.out.print("Enter role: ");
        String role = scanner.nextLine();
        StaffMember staff = new StaffMember(name, role);
        staffList.add(staff);
        System.out.println("Staff member added: " + staff);
    }

    // 2. Update Staff Availability
    private static void updateStaffAvailability() {
        StaffMember staff = selectStaffMember();
        if (staff == null) return;
        System.out.println("Enter unavailable dates (yyyy-mm-dd), type 'done' to finish:");
        List<LocalDate> unavailable = new ArrayList<>();
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("done")) break;
            try {
                LocalDate date = LocalDate.parse(input);
                unavailable.add(date);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }
        staff.setUnavailableDates(unavailable);
        System.out.println("Updated unavailable dates for " + staff.getName());
    }

    // 3. Assign Shifts (Simple auto assign based on availability and role)
    private static void assignShifts() {
        System.out.print("Enter date to assign shift (yyyy-mm-dd): ");
        LocalDate date;
        try {
            date = LocalDate.parse(scanner.nextLine());
        } catch(Exception e) {
            System.out.println("Invalid date.");
            return;
        }

        System.out.print("Enter shift type (Morning/Evening/Night): ");
        String shiftType = scanner.nextLine();

        // Find available staff
        List<StaffMember> availableStaff = new ArrayList<>();
        for (StaffMember s : staffList) {
            if (s.isAvailable(date)) {
                availableStaff.add(s);
            }
        }
        if (availableStaff.isEmpty()) {
            System.out.println("No available staff for " + date);
            return;
        }

        System.out.println("Available staff:");
        for (int i=0; i < availableStaff.size(); i++) {
            System.out.println((i+1) + ". " + availableStaff.get(i));
        }
        System.out.print("Select staff to assign shift: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx < 0 || idx >= availableStaff.size()) throw new Exception();
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return;
        }
        StaffMember chosen = availableStaff.get(idx);
        Shift shift = new Shift(date, shiftType, chosen);
        chosen.assignShift(shift);
        System.out.println("Shift assigned: " + shift);
    }

    // 4. View Staff Schedule
    private static void viewStaffSchedule() {
        StaffMember staff = selectStaffMember();
        if (staff == null) return;

        System.out.println("Shifts for " + staff.getName() + ":");
        if (staff.getAssignedShifts().isEmpty()) {
            System.out.println("No assigned shifts.");
        } else {
            staff.getAssignedShifts().values().stream()
                .sorted(Comparator.comparing(Shift::getDate))
                .forEach(s -> System.out.println(s));
        }
    }

    // 5. Request Time Off
    private static void requestTimeOff() {
        StaffMember staff = selectStaffMember();
        if (staff == null) return;

        System.out.print("Enter start date (yyyy-mm-dd): ");
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(scanner.nextLine());
            System.out.print("Enter end date (yyyy-mm-dd): ");
            endDate = LocalDate.parse(scanner.nextLine());
            if (endDate.isBefore(startDate)) {
                System.out.println("End date must be after start date.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Invalid date format.");
            return;
        }

        System.out.print("Enter reason for time off: ");
        String reason = scanner.nextLine();

        TimeOffRequest request = new TimeOffRequest(staff, startDate, endDate, reason);
        timeOffRequests.add(request);
        System.out.println("Time off requested: " + request);
    }

    // 6. Approve/Reject Time-Off Requests
    private static void reviewTimeOffRequests() {
        List<TimeOffRequest> pendingRequests = new ArrayList<>();
        for (TimeOffRequest r : timeOffRequests) {
            if (!r.isReviewed()) {
                pendingRequests.add(r);
            }
        }

        if (pendingRequests.isEmpty()) {
            System.out.println("No pending time-off requests.");
            return;
        }

        for (int i=0; i < pendingRequests.size(); i++) {
            System.out.println((i+1) + ". " + pendingRequests.get(i));
        }
        System.out.print("Select request to review: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx < 0 || idx >= pendingRequests.size()) throw new Exception();
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return;
        }

        TimeOffRequest req = pendingRequests.get(idx);
        System.out.print("Approve (A) or Reject (R)? ");
        String decision = scanner.nextLine();
        if (decision.equalsIgnoreCase("A")) {
            req.approve();
            System.out.println("Request approved.");
            // Mark dates unavailable for staff
            StaffMember staff = req.getStaff();
            List<LocalDate> unavailable = staff.getUnavailableDates();
            LocalDate d = req.getStartDate();
            while (!d.isAfter(req.getEndDate())) {
                if(!unavailable.contains(d)) {
                    unavailable.add(d);
                    staff.removeShift(d); // Remove any assigned shift on these dates
                }
                d = d.plusDays(1);
            }
            staff.setUnavailableDates(unavailable);
        } else if (decision.equalsIgnoreCase("R")) {
            req.reject();
            System.out.println("Request rejected.");
        } else {
            System.out.println("Invalid input.");
        }
    }

    // 7. Shift Swapping
    private static void shiftSwapping() {
        System.out.println("Shift swapping feature:");

        System.out.println("Select your staff account:");
        StaffMember staff1 = selectStaffMember();
        if (staff1 == null) return;

        if (staff1.getAssignedShifts().isEmpty()) {
            System.out.println("You have no assigned shifts to swap.");
            return;
        }

        System.out.println("Your assigned shifts:");
        List<Shift> shifts1 = new ArrayList<>(staff1.getAssignedShifts().values());
        for (int i = 0; i < shifts1.size(); i++) {
            System.out.println((i+1) + ". " + shifts1.get(i));
        }

        System.out.print("Select shift to swap: ");
        int shiftIdx;
        try {
            shiftIdx = Integer.parseInt(scanner.nextLine()) - 1;
            if (shiftIdx < 0 || shiftIdx >= shifts1.size()) throw new Exception();
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return;
        }
        Shift shiftToSwap = shifts1.get(shiftIdx);

        System.out.println("Select staff member to swap with:");
        StaffMember staff2 = selectStaffMember();
        if (staff2 == null || staff2 == staff1) {
            System.out.println("Invalid staff selected.");
            return;
        }

        if (staff2.getAssignedShifts().isEmpty()) {
            System.out.println(staff2.getName() + " has no assigned shifts to swap.");
            return;
        }

        System.out.println(staff2.getName() + "'s assigned shifts:");
        List<Shift> shifts2 = new ArrayList<>(staff2.getAssignedShifts().values());
        for (int i = 0; i < shifts2.size(); i++) {
            System.out.println((i+1) + ". " + shifts2.get(i));
        }

        System.out.print("Select shift to swap with: ");
        int shiftIdx2;
        try {
            shiftIdx2 = Integer.parseInt(scanner.nextLine()) - 1;
            if (shiftIdx2 < 0 || shiftIdx2 >= shifts2.size()) throw new Exception();
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return;
        }
        Shift shiftToSwapWith = shifts2.get(shiftIdx2);

        // Confirm swap
        System.out.println("Propose swap:");
        System.out.println(staff1.getName() + " " + shiftToSwap);
        System.out.println("WITH");
        System.out.println(staff2.getName() + " " + shiftToSwapWith);

        System.out.print("Manager approval required? (Y/N): ");
        String approval = scanner.nextLine();

        if (approval.equalsIgnoreCase("Y")) {
            System.out.println("Manager approved the swap.");
        } else {
            System.out.println("Swap not approved by manager.");
            return;
        }

        // Perform swap
        staff1.assignShift(new Shift(shiftToSwapWith.getDate(), shiftToSwapWith.getShiftType(), staff1));
        staff2.assignShift(new Shift(shiftToSwap.getDate(), shiftToSwap.getShiftType(), staff2));

        staff1.removeShift(shiftToSwap.getDate());
        staff2.removeShift(shiftToSwapWith.getDate());

        System.out.println("Shift swap completed.");
    }

    // 8. Schedule Conflict Detection and Resolution (detect overlapping shifts or unavailable dates)
    private static void detectScheduleConflicts() {
        System.out.println("Detecting schedule conflicts...");
        boolean conflictsFound = false;
        for (StaffMember staff : staffList) {
            Set<LocalDate> assignedDates = new HashSet<>();
            for (Shift shift : staff.getAssignedShifts().values()) {
                LocalDate date = shift.getDate();
                if (assignedDates.contains(date)) {
                    System.out.println("Conflict: " + staff.getName() + " has multiple shifts on " + date);
                    conflictsFound = true;
                }
                assignedDates.add(date);
                if (staff.getUnavailableDates().contains(date)) {
                    System.out.println("Conflict: " + staff.getName() + " assigned shift on unavailable date " + date);
                    conflictsFound = true;
                }
            }
        }
        if (!conflictsFound) {
            System.out.println("No conflicts detected.");
        }
    }

    // 9. Emergency Shift Replacement (find available staff for a given date)
    private static void emergencyShiftReplacement() {
        System.out.print("Enter date of emergency shift (yyyy-mm-dd): ");
        LocalDate date;
        try {
            date = LocalDate.parse(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid date.");
            return;
        }

        List<StaffMember> availableStaff = new ArrayList<>();
        for (StaffMember s : staffList) {
            if (s.isAvailable(date)) {
                availableStaff.add(s);
            }
        }

        if (availableStaff.isEmpty()) {
            System.out.println("No available staff for emergency shift on " + date);
        } else {
            System.out.println("Available staff for emergency shift:");
            for (StaffMember s : availableStaff) {
                System.out.println("- " + s);
            }
        }
    }

    // 10. Track Staff Attendance (Placeholder: just display message, since this needs more infrastructure)
    private static void trackAttendance() {
        System.out.println("Tracking staff attendance feature coming soon...");
    }

    // Helper to select staff
    private static StaffMember selectStaffMember() {
        if (staffList.isEmpty()) {
            System.out.println("No staff available.");
            return null;
        }
        System.out.println("Select staff member:");
        for (int i=0; i < staffList.size(); i++) {
            System.out.println((i+1) + ". " + staffList.get(i));
        }
        System.out.print("Choice: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx < 0 || idx >= staffList.size()) throw new Exception();
            return staffList.get(idx);
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return null;
        }
    }
}
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

// Main class to run the Order Management System
public class OrderManagement {
    public static void main(String[] args) {
        MenuController menuController = new MenuController();
        OrderController orderController = new OrderController(menuController);
        orderController.start();
    }
}

// === MODEL CLASSES ===

// Menu Item Model (simplified version from Menu Management)
class MenuItem {
    private String name;
    private double price;
    private String category;
    private String ingredients;
    private boolean isAvailable;

    public MenuItem(String name, double price, String category, String ingredients, boolean isAvailable) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.ingredients = ingredients;
        this.isAvailable = isAvailable;
    }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return String.format("%s (%.2f) - %s - %s", name, price, category, isAvailable ? "Available" : "Out of Stock");
    }
}

// Order Item - to hold item and quantity
class OrderItem {
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }
    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() {
        return menuItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return menuItem.getName() + " x " + quantity + " = $" + String.format("%.2f", getTotalPrice());
    }
}

// Order Status Enum
enum OrderStatus {
    PLACED, IN_PROGRESS, READY, COMPLETED, CANCELED
}

// Order Model
class Order {
    private static int nextOrderId = 1;

    private int orderId;
    private List<OrderItem> items;
    private OrderStatus status;
    private Date orderDate;
    private boolean paymentProcessed;
    private String specialRequests;

    public Order() {
        this.orderId = nextOrderId++;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PLACED;
        this.orderDate = new Date();
        this.paymentProcessed = false;
        this.specialRequests = "";
    }

    public int getOrderId() { return orderId; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public boolean isPaymentProcessed() { return paymentProcessed; }
    public void setPaymentProcessed(boolean processed) { this.paymentProcessed = processed; }
    public Date getOrderDate() { return orderDate; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String requests) { this.specialRequests = requests; }

    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate)).append("\n");
        if (!specialRequests.isEmpty()) {
            sb.append("Special Requests: ").append(specialRequests).append("\n");
        }
        sb.append("Items:\n");
        for (OrderItem item : items) {
            sb.append("  ").append(item).append("\n");
        }
        sb.append("Total: $").append(String.format("%.2f", calculateTotal())).append("\n");
        sb.append("Payment Processed: ").append(paymentProcessed ? "Yes" : "No");
        return sb.toString();
    }
}

// === VIEW ===
class OrderView {
    private Scanner scanner = new Scanner(System.in);

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public void displayOrderSummary(Order order) {
        System.out.println("----- ORDER SUMMARY -----");
        System.out.println(order);
        System.out.println("-------------------------");
    }

    public void displayMenuItems(List<MenuItem> items) {
        if (items.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }
        System.out.println("----- MENU ITEMS -----");
        for (MenuItem item : items) {
            System.out.println(item);
        }
        System.out.println("----------------------");
    }

    public String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int getIntInput(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(getInput(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
}

// === CONTROLLERS ===

// Simplified Menu Controller - preloaded items for demo and availability check
class MenuController {
    private List<MenuItem> menuItems;

    public MenuController() {
        menuItems = new ArrayList<>();
        // Preload some menu items
        menuItems.add(new MenuItem("Burger", 5.99, "Fast Food", "Beef, Bun, Lettuce", true));
        menuItems.add(new MenuItem("Veggie Wrap", 4.99, "Vegetarian", "Lettuce, Tomato, Wrap", true));
        menuItems.add(new MenuItem("Gluten-Free Salad", 6.99, "Salad", "Lettuce, Tomato, Cucumber", true));
        menuItems.add(new MenuItem("French Fries", 2.99, "Sides", "Potato, Salt", true));
        menuItems.add(new MenuItem("Chicken Nuggets", 4.50, "Fast Food", "Chicken, Bread Crumbs", true));
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public MenuItem findMenuItemByName(String name) {
        for (MenuItem item : menuItems) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public boolean isAvailable(String name) {
        MenuItem item = findMenuItemByName(name);
        return item != null && item.isAvailable();
    }

    public void updateAvailability(String name, boolean available) {
        MenuItem item = findMenuItemByName(name);
        if (item != null) {
            item.setAvailable(available);
        }
    }
}

// Order Controller - handles all order use cases
class OrderController {
    private Map<Integer, Order> orders;
    private OrderView view;
    private MenuController menuController;

    public OrderController(MenuController menuController) {
        this.menuController = menuController;
        this.orders = new HashMap<>();
        this.view = new OrderView();
    }

    public void start() {
        while (true) {
            view.displayMessage("\nORDER MANAGEMENT SYSTEM");
            view.displayMessage("1. Place an Order");
            view.displayMessage("2. Modify an Existing Order");
            view.displayMessage("3. Cancel an Order");
            view.displayMessage("4. View Order Summary");
            view.displayMessage("5. Process Payment for an Order");
            view.displayMessage("6. Track Order Status");
            view.displayMessage("7. Handle Special Requests");
            view.displayMessage("8. Generate Order Receipt");
            view.displayMessage("9. Generate Daily Order Report");
            view.displayMessage("10. Exit");

            String choice = view.getInput("Select option: ");
            switch (choice) {
                case "1": placeOrder(); break;
                case "2": modifyOrder(); break;
                case "3": cancelOrder(); break;
                case "4": viewOrderSummary(); break;
                case "5": processPayment(); break;
                case "6": trackOrderStatus(); break;
                case "7": handleSpecialRequests(); break;
                case "8": generateReceipt(); break;
                case "9": generateDailyReport(); break;
                case "10": 
                    view.displayMessage("Exiting system.");
                    return;
                default: view.displayMessage("Invalid option, try again.");
            }
        }
    }

    // UC1: Place an Order
    private void placeOrder() {
        Order order = new Order();
        view.displayMessage("Place your order by selecting items. Type 'done' to finish.");
        while (true) {
            view.displayMenuItems(menuController.getMenuItems());
            String itemName = view.getInput("Enter item name (or 'done'): ");
            if (itemName.equalsIgnoreCase("done")) break;

            MenuItem menuItem = menuController.findMenuItemByName(itemName);
            if (menuItem == null) {
                view.displayMessage("Item not found.");
                continue;
            }
            if (!menuItem.isAvailable()) {
                view.displayMessage("Item is currently out of stock.");
                continue;
            }

            int qty = view.getIntInput("Enter quantity: ");
            if (qty <= 0) {
                view.displayMessage("Quantity must be positive.");
                continue;
            }

            // Check if already in order, then increase quantity
            boolean found = false;
            for (OrderItem oi : order.getItems()) {
                if (oi.getMenuItem().getName().equalsIgnoreCase(itemName)) {
                    oi.setQuantity(oi.getQuantity() + qty);
                    found = true;
                    break;
                }
            }
            if (!found) {
                order.getItems().add(new OrderItem(menuItem, qty));
            }
            view.displayMessage("Added " + qty + " x " + itemName);
        }

        if (order.getItems().isEmpty()) {
            view.displayMessage("No items added. Order cancelled.");
            return;
        }

        orders.put(order.getOrderId(), order);
        view.displayMessage("Order placed successfully! Your Order ID is " + order.getOrderId());
    }

    // UC2: Modify an Existing Order (if not confirmed)
    private void modifyOrder() {
        int id = view.getIntInput("Enter Order ID to modify: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        if (order.isPaymentProcessed()) {
            view.displayMessage("Cannot modify an order that has been paid.");
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            view.displayMessage("Order is canceled and cannot be modified.");
            return;
        }

        while (true) {
            view.displayOrderSummary(order);
            view.displayMessage("1. Add Item\n2. Remove Item\n3. Done");
            String choice = view.getInput("Select option: ");
            if (choice.equals("3")) break;

            switch (choice) {
                case "1":
                    view.displayMenuItems(menuController.getMenuItems());
                    String itemName = view.getInput("Enter item name to add: ");
                    MenuItem menuItem = menuController.findMenuItemByName(itemName);
                    if (menuItem == null) {
                        view.displayMessage("Item not found.");
                        continue;
                    }
                    if (!menuItem.isAvailable()) {
                        view.displayMessage("Item is out of stock.");
                        continue;
                    }
                    int qty = view.getIntInput("Enter quantity: ");
                    if (qty <= 0) {
                        view.displayMessage("Quantity must be positive.");
                        continue;
                    }
                    boolean found = false;
                    for (OrderItem oi : order.getItems()) {
                        if (oi.getMenuItem().getName().equalsIgnoreCase(itemName)) {
                            oi.setQuantity(oi.getQuantity() + qty);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        order.getItems().add(new OrderItem(menuItem, qty));
                    }
                    view.displayMessage("Item added.");
                    break;

                case "2":
                    if (order.getItems().isEmpty()) {
                        view.displayMessage("No items to remove.");
                        continue;
                    }
                    String remName = view.getInput("Enter item name to remove: ");
                    Iterator<OrderItem> iter = order.getItems().iterator();
                    boolean removed = false;
                    while (iter.hasNext()) {
                        OrderItem oi = iter.next();
                        if (oi.getMenuItem().getName().equalsIgnoreCase(remName)) {
                            iter.remove();
                            removed = true;
                            break;
                        }
                    }
                    if (removed) {
                        view.displayMessage("Item removed.");
                    } else {
                        view.displayMessage("Item not found in order.");
                    }
                    break;
                default:
                    view.displayMessage("Invalid option.");
            }
        }
        view.displayMessage("Modification complete.");
    }

    // UC3: Cancel an Order (before processed)
    private void cancelOrder() {
        int id = view.getIntInput("Enter Order ID to cancel: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            view.displayMessage("Order is already canceled.");
            return;
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            view.displayMessage("Cannot cancel a completed order.");
            return;
        }
        order.setStatus(OrderStatus.CANCELED);
        // Optionally update inventory if needed here
        view.displayMessage("Order canceled successfully.");
    }

    // UC4: View Order Summary
    private void viewOrderSummary() {
        int id = view.getIntInput("Enter Order ID to view summary: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        view.displayOrderSummary(order);
    }

    // UC5: Process Payment
    private void processPayment() {
        int id = view.getIntInput("Enter Order ID to process payment: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        if (order.isPaymentProcessed()) {
            view.displayMessage("Payment already processed.");
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            view.displayMessage("Cannot process payment for canceled order.");
            return;
        }

        view.displayOrderSummary(order);
        String paymentMethod = view.getInput("Enter payment method (cash/card/other): ");
        // For simplicity, assume payment always succeeds
        order.setPaymentProcessed(true);
        order.setStatus(OrderStatus.COMPLETED);
        view.displayMessage("Payment processed successfully via " + paymentMethod + ". Order completed.");
    }

    // UC6: Track Order Status
    private void trackOrderStatus() {
        int id = view.getIntInput("Enter Order ID to track: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        view.displayMessage("Order ID " + id + " Status: " + order.getStatus());
    }

    // UC7: Handle Special Requests
    private void handleSpecialRequests() {
        int id = view.getIntInput("Enter Order ID to add special requests: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        String requests = view.getInput("Enter special requests (e.g., allergen-free, custom instructions): ");
        order.setSpecialRequests(requests);
        view.displayMessage("Special requests updated.");
    }

    // UC8: Generate Order Receipt
    private void generateReceipt() {
        int id = view.getIntInput("Enter Order ID to generate receipt: ");
        Order order = orders.get(id);
        if (order == null) {
            view.displayMessage("Order not found.");
            return;
        }
        if (!order.isPaymentProcessed()) {
            view.displayMessage("Payment not processed. Receipt not generated.");
            return;
        }
        String receipt = buildReceipt(order);
        view.displayMessage(receipt);
        // Optionally, write to file
        try {
            String filename = "OrderReceipt_" + order.getOrderId() + ".txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(receipt);
            writer.close();
            view.displayMessage("Receipt saved to file: " + filename);
        } catch (IOException e) {
            view.displayMessage("Error saving receipt to file.");
        }
    }

    private String buildReceipt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("***** ORDER RECEIPT *****\n");
        sb.append("Order ID: ").append(order.getOrderId()).append("\n");
        sb.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getOrderDate())).append("\n");
        if (!order.getSpecialRequests().isEmpty()) {
            sb.append("Special Requests: ").append(order.getSpecialRequests()).append("\n");
        }
        sb.append("------------------------\n");
        for (OrderItem item : order.getItems()) {
            sb.append(item).append("\n");
        }
        sb.append("------------------------\n");
        sb.append("Total Paid: $").append(String.format("%.2f", order.calculateTotal())).append("\n");
        sb.append("Payment Method: Processed\n");
        sb.append("************************");
        return sb.toString();
    }

    // UC9: Integration with Menu Management (Inventory updates)
    // This demo version does not adjust inventory quantities but could be extended to.
    // We simulate availability updates in modify and place order by checking isAvailable.

    // UC10: Daily Order Report
    private void generateDailyReport() {
        int totalOrders = 0;
        double totalRevenue = 0;
        Map<String, Integer> popularItems = new HashMap<>();

        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        for (Order order : orders.values()) {
            // Consider only completed orders today (for simplicity, all orders)
            if (order.getStatus() == OrderStatus.COMPLETED) {
                totalOrders++;
                totalRevenue += order.calculateTotal();
                for (OrderItem item : order.getItems()) {
                    popularItems.put(item.getMenuItem().getName(),
                        popularItems.getOrDefault(item.getMenuItem().getName(), 0) + item.getQuantity());
                }
            }
        }

        view.displayMessage("----- DAILY ORDER REPORT -----");
        view.displayMessage("Total Orders: " + totalOrders);
        view.displayMessage("Total Revenue: $" + String.format("%.2f", totalRevenue));
        view.displayMessage("Popular Menu Items:");
        popularItems.entrySet()
            .stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach(e -> view.displayMessage(e.getKey() + ": " + e.getValue() + " sold"));
        view.displayMessage("------------------------------");
    }
}
import java.io.*;
import java.util.*;

// Main class to start the menu system (to hold the single instance)
public class MenuManagement {

    public static void main(String[] args) {
        MenuManagement menuManagement = new MenuManagement();
        menuManagement.runMenuManagement();
    }

    public void runMenuManagement() {
        MenuController controller = new MenuController();
        controller.start();
    }
}
// Model: Represents a Menu Item
class MenuItem {
    private String name;
    private double price;
    private String category;
    private String ingredients;
    private boolean isAvailable;

    public MenuItem(String name, double price, String category, String ingredients, boolean isAvailable) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.ingredients = ingredients;
        this.isAvailable = isAvailable;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return String.format("Name: %s | Price: $%.2f | Category: %s | Ingredients: %s | Available: %s",
                name, price, category, ingredients, isAvailable ? "Yes" : "No");
    }
}

// View: Handles Display and Input
class MenuView {
    private Scanner scanner = new Scanner(System.in);

    public void displayMenu(List<MenuItem> menuItems) {
        if (menuItems.isEmpty()) {
            System.out.println("No menu items available.");
        } else {
            System.out.println("\nMenu Items:");
            for (MenuItem item : menuItems) {
                System.out.println(item);
            }
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}

// Controller: Coordinates Actions
class MenuController {
    private List<MenuItem> menuItems;
    private MenuView view;

    public MenuController() {
        menuItems = new ArrayList<>();
        view = new MenuView();
    }

    // Start the menu system loop
    public void start() {
        while (true) {
            view.showMessage("\nMenu Management System:");
            view.showMessage("1. Add Menu Item");
            view.showMessage("2. Update Menu Item");
            view.showMessage("3. Delete Menu Item");
            view.showMessage("4. Mark Item as Out of Stock");
            view.showMessage("5. Mark Item as Available");
            view.showMessage("6. Search for a Menu Item");
            view.showMessage("7. Filter Menu by Dietary Requirements");
            view.showMessage("8. Import Menu from File");
            view.showMessage("9. Export Menu to File");
            view.showMessage("10. Exit");

            String choice = view.getInput("Choose an option: ");

            switch (choice) {
                case "1":
                    addMenuItem();
                    break;
                case "2":
                    updateMenuItem();
                    break;
                case "3":
                    deleteMenuItem();
                    break;
                case "4":
                    markOutOfStock();
                    break;
                case "5":
                    markAvailable();
                    break;
                case "6":
                    searchMenuItem();
                    break;
                case "7":
                    filterMenu();
                    break;
                case "8":
                    importMenuFromFile();
                    break;
                case "9":
                    exportMenuToFile();
                    break;
                case "10":
                    view.showMessage("Exiting the system.");
                    return;
                default:
                    view.showMessage("Invalid option. Please try again.");
            }
        }
    }

    // All public so they can be called from outside if needed
    public void addMenuItem() {
        String name = view.getInput("Enter item name: ");
        double price = readDouble("Enter price: ");
        String category = view.getInput("Enter category: ");
        String ingredients = view.getInput("Enter ingredients: ");
        menuItems.add(new MenuItem(name, price, category, ingredients, true));
        view.showMessage("Menu item added successfully.");
    }

    public void updateMenuItem() {
        String name = view.getInput("Enter the name of the item to update: ");
        MenuItem item = findMenuItemByName(name);
        if (item != null) {
            double price = readDouble("Enter new price: ");
            String ingredients = view.getInput("Enter new ingredients: ");
            item.setPrice(price);
            item.setIngredients(ingredients);
            view.showMessage("Menu item updated successfully.");
        } else {
            view.showMessage("Item not found.");
        }
    }

    public void deleteMenuItem() {
        String name = view.getInput("Enter the name of the item to delete: ");
        boolean removed = menuItems.removeIf(item -> item.getName().equalsIgnoreCase(name));
        if (removed) {
            view.showMessage("Menu item deleted successfully.");
        } else {
            view.showMessage("Item not found.");
        }
    }

    public void markOutOfStock() {
        String name = view.getInput("Enter the name of the item to mark as out of stock: ");
        MenuItem item = findMenuItemByName(name);
        if (item != null) {
            item.setAvailable(false);
            view.showMessage("Menu item marked as out of stock.");
        } else {
            view.showMessage("Item not found.");
        }
    }

    public void markAvailable() {
        String name = view.getInput("Enter the name of the item to mark as available: ");
        MenuItem item = findMenuItemByName(name);
        if (item != null) {
            item.setAvailable(true);
            view.showMessage("Menu item marked as available.");
        } else {
            view.showMessage("Item not found.");
        }
    }

    public void searchMenuItem() {
        String name = view.getInput("Enter the name of the item to search: ");
        MenuItem item = findMenuItemByName(name);
        if (item != null) {
            view.showMessage(item.toString());
        } else {
            view.showMessage("Item not found.");
        }
    }

    public void filterMenu() {
        String filter = view.getInput("Enter dietary requirement to filter by (e.g., vegetarian): ");
        List<MenuItem> filteredItems = new ArrayList<>();
        for (MenuItem item : menuItems) {
            if (item.getIngredients().toLowerCase().contains(filter.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        view.displayMenu(filteredItems);
    }

    public void importMenuFromFile() {
        String filePath = view.getInput("Enter file path to import: ");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String name = parts[0];
                    double price = Double.parseDouble(parts[1]);
                    String category = parts[2];
                    String ingredients = parts[3];
                    boolean isAvailable = Boolean.parseBoolean(parts[4]);
                    menuItems.add(new MenuItem(name, price, category, ingredients, isAvailable));
                }
            }
            view.showMessage("Menu imported successfully.");
        } catch (IOException | NumberFormatException e) {
            view.showMessage("Error reading file: " + e.getMessage());
        }
    }

    public void exportMenuToFile() {
        String filePath = view.getInput("Enter file path to export: ");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (MenuItem item : menuItems) {
                bw.write(item.getName() + "," + item.getPrice() + "," + item.getCategory() + "," +
                        item.getIngredients() + "," + item.isAvailable());
                bw.newLine();
            }
            view.showMessage("Menu exported successfully.");
        } catch (IOException e) {
            view.showMessage("Error writing file: " + e.getMessage());
        }
    }

    // Helper method: find menu item by name (case-insensitive)
    public MenuItem findMenuItemByName(String name) {
        for (MenuItem item : menuItems) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    // Getter for menuItems list, if needed
    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    // Helper method to safely read double input
    private double readDouble(String prompt) {
        while (true) {
            try {
                String input = view.getInput(prompt);
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                view.showMessage("Invalid number format. Please enter a valid decimal number.");
            }
        }
    }
}


