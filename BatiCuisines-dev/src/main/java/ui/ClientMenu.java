package main.java.ui;

import main.java.domain.entities.Client;
import main.java.exception.ClientNotFoundException;
import main.java.service.ClientService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.InputMismatchException;

public class ClientMenu {
    private final ClientService clientService;
    private static Scanner scanner;

    public ClientMenu(ClientService clientService) {
        this.clientService = clientService;
        scanner = new Scanner(System.in);
    }

    public void clientMenu() {
        int choice;

        do {
            displayMenu();
            choice = getValidIntInput("Enter your choice: ", 1, 6);

            try {
                switch (choice) {
                    case 1:
                        addNewClient();
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
                        System.out.println("Exiting client menu...");
                        break;
                }
            } catch (ClientNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        } while (choice != 6);
    }

    private void displayMenu() {
        System.out.println("\n--- Client Management Menu ---");
        System.out.println("1. Save new client");
        System.out.println("2. Find all clients");
        System.out.println("3. Find client by ID");
        System.out.println("4. Update client");
        System.out.println("5. Delete client");
        System.out.println("6. Exit");
    }

    private int getValidIntInput(String prompt, int min, int max) {
        int input;
        do {
            System.out.print(prompt);
            try {
                input = scanner.nextInt();
                if (input < min || input > max) {
                    System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
                input = min - 1; // Set input to an invalid value to continue the loop
            }
        } while (true);
    }

    public Client searchByName(String name) {
        Optional<Client> optionalClient = this.clientService.findByName(name);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            System.out.println("Client found!");
            System.out.println("Name: " + client.getName());
            System.out.println("Address: " + client.getAddress());
            System.out.println("Phone number: " + client.getPhone());

            while (true) {
                System.out.print("Would you like to continue with this client? (y/n): ");
                String choiceToContinue = scanner.nextLine().trim().toLowerCase();
                if (choiceToContinue.equals("y")) {
                    return client;
                } else if (choiceToContinue.equals("n")) {
                    addNewClient();
                } else {
                    System.out.println("Invalid choice. Please enter 'y' or 'n'.");
                }
            }
        } else {
            throw new ClientNotFoundException("Client not found");
        }
    }

    public Client addNewClient() {
        System.out.println("\n--- Add a new client ---");
        scanner.nextLine();
        String name = getValidStringInput("Enter the name for a client: ");
        String address = getValidStringInput("Enter the address for a client: ");
        String phoneNumber = getValidStringInput("Enter the phone number for a client: ");
        boolean status = getValidBooleanInput("Is the client professional? (true/false): ");

        Client client = new Client(0L, name, address, phoneNumber, status);
        Client savedClient = clientService.save(client);
        System.out.println("Client added successfully!");
        return savedClient;
    }

    public void findById() {
        System.out.println("\n--- Find client by id ---");
        Long id = getValidLongInput("Enter id: ");

        try {
            Client client = clientService.findById(id)
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + id));
            displayClientInfo(client);
        } catch (ClientNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void update() {
        System.out.println("\n--- Update client ---");
        Long id = getValidLongInput("Enter the Id: ");

        try {
            Client clientToUpdate = clientService.findById(id)
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + id));

            scanner.nextLine(); // Consume newline
            String name = getValidStringInput("Enter new name: ");
            String address = getValidStringInput("Enter new address: ");
            String phoneNumber = getValidStringInput("Enter new phone number: ");
            boolean status = getValidBooleanInput("Is the client professional? (true/false): ");

            Client updatedClient = new Client(id, name, address, phoneNumber, status);
            clientService.update(updatedClient);
            System.out.println("Client updated successfully!");
        } catch (ClientNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void delete() {
        System.out.println("\n--- Delete client ---");
        Long id = getValidLongInput("Enter id: ");

        try {
            Client clientToDelete = clientService.findById(id)
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + id));
            displayClientInfo(clientToDelete);

            if (getValidBooleanInput("Are you sure you want to delete this client? (true/false): ")) {
                boolean deleted = clientService.delete(id);
                if (deleted) {
                    System.out.println("Client deleted successfully!");
                } else {
                    System.out.println("Failed to delete client.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (ClientNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void findAll() {
        List<Client> clientList = clientService.findAll();

        if (clientList.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }

        displayClientTable(clientList);
    }

    private void displayClientInfo(Client client) {
        System.out.println("Name: " + client.getName());
        System.out.println("Address: " + client.getAddress());
        System.out.println("Phone number: " + client.getPhone());
        System.out.println("Professional: " + (client.isProfessional() ? "Yes" : "No"));
    }

    private void displayClientTable(List<Client> clientList) {
        System.out.printf("+--------------------+-----------------------------+----------------+--------------+%n");
        System.out.printf("| %-18s | %-27s | %-14s | %-12s |%n",
                "Name", "Address", "Phone", "Professional");
        System.out.printf("+--------------------+-----------------------------+----------------+--------------+%n");

        for (Client client : clientList) {
            System.out.printf("| %-18s | %-27s | %-14s | %-12s |%n",
                    client.getName(),
                    client.getAddress(),
                    client.getPhone(),
                    client.isProfessional() ? "Yes" : "No");
        }
        System.out.printf("+--------------------+-----------------------------+----------------+--------------+%n");
    }

    private String getValidStringInput(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            } else {
                return input;
            }
        } while (true);
    }

    private boolean getValidBooleanInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("true") || input.equals("false")) {
                return Boolean.parseBoolean(input);
            } else {
                System.out.println("Invalid input. Please enter 'true' or 'false'.");
            }
        }
    }

    private Long getValidLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return scanner.nextLong();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }
}