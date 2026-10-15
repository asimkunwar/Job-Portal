package project;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployerApplicationScreen extends JPanel {
    private Main main;
    private JPanel applicationsPanel;
    private List<ApplicationData> allApplications = new ArrayList<>();
    private JLabel titleLabel;
    private JPanel titleWrapper; // To hold title + badge

    public EmployerApplicationScreen(Main main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));
        add(createNavbar(main), BorderLayout.NORTH);
        add(createLeftMenu(main), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel createCenterPanel() {
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(240, 242, 245));

        // Top panel with title and badge
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 242, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        titleWrapper = new JPanel(new BorderLayout());
        titleWrapper.setBackground(new Color(240, 242, 245));

        titleLabel = new JLabel("Job Applications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleWrapper.add(titleLabel, BorderLayout.WEST);

        topPanel.add(titleWrapper, BorderLayout.WEST);
        centerWrapper.add(topPanel, BorderLayout.NORTH);

        // Main applications panel
        JPanel mainCenter = new JPanel();
        mainCenter.setLayout(new BoxLayout(mainCenter, BoxLayout.Y_AXIS));
        mainCenter.setBackground(new Color(240, 242, 245));
        mainCenter.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        applicationsPanel = new JPanel();
        applicationsPanel.setLayout(new BoxLayout(applicationsPanel, BoxLayout.Y_AXIS));
        applicationsPanel.setBackground(new Color(240, 242, 245));

        JScrollPane scrollPane = new JScrollPane(applicationsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainCenter.add(scrollPane);

        centerWrapper.add(mainCenter, BorderLayout.CENTER);
        return centerWrapper;
    }

    public void refreshApplications() {
        if (main.currentUser == null || main.currentUser.isEmpty()) {
            showApplications(new ArrayList<>());
        } else {
            allApplications = fetchApplicationsForEmployer(main.currentUser);
            showApplications(allApplications);
        }
    }

    private List<ApplicationData> fetchApplicationsForEmployer(String employerUsername) {
        List<ApplicationData> list = new ArrayList<>();
        if (employerUsername == null || employerUsername.isEmpty()) return list;
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT a.id, a.job_id, a.username, u.full_name, " +
                    "j.title AS job_title, j.company, a.status, a.applied_on, " +
                    "IFNULL(a.resume_link, '') AS resume_link, " +
                    "IFNULL(a.email, '') AS email, " +
                    "IFNULL(a.phone, '') AS phone, " +
                    "IFNULL(sw.file_path, '') AS file_path, " +
                    "IFNULL(a.feedback, '') AS feedback " +
                    "FROM applications a " +
                    "JOIN jobs j ON a.job_id = j.id " +
                    "JOIN users u ON a.username = u.username " +
                    "LEFT JOIN submitted_work sw ON a.id = sw.application_id " +
                    "WHERE j.posted_by = ? " +
                    "ORDER BY sw.uploaded_on DESC, a.applied_on DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, employerUsername);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ApplicationData app = new ApplicationData(
                        rs.getInt("id"),
                        rs.getInt("job_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("job_title"),
                        rs.getString("company"),
                        rs.getString("status"),
                        rs.getTimestamp("applied_on"),
                        rs.getString("resume_link"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("file_path"),
                        rs.getString("feedback")
                );
                list.add(app);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
        return list;
    }

    private void showApplications(List<ApplicationData> apps) {
        applicationsPanel.removeAll();
        if (apps.isEmpty()) {
            JLabel noLabel = new JLabel("No applications yet.");
            noLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noLabel.setForeground(new Color(100, 100, 100));
            noLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            applicationsPanel.add(Box.createVerticalGlue());
            applicationsPanel.add(noLabel);
            applicationsPanel.add(Box.createVerticalGlue());
        } else {
            for (ApplicationData app : apps) {
                applicationsPanel.add(createApplicationCard(app));
                applicationsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        applicationsPanel.revalidate();
        applicationsPanel.repaint();
        updateResubmitBadge();
    }

    private void updateResubmitBadge() {
        int resubmitCount = 0;
        for (ApplicationData app : allApplications) {
            if ("Feedback".equalsIgnoreCase(app.getStatus()) &&
                    app.getFilePath() != null && !app.getFilePath().isEmpty()) {
                resubmitCount++;
            }
        }

        // Remove existing badge
        Component[] components = titleWrapper.getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && "resubmitBadge".equals(c.getName())) {
                titleWrapper.remove(c);
            }
        }

        if (resubmitCount > 0) {
            JLabel badge = new JLabel(" " + resubmitCount + " ");
            badge.setName("resubmitBadge");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setForeground(Color.WHITE);
            badge.setBackground(new Color(220, 53, 69));
            badge.setOpaque(true);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            titleWrapper.add(badge, BorderLayout.EAST);
        }

        titleWrapper.revalidate();
        titleWrapper.repaint();
    }

    private JPanel createApplicationCard(ApplicationData app) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Applicant info
        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(app.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel jobLabel = new JLabel(app.getJobTitle());
        jobLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobLabel.setForeground(new Color(100, 100, 100));
        JLabel dateLabel = new JLabel("Applied: " + app.getAppliedDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(120, 120, 120));
        leftInfo.add(nameLabel);
        leftInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        leftInfo.add(jobLabel);
        leftInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        leftInfo.add(dateLabel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        // View Profile Button - FULLY IMPLEMENTED
        JButton viewProfileBtn = new JButton("View Profile");
        viewProfileBtn.setPreferredSize(new Dimension(120, 35));
        viewProfileBtn.setBackground(new Color(108, 117, 125));
        viewProfileBtn.setForeground(Color.WHITE);
        viewProfileBtn.setFocusPainted(false);
        viewProfileBtn.addActionListener(e -> {
            JDialog profileDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Applicant Profile", true);
            profileDialog.setSize(500, 550);
            profileDialog.setLocationRelativeTo(this);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=?");
                ps.setString(1, app.getUsername());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JLabel picLabel = new JLabel();
                    picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    String picPath = rs.getString("profile_pic");
                    if (picPath != null && !picPath.isEmpty()) {
                        picLabel.setIcon(new ImageIcon(new ImageIcon(picPath)
                                .getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                    } else {
                        picLabel.setText("👤");
                        picLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
                        picLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    panel.add(picLabel);
                    panel.add(Box.createRigidArea(new Dimension(0, 15)));

                    JLabel nameLbl = new JLabel("Full Name: " + rs.getString("full_name"));
                    nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    panel.add(nameLbl);
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));

                    JLabel userLbl = new JLabel("Username: " + rs.getString("username"));
                    userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    panel.add(userLbl);
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));

                    JLabel emailLbl = new JLabel("Email: " + rs.getString("email"));
                    emailLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    panel.add(emailLbl);
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));

                    JLabel phoneLbl = new JLabel("Phone: " + (rs.getString("phone") != null ? rs.getString("phone") : "-"));
                    phoneLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    panel.add(phoneLbl);
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));

                    JLabel locLbl = new JLabel("Location: " + (rs.getString("location") != null ? rs.getString("location") : "-"));
                    locLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    panel.add(locLbl);
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));

                    JLabel bioLbl = new JLabel("About: ");
                    bioLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    panel.add(bioLbl);

                    JTextArea bioArea = new JTextArea(rs.getString("bio") != null ? rs.getString("bio") : "-");
                    bioArea.setLineWrap(true);
                    bioArea.setWrapStyleWord(true);
                    bioArea.setEditable(false);
                    bioArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    bioArea.setBackground(new Color(245, 245, 245));
                    bioArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    JScrollPane bioScroll = new JScrollPane(bioArea);
                    bioScroll.setPreferredSize(new Dimension(440, 80));
                    bioScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                    panel.add(bioScroll);
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));

                    JLabel resumeLbl = new JLabel("Resume:");
                    resumeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    panel.add(resumeLbl);
                    panel.add(Box.createRigidArea(new Dimension(0, 5)));

                    JPanel resumePanel = new JPanel(new BorderLayout(5, 0));
                    JTextField resumeField = new JTextField(rs.getString("resume_link") != null ? rs.getString("resume_link") : "");
                    resumeField.setEditable(false);
                    resumePanel.add(resumeField, BorderLayout.CENTER);

                    JButton copyBtn = new JButton("Copy");
                    copyBtn.addActionListener(ev -> {
                        StringSelection selection = new StringSelection(resumeField.getText());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                        JOptionPane.showMessageDialog(profileDialog, "Resume link copied to clipboard!");
                    });
                    resumePanel.add(copyBtn, BorderLayout.EAST);
                    panel.add(resumePanel);

                    String workFile = app.getFilePath();
                    if (workFile != null && !workFile.isEmpty()) {
                        panel.add(Box.createRigidArea(new Dimension(0, 10)));
                        JButton copyWorkBtn = new JButton("Copy Work");
                        copyWorkBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                        copyWorkBtn.setMaximumSize(new Dimension(150, 35));
                        copyWorkBtn.setBackground(new Color(108, 117, 125));
                        copyWorkBtn.setForeground(Color.WHITE);
                        copyWorkBtn.setFocusPainted(false);
                        copyWorkBtn.addActionListener(ev -> {
                            StringSelection selection = new StringSelection(workFile);
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                            JOptionPane.showMessageDialog(profileDialog, "Work file path copied to clipboard!");
                        });
                        panel.add(copyWorkBtn);
                    }
                }
                rs.close();
                ps.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
            }

            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBorder(null);
            profileDialog.add(scroll);
            profileDialog.setVisible(true);
        });
        buttonPanel.add(viewProfileBtn);

        // Status-based buttons
        if ("Pending".equalsIgnoreCase(app.getStatus())) {
            JButton acceptBtn = new JButton("Accept");
            acceptBtn.setPreferredSize(new Dimension(100, 35));
            acceptBtn.setBackground(new Color(40, 167, 69));
            acceptBtn.setForeground(Color.WHITE);
            acceptBtn.setFocusPainted(false);
            acceptBtn.addActionListener(e -> updateApplicationStatus(app, "Accepted"));
            JButton rejectBtn = new JButton("Reject");
            rejectBtn.setPreferredSize(new Dimension(100, 35));
            rejectBtn.setBackground(new Color(220, 53, 69));
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setFocusPainted(false);
            rejectBtn.addActionListener(e -> updateApplicationStatus(app, "Rejected"));
            buttonPanel.add(acceptBtn);
            buttonPanel.add(rejectBtn);
        } else if ("Accepted".equalsIgnoreCase(app.getStatus())) {
            if (app.getFilePath() != null && !app.getFilePath().isEmpty()) {
                JButton checkWorkBtn = new JButton("Check Work");
                checkWorkBtn.setPreferredSize(new Dimension(120, 35));
                checkWorkBtn.setBackground(new Color(24, 119, 242));
                checkWorkBtn.setForeground(Color.WHITE);
                checkWorkBtn.setFocusPainted(false);
                checkWorkBtn.addActionListener(ev -> {
                    StringSelection selection = new StringSelection(app.getFilePath());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                    JOptionPane.showMessageDialog(this, "File path copied to clipboard!");
                });
                JButton acceptWorkBtn = new JButton("Accept Work");
                acceptWorkBtn.setPreferredSize(new Dimension(120, 35));
                acceptWorkBtn.setBackground(new Color(40, 167, 69));
                acceptWorkBtn.setForeground(Color.WHITE);
                acceptWorkBtn.setFocusPainted(false);
                acceptWorkBtn.addActionListener(ev -> updateApplicationStatus(app, "Completed"));
                JButton feedbackBtn = new JButton("Feedback");
                feedbackBtn.setPreferredSize(new Dimension(100, 35));
                feedbackBtn.setBackground(new Color(108, 117, 125));
                feedbackBtn.setForeground(Color.WHITE);
                feedbackBtn.setFocusPainted(false);
                feedbackBtn.addActionListener(ev -> showFeedbackDialog(app));
                buttonPanel.add(checkWorkBtn);
                buttonPanel.add(acceptWorkBtn);
                buttonPanel.add(feedbackBtn);
            } else {
                JLabel statusLabel = new JLabel("Accepted");
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                statusLabel.setForeground(new Color(40, 167, 69));
                buttonPanel.add(statusLabel);
            }
        } else if ("Feedback".equalsIgnoreCase(app.getStatus())) {
            JLabel statusLabel = new JLabel("Feedback Sent");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            statusLabel.setForeground(new Color(23, 162, 184));
            buttonPanel.add(statusLabel);

            if (app.getFilePath() != null && !app.getFilePath().isEmpty()) {
                JButton viewWorkBtn = new JButton("View Latest Work");
                viewWorkBtn.setPreferredSize(new Dimension(140, 35));
                viewWorkBtn.setBackground(new Color(24, 119, 242));
                viewWorkBtn.setForeground(Color.WHITE);
                viewWorkBtn.addActionListener(ev -> {
                    StringSelection selection = new StringSelection(app.getFilePath());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                    JOptionPane.showMessageDialog(this, "Latest file path copied!");
                });
                buttonPanel.add(viewWorkBtn);

                JButton acceptBtn = new JButton("Accept Work");
                acceptBtn.setPreferredSize(new Dimension(120, 35));
                acceptBtn.setBackground(new Color(40, 167, 69));
                acceptBtn.setForeground(Color.WHITE);
                acceptBtn.addActionListener(ev -> updateApplicationStatus(app, "Completed"));
                buttonPanel.add(acceptBtn);
            }
        } else {
            if ("Completed".equalsIgnoreCase(app.getStatus())) {
                if (app.getFilePath() != null && !app.getFilePath().isEmpty()) {
                    JButton viewWorkBtn = new JButton("View Work");
                    viewWorkBtn.setPreferredSize(new Dimension(120, 35));
                    viewWorkBtn.setBackground(new Color(24, 119, 242));
                    viewWorkBtn.setForeground(Color.WHITE);
                    viewWorkBtn.setFocusPainted(false);
                    viewWorkBtn.addActionListener(ev -> {
                        StringSelection selection = new StringSelection(app.getFilePath());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                        JOptionPane.showMessageDialog(this, "File path copied to clipboard!");
                    });
                    buttonPanel.add(viewWorkBtn);
                }
                JLabel statusLabel = new JLabel("Completed");
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                statusLabel.setForeground(new Color(40, 167, 69));
                buttonPanel.add(statusLabel);
            } else {
                JLabel statusLabel = new JLabel(app.getStatus());
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                statusLabel.setForeground(new Color(220, 53, 69));
                buttonPanel.add(statusLabel);
            }
        }

        card.add(leftInfo, BorderLayout.WEST);
        card.add(buttonPanel, BorderLayout.EAST);
        return card;
    }

    private void showFeedbackDialog(ApplicationData app) {
        JDialog feedbackDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Provide Feedback", true);
        feedbackDialog.setSize(500, 300);
        feedbackDialog.setLocationRelativeTo(this);
        feedbackDialog.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Enter feedback for " + app.getFullName() + ":");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea feedbackArea = new JTextArea(5, 40);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> feedbackDialog.dispose());

        JButton submitBtn = new JButton("Submit");
        submitBtn.setPreferredSize(new Dimension(100, 35));
        submitBtn.setBackground(new Color(40, 167, 69));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> {
            String feedback = feedbackArea.getText().trim();
            if (!feedback.isEmpty()) {
                if (updateApplicationStatus(app, "Feedback", feedback)) {
                    JOptionPane.showMessageDialog(feedbackDialog, "Feedback sent! Applicant can now view and resubmit.");
                    refreshApplications();
                } else {
                    JOptionPane.showMessageDialog(feedbackDialog, "Failed to send feedback.");
                }
                feedbackDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(feedbackDialog, "Please enter feedback before submitting.");
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(submitBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        feedbackDialog.add(mainPanel);
        feedbackDialog.setVisible(true);
    }

    private boolean updateApplicationStatus(ApplicationData app, String status, String feedback) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE applications SET status=?, feedback=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, feedback != null ? feedback : "");
            ps.setInt(3, app.getId());
            ps.executeUpdate();
            ps.close();

            // ✅ REFRESH ALL SCREENS AFTER STATUS CHANGE
            main.refreshAllScreens();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating application: " + ex.getMessage());
            return false;
        }
    }

    private boolean updateApplicationStatus(ApplicationData app, String status) {
        return updateApplicationStatus(app, status, null);
    }

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

    private JButton createMenuButton(String text){
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        btn.setPreferredSize(new Dimension(190,42));
        btn.setMaximumSize(new Dimension(190,42));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50,50,50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent e){ btn.setBackground(new Color(230,235,240));}
            public void mouseExited(java.awt.event.MouseEvent e){ btn.setBackground(Color.WHITE);}
        });
        return btn;
    }

}