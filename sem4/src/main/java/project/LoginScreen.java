package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginScreen extends JPanel {

    private JTextField userField;
    private JPasswordField passField;

    public LoginScreen(Main main) {
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245));

        JPanel loginBox = new JPanel();
        loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.Y_AXIS));
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        loginBox.setPreferredSize(new Dimension(450, 400));

        JLabel logo = new JLabel(" JobPortal", JLabel.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(new Color(24, 119, 242));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome Back!", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 100, 100));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel userLabel = createLabel("Username");
        userField = createInputField();

        // Password
        JLabel passLabel = createLabel("Password");
        passField = new JPasswordField(20);
        passField.setMaximumSize(new Dimension(350, 40));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Login button
        JButton loginBtn = createStyledButton("Login", new Color(24, 119, 242));
        loginBtn.setMaximumSize(new Dimension(350, 45));
        loginBtn.addActionListener(e -> loginUser(main));

        // Signup link
        JPanel signupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        signupPanel.setBackground(Color.WHITE);
        JLabel signupLabel = new JLabel("Don't have an account? ");
        JLabel signupLink = new JLabel("Sign Up");
        signupLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signupLink.setForeground(new Color(24, 119, 242));
        signupLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                main.switchScreen("signup");
                clearFields();
            }
        });
        signupPanel.add(signupLabel);
        signupPanel.add(signupLink);

        // Add components to loginBox
        loginBox.add(logo);
        loginBox.add(Box.createRigidArea(new Dimension(0, 10)));
        loginBox.add(subtitle);
        loginBox.add(Box.createRigidArea(new Dimension(0, 30)));
        loginBox.add(userLabel);
        loginBox.add(Box.createRigidArea(new Dimension(0, 5)));
        loginBox.add(userField);
        loginBox.add(Box.createRigidArea(new Dimension(0, 15)));
        loginBox.add(passLabel);
        loginBox.add(Box.createRigidArea(new Dimension(0, 5)));
        loginBox.add(passField);
        loginBox.add(Box.createRigidArea(new Dimension(0, 30)));
        loginBox.add(loginBtn);
        loginBox.add(Box.createRigidArea(new Dimension(0, 20)));
        loginBox.add(signupPanel);

        add(loginBox);
    }

    private void loginUser(Main main) {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                main.currentUser = rs.getString("username");
                main.userType = rs.getString("type");

                // Load full user data into Main.users (updated constructor with username first)
                UserData userData = new UserData(
                        rs.getString("username"),   // username added first
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("type"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("location"),
                        rs.getString("bio"),
                        rs.getString("resume_link"),
                        rs.getString("profile_pic"),
                        rs.getString("company"),
                        rs.getString("website")
                );
                Main.users.put(username, userData);

                JOptionPane.showMessageDialog(this, "Login successful!");
                clearFields();

                // Switch to dashboard
                if (main.userType.equals("employer")) {
                    main.switchScreen("employer");
                } else {
                    main.switchScreen("jobseeker");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createInputField() {
        JTextField field = new JTextField(20);
        field.setMaximumSize(new Dimension(350, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    public void clearFields() {
        userField.setText("");
        passField.setText("");
    }
}