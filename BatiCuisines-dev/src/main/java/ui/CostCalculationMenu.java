package main.java.ui;

import main.java.domain.entities.Devis;
import main.java.domain.entities.Material;
import main.java.domain.entities.Project;
import main.java.domain.entities.WorkForce;
import main.java.domain.enums.ProjectStatus;
import main.java.exception.DevisNotFoundException;
import main.java.exception.ProjectNotFoundException;
import main.java.repository.impl.ComponentRepository;
import main.java.repository.impl.ProjectRepository;
import main.java.service.DevisService;
import main.java.service.MaterialService;
import main.java.service.WorkForceService;
import main.java.utils.DateFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;

public class CostCalculationMenu {
    private static final Scanner scanner = new Scanner(System.in);
    private final ProjectRepository projectRepository;
    private final ComponentRepository componentRepository;
    private final MaterialService materialService;
    private final WorkForceService workForceService;
    private final DevisService devisService;
    private final DevisMenu devisMenu;
    private final double discount = 0.7;

    public CostCalculationMenu(ProjectRepository projectRepository, ComponentRepository componentRepository,
                               MaterialService materialService, WorkForceService workForceService,
                               DevisService devisService, DevisMenu devisMenu) {
        this.devisService = devisService;
        this.devisMenu = devisMenu;
        this.projectRepository = projectRepository;
        this.componentRepository = componentRepository;
        this.materialService = materialService;
        this.workForceService = workForceService;
    }

    private static boolean getYesNoInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.next().trim().toLowerCase();
            scanner.nextLine(); // Consume newline
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }

    public void save() {
        System.out.println("--- Total Cost Calculation ---");

        Project project = getValidProject();
        if (project == null) return;

        List<Material> materials = materialService.findAllByProjectId(project.getId());
        List<WorkForce> workforce = workForceService.findAllByProjectId(project.getId());

        double totalMaterialBeforeVat = calculateTotalMaterialCost(materials, false);
        double totalMaterialAfterVat = calculateTotalMaterialCost(materials, true);
        double totalWorkforceBeforeVat = calculateTotalWorkforceCost(workforce, false);
        double totalWorkforceAfterVat = calculateTotalWorkforceCost(workforce, true);

        double totalCostBeforeMargin = totalMaterialBeforeVat + totalWorkforceBeforeVat;
        double totalCostAfterVat = totalMaterialAfterVat + totalWorkforceAfterVat;
        double totalCost = totalCostAfterVat;

        double marginRate = applyProfitMargin(project, totalCost);

        if (project.getClient().isProfessional()) {
            totalCost = applyProfessionalDiscount(totalCost);
        }

        projectRepository.updateProjectFields(project.getId(), project.getProfitMargin(), totalCost);

        displayCostDetails(project, totalMaterialBeforeVat, totalMaterialAfterVat,
                totalWorkforceBeforeVat, totalWorkforceAfterVat,
                totalCostBeforeMargin, totalCost);

        Devis devis = createDevis(project, totalCost);
        handleDevisValidation(devis, project);
    }

    private Project getValidProject() {
        while (true) {
            try {
                System.out.print("Enter project ID: ");
                Long projectId = scanner.nextLong();
                scanner.nextLine(); // Consume newline
                return projectRepository.findById(projectId)
                        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            } catch (ProjectNotFoundException e) {
                System.out.println(e.getMessage());
                if (!getYesNoInput("Do you want to try another ID? (y/n): ")) {
                    return null;
                }
            }
        }
    }

    private double calculateTotalMaterialCost(List<Material> materials, boolean afterVat) {
        return materials.stream()
                .mapToDouble(material -> afterVat ?
                        materialService.calculateMaterialAfterVatRate(material) :
                        materialService.calculateMaterialBeforeVatRate(material))
                .sum();
    }

    private double calculateTotalWorkforceCost(List<WorkForce> workforce, boolean afterVat) {
        return workforce.stream()
                .mapToDouble(workForce -> afterVat ?
                        workForceService.calculateWorkforceAfterVat(workForce) :
                        workForceService.calculateWorkforceBeforeVat(workForce))
                .sum();
    }

    private double applyProfitMargin(Project project, double totalCost) {
        if (getYesNoInput("Do you want to apply a profit margin to the project? (y/n): ")) {
            double marginRate = getValidDoubleInput("Enter profit margin percentage: ", 0, 100);
            project.setProfitMargin(marginRate);
            double profitMargin = totalCost * marginRate / 100;
            totalCost += profitMargin;
            return marginRate;
        }
        return 0.0;
    }

    private double applyProfessionalDiscount(double totalCost) {
        System.out.println("\n--- Professional Client Discount Applied ---");
        double discountedCost = totalCost * discount;
        System.out.println("Discounted Total Cost: " + String.format("%.2f", discountedCost) + " €");
        return discountedCost;
    }

    private void displayCostDetails(Project project, double totalMaterialBeforeVat, double totalMaterialAfterVat,
                                    double totalWorkforceBeforeVat, double totalWorkforceAfterVat,
                                    double totalCostBeforeMargin, double totalCost) {
        System.out.println("\n--- Calculation Result ---");
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Client: " + project.getClient().getName());
        System.out.println("Address: " + project.getClient().getAddress());
        System.out.println("Area: " + project.getSurface() + " m²");
        System.out.println("--- Cost Details ---");
        System.out.println("Materials Cost Before VAT: " + String.format("%.2f", totalMaterialBeforeVat) + " €");
        System.out.println("Materials Cost After VAT: " + String.format("%.2f", totalMaterialAfterVat) + " €");
        System.out.println("Workforce Cost Before VAT: " + String.format("%.2f", totalWorkforceBeforeVat) + " €");
        System.out.println("Workforce Cost After VAT: " + String.format("%.2f", totalWorkforceAfterVat) + " €");
        System.out.println("Total Cost Before Margin: " + String.format("%.2f", totalCostBeforeMargin) + " €");
        System.out.println("Final Total Cost: " + String.format("%.2f", totalCost) + " €");
    }

    private Devis createDevis(Project project, double totalCost) {
        LocalDate issueDate = getValidDate("Enter issue date (yyyy-MM-dd): ");
        LocalDate validatedDate = getValidDate("Enter validated date (yyyy-MM-dd): ");
        Devis devis = new Devis(0L, totalCost, issueDate, validatedDate, false, project);
        return devisService.save(devis);
    }

    private void handleDevisValidation(Devis devis, Project project) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(devis.getValidatedDate())) {
            System.out.println("The Devis is pending validation until " + devis.getValidatedDate() + ".");
            System.out.println("You can accept or reject it before this date.");
            return;
        }

        if (getYesNoInput("Do you want to accept the Devis? (y/n): ")) {
            devisService.updateDevisStatus(devis.getId());
            projectRepository.updateProjectStatus(project.getId(), ProjectStatus.FINISHED.name());
            System.out.println("Devis accepted. Project marked as FINISHED.");
        } else {
            devisService.cancelDevisAndProjectIfNotAccepted(devis.getId(), devis.getValidatedDate());
            projectRepository.updateProjectStatus(project.getId(), ProjectStatus.CANCELLED.name());
            System.out.println("Devis rejected. Project marked as CANCELLED.");
        }
    }

    private double getValidDoubleInput(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                double input = scanner.nextDouble();
                scanner.nextLine();
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Please enter a value between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine();
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
}