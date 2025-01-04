package com.database_manager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.net.*;

import org.bytedeco.javacv.*;
import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PaymentSystem {
    private static final String FOLDER = "output/";
    private static final String FILE_PATH = FOLDER + "cart.csv";
    private static final String DEBIT_PAYMENT_FILE_PATH = FOLDER + "debit_payment.csv";
    private static final String CREDIT_PAYMENT_FILE_PATH = FOLDER + "credit_payment.csv";
    private static final String ORDER_ID_FILE_PATH = FOLDER + "orderID.csv";
    private static final String ORDER_DETAIL_ID_FILE_PATH = FOLDER + "order_DetailID.csv";

    private static final Scanner scanner = new Scanner(System.in);
    private static int nextId = getNextIdFromFile();

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String YELLOW = "\u001B[33m";

    static {
        // Ensure the output directory exists
        File directory = new File(FOLDER);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    // Helper method to check for cancel command
    public static boolean isCancelCommand(String input) {
        return input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("esc") || input.equalsIgnoreCase("exit");
    }

    private static boolean serverRunning = false;

    public static void main(String[] args) {
        while (true) {
            System.out.println(BOLD + "\n--- Item Management System ---" + RESET);
            System.out.println(GREEN + "1. Add Item" + RESET);
            System.out.println(YELLOW + "2. View All Items (Counter check)" + RESET);
            System.out.println(CYAN + "3. View Items by ProductID" + RESET);
            System.out.println(BLUE + "4. Update Items" + RESET);
            System.out.println(PURPLE + "5. Delete Items" + RESET);
            System.out.println(BOLD + "\n----- Payment System -----" + RESET);
            System.out.println(GREEN + "6. Make Payment (Debit or Credit)" + RESET);
            System.out.println(BOLD + "\n----- Table Management System -----" + RESET);
            System.out.println(YELLOW + "7. Create Table" + RESET);
            System.out.println(CYAN + "8. Edit Table" + RESET);
            System.out.println(BLUE + "9. Create ViewTable" + RESET);
            System.out.println(PURPLE + "10. Drop Table" + RESET);
            System.out.println(GREEN + "11. Delete Table Record" + RESET);
            System.out.println(YELLOW + "12. View Table Data" + RESET);
            System.out.println(CYAN + "13. Start Local Server" + RESET);
            System.out.println(BLUE+ "14. End Local Server" + RESET);
            System.out.println(RED + "15. Exit" + RESET);
            System.out.print("\n" + BOLD + "Choose an option: " + RESET);

            int choice = -1; // Default invalid value
            while (choice == -1) {
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println(RED + BOLD + "Invalid input. Please enter a number between 1 and 11." + RESET);
                    scanner.nextLine();  // Clear invalid input
                    System.out.print("\n" + BOLD + "Choose an option: " + RESET);
                }
            }

            switch (choice) {
                case 1:
                    createItem();
                    break;
                case 2:
                    viewAllItems();
                    break;
                case 3:
                    viewItemById();
                    break;
                case 4:
                    updateItem();
                    break;
                case 5:
                    deleteItems();
                    break;
                case 6:
                    processPayment();
                    break;
                case 7:
                    createTable();
                    break;
                case 8:
                    editTable();
                    break;
                case 9:
                    createViewTable();
                    break;
                case 10:
                    deleteTable();
                    break;
                case 11:
                    deleteRecords();
                    break;
                case 12:
                    viewTableData();
                    break;
                case 13:
                    LocalServer.maybeStartServer(FOLDER);
                    serverRunning = true;
                    break;
                case 14:
                    if (serverRunning) {
                        System.out.print(BOLD + YELLOW + "Do you want to stop the server before exiting? (y/n): " + RESET);
                        String stopServer = scanner.nextLine().trim().toLowerCase();
                        if (stopServer.equalsIgnoreCase("y") || stopServer.equalsIgnoreCase("yes")) {
                            LocalServer.stopServer();
                            serverRunning = false;
                        }
                    }
                    System.out.println("\n" + RED + BOLD + "Ending the server..." + RESET);
                    break;
                case 15:
                    System.out.println("\n" + RED + BOLD + "Exiting..." + RESET);
                    return;
                default:
                    System.out.println("\n" + RED + BOLD + "Invalid choice. Please choose a valid option from 1-11." + RESET);
                    break;
            }
        }
    }

    private static void viewTableData() {
        // List available files in the folder
        List<String> fileNames = listFilesInOutputFolder();
        if (fileNames.isEmpty()) {
            System.out.println(RED + BOLD + "No files found in the output folder." + RESET);
            return;
        }
        System.out.print(YELLOW + BOLD + "\nEnter the table index or name to view: " + RESET);
        String input = scanner.nextLine().trim();
        if (isCancelCommand(input)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
    
        String tableName;
        if (input.matches("\\d+")) { // If the input is numeric
            int index = Integer.parseInt(input) - 1; // Convert to zero-based index
            if (index < 0 || index >= fileNames.size()) {
                System.out.println(RED + "Invalid index. Please select a valid table index." + RESET);
                return;
            }
            tableName = fileNames.get(index);
        } else {
            tableName = input + ".csv"; // Treat input as a file name
        }
    
        File file = new File(FOLDER + tableName);
        if (!file.exists()) {
            System.out.println(RED + "Table (CSV file) '" + tableName + "' does not exist. Please check the name and try again." + RESET);
            return;
        }
    
        List<List<String>> tableData = readTableData(file.getPath());
        if (tableData.isEmpty()) {
            System.out.println(RED + BOLD + "The table is empty." + RESET);
            return;
        }
    
        // Display table headers in color
        System.out.println(BOLD + "\n--- Table Data ---" + RESET);
        List<String> headers = tableData.get(0);

        // Display each row with the corresponding header name
        for (int i = 1; i < tableData.size(); i++) { 
            List<String> row = tableData.get(i);

            // Loop through each header and data pair using switch-case
            for (int j = 0; j < headers.size(); j++) {
                String data = row.get(j);

                // Color code based on the column index
                switch (j % 6) {
                    case 0: // (First column)
                        System.out.print(BOLD + YELLOW + headers.get(j) + ": " + RESET + YELLOW + data + ", " + RESET);
                        break;
                    case 1: //(Second column)
                        System.out.print(BOLD + GREEN + headers.get(j) + ": " + RESET + GREEN + data + ", " + RESET);
                        break;
                    case 2: // (Third column)
                        System.out.print(BOLD + CYAN + headers.get(j) + ": " + RESET + CYAN + data + ", " + RESET);
                        break;
                    case 3: // (Fourth column)
                        System.out.print(BOLD + BLUE + headers.get(j) + ": " + RESET + BLUE + data + ", " + RESET);
                        break;
                    case 4: // (Fifth column)
                        System.out.print(BOLD + PURPLE + headers.get(j) + ": " + RESET + PURPLE + data + ", " + RESET);
                        break;
                    case 5: // (Sixth column)
                        System.out.print(BOLD + RED + headers.get(j) + ": " + RESET + RED + data + ", " + RESET);
                        break;
                        // loops from 1st column for 6th column
                    default:
                        System.out.print(headers.get(j) + ": " + data + ", ");
                        break;
                }
            }
            System.out.println();  // Move to the next line after a row
        }
    }
    
    private static List<List<String>> readTableData(String filePath) {
        List<List<String>> tableData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
    
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                List<String> row = new ArrayList<>(Arrays.asList(parts));
                if (isFirstLine) {
                    isFirstLine = false; // Skip the header
                }
                tableData.add(row);
            }
        } catch (IOException e) {
            System.out.println("\n" + RED + BOLD + "An error occurred while reading the file." + RESET);
        }
        return tableData;
    }

    private static void createTable() {
        System.out.print(CYAN + BOLD + "Enter the name of the table (CSV file) to create: " + RESET);
        String tableName = scanner.nextLine().trim();
        if (isCancelCommand(tableName)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
        tableName += ".csv";
        
        System.out.println(YELLOW + BOLD + "Enter the column headers, separated by commas (e.g., Name,Age,Address): " + RESET);
        String headers = scanner.nextLine().trim();
        if (isCancelCommand(headers)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
    
        File file = new File(FOLDER + tableName);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(headers);
            writer.newLine();
            System.out.println(GREEN + "Table (CSV file) '" + tableName + "' created successfully." + RESET);
            System.out.println(GREEN + BOLD + "\nHeaders: " + headers + RESET);
    
            // Optionally, add data immediately
            while (true) {
                System.out.print(YELLOW + BOLD + "Do you want to add data to the table now? (yes/no): " + RESET);
                String response = scanner.nextLine().trim().toLowerCase();
                if (isCancelCommand(response)) {
                    System.out.println(RED + BOLD + "Operation cancelled." + RESET);
                    return;
                }
                if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                    System.out.println(CYAN + "Enter data for the columns (aligned with headers), separated by commas: " + RESET);
                    String data = scanner.nextLine().trim();
                    if (isCancelCommand(data)) {
                        System.out.println(RED + BOLD + "Operation cancelled." + RESET);
                        return;
                    }
    
                    // Replace blank data with "NULL"
                    String[] fields = data.split(",");
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().isEmpty()) {
                            fields[i] = "NULL";
                        }
                    }
                    String formattedData = String.join(",", fields);
                    writer.write(formattedData);
                    writer.newLine();
                    System.out.println(GREEN + "Data added to the table." + RESET);
                } else if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                    System.out.println(GREEN + BOLD + "You can add data later by editing the table." + RESET);
                    break;
                } else {
                    System.out.println(RED + "Invalid input. Please enter 'yes' or 'no'." + RESET);
                }
            }
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error creating table: " + e.getMessage() + RESET);
        }
    }
    
    private static void editTable() {
        List<String> fileNames = listFilesInOutputFolder();
        System.out.print(YELLOW + BOLD + "\nEnter the table index or name to edit: " + RESET);
        String input = scanner.nextLine().trim();
        if (isCancelCommand(input)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
    
        String tableName;
        if (input.matches("\\d+")) { // If the input is numeric
            int index = Integer.parseInt(input) - 1; // Convert to zero-based index
            if (index < 0 || index >= fileNames.size()) {
                System.out.println(RED + "Invalid index. Please select a valid table index." + RESET);
                return;
            }
            tableName = fileNames.get(index);
        } else {
            tableName = input + ".csv"; // Treat input as a file name
        }
    
        File file = new File(FOLDER + tableName);
        if (!file.exists()) {
            System.out.println(RED + "Table (CSV file) '" + tableName + "' does not exist. Please check the name and try again." + RESET);
            return;
        }
    
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) {
                System.out.println(RED + "Table is empty. Cannot edit." + RESET);
                return;
            }
    
            String headers = lines.get(0); // First line contains the headers
            System.out.println(GREEN + BOLD + "Current headers: " + headers + RESET);
    
            System.out.print(YELLOW + BOLD + "Do you want to modify the headers? (yes/no): " + RESET);
            String modifyHeaders = scanner.nextLine().trim().toLowerCase();
            if (isCancelCommand(modifyHeaders)) {
                System.out.println(RED + BOLD + "Operation cancelled." + RESET);
                return;
            }
            if (modifyHeaders.equalsIgnoreCase("yes") || modifyHeaders.equalsIgnoreCase("y")) {
                System.out.print(CYAN + BOLD + "\nEnter the new headers, separated by commas: " + RESET);
                String newHeaders = scanner.nextLine();
                if (isCancelCommand(newHeaders)) {
                    System.out.println(RED + BOLD + "Operation cancelled." + RESET);
                    return;
                }
                lines.set(0, newHeaders);
                System.out.println(GREEN + "Headers updated successfully." + RESET);
            }
    
            // Display data for editing
            System.out.println(GREEN + BOLD + "\nCurrent data in the table:" + RESET);
            for (int i = 1; i < lines.size(); i++) {
                System.out.println("Row " + i + ": " + lines.get(i));
            }
    
            System.out.print(YELLOW + BOLD + "Do you want to edit or add data? (yes/no): " + RESET);
            String editData = scanner.nextLine().trim().toLowerCase();
            if (isCancelCommand(editData)) {
                System.out.println(RED + BOLD + "Operation cancelled." + RESET);
                return;
            }
            if (editData.equalsIgnoreCase("yes") || editData.equalsIgnoreCase("y")) {
                System.out.println(CYAN + "Enter the row number to edit or add (e.g., 1 for the first row, " + (lines.size()) + " to add a new row): " + RESET);
                int rowNumber = Integer.parseInt(scanner.nextLine().trim());
    
                // Validate the row number (1-based index)
                if (rowNumber < 1 || rowNumber > lines.size()) {
                    System.out.println(RED + "Invalid row number. Please enter a number between 1 and " + (lines.size()) + "." + RESET);
                    return;
                }
                System.out.println(GREEN + BOLD + "\nHeaders: " + headers + RESET);
                
                System.out.println(CYAN + "Enter the new data for Row " + rowNumber + ", aligned with headers: " + RESET);
                String newData = scanner.nextLine().trim();
                if (isCancelCommand(newData)) {
                    System.out.println(RED + BOLD + "Operation cancelled." + RESET);
                    return;
                }
    
                // Replace blank data with "NULL"
                String[] fields = newData.split(",");
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].trim().isEmpty()) {
                        fields[i] = "NULL";
                    }
                }
                String formattedData = String.join(",", fields);
    
                // If row number is larger than the current rows, add a new row
                if (rowNumber == lines.size()) {
                    // Add new row at the end
                    lines.add(formattedData);
                    System.out.println(GREEN + "New Row " + rowNumber + " added successfully." + RESET);
                } else if (rowNumber <= lines.size()) {
                    // Edit existing row
                    lines.set(rowNumber, formattedData);
                    System.out.println(GREEN + "Row " + rowNumber + " updated successfully." + RESET);
                } else {
                    System.out.println(RED + "Invalid row number." + RESET);
                }
            }
    
            // Save changes back to the file
            Files.write(file.toPath(), lines);
            System.out.println(GREEN + "Changes saved to '" + tableName + "'." + RESET);
    
        } catch (IOException | NumberFormatException e) {
            System.out.println(RED + BOLD + "Error editing table: " + e.getMessage() + RESET);
        }
    }
    
    // Method to process payment
    private static void processPayment() {
        // Select multiple items using the helper method
        List<String> selectedItemsStrings = selectMultipleItemsByIndexOrName();

        if (selectedItemsStrings.isEmpty()) {
            System.out.println(RED + BOLD + "No items selected." + RESET);
            return;
        }

        List<Item> allItems = readAllItems();
        List<Item> selectedItems = new ArrayList<>();

        for (String selectedItem : selectedItemsStrings) {
            addItemToSelection(allItems, selectedItem, selectedItems);
        }

        displaySelectedItems(selectedItems);

        // Process payment for each selected item
        for (Item item : selectedItems) {
            PaymentDetails paymentDetails = handlePaymentForItem(item); // Get PaymentDetails object
            if (!paymentDetails.getOrderID().isEmpty()) {
                markItemAsPaid(item, paymentDetails); // Pass entire PaymentDetails
            }
        }

        updateItemIdsAfterPayment();
    }
    
    private static void addItemToSelection(List<Item> allItems, String selectedItem, List<Item> selectedItems) {
        try {
            int index = Integer.parseInt(selectedItem.trim()) - 1;
            if (index >= 0 && index < allItems.size()) {
                selectedItems.add(allItems.get(index));
            } else {
                System.out.println(RED + "Invalid index: " + selectedItem + RESET);
            }
        } catch (NumberFormatException e) {
            // If it's not an index, try to find it by name
            Item item = allItems.stream()
                                .filter(i -> i.getName().equalsIgnoreCase(selectedItem.trim()))
                                .findFirst()
                                .orElse(null);
            if (item != null) {
                selectedItems.add(item);
            } else {
                System.out.println(RED + "Item not found: " + selectedItem + RESET);
            }
        }
    }
    
    private static void displaySelectedItems(List<Item> selectedItems) {
        System.out.println("\n" + CYAN + "You selected the following items: " + RESET);
        for (Item item : selectedItems) {
            System.out.println(BOLD + YELLOW + "ProductID: " + item.getId() + RESET + " | " + item.getName());
        }
    }
    
    private static PaymentDetails handlePaymentForItem(Item item) {
        String orderID = "";
        String customerName = "";
        String customerContact = "";
    
        System.out.print("\n" + BOLD + "Select payment method for " + item.getName() + " (1 for Debit, 2 for Credit): " + RESET);
        int paymentOption = scanner.nextInt();
        scanner.nextLine();  // consume the newline
    
        System.out.print("Select payment type (1 for Scan to Pay, 2 for Manual Payment): ");
        int paymentType = scanner.nextInt();
        scanner.nextLine();  // consume the newline
    
        if (paymentType == 1) {
            // QR Code scan to extract Name and Contact
            String[] customerInfo = QRCodeScanner.scanQRCode();
            if (customerInfo != null) {
                customerName = customerInfo[0];
                customerContact = customerInfo[1];
            }
        }
    
        // If QR code data is not available, prompt for manual input
        if (customerName.isEmpty() || customerContact.isEmpty()) {
            System.out.print("Enter customer name: ");
            customerName = scanner.nextLine();
            System.out.print("Enter customer contact: ");
            customerContact = scanner.nextLine();
        }
    
        switch (paymentOption) {
            case 1:
                orderID = processDebitPayment(item, paymentType, customerName, customerContact);
                break;
            case 2:
                orderID = processCreditPayment(item, paymentType, customerName, customerContact);
                break;
            default:
                System.out.println(RED + BOLD + "Invalid payment option." + RESET);
                break;
        }
    
        return new PaymentDetails(orderID, customerName, customerContact, paymentOption);
    }
    
    private static String processDebitPayment(Item item, int paymentType, String customerName, String customerContact) {
        String paymentMethod = "Debit Card";
        String orderID = generateOrderID();
    
        if (paymentType == 1) {
            // QR Code scan to extract Name and Contact
            String[] customerInfo = QRCodeScanner.scanQRCode();
    
            if (customerInfo != null && customerInfo.length == 2) {
                // Extract customer name and contact from the QR code
                customerName = customerInfo[0];
                customerContact = customerInfo[1];
                orderID = generateOrderID();  // Generate order ID based on QR Code data
                System.out.println("QR Code Scanned: OrderID " + orderID);
            } else {
                System.out.println(RED + "Error: QR Code did not return valid customer information." + RESET);
                return "";  // Return empty string to indicate failure
            }
        }
    
        // Save payment details along with customer information
        savePaymentDetails(paymentMethod, item, orderID, customerName, customerContact);
        System.out.println(GREEN + "Payment processed via Debit Card. Item saved to debit payment file." + RESET);
    
        return orderID;
    }
    
    private static String processCreditPayment(Item item, int paymentType, String customerName, String customerContact) {
        String paymentMethod = "Credit Card";
        String orderID = generateOrderID();
    
        if (paymentType == 1) {
            // QR Code scan to extract Name and Contact
            String[] customerInfo = QRCodeScanner.scanQRCode();
    
            if (customerInfo != null && customerInfo.length == 2) {
                // Extract customer name and contact from the QR code
                customerName = customerInfo[0];
                customerContact = customerInfo[1];
                orderID = generateOrderID();  // Generate order ID based on QR Code data
                System.out.println("QR Code Scanned: OrderID " + orderID);
            } else {
                System.out.println(RED + "Error: QR Code did not return valid customer information." + RESET);
                return "";  // Return empty string to indicate failure
            }
        }
    
        // Save payment details along with customer information
        savePaymentDetails(paymentMethod, item, orderID, customerName, customerContact);
        System.out.println(GREEN + "Payment processed via Credit Card. Item saved to credit payment file." + RESET);
    
        return orderID;
    }
    
    private static void markItemAsPaid(Item item, PaymentDetails paymentDetails) {
        List<Item> allItems = readAllItems();
        List<Item> updatedItems = new ArrayList<>();
        List<String> headers = readHeadersFromFile(FILE_PATH);
    
        for (Item i : allItems) {
            if (i.getId() == item.getId()) {
                i.setIsPaid(true);  // Set the IsPaid field to True for the selected item
                saveOrderDetails(paymentDetails.getOrderID(), i, paymentDetails.getCustomerName(), paymentDetails.getCustomerContact(), paymentDetails.getpaymentOption());
            }
            updatedItems.add(i);
        }
    
        // Rewrite the cart.csv file with the updated list
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.join(",", headers));
            writer.newLine();
    
            for (Item i : updatedItems) {
                writer.write(i.toRawString());
                writer.newLine();
            }
            System.out.println(GREEN + "Item " + item.getName() + " has been marked as paid." + RESET);
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error marking item as paid." + RESET);
        }
    }
    
    private static void saveOrderDetails(String orderID, Item item, String customerName, String customerContact, int paymentOption) {
        String orderDate = generateCurrentTimestamp();
        String orderDetailID = generateOrderDetailID(item.getId());
        
        File file = new File(ORDER_ID_FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_ID_FILE_PATH, true))) {
            // Write header if file is empty
            if (file.length() == 0) {
                writer.write("OrderID,CustomerName,CustomerContact,OrderDate,OrderDetailID");
                writer.newLine();  // Add header line
            }
            // Write order details to file
            writer.write(orderID + "," + customerName + "," + customerContact + "," + orderDate + "," + orderDetailID);
            writer.newLine();
        } catch (IOException e) {
            System.out.println(RED + "Error saving order details." + RESET);
        }
        
        // Call saveOrderDetail method, passing paymentMethod for the payment ID generation
        saveOrderDetail(item, orderDetailID, paymentOption);
    }
    
    private static void saveOrderDetail(Item item, String orderDetailID, int paymentOption) {
        // Generate the payment ID based on the payment method
        String paymentID = generatePaymentID(paymentOption == 1 ? "Debit Card" : "Credit Card");
        
        File file = new File(ORDER_DETAIL_ID_FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_DETAIL_ID_FILE_PATH, true))) {
            // If the file is empty, write the header
            if (file.length() == 0) {
                writer.write("OrderDetailID,ProductID,PaymentID");
                writer.newLine();  // Add header line
            }
            // Write the order details to the file
            writer.write(orderDetailID + "," + item.getId() + "," + paymentID);
            writer.newLine();
        } catch (IOException e) {
            System.out.println(RED + "Error saving order detail." + RESET);
        }
    }
    
    private static void savePaymentDetails(String paymentMethod, Item item, String orderID, String customerName, String customerContact) {
        String filePath = paymentMethod.equals("Debit Card") ? DEBIT_PAYMENT_FILE_PATH : CREDIT_PAYMENT_FILE_PATH;
        String orderDetailID = generateOrderDetailID(item.getId());
    
        String record = paymentMethod + "," + orderID + "," + orderDetailID;
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            File file = new File(filePath);
            if (file.length() == 0) {
                writer.write("PaymentMethod,OrderID,OrderDetailID");
                writer.newLine();
            }
    
            writer.write(record);
            writer.newLine();
            System.out.println(GREEN + "Payment processed via " + paymentMethod + ". Payment details saved." + RESET);
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error saving payment details." + RESET);
        }
    }
    
    private static String generateOrderID() {
        return "ORD-" + generateCurrentTimestamp();
    }
    
    private static String generateOrderDetailID(int productID) {
        return "OD-" + productID;
    }
    
    private static String generatePaymentID(String paymentMethod) {
        // Determine the correct file path based on payment method
        String filePath = paymentMethod.equals("Debit Card") ? DEBIT_PAYMENT_FILE_PATH : CREDIT_PAYMENT_FILE_PATH;
        int currentPaymentID = 0;
    
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
    
            // If the file contains records, process the last record to get the last PaymentID
            if (lines.size() > 1) { // Skip the header row (if it exists)
                String lastLine = lines.get(lines.size() - 1); // Get the last line
                String[] columns = lastLine.split(",");
                if (columns.length > 1) {
                    String lastPaymentID = columns[1]; // The second column contains the PaymentID
                    String[] paymentIDParts = lastPaymentID.split("-");
                    if (paymentIDParts.length > 1) {
                        currentPaymentID = Integer.parseInt(paymentIDParts[1]); // Get the numeric part
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "Error reading payment records to generate PaymentID." + RESET);
        } catch (NumberFormatException e) {
            System.out.println(RED + "Error parsing PaymentID." + RESET);
        }
    
        // Increment the current payment ID and generate the next one
        currentPaymentID++;
        
        if (paymentMethod.equals("Debit Card")) {
            return "DEB-" + currentPaymentID;
        } else if (paymentMethod.equals("Credit Card")) {
            return "CRED-" + currentPaymentID;
        }
    
        return "UNKNOWN";
    }
    
    // Method to update item IDs after all payments
    private static void updateItemIdsAfterPayment() {
        List<Item> allItems = readAllItems();  // Read all items from cart.csv
        List<Item> updatedItems = new ArrayList<>();
        List<String> headers = readHeadersFromFile(FILE_PATH);
    
        updatedItems.addAll(allItems);
        for (int i = 0; i < updatedItems.size(); i++) {
            updatedItems.get(i).setId(i + 1); 
        }
    
        // Rewrite the cart.csv file with the updated list of items
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            // Write the header first (join headers with commas)
            writer.write(String.join(",", headers));
            writer.newLine();
    
            for (Item i : updatedItems) {
                writer.write(i.toRawString());
                writer.newLine();
            }
            System.out.println(GREEN + "Item IDs updated!" + RESET);
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error updating item IDs after payment." + RESET);
        }
    }

