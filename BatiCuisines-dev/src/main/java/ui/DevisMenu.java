package main.java.ui;

import main.java.domain.entities.Devis;
import main.java.domain.entities.Project;
import main.java.domain.enums.ProjectStatus;
import main.java.exception.DevisNotFoundException;
import main.java.exception.ProjectNotFoundException;
import main.java.repository.impl.ProjectRepository;
import main.java.service.DevisService;
import main.java.service.ProjectService;
import main.java.utils.DateFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.InputMismatchException;

public class DevisMenu {

    private final Scanner scanner;
    private final DevisService devisService;
    private final ProjectService projectService;

    public DevisMenu(DevisService devisService, ProjectService projectService) {
        this.devisService = devisService;
        this.projectService = projectService;
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\nDevis Management Menu");
            System.out.println("1. Save Devis");
            System.out.println("2. Delete Devis");
            System.out.println("3. Find All Devis");
            System.out.println("4. Find Devis by ID");
            System.out.println("5. Update Devis");
            System.out.println("6. Accept Devis");
            System.out.println("7. Exit");

            int choice = getValidIntInput("Choose an option: ", 1, 7);

            switch (choice) {
                case 1:
                    saveDevis();
                    break;
                case 2:
                    delete();
                    break;
                case 3:
                    findAll();
                    break;
                case 4:
                    findById();
                    break;
                case 5:
                    update();
                    break;
                case 6:
                    acceptDevis();
                    break;
                case 7:
                    System.out.println("Exiting...");
                    return;
            }
        }
    }

    public void save(Devis devis) {
        try {
            devisService.save(devis);
            System.out.println("Devis saved successfully.");
        } catch (Exception e) {
            System.out.println("Error saving Devis: " + e.getMessage());
        }
    }

    private void saveDevis() {
        try {
            System.out.print("Enter Project Name: ");
            String projectName = scanner.nextLine();
            Project project = projectService.findProjectByName(projectName);

            double estimatedAmount = getValidDoubleInput("Enter estimated amount: ", 0, Double.MAX_VALUE);

            LocalDate issueDate = getValidDate("Enter issue date (yyyy-MM-dd): ");
            LocalDate validatedDate = getValidDate("Enter validated date (yyyy-MM-dd): ");

            Devis devis = new Devis(0L, estimatedAmount, issueDate, validatedDate, false, project);
            save(devis);
        } catch (ProjectNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            Long id = getValidLongInput("Enter Devis ID: ", 1L, Long.MAX_VALUE);
            devisService.delete(id);
            System.out.println("Devis deleted successfully.");
        } catch (DevisNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public void findAll() {
        try {
            List<Devis> devisList = devisService.findAll();
            if (devisList.isEmpty()) {
                System.out.println("No Devis found.");
                return;
            }
            printDevisTable(devisList);
        } catch (Exception e) {
            System.out.println("An error occurred while fetching Devis: " + e.getMessage());
        }
    }

    private void findById() {
        try {
            Long id = getValidLongInput("Enter Devis ID: ", 1L, Long.MAX_VALUE);
            Optional<Devis> devis = devisService.findById(id);
            devis.ifPresentOrElse(
                    this::printSingleDevis,
                    () -> System.out.println("Devis not found.")
            );
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void update() {
        try {
            Long id = getValidLongInput("Enter Devis ID: ", 1L, Long.MAX_VALUE);
            Optional<Devis> existingDevis = devisService.findById(id);
            if (existingDevis.isEmpty()) {
                System.out.println("Devis not found.");
                return;
            }

            double estimatedAmount = getValidDoubleInput("Enter new estimated amount: ", 0, Double.MAX_VALUE);
            LocalDate issueDate = getValidDate("Enter new issue date (yyyy-MM-dd): ");
            LocalDate validatedDate = getValidDate("Enter new validated date (yyyy-MM-dd): ");

            System.out.print("Enter Project Name: ");
            String projectName = scanner.nextLine();
            Project project = projectService.findProjectByName(projectName);

            Devis updatedDevis = new Devis(id, estimatedAmount, issueDate, validatedDate, existingDevis.get().isAccepted(), project);
            devisService.update(updatedDevis);
            System.out.println("Devis updated successfully.");
        } catch (ProjectNotFoundException | DevisNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public void findDevisByProject(Long projectId) {
        try {
            Optional<Devis> devisOptional = this.devisService.findDevisByproject(projectId);
            devisOptional.ifPresentOrElse(
                    this::printSingleDevis,
                    () -> System.out.println("No Devis found for the given project.")
            );
        } catch (DevisNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public void acceptDevis() {
        try {
            System.out.print("Enter Project Name: ");
            String projectName = scanner.nextLine();
            Project project = this.projectService.findProjectByName(projectName);

            Optional<Devis> devis = this.devisService.findDevisByproject(project.getId());
            if (devis.isPresent()) {
                if (getYesNoInput("Do you want to accept this devis? (y/n): ")) {
                    this.devisService.updateDevisStatus(devis.get().getId());
                    this.projectService.updateProjectStatus(project.getId(), ProjectStatus.FINISHED.name());
                    System.out.println("Devis accepted. Project status updated to FINISHED.");
                } else {
                    System.out.println("Devis not accepted.");
                }
            } else {
                System.out.println("Devis not found for the given project.");
            }
        } catch (ProjectNotFoundException | DevisNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void printDevisTable(List<Devis> devisList) {
        System.out.printf("+--------------+-------------------+--------------+--------------+-------------+--------------------+--------------------+%n");
        System.out.printf("| %-12s | %-17s | %-12s | %-12s | %-11s | %-18s | %-18s |%n",
                "Devis ID", "Estimated Amount", "Issue Date", "Validated Date", "Is Accepted", "Project Name", "Client Name");
        System.out.printf("+--------------+-------------------+--------------+--------------+-------------+--------------------+--------------------+%n");

        devisList.forEach(this::printDevisRow);

        System.out.printf("+--------------+-------------------+--------------+--------------+-------------+--------------------+--------------------+%n");
    }

    private void printSingleDevis(Devis devis) {
        System.out.printf("+--------------+-------------------+--------------+--------------+-------------+--------------------+--------------------+%n");
        System.out.printf("| %-12s | %-17s | %-12s | %-12s | %-11s | %-18s | %-18s |%n",
                "Devis ID", "Estimated Amount", "Issue Date", "Validated Date", "Is Accepted", "Project Name", "Client Name");
        System.out.printf("+--------------+-------------------+--------------+--------------+-------------+--------------------+--------------------+%n");

        printDevisRow(devis);

        System.out.printf("+--------------+-------------------+--------------+--------------+-------------+--------------------+--------------------+%n");
    }

    private void printDevisRow(Devis devis) {
        System.out.printf("| %-12d | %-17.2f | %-12s | %-12s | %-11b | %-18s | %-18s |%n",
                devis.getId(),
                devis.getEstimatedAmount(),
                devis.getIssueDate(),
                devis.getValidatedDate() != null ? devis.getValidatedDate() : "N/A",
                devis.isAccepted(),
                devis.getProject().getProjectName() != null ? devis.getProject().getProjectName() : "N/A",
                devis.getProject().getClient().getName() != null ? devis.getProject().getClient().getName() : "N/A"
        );
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

    private LocalDate getValidDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateString = scanner.nextLine();
                return DateFormat.parseDate(dateString);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
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