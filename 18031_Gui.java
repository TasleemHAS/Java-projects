import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PizzaShopWelcomePage {
    private static JPanel mainPanel;
    private static JFrame frame;
    private static double total = 0;
    private static JComboBox<String> foodCategory, itemMenu;
    private static JTextField quantityField;
    private static JTextArea orderSummary;
    private static JLabel totalLabel;
    private static String orderDetails = ""; // To store order details for the customer

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3307/pizza_shop";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        frame = new JFrame("Slice Heaven");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new CardLayout()); // Using CardLayout for multiple pages

        mainPanel = new JPanel(new CardLayout());
        mainPanel.add(createWelcomePage(), "WelcomePage");
        mainPanel.add(createOrderPage(), "OrderPage");
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Create Welcome Page
    private static JPanel createWelcomePage() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Slice Heaven");
        titleLabel.setFont(new Font("Brush Script MT", Font.BOLD, 50));
        titleLabel.setForeground(new Color(255, 153, 51));
        titleLabel.setBounds(30, 20, 300, 50);
        panel.add(titleLabel);

        JLabel sloganLabel = new JLabel("<html>Escape into a world of delicious pizza<br><span style='font-size:20px; color:black;'>where every slice is a treat.</span></html>");
        sloganLabel.setFont(new Font("Arial", Font.BOLD, 30));
        sloganLabel.setForeground(Color.BLACK);
        sloganLabel.setBounds(30, 100, 600, 100);
        panel.add(sloganLabel);

        JButton orderButton = new JButton("Order Now");
        orderButton.setFont(new Font("Arial", Font.BOLD, 18));
        orderButton.setBackground(new Color(255, 153, 51));
        orderButton.setBounds(650, 450, 120, 40);
        orderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        orderButton.addActionListener(e -> showOrderPage());
        panel.add(orderButton);

        return panel;
    }

    // Show Order Page
    private static void showOrderPage() {
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        cardLayout.show(mainPanel, "OrderPage");
    }

    // Create Order Page
    private static JPanel createOrderPage() {
        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(null);
        orderPanel.setBackground(Color.WHITE);

        JLabel orderTitle = new JLabel("Your Order is Just 15 Minutes Away!");
        orderTitle.setFont(new Font("Brush Script MT", Font.BOLD, 40));
        orderTitle.setForeground(new Color(255, 153, 51));
        orderTitle.setBounds(30, 30, 500, 50);
        orderPanel.add(orderTitle);

        JButton menuButtonOnOrderPage = new JButton("Menu");
        menuButtonOnOrderPage.setFont(new Font("Arial", Font.BOLD, 18));
        menuButtonOnOrderPage.setBackground(new Color(255, 204, 0));
        menuButtonOnOrderPage.setBounds(30, 250, 120, 40);
        menuButtonOnOrderPage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButtonOnOrderPage.addActionListener(e -> showMenuPage());
        orderPanel.add(menuButtonOnOrderPage);

        return orderPanel;
    }

    // Show Menu Page
    private static void showMenuPage() {
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        mainPanel.add(createMenuPage(), "MenuPage");
        cardLayout.show(mainPanel, "MenuPage");
    }

    // Create Menu Page
    private static JPanel createMenuPage() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(240, 248, 255));

        JLabel menuLabel = new JLabel("Menu", SwingConstants.CENTER);
        menuLabel.setFont(new Font("Arial", Font.BOLD, 22));
        menuLabel.setForeground(new Color(102, 51, 0));
        menuPanel.add(menuLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(255, 250, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        foodCategory = new JComboBox<>(new String[]{"Select Category", "Fries", "Pizzas (Veg)", "Pizzas (Non-Veg)"});
        itemMenu = new JComboBox<>();
        quantityField = new JTextField(5);
        orderSummary = new JTextArea(10, 30);
        orderSummary.setEditable(false);
        totalLabel = new JLabel("Total: ₹0", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(new Color(0, 128, 0));

        JButton addButton = new JButton("Add to Order");
        JButton generateBillButton = new JButton("Generate Bill");

        foodCategory.addActionListener(e -> updateItemMenu());
        addButton.addActionListener(e -> addToOrder());
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
        inputPanel.add(generateBillButton, gbc);

        menuPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel orderPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(orderSummary);
        orderPanel.add(scrollPane, BorderLayout.CENTER);
        orderPanel.add(totalLabel, BorderLayout.SOUTH);
        menuPanel.add(orderPanel, BorderLayout.SOUTH);

        return menuPanel;
    }

    // Update Item Menu based on Category
    private static void updateItemMenu() {
        String selectedCategory = (String) foodCategory.getSelectedItem();
        itemMenu.removeAllItems();
        itemMenu.addItem("Select Item");

        if ("Fries".equals(selectedCategory)) {
            itemMenu.addItem("Classic Fries");
            itemMenu.addItem("Cheese Fries");
        } else if ("Pizzas (Veg)".equals(selectedCategory)) {
            itemMenu.addItem("Margherita Pizza");
            itemMenu.addItem("Veggie Supreme Pizza");
        } else if ("Pizzas (Non-Veg)".equals(selectedCategory)) {
            itemMenu.addItem("Pepperoni Pizza");
            itemMenu.addItem("Chicken Tikka Pizza");
        }
    }

    // Add selected item to Order
    private static void addToOrder() {
        String category = (String) foodCategory.getSelectedItem();
        String item = (String) itemMenu.getSelectedItem();
        String quantityText = quantityField.getText();

        if ("Select Category".equals(category) || "Select Item".equals(item) || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select category, item, and enter quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            double price = getPrice(item);
            double itemTotal = price * quantity;

            orderDetails += item + " (" + quantity + "): ₹" + itemTotal + "\n";
            orderSummary.setText(orderDetails);
            total += itemTotal;
            totalLabel.setText("Total: ₹" + total);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number for quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Get price for selected item
    private static double getPrice(String item) {
        switch (item) {
            case "Classic Fries": return 100;
            case "Cheese Fries": return 120;
            case "Margherita Pizza": return 250;
            case "Veggie Supreme Pizza": return 300;
            case "Pepperoni Pizza": return 450;
            case "Chicken Tikka Pizza": return 480;
            default: return 0;
        }
    }

    // Generate Bill, display total, and save customer details
    private static void generateBill() {
        double finalTotal = total;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String insertOrderQuery = "INSERT INTO orders (order_details, total) VALUES (?, ?)";
            try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStatement.setString(1, orderDetails);
                orderStatement.setDouble(2, finalTotal);
                orderStatement.executeUpdate();

                ResultSet generatedKeys = orderStatement.getGeneratedKeys();
                int orderId = -1;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }

                JOptionPane.showMessageDialog(frame, "Order placed successfully!\nTotal Amount: ₹" + finalTotal, "Order Placed", JOptionPane.INFORMATION_MESSAGE);

                String customerName = JOptionPane.showInputDialog(frame, "Enter Customer Name:");
                String contactNumber = JOptionPane.showInputDialog(frame, "Enter Contact Number:");

                if (customerName != null && !customerName.isEmpty() && contactNumber != null && !contactNumber.isEmpty()) {
                    String insertCustomerQuery = "INSERT INTO customer (name, contact_number, order_id) VALUES (?, ?, ?)";
                    try (PreparedStatement customerStatement = connection.prepareStatement(insertCustomerQuery)) {
                        customerStatement.setString(1, customerName);
                        customerStatement.setString(2, contactNumber);
                        customerStatement.setInt(3, orderId);
                        customerStatement.executeUpdate();

                        JOptionPane.showMessageDialog(frame, "Customer details saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Customer details not provided. Skipping this step.", "Warning", JOptionPane.WARNING_MESSAGE);
                }

                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                cardLayout.show(mainPanel, "WelcomePage");

                total = 0;
                orderDetails = "";
                orderSummary.setText("");
                totalLabel.setText("Total: ₹0");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Failed to place order. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
} 