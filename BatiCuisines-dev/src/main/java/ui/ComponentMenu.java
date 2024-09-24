package main.java.ui;

import java.util.Scanner;
import java.util.InputMismatchException;

public class ComponentMenu {
    private final MaterialMenu materialMenu;
    private final WorkForceMenu workForceMenu;
    private final static Scanner scanner = new Scanner(System.in);

    public ComponentMenu(MaterialMenu materialMenu, WorkForceMenu workForceMenu) {
        this.materialMenu = materialMenu;
        this.workForceMenu = workForceMenu;
    }

    public void menu() {
        int choice;
        do {
            displayMenu();
            choice = getValidChoice();

            try {
                switch (choice) {
                    case 1:
                        materialMenu();
                        break;
                    case 2:
                        workForceMenu();
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose again.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        } while (choice != 3);
    }

    private void displayMenu() {
        System.out.println("\n--- Component Menu ---");
        System.out.println("1- Material Menu");
        System.out.println("2- Workforce Menu");
        System.out.println("3- Exit");
    }

    private int getValidChoice() {
        while (true) {
            System.out.print("Enter your choice: ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                return choice;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    public void workForceMenu() {
        try {
            this.workForceMenu.displayMenu();
        } catch (Exception e) {
            System.out.println("Error in Workforce Menu: " + e.getMessage());
        }
    }

    public void materialMenu() {
        try {
            this.materialMenu.materialMenu();
        } catch (Exception e) {
            System.out.println("Error in Material Menu: " + e.getMessage());
        }
    }
}