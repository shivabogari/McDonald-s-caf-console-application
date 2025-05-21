git commit -m "Revised code after review"
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

    public void runStaffScheduling(){
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
