package main.java.ui;

import java.util.Scanner;
import java.util.InputMismatchException;

public class PrincipalMenu {
    private final ProjectMenu projectMenu;
    private final DevisMenu devisMenu;
    private final ClientMenu clientMenu;
    private final CostCalculationMenu costCalculationMenu;
    private final ComponentMenu componentMenu;
    private final Scanner scanner;

    public PrincipalMenu(ProjectMenu projectMenu, DevisMenu devisMenu, ClientMenu clientMenu,
                         CostCalculationMenu costCalculationMenu, ComponentMenu componentMenu) {
        this.projectMenu = projectMenu;
        this.devisMenu = devisMenu;
        this.clientMenu = clientMenu;
        this.costCalculationMenu = costCalculationMenu;
        this.componentMenu = componentMenu;
        this.scanner = new Scanner(System.in);
    }

    public void menu() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getValidInput(1, 7);

            try {
                switch (choice) {
                    case 1:
                        projectAddMenu();
                        break;
                    case 2:
                        oldProjectsMenu();
                        break;
                    case 3:
                        totalCost();
                        break;
                    case 4:
                        devisMenu();
                        break;
                    case 5:
                        clientMenu();
                        break;
                    case 6:
                        componentMenu();
                        break;
                    case 7:
                        running = false;
                        System.out.println("Thank you for using the Kitchen Renovation Project Management Application. Goodbye!");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n=== Welcome to the Kitchen Renovation Project Management Application ===");
        System.out.println("1. Create a new project");
        System.out.println("2. Display existing projects");
        System.out.println("3. Calculate project cost");
        System.out.println("4. Devis Menu");
        System.out.println("5. Client Menu");
        System.out.println("6. Components Menu");
        System.out.println("7. Quit");
    }

    private int getValidInput(int min, int max) {
        while (true) {
            try {
                System.out.print("Please select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println("Invalid option. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    private void projectAddMenu() {
        try {
            this.projectMenu.addOrSearchClientMenu();
        } catch (Exception e) {
            System.out.println("Error in Project Add Menu: " + e.getMessage());
        }
    }

    private void oldProjectsMenu() {
        try {
            this.projectMenu.findAll();
        } catch (Exception e) {
            System.out.println("Error displaying existing projects: " + e.getMessage());
        }
    }

    private void devisMenu() {
        try {
            this.devisMenu.displayMenu();
        } catch (Exception e) {
            System.out.println("Error in Devis Menu: " + e.getMessage());
        }
    }

    private void clientMenu() {
        try {
            this.clientMenu.clientMenu();
        } catch (Exception e) {
            System.out.println("Error in Client Menu: " + e.getMessage());
        }
    }

    private void totalCost() {
        try {
            costCalculationMenu.save();
        } catch (Exception e) {
            System.out.println("Error calculating total cost: " + e.getMessage());
        }
    }

    private void componentMenu() {
        try {
            this.componentMenu.menu();
        } catch (Exception e) {
            System.out.println("Error in Component Menu: " + e.getMessage());
        }
    }
}