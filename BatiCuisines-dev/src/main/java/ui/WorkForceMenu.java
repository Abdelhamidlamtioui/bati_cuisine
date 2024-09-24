package main.java.ui;

import main.java.domain.entities.Component;
import main.java.domain.entities.Project;
import main.java.domain.entities.WorkForce;
import main.java.domain.enums.ComponentType;
import main.java.exception.LaborNotFoundException;
import main.java.service.ComponentService;
import main.java.service.WorkForceService;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class WorkForceMenu {
    private final WorkForceService workForceService;
    private final ComponentService componentService;
    private final Scanner scanner;

    public WorkForceMenu(WorkForceService workForceService, ComponentService componentService) {
        this.workForceService = workForceService;
        this.componentService = componentService;
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        int choice;
        do {
            try {
                System.out.println("\n--- Workforce Management Menu ---");
                System.out.println("1. Add Workforce");
                System.out.println("2. Update Workforce");
                System.out.println("3. Delete Workforce");
                System.out.println("4. Find Workforce by ID");
                System.out.println("5. List All Workforces");
                System.out.println("0. Exit");
                System.out.print("Select an option: ");
                choice = getValidIntInput();

                switch (choice) {
                    case 1:
                        addWorkForce(new Project());
                        break;
                    case 2:
                        update();
                        break;
                    case 3:
                        delete();
                        break;
                    case 4:
                        findById();
                        break;
                    case 5:
                        findAll();
                        break;
                    case 0:
                        System.out.println("Exiting Workforce Management.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a valid option.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                choice = -1; // Set to invalid choice to continue the loop
            }
        } while (choice != 0);
    }

    public WorkForce addWorkForce(Project project) {
        String continueChoice;
        WorkForce workForce = null;
        do {
            try {
                System.out.println("--- Add Workforce ---");

                System.out.print("Enter the name of the Workforce: ");
                String name = scanner.nextLine();

                double vatRate = getValidDoubleInput("Enter the VAT rate of the workforce: ");
                double hourlyRate = getValidDoubleInput("Enter the hourly rate for this labor (€/h): ");
                double hoursWorked = getValidDoubleInput("Enter the number of hours worked: ");
                double productivityFactor = getValidDoubleInput("Enter the productivity factor (1.0 = standard, > 1.0 = high productivity): ");

                workForce = new WorkForce(0L, name, "workforce", vatRate, project, hourlyRate, hoursWorked, productivityFactor);
                workForceService.save(workForce);

                System.out.print("Would you like to add another workforce? (y/n): ");
                continueChoice = scanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                System.out.println("An error occurred while adding workforce: " + e.getMessage());
                continueChoice = "n";
            }
        } while (continueChoice.equals("y"));

        return workForce;
    }

    public void update() {
        try {
            System.out.print("Enter id of workforce: ");
            Long id = getValidLongInput();
            WorkForce existingWorkForce = workForceService.findById(id)
                    .orElseThrow(() -> new LaborNotFoundException("Workforce not found"));

            System.out.print("Enter name of workforce: ");
            String name = scanner.nextLine();
            double hoursWorked = getValidDoubleInput("Enter number of hours worked: ");
            double hourlyCost = getValidDoubleInput("Enter number of hourly cost: ");
            double productivityFactor = getValidDoubleInput("Enter productivity factor: ");
            System.out.print("Enter the name of component: ");
            String componentName = scanner.nextLine();
            double vatRate = getValidDoubleInput("Enter vat rate: ");

            WorkForce updatedWorkForce = new WorkForce(id, name, ComponentType.WORKFORCE.name(), vatRate,
                    existingWorkForce.getProject(), hourlyCost, hoursWorked, productivityFactor);
            this.workForceService.update(updatedWorkForce);
            System.out.println("Workforce updated successfully.");
        } catch (LaborNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred while updating workforce: " + e.getMessage());
        }
    }

    public void delete() {
        try {
            System.out.print("Enter a workforce Id: ");
            Long id = getValidLongInput();
            this.workForceService.delete(id);
            System.out.println("Workforce deleted successfully.");
        } catch (Exception e) {
            System.out.println("An error occurred while deleting workforce: " + e.getMessage());
        }
    }

    public void findById() {
        try {
            System.out.print("Enter the workforce ID: ");
            Long id = getValidLongInput();

            workForceService.findById(id).ifPresentOrElse(this::printWorkForce, () -> {
                System.out.println("Workforce with ID " + id + " not found.");
                System.out.println("-------------------------------------------------------------------------------");
            });
        } catch (Exception e) {
            System.out.println("An error occurred while finding workforce: " + e.getMessage());
        }
    }

    public void findAll() {
        try {
            System.out.println("--- List of All Workforce ---");
            List<WorkForce> workForces = workForceService.findAll();

            printWorkForceHeader();

            if (workForces.isEmpty()) {
                System.out.println("| No workforce found.");
                System.out.println("-------------------------------------------------------------------------------");
                return;
            }

            workForces.forEach(this::printWorkForce);
            System.out.println("-------------------------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("An error occurred while fetching workforces: " + e.getMessage());
        }
    }

    private void printWorkForceHeader() {
        System.out.printf("| %-10s | %-15s | %-15s | %-15s | %-20s |%n",
                "ID", "Hourly Cost", "Working Hours", "Productivity", "Component Name");
        System.out.println("-------------------------------------------------------------------------------");
    }

    private void printWorkForce(WorkForce workForce) {
        System.out.printf("| %-10d | %-15.2f | %-15.2f | %-15.2f | %-20s |%n",
                workForce.getId(),
                workForce.getHourlyCost(),
                workForce.getWorkingHours(),
                workForce.getWorkerProductivity(),
                workForce.getName());
    }

    private int getValidIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    private long getValidLongInput() {
        while (true) {
            try {
                return scanner.nextLong();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    private double getValidDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }
}