package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class EmployerSettingScreen extends JPanel {

    private Main main;

    private JTextField nameField, emailField, phoneField, companyField, websiteField;
    private JLabel logoLabel;
    private File selectedLogoFile = null;

    public EmployerSettingScreen(Main main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Navbar
        add(createNavbar(main), BorderLayout.NORTH);

        // Left menu
        add(createLeftMenu(main), BorderLayout.WEST);

        // Center panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 242, 245));

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        box.setPreferredSize(new Dimension(700, 750));

        JLabel title = new JLabel(" Employer Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(24, 119, 242));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(title);
        box.add(Box.createRigidArea(new Dimension(0, 30)));

        // Company Logo
        logoLabel = new JLabel("🏢", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        logoLabel.setOpaque(true);
        logoLabel.setBackground(new Color(230, 235, 240));
        logoLabel.setPreferredSize(new Dimension(120, 120));
        logoLabel.setMaximumSize(new Dimension(120, 120));
        logoLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton uploadLogoBtn = new JButton("Upload Company Logo");
        uploadLogoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadLogoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedLogoFile = chooser.getSelectedFile();
                logoLabel.setIcon(new ImageIcon(
                        new ImageIcon(selectedLogoFile.getAbsolutePath())
                                .getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)
                ));
                logoLabel.setText("");
            }
        });

        box.add(logoLabel);
        box.add(Box.createRigidArea(new Dimension(0, 10)));
        box.add(uploadLogoBtn);
        box.add(Box.createRigidArea(new Dimension(0, 30)));

        // Full Name (auto, non-editable)
        box.add(createLabel("Full Name"));
        nameField = createInputField();
        nameField.setEditable(false);
        box.add(nameField);
        box.add(Box.createRigidArea(new Dimension(0, 15)));

        // Email (auto, non-editable)
        box.add(createLabel("Email Address"));
        emailField = createInputField();
        emailField.setEditable(false);
        box.add(emailField);
        box.add(Box.createRigidArea(new Dimension(0, 15)));

        // Phone
        box.add(createLabel("Phone Number"));
        phoneField = createInputField();
        box.add(phoneField);
        box.add(Box.createRigidArea(new Dimension(0, 15)));

        // Company
        box.add(createLabel("Company Name"));
        companyField = createInputField();
        box.add(companyField);
        box.add(Box.createRigidArea(new Dimension(0, 15)));

        // Website
        box.add(createLabel("Company Website"));
        websiteField = createInputField();
        box.add(websiteField);
        box.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton saveBtn = new JButton("💾 Save Changes");
        saveBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> saveEmployerData());

        box.add(saveBtn);

        centerPanel.add(box);
        add(centerPanel, BorderLayout.CENTER);
    }

    // 🔄 CALLED WHEN SCREEN OPENS
    public void refresh() {
        loadEmployerData();
    }

    // 🔽 FETCH DATA FROM DB
    private void loadEmployerData() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT full_name, email, phone, company, website, company_logo FROM users WHERE username=?"
            );
            ps.setString(1, main.currentUser);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                companyField.setText(rs.getString("company"));
                websiteField.setText(rs.getString("website"));

                String logo = rs.getString("company_logo");
                if (logo != null && !logo.isEmpty()) {
                    logoLabel.setIcon(new ImageIcon(
                            new ImageIcon(logo)
                                    .getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)
                    ));
                    logoLabel.setText("");
                }
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 💾 SAVE TO DB
    private void saveEmployerData() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET phone=?, company=?, website=?, company_logo=? WHERE username=?"
            );
            ps.setString(1, phoneField.getText().trim());
            ps.setString(2, companyField.getText().trim());
            ps.setString(3, websiteField.getText().trim());
            ps.setString(4, selectedLogoFile != null ? selectedLogoFile.getAbsolutePath() : null);
            ps.setString(5, main.currentUser);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Employer profile updated successfully!");

            // ✅ REFRESH EMPLOYER DATA
            refresh();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving profile: " + e.getMessage());
        }
    }

    // ================= UI HELPERS =================

    private JTextField createInputField() {
        JTextField field = new JTextField(20);
        field.setMaximumSize(new Dimension(600, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ===== NAVBAR =====
    private JPanel createNavbar(Main main) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(100, 60));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftTop.setBackground(Color.WHITE);
        JLabel logo = new JLabel("💼 JobPortal");
        logo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));

        logo.setForeground(new Color(24, 119, 242));
        leftTop.add(logo);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        rightTop.setBackground(Color.WHITE);
        JLabel user = new JLabel(main.currentUser + " (Employer)");
        user.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rightTop.add(user);

        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);
        return topBar;
    }
    private JPanel createLeftMenu(Main main) {
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(220, 100));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        leftPanel.setBackground(new Color(240, 242, 245));

        String[] items = {"🏠 Dashboard", "➕ Post New Job", "💼 My Job Posts", "📊 Applications", "⚙ Settings"};

        for (String item : items) {
            JButton btn = createMenuButton(item);

            // Add action listeners for each menu item
            if (item.contains("Dashboard")) {
                btn.addActionListener(e -> main.switchScreen("employer"));
            } else if (item.contains("Post New Job")) {
                btn.addActionListener(e -> {
                    // Clear form only when posting new job
                    if (main.postjobScreen != null) {
                        main.postjobScreen.setJobForEdit(null);
                    }
                    main.switchScreen("postjob");
                });
            } else if (item.contains("My Job Posts")) {
                btn.addActionListener(e -> main.switchScreen("myjobposts"));
            } else if (item.contains("Applications")) {
                btn.addActionListener(e -> main.switchScreen("employerapplications"));
            } else if (item.contains("Settings")) {
                btn.addActionListener(e -> main.switchScreen("employersettings"));
            }

            leftPanel.add(btn);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        leftPanel.add(Box.createVerticalGlue());

        JButton btnLogout = createMenuButton("🚪 Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                main.switchScreen("login");
                main.currentUser = "";
                main.userType = "";
            }
        });
        leftPanel.add(btnLogout);

        return leftPanel;
    }


    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        btn.setPreferredSize(new Dimension(190, 42));
        btn.setMaximumSize(new Dimension(190, 42));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 235, 240));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }
}