/*
    // Method to generate unique PaymentID based on payment method
    private static String generatePaymentID(String paymentMethod) {
        // Read the current file to count the number of records and determine the last PaymentID
        String filePath = paymentMethod.equals("Debit Card") ? DEBIT_PAYMENT_FILE_PATH : CREDIT_PAYMENT_FILE_PATH;
        int currentPaymentID = 0;

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // If the file has data, we parse the last PaymentID
            if (lines.size() > 1) { // Skip the header row
                String lastLine = lines.get(lines.size() - 1);
                String[] columns = lastLine.split(",");
                String lastPaymentID = columns[1];
                currentPaymentID = Integer.parseInt(lastPaymentID.split("-")[1]);
            }
        } catch (IOException e) {
            System.out.println(RED + "Error reading payment records to generate PaymentID." + RESET);
        }

        // Generate the new PaymentID
        currentPaymentID++;
        if (paymentMethod.equals("Debit Card")) {
            return "DEB-" + currentPaymentID;
        } else if (paymentMethod.equals("Credit Card")) {
            return "CRED-" + currentPaymentID;
        }
        return "UNKNOWN";
    }
         */

    private static void createItem() {
        // Name Input & Cancel Check
        System.out.print(BOLD + GREEN + "Enter item name: " + RESET);
        String name = scanner.nextLine();
        if (isCancelCommand(name)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return; 
        }
        // price Input & Cancel Check
        System.out.print(BOLD + YELLOW + "Enter price: " + RESET);
        Object price = getValidprice();
        if (price instanceof String) { 
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
        // Handle multiplier in the name (e.g., "apple x5")
        String itemNo = "1";
        int multiplier = 1;
        if (name.contains(" x")) {
            String[] nameParts = name.split(" x");
            name = nameParts[0];
            itemNo = nameParts[1];
            try {
                multiplier = Integer.parseInt(nameParts[1]);
            } catch (NumberFormatException e) {
                System.out.println(RED + BOLD + "Invalid multiplier format. Please enter in the form 'name xN'." + RESET);
                return;
            }
        }

        // Apply multiplier to price
        Object finalPrice = 1;
        if (price instanceof Integer) {
            finalPrice = new BigDecimal((int) price * multiplier).setScale(2, RoundingMode.HALF_UP);
        } else if (price instanceof Double) {
            finalPrice = new BigDecimal((double) price * multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        // Item_type Input & Cancel Check
        System.out.print(BOLD + GREEN + "Enter Item type: " + RESET);
        String Item_type = scanner.nextLine();
        if (isCancelCommand(Item_type)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
        // category Input & Cancel Check
        System.out.print(BOLD + CYAN + "Enter category type: " + RESET);
        String category = scanner.nextLine();
        if (isCancelCommand(category)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }

        // Choose supplier
        Supplier chosenSupplier = Supplier.chooseSupplier();
        if (chosenSupplier == null) {
            System.out.println(RED + "No supplier chosen. Operation cancelled." + RESET);
            return;
        }
        boolean isPaid = false;
        // Creating the Item object
        int id = nextId++;
        Item item = new Item(id, name, finalPrice, Item_type, category, itemNo, chosenSupplier.supplierId,isPaid);
        // Append Item to the file
        appendItemToFile(item);
        System.out.println("\n" + GREEN + "Created: " + item + RESET);
    }
    
    private static String generateCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        
        // Format the timestamp to a readable string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private static void viewAllItems() {
        System.out.println(BOLD + "--- List of Items ---" + RESET);
        List<Item> Items = readAllItems();
        if (Items.isEmpty()) {
            System.out.println("\n" + RED + BOLD + "No Items found." + RESET);
        } else {
            for (Item Item : Items) {
                System.out.println(Item);
            }
        }
    }

    private static void viewItemById() {
        List<Item> Items = readAllItems();
        if (Items.isEmpty()) {
            System.out.println("\n" + RED + BOLD + "No Items found." + RESET);
            return;
        }

        // Display available Items with their IDs
        System.out.println("\n"+BOLD + "--- Available Items ---" + RESET);
        for (Item Item : Items) {
            System.out.println(BOLD + YELLOW + "ProductID: " + Item.getId() + RESET + " | " + Item.getName());
        }
        // Ask the user to choose a Item by ID
        System.out.print(BOLD + "Enter Item ID to update or type 'cancel' to abort: " + RESET);
        String input = scanner.nextLine();
        if (isCancelCommand(input)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
        try {
            int id = Integer.parseInt(input);
            Items.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .ifPresentOrElse(
                            Item -> System.out.println("\n" + CYAN + Item + RESET),
                            () -> System.out.println("\n" + RED + BOLD + "Item not found." + RESET)
                    );
        } catch (NumberFormatException e) {
            System.out.println(RED + BOLD + "Invalid input. Please enter a valid number." + RESET);
        }
    }

    private static void updateItem() {
        List<Item> Items = readAllItems();
        if (Items.isEmpty()) {
            System.out.println("\n" + RED + BOLD + "No Items found to update." + RESET);
            return;
        }
        // Display available Items with their IDs
        System.out.println("\n" + BOLD + "--- Available Items ---" + RESET);
        for (Item Item : Items) {
            System.out.println(BOLD + YELLOW + "ProductID: " + Item.getId() + RESET + " | " + Item.getName());
        }
        // Ask the user to choose a Item by ID to update
        System.out.print(BOLD + "Enter Item ID to update or type 'cancel' to abort: " + RESET);
        String input = scanner.nextLine();
        if (isCancelCommand(input)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }

        try {
            int id = Integer.parseInt(input);
            Item ItemToUpdate = Items.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (ItemToUpdate == null) {
                System.out.println("\n" + RED + BOLD + "Item not found." + RESET);
                return;
            }

        System.out.print("\n" + BOLD + GREEN + "Enter new name (current: " + ItemToUpdate.getName() + "): " + RESET);
        String newName = scanner.nextLine();
        if (isCancelCommand(newName)) {
            System.out.println(RED+ BOLD + "Operation cancelled." + RESET);
            return;
        }
        System.out.print(BOLD + YELLOW + "Enter new price (current: " + ItemToUpdate.getprice() + "): " + RESET);
        Object newprice = getValidprice();
        if (newprice instanceof String) {
            System.out.println(RED+ BOLD + "Operation cancelled." + RESET);
            return;
        }

        // Handle multiplier in the name (e.g., "apple x5")
        String itemNo = "1";
        int multiplier = 1;
        if (newName.contains(" x")) {
            String[] nameParts = newName.split(" x");
            newName = nameParts[0];
            itemNo = nameParts[1];
            try {
                multiplier = Integer.parseInt(nameParts[1]);
            } catch (NumberFormatException e) {
                System.out.println(RED + BOLD + "Invalid multiplier format. Please enter in the form 'name xN'." + RESET);
                return;
            }
        }

        // Apply multiplier to price
        Object newFinalPrice = 1;
        if (newprice instanceof Integer) {
            newFinalPrice = new BigDecimal((int) newprice * multiplier).setScale(2, RoundingMode.HALF_UP);
        } else if (newprice instanceof Double) {
            newFinalPrice = new BigDecimal((double) newprice * multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        
        System.out.print(BOLD + CYAN + "Enter new Item type (current: " + ItemToUpdate.getItem_type() + "): " + RESET);
        String newItem_type = scanner.nextLine();
        if (isCancelCommand(newItem_type)) {
            System.out.println(RED+ BOLD + "Operation cancelled." + RESET);
            return;
        }
        System.out.print(BOLD + BLUE + "Enter new category type (current: " + ItemToUpdate.getcategory() + "): " + RESET);
        String newcategory = scanner.nextLine();
        if (isCancelCommand(newcategory)) {
            System.out.println(RED+ BOLD + "Operation cancelled." + RESET);
            return;
        }
        // Choose supplier
        Supplier chosenSupplier = Supplier.chooseSupplier();
        if (chosenSupplier == null) {
            System.out.println(RED + "No supplier chosen. Operation cancelled." + RESET);
            return;
        }
        boolean isPaid = false;
        Item updatedItem = new Item(id, newName, newFinalPrice, newItem_type, newcategory, itemNo, chosenSupplier.supplierId,isPaid);
        // Replace the updated Item in the list
        Items.replaceAll(p -> p.getId() == id ? updatedItem : p);

        writeAllItemsToFile(Items);

        System.out.println("\n" + GREEN + BOLD+"Item updated successfully." + RESET);
        } catch (NumberFormatException e) {
            System.out.println(RED + BOLD + "Invalid input. Please enter a valid number." + RESET);
        }
    }

    // Helper method to list item names in the cart
    public static List<String> ItemsOnCart() {
        List<Item> items = readAllItems();

        if (items.isEmpty()) {
            System.out.println("\n" + RED + BOLD + "No Items found in the cart." + RESET);
            return Collections.emptyList();
        }
        // Show the list of available items in the cart
        System.out.println("\n" + BOLD + "--- Available Items in Cart ---" + RESET);
        List<String> itemNames = new ArrayList<>();
        for (Item item : items) {
            itemNames.add(item.getName());
            System.out.println(BOLD + YELLOW + "ProductID: " + item.getId() + RESET + " | " + item.getName());
        }
        
        return itemNames;
    }

    public static List<String> selectMultipleItemsByIndexOrName() {
        List<String> itemNames = ItemsOnCart();
        System.out.print("\n");
        System.out.print(PURPLE+BOLD+"Enter the record number(s) to be choosen (comma-separated): "+RESET);
        String input = scanner.nextLine();
        if (isCancelCommand(input)) {
            System.out.println(RED+ BOLD + "Operation cancelled." + RESET);
            return Collections.emptyList();
        }
        // Split the input based on '+' and collect the selected files
        List<String> selectedFiles = new ArrayList<>();
        for (String part : input.split("\\,")) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < itemNames.size()) {
                    selectedFiles.add(itemNames.get(index));
                } else {
                    System.out.println(RED + "Invalid index: " + part + RESET);
                }
            } catch (NumberFormatException e) {
                if (itemNames.contains(part.trim())) {
                    selectedFiles.add(part.trim());
                } else {
                    System.out.println(RED + "File not found: " + part + RESET);
                }
            }
        }
        return selectedFiles;
    }

    public static void deleteItems() {
        List<String> ItemsToDelete = selectMultipleItemsByIndexOrName();
        for (String Items : ItemsToDelete) {
            deleteItem(Items);
        }
    }

    private static void deleteItem(String itemName) {
        List<Item> items = readAllItems();  // Read all items from the file
        // Check if the items list is empty
        if (items.isEmpty()) {
            System.out.println("\n" + RED + BOLD + "No Items found to delete." + RESET);
            return;
        }
        // Search for the item by name
        Optional<Item> itemToDelete = items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(itemName))
                .findFirst();
        // If the item exists, remove it
        if (itemToDelete.isPresent()) {
            items.remove(itemToDelete.get());
    
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                items.set(i, new Item(i + 1, item.getName(), item.getprice(), item.getItem_type(), item.getcategory(), item.getItemNo(), item.getsupplerID(), item.getisPaid()));
            }
            writeAllItemsToFile(items);
    
            System.out.println(YELLOW + "Item deleted successfully: " + itemName + RESET);
            updateItemIdsAfterPayment();
        } else {
            System.out.println("\n" + RED + BOLD + "Item not found: " + itemName + RESET);
        }
    }

    public static void deleteRecords() {
        File folder = new File(FOLDER);
        File[] files = folder.listFiles();
    
        if (files == null || files.length == 0) {
            System.out.println(RED + BOLD + "No files found in the output folder." + RESET);
            return;
        }
    
        System.out.println(YELLOW + "Available files in the output folder:" + RESET);
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
    
        System.out.print("\n" + CYAN + BOLD + "Enter the file number or name to edit: " + RESET);
        String fileChoiceInput = scanner.nextLine().trim();
        File chosenFile = null;
    
        try {
            // Try to interpret the input as an index
            int fileChoice = Integer.parseInt(fileChoiceInput);
            if (fileChoice < 1 || fileChoice > files.length) {
                System.out.println(RED + "Invalid file index." + RESET);
                return;
            }
            chosenFile = files[fileChoice - 1];
        } catch (NumberFormatException e) {
            // If it's not an index, try to match by file name
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(fileChoiceInput)) {
                    chosenFile = file;
                    break;
                }
            }
            if (chosenFile == null) {
                System.out.println(RED + "File not found: " + fileChoiceInput + RESET);
                return;
            }
        }
    
        System.out.println(CYAN + "Selected file: " + chosenFile.getName() + RESET);
    
        List<String> records = readLinesFromFile(chosenFile.getPath());
        if (records.isEmpty()) {
            System.out.println(RED + BOLD + "No records found in the file." + RESET);
            return;
        }
    
        // Display the records (skip the header)
        System.out.println(CYAN + "\nCurrent records in the file:" + RESET);
        List<String> headers = Arrays.asList(records.get(0).split(",")); // Split the header line into individual headers

        // Display the headers with matching colors
        System.out.print(YELLOW + "Index, " + RESET);
        for (int j = 0; j < headers.size(); j++) {
            String header = headers.get(j);

            // Apply color coding to headers based on column index
            switch (j % 6) {
                case 0: // (First column)
                    System.out.print(YELLOW + header + RESET + ", ");
                    break;
                case 1: // (Second column)
                    System.out.print(GREEN + header + RESET + ", ");
                    break;
                case 2: // (Third column)
                    System.out.print(CYAN + header + RESET + ", ");
                    break;
                case 3: // (Fourth column)
                    System.out.print(BLUE + header + RESET + ", ");
                    break;
                case 4: // (Fifth column)
                    System.out.print(PURPLE + header + RESET + ", ");
                    break;
                case 5: // (Sixth column)
                    System.out.print(RED + header + RESET + ", ");
                    break;
                default:
                    System.out.print(header + ", ");
                    break;
            }
        }
        System.out.println(); // Move to the next line after displaying headers

        for (int i = 1; i < records.size(); i++) {
            List<String> row = Arrays.asList(records.get(i).split(",")); 
            
            // Display the record index
            System.out.print(YELLOW + i + ". " + RESET);
            
            for (int j = 0; j < headers.size(); j++) {
                String data = j < row.size() ? row.get(j) : "";
                
                // Apply color coding to row values based on column index
                switch (j % 6) {
                    case 0: // (First column)
                        System.out.print(YELLOW + data + RESET + ", ");
                        break;
                    case 1: // (Second column)
                        System.out.print(GREEN + data + RESET + ", ");
                        break;
                    case 2: // (Third column)
                        System.out.print(CYAN + data + RESET + ", ");
                        break;
                    case 3: // (Fourth column)
                        System.out.print(BLUE + data + RESET + ", ");
                        break;
                    case 4: // (Fifth column)
                        System.out.print(PURPLE + data + RESET + ", ");
                        break;
                    case 5: // (Sixth column)
                        System.out.print(RED + data + RESET + ", ");
                        break;
                    default:
                        System.out.print(data + ", ");
                        break;
                }
            }
            System.out.println(); // Move to the next line after a row
        }

    
        // Step 4: Let the user select records to delete
        System.out.println("\n" + YELLOW + BOLD + "Enter the record number(s) to delete (comma-separated): " + RESET);
        String input = scanner.nextLine().trim();
        String[] recordIndices = input.split(",");
        Set<Integer> indicesToDelete = new HashSet<>();
        for (String indexStr : recordIndices) {
            try {
                int index = Integer.parseInt(indexStr.trim());
                if (index >= 1 && index < records.size()) { // Ensure index is valid (ignoring header)
                    indicesToDelete.add(index);
                } else {
                    System.out.println(RED + "Invalid index: " + index + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid input: " + indexStr.trim() + RESET);
            }
        }
    
        if (!indicesToDelete.isEmpty()) {
            List<String> updatedRecords = new ArrayList<>();
            updatedRecords.add(records.get(0)); // Keep the header
            for (int i = 1; i < records.size(); i++) {
                if (!indicesToDelete.contains(i)) {
                    updatedRecords.add(records.get(i));
                }
            }
    
            // Rewrite the file with updated records
            writeLinesToFile(chosenFile.getPath(), updatedRecords);
            System.out.println(GREEN + "Selected records deleted successfully." + RESET);
        } else {
            System.out.println(RED + "No valid records selected for deletion." + RESET);
        }
    }
    
    // Helper method to read lines from a file
    private static List<String> readLinesFromFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error reading file: " + e.getMessage() + RESET);
            return Collections.emptyList();
        }
    }
    
    // Helper method to write lines to a file
    private static void writeLinesToFile(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error writing to file: " + e.getMessage() + RESET);
        }
    }

    private static void appendItemToFile(Item Item) {
        File file = new File(FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            // Write headers if the file is new or empty
            if (!file.exists() || file.length() == 0) {
                writer.write(Item.getHeaders());
                writer.newLine();
            }
            // Append the item's raw string
            writer.write(Item.toRawString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("\n" + RED + BOLD + "An error occurred while writing to the file." + RESET);
        }
    }

    private static List<Item> readAllItems() {
        List<Item> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean isFirstLine = true;
    
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { 
                    isFirstLine = false; // Skip the header
                    continue;
                }
                items.add(Item.fromString(line, false)); // Parse the data line into an Item object
            }
        } catch (IOException e) {
            System.out.println("\n" + RED + BOLD + "An error occurred while reading the file." + RESET);
        }
        return items;
    }
    
    // Modified method to delete multiple files
    public static void deleteTable() {
        List<String> filesToDelete = selectMultipleFilesByIndexOrName();
        for (String fileName : filesToDelete) {
            deleteFile(fileName);
        }
    }
    
    // Helper method to select multiple files by index or name
    public static List<String> selectMultipleFilesByIndexOrName() {
        List<String> fileNames = listFilesInOutputFolder();
        System.out.print("\n");
        System.out.print(PURPLE+BOLD+"Enter the record number(s) to delete (comma-separated): "+RESET);
        String input = scanner.nextLine();
        if (isCancelCommand(input)) {
            System.out.println(RED+ BOLD + "Operation cancelled." + RESET);
            return Collections.emptyList();
        }
        // Split the input based on '+' and collect the selected files
        List<String> selectedFiles = new ArrayList<>();
        for (String part : input.split("\\,")) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < fileNames.size()) {
                    selectedFiles.add(fileNames.get(index));
                } else {
                    System.out.println(RED + "Invalid index: " + part + RESET);
                }
            } catch (NumberFormatException e) {
                if (fileNames.contains(part.trim())) {
                    selectedFiles.add(part.trim());
                } else {
                    System.out.println(RED + "File not found: " + part + RESET);
                }
            }
        }
        return selectedFiles;
    }

    // Method to delete a file
    public static void deleteFile(String fileName) {
        try {
            Path path = Paths.get(FOLDER + fileName);
            Files.delete(path);
            System.out.println(GREEN + "File deleted successfully." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "An error occurred while deleting the file." + RESET);
            e.printStackTrace();
        }
    }

    private static void writeAllItemsToFile(List<Item> items) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(Item.getHeaders());
            writer.newLine();
            for (Item item : items) {
                writer.write(item.toRawString());
                writer.newLine();
            }
            System.out.println(GREEN + "Cart updated successfully." + RESET);
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error updating cart file." + RESET);
        }
    }
    
    private static int getNextIdFromFile() {
        int maxId = 0;
        for (Item Item : readAllItems()) {
            if (Item.getId() > maxId) {
                maxId = Item.getId();
            }
        }
        return maxId + 1;
    }

    // Utility method for getting valid price input
    private static Object getValidprice() {
        while (true) {
            String input = scanner.nextLine();
    
            if (isCancelCommand(input)) {
                return null;
            }
    
            try {
                // First, try to parse the input as an Integer
                int intPrice = Integer.parseInt(input);
                if (intPrice >= 0) {
                    return intPrice;  // Return the integer price if valid
                } else {
                    System.out.print("\n" + RED + BOLD + "Price must be a positive integer. Please try again: " + RESET);
                }
            } catch (NumberFormatException e1) {
                try {
                    // Next, try to parse the input as a Double
                    double doublePrice = Double.parseDouble(input);
                    if (doublePrice >= 0) {
                        return doublePrice;
                    } else {
                        System.out.print("\n" + RED + BOLD + "Price must be a positive number. Please try again: " + RESET);
                    }
                } catch (NumberFormatException e2) {
                    return input; 
                }
            }
        }
    }

    private static void createViewTable() {
        // Prompt for the view file name
        System.out.print(BOLD + GREEN + "CREATE VIEW (Enter the view file name): " + RESET);
        String viewFileName = scanner.nextLine().trim();
        if (isCancelCommand(viewFileName)) {
            System.out.println(RED + BOLD + "Operation cancelled." + RESET);
            return;
        }
        viewFileName += ".csv";

        // Display available files from the output folder
        List<String> availableFiles = listFilesInOutputFolder();
        if (availableFiles.isEmpty()) {
            System.out.println(RED + BOLD + "\nNo files found in the output folder." + RESET);
            return;
        }
        // Prompt for the source file
        System.out.print(BOLD + GREEN + "\nFROM (Enter file name or index): " + RESET);
        String fromInput = scanner.nextLine().trim();
        if (isCancelCommand(fromInput)) {
            System.out.println(RED + BOLD + "\nOperation cancelled." + RESET);
            return;
        }
        String sourceFile = getFileFromInput(fromInput, availableFiles);
        if (sourceFile == null) {
            System.out.println(RED + BOLD + "\nInvalid file selection." + RESET);
            return;
        }

        // Read the header from the file
        List<String> headers = readHeadersFromFile(sourceFile);
        if (headers.isEmpty()) {
            System.out.println(RED + BOLD + "\nNo headers found in the file." + RESET);
            return;
        }

        // Display the headers to the user
        System.out.println(BOLD + CYAN + "Available columns in the file:" + RESET);
        for (int i = 0; i < headers.size(); i++) {
            System.out.println((i + 1) + ". " + headers.get(i));
        }
    
        // Prompt for SELECT columns
        System.out.print(BOLD + YELLOW + "SELECT (Enter columns separated by commas): " + RESET);
        String selectColumnsInput = scanner.nextLine().trim();
        if (isCancelCommand(selectColumnsInput)) {
            System.out.println(RED + BOLD + "\nOperation cancelled." + RESET);
            return;
        }
        String[] selectColumns = selectColumnsInput.split(",");
    
        // Prompt for WHERE conditions
        System.out.print(BOLD + YELLOW + "WHERE (Enter condition, e.g., price > 100): " + RESET);
        String whereCondition = scanner.nextLine().trim();
        if (isCancelCommand(whereCondition)) {
            System.out.println(RED + BOLD + "\nOperation cancelled." + RESET);
            return;
        }
    
        // Filter and create the view
        try {
            List<String> filteredRows = filterFile(sourceFile, selectColumns, whereCondition);
            writeViewToFile(viewFileName, selectColumns, filteredRows, sourceFile);
            System.out.println(GREEN + BOLD + "\nView created successfully: " + viewFileName + RESET);
        } catch (Exception e) {
            System.out.println(RED + BOLD + "\nError creating view: " + e.getMessage() + RESET);
        }
    }

    // Method to list all files in the output folder with indexes
    public static List<String> listFilesInOutputFolder() {
        List<String> fileNames = new ArrayList<>();
        System.out.print("\n");
        System.out.println(BOLD + GREEN + "Tables from the " + FOLDER + " folder" + RESET);
        try (Stream<Path> paths = Files.list(Paths.get(FOLDER))) {
            List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            for (int i = 0; i < files.size(); i++) {
                String fileName = files.get(i).getFileName().toString();
                fileNames.add(fileName);
                System.out.println((i + 1) + ". " + fileName + RESET);
            }
        } catch (IOException e) {
            System.out.println(RED + "An error occurred while listing files." + RESET);
            e.printStackTrace();
        }
        return fileNames;
    }

    private static String getFileFromInput(String input, List<String> availableFiles) {
        try {
            int index = Integer.parseInt(input.trim()) - 1;
            if (index >= 0 && index < availableFiles.size()) {
                return FOLDER + availableFiles.get(index);
            }
        } catch (NumberFormatException e) {
            for (String file : availableFiles) {
                if (file.equalsIgnoreCase(input.trim())) {
                    return FOLDER + file;
                }
            }
        }
        return null;
    }
    // Helper method to read headers from a CSV file
    private static List<String> readHeadersFromFile(String fileName) {
        List<String> headers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String firstLine = br.readLine();  // Read the first line (header)
            if (firstLine != null) {
                String[] headerColumns = firstLine.split(",");
                headers.addAll(Arrays.asList(headerColumns));
            }
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error reading the file: " + fileName + RESET);
        }
        return headers;
    }
    
    private static List<String> filterFile(String sourceFile, String[] columns, String whereCondition) throws IOException {
        List<String> resultRows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String header = br.readLine();  // Read header but don't add to filtered rows
            String[] headers = header.split(",");
        
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i], values[i]);
                }
        
                // Apply the WHERE condition to filter rows
                if (evaluateCondition(row, whereCondition)) {
                    resultRows.add(line);  // Add the filtered row (without the header)
                }
            }
        }
        return resultRows;
    }
    
    private static void writeViewToFile(String viewFileName, String[] columns, List<String> filteredRows, String sourceFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FOLDER + viewFileName))) {
            // Get the headers from the source file
            List<String> headers = readHeadersFromFile(sourceFile);
            // Write the header to the output file
            if (headers != null && !headers.isEmpty()) {
                writer.write(String.join(",", headers));  // Join headers as CSV and write to file
                writer.newLine();
            }
            // Write filtered rows (which do not include the header)
            for (String row : filteredRows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }
    
    private static boolean evaluateCondition(Map<String, String> row, String condition) {
        // Split the condition into sub-conditions based on AND/OR
        String[] orConditions = condition.split("\\s+OR\\s+");
        for (String orCondition : orConditions) {
            String[] andConditions = orCondition.split("\\s+AND\\s+");
            boolean andResult = true;
            for (String andCondition : andConditions) {
                if (!evaluateSingleCondition(row, andCondition.trim())) {
                    andResult = false;
                    break;
                }
            }
            // If any OR condition is true, we return true
            if (andResult) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean evaluateSingleCondition(Map<String, String> row, String condition) {
        // Basic condition evaluation (single condition like "price > 100")
        try {
            String[] parts = condition.split(" ");
            if (parts.length != 3) return false;
        
            String column = parts[0].trim();
            String operator = parts[1].trim();
            String value = parts[2].trim();
        
            String columnValue = row.get(column);
            if (columnValue == null) return false;
        
            try {
                double columnNumericValue = Double.parseDouble(columnValue);
                double conditionValue = Double.parseDouble(value);
                // Numeric comparison
                switch (operator) {
                    case "==": return columnNumericValue == conditionValue;
                    case "!=": return columnNumericValue != conditionValue;
                    case ">": return columnNumericValue > conditionValue;
                    case "<": return columnNumericValue < conditionValue;
                    case ">=": return columnNumericValue >= conditionValue;
                    case "<=": return columnNumericValue <= conditionValue;
                    default: return false;
                }
            } catch (NumberFormatException e) {
                // If it fails to parse as numeric, treat it as a string
                // String comparison
                switch (operator) {
                    case "==": return columnValue.equals(value);
                    case "!=": return !columnValue.equals(value);
                    default: return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
}

class Item {
    private int id;
    private final String name;
    private final Object finalPrice;
    private final String Item_type;
    private final String category;
    private final String itemNo;
    private final int supplierId;
    private boolean isPaid = false;

    // Color codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String YELLOW = "\u001B[33m";

    public Item(int id, String name, Object finalPrice, String Item_type, String category, String itemNo, int supplierId, boolean isPaid) {
        this.id = id;
        this.name = name;
        this.finalPrice = finalPrice;
        this.Item_type = Item_type;
        this.category = category;
        this.itemNo = itemNo;
        this.supplierId = supplierId;
        this.isPaid = isPaid;
        //this.date = date;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public Object getprice() { return finalPrice; }
    public String getItem_type() { return Item_type; }
    public String getcategory() { return category; }
    public String getItemNo() { return itemNo; }
    public int getsupplerID () { return supplierId; }
    public boolean getisPaid() { return isPaid; }
    //public String getdate() { return date; }
    @Override
    public String toString() {
        return BOLD + YELLOW + "ProductID: " + id + RESET +
               ", " + GREEN + "Item: " + name + RESET +
               ", " + YELLOW + "price: " + finalPrice + RESET +
               ", " + CYAN + "Item_type: " + Item_type + RESET +
               ", " + BLUE + "category: " + category + RESET +
               ", " + PURPLE + "ItemNo: " + itemNo + RESET +
               ", " + RED + "SupplierID: " + supplierId + RESET +
               ", " + GREEN + "isPaid: " + isPaid + RESET;
               //", " + PURPLE + "DateTime: " + date + RESET;
    }

    // Parsing raw data from CSV format (without colors)
    public static Item fromString(String line, boolean isHeader) {
        // Skip header line
        if (isHeader) {
            return null;
        }
 
        String[] parts = line.split(",");
        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        // Parse the price as Object (can be Integer or Double)
        Object price = null;
        try {
            price = Double.parseDouble(parts[2]); 
        } catch (NumberFormatException e) {
            // If Double parsing fails, try Integer
            price = Integer.parseInt(parts[2]);
        }
        String Item_type = parts[3];
        String category = parts[4];
        String itemNo = parts[5];
        int supplierId = Integer.parseInt(parts[6].trim());
        boolean isPaid = Boolean.parseBoolean(parts[7].trim());
        //String date = parts[6];
        return new Item(id, name, price, Item_type, category, itemNo, supplierId, isPaid);
    }

    // Converts Item to a raw CSV format (no colors, just data)
    public String toRawString() {
        return id + "," + name + "," + finalPrice + "," + Item_type + "," + category + "," + itemNo + "," + supplierId + "," + isPaid;
    }
    public static String getHeaders() {
        return "ProductID,ItemName,Price,ItemType,CategoryType,ItemNo,SupplierID,isPaid";
    }
}

class LocalServer {
    // Color codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String YELLOW = "\u001B[33m";

    // Start the local server
    public static HttpServer server;  // Store server instance to stop it later

    public static void startServer(int port, List<String> csvFilePaths) {
        try {
            String localIP = getLocalIpAddress();
            if (localIP == null) {
                System.out.println(RED + BOLD + "Unable to get the local IP address." + RESET);
                return;
            }

            server = HttpServer.create(new InetSocketAddress(localIP, port), 0);
            server.createContext("/", new CsvHandler(csvFilePaths));
            server.setExecutor(null);
            server.start();

            System.out.println(GREEN + "\nServer started at http://" + localIP + ":" + port + RESET);
            System.out.println(GREEN + "You can view the CSV data from devices connected to the same network." + RESET);
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error starting server: " + e.getMessage() + RESET);
        }
    }

    private static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Stop the server gracefully
    public static void stopServer() {
        if (server != null) {
            server.stop(0);  // Stop the server immediately
            System.out.println(GREEN + "Server stopped." + RESET);
        } else {
            System.out.println(RED + "No server running." + RESET);
        }
    }

    // HTTP handler to serve multiple CSV files as HTML tables
    static class CsvHandler implements HttpHandler {
        private final List<String> csvFilePaths;

        public CsvHandler(List<String> csvFilePaths) {
            this.csvFilePaths = csvFilePaths;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder response = new StringBuilder("<html><body>");
            response.append("<h1>CSV Data</h1>");

            for (String csvFilePath : csvFilePaths) {
                String fileName = new File(csvFilePath).getName();  // Extract only the file name
                response.append("<h2>").append(fileName).append("</h2>");
                response.append("<table border='1'>");

                // Read CSV file and generate HTML table
                try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                    String line;
                    boolean isHeader = true;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");
                        response.append("<tr>");
                        for (String value : values) {
                            if (isHeader) {
                                response.append("<th>").append(value).append("</th>");
                            } else {
                                response.append("<td>").append(value).append("</td>");
                            }
                        }
                        response.append("</tr>");
                        isHeader = false;
                    }
                } catch (IOException e) {
                    response.append("<tr><td colspan='5'>Error reading CSV file: ").append(e.getMessage()).append("</td></tr>");
                }
                response.append("</table><br>");
            }
            response.append("</body></html>");

            // Send HTTP response
            byte[] bytes = response.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // Trigger function to decide if the server should run
    public static void maybeStartServer(String outputFolderPath) {
        try {
            System.out.print(BOLD + GREEN + "Do you want to start the local server to view CSV data? (y/n): " + RESET);
            String userChoice = new BufferedReader(new InputStreamReader(System.in)).readLine().trim().toLowerCase();

            if (userChoice.equalsIgnoreCase("y") || userChoice.equalsIgnoreCase("yes")) {
                System.out.print(BOLD + CYAN + "Enter the port number to start the server (default 8080): " + RESET);
                String portInput = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
                int port = portInput.isEmpty() ? 8080 : Integer.parseInt(portInput);

                // List available CSV files in the output folder
                File folder = new File(outputFolderPath);
                File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

                if (csvFiles == null || csvFiles.length == 0) {
                    System.out.println(RED + "No CSV files found in the output folder." + RESET);
                    return;
                }

                System.out.println(BOLD + GREEN + "\nAvailable CSV files:" + RESET);
                for (int i = 0; i < csvFiles.length; i++) {
                    System.out.println((i + 1) + ". " + csvFiles[i].getName());
                }

                System.out.print(BOLD + YELLOW + "Do you want to display (1) only one file or (2) all files? (1/2): " + RESET);
                String fileOption = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

                List<String> selectedCsvFilePaths = new ArrayList<>();

                if (fileOption.equals("1")) {
                    System.out.print(BOLD + YELLOW + "Select the CSV file to serve (enter the number): " + RESET);
                    int fileChoice = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine().trim());

                    if (fileChoice < 1 || fileChoice > csvFiles.length) {
                        System.out.println(PURPLE + BOLD + "Invalid choice. Server not started." + RESET);
                        return;
                    }

                    selectedCsvFilePaths.add(csvFiles[fileChoice - 1].getAbsolutePath());
                } else if (fileOption.equals("2")) {
                    for (File csvFile : csvFiles) {
                        selectedCsvFilePaths.add(csvFile.getAbsolutePath());
                    }
                } else {
                    System.out.println(PURPLE + BOLD + "Invalid option selected. Server not started." + RESET);
                    return;
                }

                // Start the server with the selected CSV file paths
                startServer(port, selectedCsvFilePaths);
            } else {
                System.out.println(YELLOW + "Server not started. Continuing program..." + RESET);
            }
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error reading input. Server not started." + RESET);
        } catch (NumberFormatException e) {
            System.out.println(PURPLE + BOLD + "Invalid input. Server not started." + RESET);
        }
    }
}

class Supplier {
    int supplierId;
    String supplierName;
    String contact;

    private static final Scanner scanner = new Scanner(System.in);

    private static final String FOLDER = "output/";
    private static final String FILE_PATH = FOLDER + "supplier.csv";

    // Color codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String YELLOW = "\u001B[33m";

    public Supplier(int supplierId, String supplierName, String contact) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contact = contact;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Contact: %s", supplierId, supplierName, contact);
    }

    // Function to view and select a supplier
    public static Supplier chooseSupplier() {
        List<Supplier> suppliers = loadSuppliersFromFile(FILE_PATH);

        if (suppliers.isEmpty()) {
            System.out.println(RED + BOLD + "No suppliers available. Please ensure the supplier.csv file is populated." + RESET);
            return null;
        }

        // Display available suppliers with only ID and Name
        System.out.println(BOLD + CYAN + "Available Suppliers:" + RESET);
        for (Supplier supplier : suppliers) {
            System.out.printf(BOLD + "ID: %d, Name: %s" + RESET + "%n", supplier.supplierId, supplier.supplierName);
        }

        // Allow user to choose a supplier
        System.out.print(BOLD + GREEN + "Enter the Supplier ID you want to choose: " + RESET);

        try {
            int chosenId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Find the chosen supplier
            for (Supplier supplier : suppliers) {
                if (supplier.supplierId == chosenId) {
                    System.out.println(GREEN + "Supplier chosen: ID: " + supplier.supplierId + ", Name: " + supplier.supplierName + RESET);
                    return supplier;
                }
            }

            System.out.println(RED + "Invalid Supplier ID. Please try again." + RESET);
        } catch (InputMismatchException e) {
            System.out.println(RED + "Invalid input. Please enter a valid Supplier ID." + RESET);
            scanner.nextLine(); // Clear invalid input
        }

        return null;
    }

    // Load suppliers from file, creating the file with default content if it doesn't exist
    private static List<Supplier> loadSuppliersFromFile(String filePath) {
        List<Supplier> suppliers = new ArrayList<>();
        File file = new File(filePath);

        try {
            if (!file.exists()) {
                System.out.println(PURPLE + BOLD + "Suppliers file not found. Creating a new file with default content: " + filePath + RESET);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    // Write default content to the new file
                    writer.write("SupplierID,SupplierName,Contact");
                    writer.newLine();
                    writer.write("1,Lax Bulangerie,0987654321");
                    writer.newLine();
                    writer.write("2,Viernes Freshness,0123456789");
                    writer.newLine();
                    writer.write("3,Ramos Supplies,0978653421");
                    writer.newLine();
                    writer.write("4,SM Supplies,0192837465");
                    writer.newLine();
                    writer.write("5,National Book Store,109221843");
                    writer.newLine();
                    writer.write("6,Samsung,0997765434");
                    writer.newLine();
                    writer.write("7,Apple,0765643566");
                    writer.newLine();
                    writer.write("8,Ace Hardware,09574346345");
                    writer.newLine();
                    writer.write("9,BreadTalk,03575674576");
                    writer.newLine();
                    writer.write("10,Starbucks,0945754336");
                    writer.newLine();
                }
            }

            // Read suppliers from the file
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() > 1) { // Skip the header
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    if (parts.length >= 3) {
                        suppliers.add(new Supplier(
                                Integer.parseInt(parts[0].trim()),
                                parts[1].trim(),
                                parts[2].trim()
                        ));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(RED + BOLD + "Error loading suppliers: " + e.getMessage() + RESET);
        }
        return suppliers;
    }
}

class PaymentDetails {
    private String orderID;
    private String customerName;
    private String customerContact;
    private int paymentOption;

    public PaymentDetails(String orderID, String customerName, String customerContact, int paymentOption) {
        this.orderID = orderID;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.paymentOption = paymentOption;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public int getpaymentOption() {
        return paymentOption;
    }
}


class QRCodeScanner {

    public static String[] scanQRCode() {
        // Create an instance of ZXing's MultiFormatReader to decode QR codes
        MultiFormatReader qrCodeReader = new MultiFormatReader();
        
        // Open the camera using JavaCV (index 0 for default webcam)
        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
            grabber.start();  // Start the webcam
            
            // Capture a frame from the camera
            Frame frame = grabber.grab();
            
            // Convert the captured frame to a BufferedImage
            BufferedImage bufferedImage = convertFrameToBufferedImage(frame);
            
            // Decode the QR code using ZXing
            String qrCodeData = decodeQRCode(bufferedImage, qrCodeReader);
            
            if (qrCodeData != null) {
                // Print the QR code data
                System.out.println("QR Code Data: " + qrCodeData);
                
                // Extract Name and Contact from QR code data using regex
                String[] customerInfo = extractCustomerInfo(qrCodeData);
                if (customerInfo != null) {
                    return customerInfo;  // Return the extracted information
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null; // Return null if no QR code found or an error occurred
    }

    private static BufferedImage convertFrameToBufferedImage(Frame frame) {
        // Convert JavaCV Frame to BufferedImage
        return Java2DFrameUtils.toBufferedImage(frame);
    }

    private static String decodeQRCode(BufferedImage bufferedImage, MultiFormatReader qrCodeReader) {
        try {
            // Convert BufferedImage to a BinaryBitmap for ZXing decoding
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            // Decode the QR code using ZXing's MultiFormatReader
            Result result = qrCodeReader.decode(bitmap);
            return result.getText(); // Return the decoded QR code text
        } catch (NotFoundException e) {
            // No QR code found in the frame
            return null;
        }
    }

    private static String[] extractCustomerInfo(String qrCodeData) {
        // Assuming the QR Code contains data in the format "Name: <Name> | Contact: <Contact>"
        // Adjust the regex to match your expected format.
        Pattern pattern = Pattern.compile("Name: ([^|]+) \\| Contact: ([^|]+)");
        Matcher matcher = pattern.matcher(qrCodeData);
        
        if (matcher.find()) {
            String name = matcher.group(1);      // Extract the Name
            String contact = matcher.group(2);   // Extract the Contact
            return new String[] { name, contact };  // Return extracted information
        } else {
            System.out.println("Error: Could not extract Name and Contact from QR Code.");
            return null;  // Return null if data is not in the expected format
        }
    }
}