package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignupScreen extends JPanel {

    public SignupScreen(Main main) {
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245));

        JPanel signupBox = new JPanel();
        signupBox.setLayout(new BoxLayout(signupBox, BoxLayout.Y_AXIS));
        signupBox.setBackground(Color.WHITE);
        signupBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        signupBox.setPreferredSize(new Dimension(450, 650));

        JLabel logo = new JLabel(" Create Account", JLabel.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(new Color(24, 119, 242));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input fields
        JTextField nameField = createInputField();
        JTextField userField = createInputField();
        JPasswordField passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(350, 40));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        JTextField emailField = createInputField();

        // User type radio buttons
        JLabel typeLabel = new JLabel("I am a:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JRadioButton employerRadio = new JRadioButton("Employer");
        employerRadio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        employerRadio.setBackground(Color.WHITE);

        JRadioButton jobseekerRadio = new JRadioButton("Job Seeker");
        jobseekerRadio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobseekerRadio.setBackground(Color.WHITE);
        jobseekerRadio.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(employerRadio);
        group.add(jobseekerRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        radioPanel.setBackground(Color.WHITE);
        radioPanel.setMaximumSize(new Dimension(350, 40));
        radioPanel.add(jobseekerRadio);
        radioPanel.add(employerRadio);

        // Signup button
        JButton signupBtn = createStyledButton("Create Account", new Color(40, 167, 69));
        signupBtn.setMaximumSize(new Dimension(350, 45));
        signupBtn.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String email = emailField.getText().trim();
            String type = employerRadio.isSelected() ? "employer" : "jobseeker";

            // EMPTY FIELD CHECK
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // FULL NAME VALIDATION: letters + space, at least 2 words
            if (!fullName.matches("^[A-Za-z]+(\\s[A-Za-z]+)+$")) {
                JOptionPane.showMessageDialog(this,
                        "Enter a valid full name (letters only, first & last name).",
                        "Invalid Full Name",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // USERNAME VALIDATION: letters, numbers, underscore, 4-20 chars
            if (!username.matches("^[a-zA-Z0-9_]{4,20}$")) {
                JOptionPane.showMessageDialog(this,
                        "Username must be 4–20 characters (letters, numbers, underscore only).",
                        "Invalid Username",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // PASSWORD VALIDATION: min 6 chars
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this,
                        "Password must be at least 6 characters.",
                        "Weak Password",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // EMAIL VALIDATION: lowercase Gmail only
            if (!email.matches("^[a-z0-9+_.-]+@gmail\\.com$")) {
                JOptionPane.showMessageDialog(this,
                        "Email must be a valid Gmail address with lowercase letters only (example@gmail.com).",
                        "Invalid Email",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Check if username exists
                PreparedStatement checkStmt = con.prepareStatement("SELECT username FROM users WHERE username=?");
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    checkStmt.close();
                    return;
                }
                rs.close();
                checkStmt.close();

                // INSERT USER INTO DB
                String sql = "INSERT INTO users (username, full_name, password, email, type, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, fullName);
                    ps.setString(3, password);
                    ps.setString(4, email);
                    ps.setString(5, type);
                    ps.executeUpdate();
                }

                // Add to in-memory map
                Main.users.put(username, new UserData(
                        username,
                        password,
                        email,
                        type,
                        fullName,
                        "", "", "", "", "", "", ""
                ));

                JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // CLEAR FIELDS
                nameField.setText("");
                userField.setText("");
                passField.setText("");
                emailField.setText("");
                jobseekerRadio.setSelected(true);
                employerRadio.setSelected(false);

                main.switchScreen("login");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // LOGIN REDIRECT
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setBackground(Color.WHITE);
        JLabel loginLabel = new JLabel("Already have an account? ");
        JLabel loginLink = new JLabel("Login");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginLink.setForeground(new Color(24, 119, 242));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                main.switchScreen("login");
            }
        });
        loginPanel.add(loginLabel);
        loginPanel.add(loginLink);

        // ADD COMPONENTS
        signupBox.add(logo);
        signupBox.add(Box.createRigidArea(new Dimension(0, 20)));
        signupBox.add(createLabel("Full Name"));
        signupBox.add(Box.createRigidArea(new Dimension(0, 5)));
        signupBox.add(nameField);
        signupBox.add(Box.createRigidArea(new Dimension(0, 15)));
        signupBox.add(createLabel("Username"));
        signupBox.add(Box.createRigidArea(new Dimension(0, 5)));
        signupBox.add(userField);
        signupBox.add(Box.createRigidArea(new Dimension(0, 15)));
        signupBox.add(createLabel("Password"));
        signupBox.add(Box.createRigidArea(new Dimension(0, 5)));
        signupBox.add(passField);
        signupBox.add(Box.createRigidArea(new Dimension(0, 15)));
        signupBox.add(createLabel("Email"));
        signupBox.add(Box.createRigidArea(new Dimension(0, 5)));
        signupBox.add(emailField);
        signupBox.add(Box.createRigidArea(new Dimension(0, 20)));
        signupBox.add(typeLabel);
        signupBox.add(Box.createRigidArea(new Dimension(0, 10)));
        signupBox.add(radioPanel);
        signupBox.add(Box.createRigidArea(new Dimension(0, 25)));
        signupBox.add(signupBtn);
        signupBox.add(Box.createRigidArea(new Dimension(0, 15)));
        signupBox.add(loginPanel);

        add(signupBox);
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

    private JTextField createInputField() {
        JTextField field = new JTextField();
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
}
