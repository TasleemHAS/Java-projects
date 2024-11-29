import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class CafeOrderingSystem {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextArea orderSummary;
    private int total = 0;
    private String customerName, customerContact, customerEmail, paymentMode;
    private Connection connection;

    private JComboBox<String> foodCategory;
    private JComboBox<String> itemMenu;
    private JTextField quantityField;
    private JLabel totalLabel;
    private JComboBox<String> paymentModeComboBox;
    private JTextField discountField; // New field for discount

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CafeOrderingSystem::new);
    }

    public CafeOrderingSystem() {
        frame = new JFrame("Cafe Ordering System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 750);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(255, 224, 204)); // Pastel background color

        mainPanel.add(createWelcomePage(), "WelcomePage");
        mainPanel.add(createLoginPage(), "LoginPage");
        mainPanel.add(createMenuPage(), "MenuPage");
        mainPanel.add(createBillingPage(), "BillingPage");

        frame.add(mainPanel);
        frame.setVisible(true);

        cardLayout.show(mainPanel, "WelcomePage");

        connectToDatabase();
    }

    private JPanel createWelcomePage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 204, 204)); // Pastel color
        JLabel welcomeLabel = new JLabel("Welcome to Our Cafe!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 30));
        welcomeLabel.setForeground(new Color(102, 51, 0));

        JButton nextButton = new JButton("Continue");
        nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
        nextButton.setBackground(new Color(255, 153, 153));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.addActionListener(e -> cardLayout.show(mainPanel, "LoginPage"));

        panel.add(welcomeLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLoginPage() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(255, 229, 204)); // Pastel color

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        nameLabel.setForeground(new Color(102, 51, 0));
        JTextField nameField = new JTextField();

        JLabel contactLabel = new JLabel("Contact Number:");
        contactLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        contactLabel.setForeground(new Color(102, 51, 0));
        JTextField contactField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        emailLabel.setForeground(new Color(102, 51, 0));
        JTextField emailField = new JTextField();

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 18));
        submitButton.setBackground(new Color(255, 153, 153));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(e -> {
            customerName = nameField.getText().trim();
            customerContact = contactField.getText().trim();
            customerEmail = emailField.getText().trim();

            if (customerName.isEmpty() || customerContact.isEmpty() || customerEmail.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all details!", "Error", JOptionPane.WARNING_MESSAGE);
            } else if (!isValidEmail(customerEmail)) {
                JOptionPane.showMessageDialog(frame, "Invalid email format!", "Error", JOptionPane.WARNING_MESSAGE);
            } else if (!isValidPhoneNumber(customerContact)) {
                JOptionPane.showMessageDialog(frame, "Invalid contact number!", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                cardLayout.show(mainPanel, "MenuPage");
            }
        });

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(contactLabel);
        panel.add(contactField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(new JLabel());
        panel.add(submitButton);

        return panel;
    }

    private JPanel createMenuPage() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(224, 224, 255)); // Pastel color

        JLabel menuLabel = new JLabel("Menu", SwingConstants.CENTER);
        menuLabel.setFont(new Font("Arial", Font.BOLD, 22));
        menuLabel.setForeground(new Color(102, 51, 0));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        inputPanel.setBackground(new Color(255, 239, 204)); // Pastel color

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        foodCategory = new JComboBox<>(new String[]{
                "Select Category", "Fries", "Burgers (Veg)", "Burgers (Non-Veg)", "Pizzas (Veg)", 
                "Pizzas (Non-Veg)", "Pasta", "Salads", "Desserts", "Beverages", "Dips"
        });
        itemMenu = new JComboBox<>();
        quantityField = new JTextField(5);
        orderSummary = new JTextArea(10, 30);
        orderSummary.setEditable(false);
        totalLabel = new JLabel("Total: ₹0", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(new Color(0, 128, 0));

        paymentModeComboBox = new JComboBox<>(new String[]{"Select Payment Mode", "Cash", "Card", "UPI"});
        discountField = new JTextField(5); // New field for discount

        JButton addButton = new JButton("Add to Order");
        JButton removeButton = new JButton("Remove Last Item");
        JButton generateBillButton = new JButton("Generate Bill");

        addButton.setFont(new Font("Arial", Font.PLAIN, 18));
        removeButton.setFont(new Font("Arial", Font.PLAIN, 18));
        generateBillButton.setFont(new Font("Arial", Font.PLAIN, 18));
        addButton.setBackground(new Color(255, 153, 153));
        removeButton.setBackground(new Color(255, 102, 102));
        generateBillButton.setBackground(new Color(102, 255, 102));
        addButton.setForeground(Color.WHITE);
        removeButton.setForeground(Color.WHITE);
        generateBillButton.setForeground(Color.WHITE);

        addButton.setFocusPainted(false);
        removeButton.setFocusPainted(false);
        generateBillButton.setFocusPainted(false);

        foodCategory.addActionListener(e -> updateItemMenu());
        addButton.addActionListener(e -> addToOrder());
        removeButton.addActionListener(e -> removeLastItem());
        generateBillButton.addActionListener(e -> generateBill());

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(foodCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Menu Item:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(itemMenu, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(addButton, gbc);
        gbc.gridx = 1;
        inputPanel.add(removeButton, gbc);
        gbc.gridx = 2;
        inputPanel.add(generateBillButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Payment Mode:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentModeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        inputPanel.add(new JLabel("Discount (%):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(discountField, gbc); // Adding discount field

        menuPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel orderPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(orderSummary);
        orderPanel.add(scrollPane, BorderLayout.CENTER);
        orderPanel.add(totalLabel, BorderLayout.SOUTH);
        menuPanel.add(orderPanel, BorderLayout.SOUTH);

        return menuPanel;
    }

    private JPanel createBillingPage() {
        JPanel billingPanel = new JPanel(new BorderLayout());
        billingPanel.setBackground(new Color(255, 248, 220)); // Pastel color
        JLabel billingLabel = new JLabel("Billing Details", SwingConstants.CENTER);
        billingLabel.setFont(new Font("Arial", Font.BOLD, 22));
        billingLabel.setForeground(new Color(102, 51, 0));

        JTextArea billDetailsArea = new JTextArea(15, 40);
        billDetailsArea.setEditable(false);
        JScrollPane billScroll = new JScrollPane(billDetailsArea);

        JButton backButton = new JButton("Back to Menu");
        JButton confirmButton = new JButton("Confirm Order");

        backButton.setFont(new Font("Arial", Font.PLAIN, 18));
        confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
        backButton.setBackground(new Color(255, 102, 102));
        confirmButton.setBackground(new Color(102, 255, 102));
        backButton.setForeground(Color.WHITE);
        confirmButton.setForeground(Color.WHITE);

        backButton.setFocusPainted(false);
        confirmButton.setFocusPainted(false);

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "MenuPage"));
        confirmButton.addActionListener(e -> confirmOrder(billDetailsArea));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2, 20, 10));
        buttonsPanel.add(backButton);
        buttonsPanel.add(confirmButton);

        billingPanel.add(billingLabel, BorderLayout.NORTH);
        billingPanel.add(billScroll, BorderLayout.CENTER);
        billingPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return billingPanel;
    }

    private void addToOrder() {
        String selectedCategory = (String) foodCategory.getSelectedItem();
        String selectedItem = (String) itemMenu.getSelectedItem();
        String quantityText = quantityField.getText().trim();

        if (selectedCategory.equals("Select Category") || selectedItem.equals("Select Item") || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        int price = getItemPrice(selectedItem);

        total += price * quantity;
        orderSummary.append(selectedItem + " (" + quantity + " x ₹" + price + ")\n");
        totalLabel.setText("Total: ₹" + total);
    }

    private void removeLastItem() {
        // Code to remove the last item from the order (this can be a bit more complex depending on how you store items in the order)
        JOptionPane.showMessageDialog(frame, "Item removed successfully!", "Item Removed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateBill() {
        String selectedPaymentMode = (String) paymentModeComboBox.getSelectedItem();
        if (selectedPaymentMode.equals("Select Payment Mode")) {
            JOptionPane.showMessageDialog(frame, "Please select a payment mode", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        paymentMode = selectedPaymentMode;
        double discount = 0;
        try {
            discount = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException e) {
            // Handle if discount is not a valid number
            JOptionPane.showMessageDialog(frame, "Please enter a valid discount.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double tax = total * 0.18;  // Assuming 18% GST
        double grandTotal = total + tax;
        if (discount > 0) {
            grandTotal -= grandTotal * (discount / 100); // Apply discount
        }

        orderSummary.append("\n-------------------------------------\n");
        orderSummary.append("Tax (18% GST): ₹" + tax + "\n");
        if (discount > 0) {
            orderSummary.append("Discount Applied: " + discount + "%\n");
        }
        orderSummary.append("Grand Total: ₹" + grandTotal + "\n");
        orderSummary.append("\nPayment Mode: " + paymentMode + "\n");
        orderSummary.append("Date & Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");

        JOptionPane.showMessageDialog(frame, "Bill generated successfully! Check your order summary.", "Bill Generated", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "BillingPage");
    }

    private void confirmOrder(JTextArea billDetailsArea) {
        // Generate Bill Details
        StringBuilder bill = new StringBuilder();
        bill.append("Cafe Order Bill\n");
        bill.append("Customer Name: " + customerName + "\n");
        bill.append("Contact: " + customerContact + "\n");
        bill.append("Email: " + customerEmail + "\n");
        bill.append("Date & Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        bill.append("\nItems Ordered:\n");
        bill.append(orderSummary.getText());

        double tax = total * 0.18;
        double grandTotal = total + tax;
        double discount = 0;
        try {
            discount = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException e) {
            // Handle if discount is not a valid number
        }
        if (discount > 0) {
            grandTotal -= grandTotal * (discount / 100); // Apply discount in total
            bill.append("Discount Applied: " + discount + "%\n");
        }
        bill.append("\n--------------------------------------\n");
        bill.append("Tax (18% GST): ₹" + tax + "\n");
        bill.append("Total Amount: ₹" + grandTotal + "\n");
        bill.append("\nPayment Method: " + paymentMode + "\n");
        bill.append("\nThank you for visiting our Cafe!\n");
        bill.append("Cafe Name: Awesome Cafe\n");
        bill.append("Address: 123 Coffee Lane, Cafe City\n");
        bill.append("Phone: (123) 456-7890\n");
        bill.append("We hope to see you again!");

        billDetailsArea.setText(bill.toString());

        // Reset fields after order is confirmed
        total = 0;
        orderSummary.setText("");
        totalLabel.setText("Total: ₹0");
        paymentModeComboBox.setSelectedIndex(0);
        foodCategory.setSelectedIndex(0);
        itemMenu.setSelectedIndex(0);
        quantityField.setText("");
        discountField.setText(""); // Reset discount field
    }

    private void updateItemMenu() {
        String category = (String) foodCategory.getSelectedItem();
        itemMenu.removeAllItems();

        if (category.equals("Fries")) {
            itemMenu.addItem("Masala Fries - ₹50");
            itemMenu.addItem("Cheese Fries - ₹60");
            itemMenu.addItem("Chili Cheese Fries - ₹70");
            itemMenu.addItem("Sweet Potato Fries - ₹80");
            itemMenu.addItem("Loaded Fries - ₹90");
        } else if (category.equals("Burgers (Veg)")) {
            itemMenu.addItem("Veg Burger - ₹70");
            itemMenu.addItem("Paneer Burger - ₹80");
            itemMenu.addItem("Mushroom Burger - ₹90");
            itemMenu.addItem("Spicy Bean Burger - ₹85");
            itemMenu.addItem("Avocado Burger - ₹100");
        } else if (category.equals("Burgers (Non-Veg)")) {
            itemMenu.addItem("Chicken Burger - ₹90");
            itemMenu.addItem("Beef Burger - ₹120");
            itemMenu.addItem("BBQ Chicken Burger - ₹110");
            itemMenu.addItem("Spicy Chicken Burger - ₹100");
            itemMenu.addItem("Teriyaki Chicken Burger - ₹130");
        } else if (category.equals("Pizzas (Veg)")) {
            itemMenu.addItem("Veg Margherita - ₹150");
            itemMenu.addItem("Veg Supreme - ₹180");
            itemMenu.addItem("Veg Exotica - ₹200");
            itemMenu.addItem("Spinach & Ricotta Pizza - ₹190");
            itemMenu.addItem("Pesto Veg Pizza - ₹180");
        } else if (category.equals("Pizzas (Non-Veg)")) {
            itemMenu.addItem("Chicken Supreme - ₹200");
            itemMenu.addItem("BBQ Chicken Pizza - ₹250");
            itemMenu.addItem("Pepperoni Pizza - ₹300");
            itemMenu.addItem("Tandoori Chicken Pizza - ₹280");
            itemMenu.addItem("Seafood Pizza - ₹350");
        } else if (category.equals("Pasta")) {
            itemMenu.addItem("Veg Pasta - ₹90");
            itemMenu.addItem("Cheese Pasta - ₹100");
            itemMenu.addItem("Penne Arrabbiata - ₹110");
            itemMenu.addItem("Alfredo Pasta - ₹120");
            itemMenu.addItem("Pesto Pasta - ₹110");
        } else if (category.equals("Salads")) {
            itemMenu.addItem("Caesar Salad - ₹80");
            itemMenu.addItem("Greek Salad - ₹90");
            itemMenu.addItem("Quinoa Salad - ₹100");
            itemMenu.addItem("Caprese Salad - ₹90");
            itemMenu.addItem("Taco Salad - ₹100");
        } else if (category.equals("Desserts")) {
            itemMenu.addItem("Chocolate Cake - ₹60");
            itemMenu.addItem("Panna Cotta - ₹50");
            itemMenu.addItem("Tiramisu - ₹70");
            itemMenu.addItem("Ice Cream Sundae - ₹40");
            itemMenu.addItem("Fruit Tart - ₹65");
        } else if (category.equals("Beverages")) {
            itemMenu.addItem("Coffee - ₹40");
            itemMenu.addItem("Tea - ₹30");
            itemMenu.addItem("Lemonade - ₹50");
            itemMenu.addItem("Soft Drink - ₹40");
            itemMenu.addItem("Fruit Juice - ₹60");
        } else if (category.equals("Dips")) {
            itemMenu.addItem("Ketchup - ₹20");
            itemMenu.addItem("Mayonnaise - ₹30");
            itemMenu.addItem("BBQ Sauce - ₹25");
            itemMenu.addItem("Ranch Dressing - ₹30");
            itemMenu.addItem("Salsa - ₹35");
        }
    }

    private int getItemPrice(String item) {
        if (item.contains("Masala Fries")) {
            return 50;
        } else if (item.contains("Cheese Fries")) {
            return 60;
        } else if (item.contains("Chili Cheese Fries")) {
            return 70;
        } else if (item.contains("Sweet Potato Fries")) {
            return 80;
        } else if (item.contains("Loaded Fries")) {
            return 90;
        } else if (item.contains("Veg Burger")) {
            return 70;
        } else if (item.contains("Paneer Burger")) {
            return 80;
        } else if (item.contains("Mushroom Burger")) {
            return 90;
        } else if (item.contains("Spicy Bean Burger")) {
            return 85;
        } else if (item.contains("Avocado Burger")) {
            return 100;
        } else if (item.contains("Chicken Burger")) {
            return 90;
        } else if (item.contains("Beef Burger")) {
            return 120;
        } else if (item.contains("BBQ Chicken Burger")) {
            return 110;
        } else if (item.contains("Spicy Chicken Burger")) {
            return 100;
        } else if (item.contains("Teriyaki Chicken Burger")) {
            return 130;
        } else i