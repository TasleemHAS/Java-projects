import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;

public class CafeOrderingSystem {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextArea orderSummary;
    private int total = 0;
    private String customerName, customerContact, customerEmail;
    private Connection connection;

    private JComboBox<String> foodCategory;
    private JComboBox<String> itemMenu;
    private JTextField quantityField;
    private JTextField customerNameField;
    private JTextField contactField;
    private JLabel totalLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CafeOrderingSystem::new);
    }

    public CafeOrderingSystem() {
        frame = new JFrame("Cafe Ordering System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createWelcomePage(), "WelcomePage");
        mainPanel.add(createLoginPage(), "LoginPage");
        mainPanel.add(createMenuPage(), "MenuPage");
        mainPanel.add(createBillingPage(), "BillingPage");

        frame.add(mainPanel);
        frame.setVisible(true);

        cardLayout.show(mainPanel, "WelcomePage");

        // Connect to database
        connectToDatabase();
    }

    private JPanel createWelcomePage() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to Our Cafe!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 30));

        JButton nextButton = new JButton("Continue");
        nextButton.addActionListener(e -> cardLayout.show(mainPanel, "LoginPage"));

        panel.add(welcomeLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLoginPage() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel contactLabel = new JLabel("Contact Number:");
        JTextField contactField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            customerName = nameField.getText().trim();
            customerContact = contactField.getText().trim();
            customerEmail = emailField.getText().trim();

            if (customerName.isEmpty() || customerContact.isEmpty() || customerEmail.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all details!", "Error", JOptionPane.WARNING_MESSAGE);
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
        panel.add(new JLabel());  // Empty Cell
        panel.add(submitButton);

        return panel;
    }

    private JPanel createMenuPage() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        JLabel menuLabel = new JLabel("Menu", SwingConstants.CENTER);
        menuLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Panel for ordering section
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(240, 248, 255)); // Alice Blue
        inputPanel.setBorder(BorderFactory.createTitledBorder("Order Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        foodCategory = new JComboBox<>(new String[]{
                "Select Category", "Fries", "Burgers (Veg)", "Burgers (Non-Veg)",
                "Pizzas (Veg)", "Pizzas (Non-Veg)", "Pasta", "Salads"
        });
        itemMenu = new JComboBox<>();
        quantityField = new JTextField(5);
        orderSummary = new JTextArea(10, 30);
        orderSummary.setEditable(false);
        totalLabel = new JLabel("Total: ₹0", JLabel.CENTER);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 128, 0)); // Green

        JButton addButton = new JButton("Add to Order");
        JButton generateBillButton = new JButton("Generate Bill");
        JButton saveOrderButton = new JButton("Save Order");

        // Actions for buttons
        foodCategory.addActionListener(e -> updateItemMenu());
        addButton.addActionListener(e -> addToOrder());
        generateBillButton.addActionListener(e -> generateBill());
        saveOrderButton.addActionListener(e -> saveOrder());

        // Add to input panel
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
        inputPanel.add(generateBillButton, gbc);
        gbc.gridx = 2;
        inputPanel.add(saveOrderButton, gbc);

        menuPanel.add(inputPanel, BorderLayout.CENTER);

        // Order summary panel
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBackground(new Color(255, 250, 240)); // Floral White
        orderPanel.setBorder(BorderFactory.createTitledBorder("Order Summary"));

        JScrollPane scrollPane = new JScrollPane(orderSummary);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        orderPanel.add(scrollPane, BorderLayout.CENTER);
        orderPanel.add(totalLabel, BorderLayout.SOUTH);
        menuPanel.add(orderPanel, BorderLayout.SOUTH);

        return menuPanel;
    }

    private JPanel createBillingPage() {
        JPanel billingPanel = new JPanel(new BorderLayout());
        JLabel billingLabel = new JLabel("Billing Details", SwingConstants.CENTER);
        billingLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Get current date and time
        String dateTime = LocalDateTime.now().toString();
        JLabel dateLabel = new JLabel("Date/Time: " + dateTime);

        // Display customer info
        JLabel customerInfo = new JLabel("Customer: " + customerName + " | " + customerContact + " | " + customerEmail);

        // Order details summary
        JScrollPane scrollPane = new JScrollPane(orderSummary);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel billingDetailsPanel = new JPanel(new GridLayout(4, 1));
        billingDetailsPanel.add(customerInfo);
        billingDetailsPanel.add(dateLabel);
        billingDetailsPanel.add(scrollPane);
        billingDetailsPanel.add(totalLabel);

        billingPanel.add(billingLabel, BorderLayout.NORTH);
        billingPanel.add(billingDetailsPanel, BorderLayout.CENTER);

        return billingPanel;
    }

    private void updateItemMenu() {
        itemMenu.removeAllItems();
        String selectedCategory = (String) foodCategory.getSelectedItem();

        if (selectedCategory.equals("Fries")) {
            itemMenu.addItem("Masala Fries - ₹80");
            itemMenu.addItem("Cheese Fries - ₹120");
            itemMenu.addItem("Garlic Fries - ₹100");
            itemMenu.addItem("Classic Fries - ₹90");
            itemMenu.addItem("Chili Fries - ₹130");
        } else if (selectedCategory.equals("Burgers (Veg)")) {
            itemMenu.addItem("Veg Cheese Burger - ₹150");
            itemMenu.addItem("Paneer Burger - ₹130");
            itemMenu.addItem("Mushroom Burger - ₹160");
            itemMenu.addItem("Veg Deluxe Burger - ₹180");
            itemMenu.addItem("Veg Mexican Burger - ₹170");
        } else if (selectedCategory.equals("Burgers (Non-Veg)")) {
            itemMenu.addItem("Chicken Cheese Burger - ₹200");
            itemMenu.addItem("Chicken Burger - ₹180");
            itemMenu.addItem("Chicken Tandoori Burger - ₹220");
            itemMenu.addItem("BBQ Chicken Burger - ₹250");
            itemMenu.addItem("Spicy Chicken Burger - ₹230");
        } else if (selectedCategory.equals("Pizzas (Veg)")) {
            itemMenu.addItem("Margherita Pizza - ₹200");
            itemMenu.addItem("Veg Supreme Pizza - ₹250");
            itemMenu.addItem("Paneer Tikka Pizza - ₹270");
            itemMenu.addItem("Veg Extravaganza Pizza - ₹300");
            itemMenu.addItem("Cheese Burst Pizza - ₹320");
      } else if (selectedCategory.equals("Pizzas (Non-Veg)")) {
            itemMenu.addItem("Chicken Tikka Pizza - ₹280");
            itemMenu.addItem("Chicken Supreme Pizza - ₹320");
            itemMenu.addItem("BBQ Chicken Pizza - ₹350");
            itemMenu.addItem("Pepperoni Pizza - ₹330");
            itemMenu.addItem("Chicken Sausage Pizza - ₹300");
        } else if (selectedCategory.equals("Pasta")) {
            itemMenu.addItem("Veg Pasta - ₹180");
            itemMenu.addItem("Penne Arrabiata - ₹220");
            itemMenu.addItem("Pasta Alfredo - ₹250");
            itemMenu.addItem("Pasta Primavera - ₹270");
            itemMenu.addItem("Spaghetti Aglio Olio - ₹230");
        } else if (selectedCategory.equals("Salads")) {
            itemMenu.addItem("Greek Salad - ₹150");
            itemMenu.addItem("Caesar Salad - ₹180");
            itemMenu.addItem("Kale Salad - ₹200");
            itemMenu.addItem("Cucumber Salad - ₹120");
            itemMenu.addItem("Quinoa Salad - ₹220");
        }
    }

    private void addToOrder() {
        String selectedItem = (String) itemMenu.getSelectedItem();
        String quantityText = quantityField.getText().trim();

        if (selectedItem != null && !selectedItem.equals("Select Item") && !quantityText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityText);
                int itemPrice = Integer.parseInt(selectedItem.split("₹")[1].split(" ")[0].trim());
                int itemTotal = itemPrice * quantity;

                orderSummary.append(selectedItem + " x " + quantity + " = ₹" + itemTotal + "\n");
                total += itemTotal;
                totalLabel.setText("Total: ₹" + total);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a valid item and quantity.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void generateBill() {
        cardLayout.show(mainPanel, "BillingPage");
    }

    private void saveOrder() {
        try {
            String sql = "INSERT INTO orders (customer_name, contact, email, order_details, total, order_date) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, customerName);
            statement.setString(2, customerContact);
            statement.setString(3, customerEmail);
            statement.setString(4, orderSummary.getText());
            statement.setInt(5, total);
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(frame, "Order saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error saving order to database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void connectToDatabase() {
        try {
            // Database connection (replace with your actual database details)
            String url = "jdbc:mysql://localhost:3306/cafe";
            String user = "root";
            String password = "password";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
