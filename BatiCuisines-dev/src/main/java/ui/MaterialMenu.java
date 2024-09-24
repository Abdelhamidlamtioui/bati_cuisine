package main.java.ui;

import main.java.domain.entities.Component;
import main.java.domain.entities.Material;
import main.java.domain.entities.Project;
import main.java.domain.enums.ComponentType;
import main.java.exception.MaterialNotFoundException;
import main.java.service.ComponentService;
import main.java.service.MaterialService;

import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;

public class MaterialMenu {
    private final MaterialService materialService;
    private final ComponentService componentService;
    private final Scanner scanner;

    public MaterialMenu(MaterialService materialService, ComponentService componentService) {
        this.materialService = materialService;
        this.componentService = componentService;
        this.scanner = new Scanner(System.in);
    }

    public void materialMenu() {
        int choice;

        do {
            displayMenu();
            choice = getValidIntInput("Enter your choice: ", 1, 6);

            try {
                switch (choice) {
                    case 1:
                        addMaterial(new Project());
                        break;
                    case 2:
                        findAll();
                        break;
                    case 3:
                        findById();
                        break;
                    case 4:
                        update();
                        break;
                    case 5:
                        delete();
                        break;
                    case 6:
                        System.out.println("Exiting material menu...");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        } while (choice != 6);
    }

    private void displayMenu() {
        System.out.println("\n--- Material Management Menu ---");
        System.out.println("1. Add new material");
        System.out.println("2. Find all materials");
        System.out.println("3. Find material by ID");
        System.out.println("4. Update material");
        System.out.println("5. Delete material");
        System.out.println("6. Exit");
    }

    public Material addMaterial(Project project) {
        Material material = null;
        do {
            System.out.println("--- Add Material ---");

            String name = getValidStringInput("Enter the name of the material: ");
            double quantity = getValidDoubleInput("Enter the quantity of this material: ", 0, Double.MAX_VALUE);
            double unitCost = getValidDoubleInput("Enter the unit cost of the material (€/m² or €/litre): ", 0, Double.MAX_VALUE);
            double transportCost = getValidDoubleInput("Enter the transport cost of the material (€): ", 0, Double.MAX_VALUE);
            double coefficientQuality = getValidDoubleInput("Enter the quality coefficient of the material (1.0 = standard, > 1.0 = high quality): ", 1.0, Double.MAX_VALUE);
            double vatRate = getValidDoubleInput("Enter the VAT rate of the material: ", 0, 100);

            material = new Material(0L, name, "Material", vatRate, project, unitCost, quantity, transportCost, coefficientQuality);

            try {
                Material savedMaterial = materialService.save(material);
                System.out.println("Material saved successfully with ID: " + savedMaterial.getId());
            } catch (Exception e) {
                System.out.println("Error saving material: " + e.getMessage());
            }

        } while (getYesNoInput("Would you like to add another material? (y/n): "));

        return material;
    }

    public void findAll() {
        System.out.println("--- List of All Materials ---");

        try {
            List<Material> materials = materialService.findAll();

            if (materials.isEmpty()) {
                System.out.println("No materials found.");
                return;
            }

            printMaterialTable(materials);
        } catch (Exception e) {
            System.out.println("Error fetching materials: " + e.getMessage());
        }
    }

    public void findById() {
        System.out.println("--- Find Material by Id ---");
        Long id = getValidLongInput("Enter the id of the material: ", 1L, Long.MAX_VALUE);

        try {
            materialService.findById(id).ifPresentOrElse(
                    this::printMaterialDetails,
                    () -> System.out.println("Material with id " + id + " not found.")
            );
        } catch (Exception e) {
            System.out.println("Error finding material: " + e.getMessage());
        }
    }

    public void update() {
        System.out.println("--- Update Material ---");
        Long id = getValidLongInput("Enter the id of the material: ", 1L, Long.MAX_VALUE);

        try {
            Material material = materialService.findById(id)
                    .orElseThrow(() -> new MaterialNotFoundException("Material not found"));

            String name = getValidStringInput("Enter the name of the material: ");
            double quantity = getValidDoubleInput("Enter the quantity of this material: ", 0, Double.MAX_VALUE);
            double unitCost = getValidDoubleInput("Enter the unit cost of the material (€/m² or €/litre): ", 0, Double.MAX_VALUE);
            double transportCost = getValidDoubleInput("Enter the transport cost of the material (€): ", 0, Double.MAX_VALUE);
            double coefficientQuality = getValidDoubleInput("Enter the quality coefficient of the material (1.0 = standard, > 1.0 = high quality): ", 1.0, Double.MAX_VALUE);
            double vatRate = getValidDoubleInput("Enter the VAT rate of the material: ", 0, 100);

            Material updatedMaterial = new Material(id, name, ComponentType.MATERIAL.name(), vatRate, material.getProject(), quantity, unitCost, transportCost, coefficientQuality);
            materialService.update(updatedMaterial);
            System.out.println("Material updated successfully.");
        } catch (MaterialNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating material: " + e.getMessage());
        }
    }

    public void delete() {
        System.out.println("--- Delete Material ---");
        Long id = getValidLongInput("Enter the id of the material: ", 1L, Long.MAX_VALUE);

        try {
            materialService.delete(id);
            System.out.println("Material deleted successfully.");
        } catch (Exception e) {
            System.out.println("Error deleting material: " + e.getMessage());
        }
    }

    private void printMaterialTable(List<Material> materials) {
        System.out.printf("| %-10s | %-20s | %-10s | %-10s | %-15s | %-20s |%n",
                "ID", "Component Name", "Unit Cost", "Quantity", "Transport Cost", "Coefficient Quality");
        System.out.println("-------------------------------------------------------------------------------");

        materials.forEach(material -> {
            System.out.printf("| %-10d | %-20s | %-10.2f | %-10.2f | %-15.2f | %-20.2f |%n",
                    material.getId(),
                    material.getName(),
                    material.getUnitCost(),
                    material.getQuantity(),
                    material.getTransportCost(),
                    material.getCoefficientQuality());
        });

        System.out.println("-------------------------------------------------------------------------------");
    }

    private void printMaterialDetails(Material material) {
        System.out.printf("| %-15s | %-20s |%n", "Property", "Value");
        System.out.println("----------------------------------------------");
        System.out.printf("| %-15s | %-20d |%n", "ID", material.getId());
        System.out.printf("| %-15s | %-20s |%n", "Component Name", material.getName());
        System.out.printf("| %-15s | %-20.2f |%n", "Unit Cost", material.getUnitCost());
        System.out.printf("| %-15s | %-20.2f |%n", "Quantity", material.getQuantity());
        System.out.printf("| %-15s | %-20.2f |%n", "Transport Cost", material.getTransportCost());
        System.out.printf("| %-15s | %-20.2f |%n", "Coefficient Quality", material.getCoefficientQuality());
        System.out.println("----------------------------------------------");
    }

    private int getValidIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    private long getValidLongInput(String prompt, long min, long max) {
        while (true) {
            try {
                System.out.print(prompt);
                long input = scanner.nextLong();
                scanner.nextLine(); // Consume newline
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    private double getValidDoubleInput(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                double input = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    private String getValidStringInput(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }

    private boolean getYesNoInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }
}