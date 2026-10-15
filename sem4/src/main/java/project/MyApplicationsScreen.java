package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;

public class MyApplicationsScreen extends JPanel {

    private Main main;
    private JPanel applicationsPanel;
    private List<ApplicationData> allApplications = new ArrayList<>();
    private JLabel totalLabel, pendingLabel, acceptedLabel, rejectedLabel, feedbackLabel, completedLabel;
    private JButton btnFeedback; // Feedback tab button

    // NETWORK FOLDER PATH – replace with your PC's IPv4 and shared folder
    private static final String NETWORK_FOLDER = "\\\\LAPTOP-4JB7UUEI\\JobPortalUploads\\";

    public MyApplicationsScreen(Main main) {
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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 242, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        JLabel welcomeLabel = new JLabel("My Applications 📋");
        welcomeLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        centerWrapper.add(topPanel, BorderLayout.NORTH);

        JPanel mainCenter = new JPanel();
        mainCenter.setLayout(new BoxLayout(mainCenter, BoxLayout.Y_AXIS));
        mainCenter.setBackground(new Color(240, 242, 245));
        mainCenter.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        tabsPanel.setBackground(new Color(240, 242, 245));

        JButton btnPending = createFilterButton("Pending");
        JButton btnAccepted = createFilterButton("Accepted");

        // Feedback button with badge
        btnFeedback = new JButton();
        btnFeedback.setLayout(new BorderLayout());
        btnFeedback.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFeedback.setBackground(new Color(24, 119, 242));
        btnFeedback.setForeground(Color.WHITE);
        btnFeedback.setFocusPainted(false);
        btnFeedback.setPreferredSize(new Dimension(110, 35));
        JLabel feedbackText = new JLabel("Feedback", SwingConstants.CENTER);
        feedbackText.setForeground(Color.WHITE);
        feedbackText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFeedback.add(feedbackText, BorderLayout.CENTER);

        JButton btnCompleted = createFilterButton("Completed");
        JButton btnRejected = createFilterButton("Rejected");

        tabsPanel.add(btnPending);
        tabsPanel.add(btnAccepted);
        tabsPanel.add(btnFeedback);
        tabsPanel.add(btnCompleted);
        tabsPanel.add(btnRejected);
        mainCenter.add(tabsPanel);

        applicationsPanel = new JPanel();
        applicationsPanel.setLayout(new BoxLayout(applicationsPanel, BoxLayout.Y_AXIS));
        applicationsPanel.setBackground(new Color(240, 242, 245));
        applicationsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(applicationsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        mainCenter.add(scrollPane);

        totalLabel = new JLabel("0");
        pendingLabel = new JLabel("0");
        acceptedLabel = new JLabel("0");
        rejectedLabel = new JLabel("0");
        feedbackLabel = new JLabel("0");
        completedLabel = new JLabel("0");

        JPanel statsPanel = createStatsPanel();
        centerWrapper.add(statsPanel, BorderLayout.EAST);

        allApplications = ApplicationDAO.getApplicationsBySeeker(main.currentUser);
        showApplications(filterByStatusForTabs(allApplications, "Pending"));

        btnPending.addActionListener(e -> showApplications(filterByStatusForTabs(allApplications, "Pending")));
        btnAccepted.addActionListener(e -> showApplications(filterByStatusForTabs(allApplications, "Accepted")));
        btnFeedback.addActionListener(e -> showApplications(filterByStatusForTabs(allApplications, "Feedback")));
        btnCompleted.addActionListener(e -> showApplications(filterByStatusForTabs(allApplications, "Completed")));
        btnRejected.addActionListener(e -> showApplications(filterByStatusForTabs(allApplications, "Rejected")));

        centerWrapper.add(mainCenter, BorderLayout.CENTER);
        return centerWrapper;
    }

    private JButton createFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(24, 119, 242));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 35));
        return btn;
    }

    private JPanel createStatsPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(300, 100));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        rightPanel.setBackground(new Color(240, 242, 245));

        JLabel statsTitle = new JLabel("📊 Quick Stats");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(statsTitle);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        rightPanel.add(createStatBox("Total Applications", totalLabel, "📝"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        rightPanel.add(createStatBox("Pending", pendingLabel, "⏳"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        rightPanel.add(createStatBox("Accepted", acceptedLabel, "✅"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        rightPanel.add(createStatBox("Feedback", feedbackLabel, "📝"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        rightPanel.add(createStatBox("Completed", completedLabel, "🎉"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        rightPanel.add(createStatBox("Rejected", rejectedLabel, "❌"));

        refreshStats();
        return rightPanel;
    }

    private JPanel createStatBox(String title, JLabel valueLabel, String emoji) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 26));

        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBackground(Color.WHITE);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(24, 119, 242));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));

        rightSide.add(valueLabel);
        rightSide.add(titleLabel);

        panel.add(emojiLabel, BorderLayout.WEST);
        panel.add(rightSide, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStats() {
        int total = allApplications.size();
        int pending = filterByStatusForTabs(allApplications, "Pending").size();
        int accepted = filterByStatusForTabs(allApplications, "Accepted").size();
        int feedback = filterByStatusForTabs(allApplications, "Feedback").size();
        int completed = filterByStatusForTabs(allApplications, "Completed").size();
        int rejected = filterByStatusForTabs(allApplications, "Rejected").size();

        totalLabel.setText(String.valueOf(total));
        pendingLabel.setText(String.valueOf(pending));
        acceptedLabel.setText(String.valueOf(accepted));
        feedbackLabel.setText(String.valueOf(feedback));
        completedLabel.setText(String.valueOf(completed));
        rejectedLabel.setText(String.valueOf(rejected));
    }

    private void showApplications(List<ApplicationData> apps) {
        applicationsPanel.removeAll();
        if (apps.isEmpty()) {
            JLabel noLabel = new JLabel("No applications in this category.");
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
        refreshStats();
        updateFeedbackBadge();
    }

    private void updateFeedbackBadge() {
        int feedbackCount = filterByStatusForTabs(allApplications, "Feedback").size();

        Component[] components = btnFeedback.getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && "badge".equals(c.getName())) {
                btnFeedback.remove(c);
            }
        }

        if (feedbackCount > 0) {
            JLabel badge = new JLabel(" " + feedbackCount + " ");
            badge.setName("badge");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setForeground(Color.WHITE);
            badge.setBackground(new Color(220, 53, 69));
            badge.setOpaque(true);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            btnFeedback.add(badge, BorderLayout.EAST);
        }

        btnFeedback.revalidate();
        btnFeedback.repaint();
    }

    private JPanel createApplicationCard(ApplicationData app) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Color.WHITE);

        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(app.getJobTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel companyLabel = new JLabel(app.getCompany());
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        companyLabel.setForeground(new Color(100, 100, 100));
        JLabel dateLabel = new JLabel("Applied on: " + app.getAppliedDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(120, 120, 120));

        leftInfo.add(titleLabel);
        leftInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        leftInfo.add(companyLabel);
        leftInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        leftInfo.add(dateLabel);

        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(getStatusColor(app.getStatus()));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        JLabel statusLabel = new JLabel(app.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        statusPanel.add(statusLabel);

        topSection.add(leftInfo, BorderLayout.WEST);
        topSection.add(statusPanel, BorderLayout.EAST);

        JPanel bottomSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomSection.setBackground(Color.WHITE);

        JButton viewBtn = new JButton("View Details");
        viewBtn.setBackground(new Color(108, 117, 125));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(130, 35));
        viewBtn.addActionListener(e -> {
            try {
                JobData job = JobDAO.getJobById(app.getJobId());
                if (job != null) {
                    String industry = (job.getIndustry() != null && !job.getIndustry().isEmpty())
                            ? job.getIndustry()
                            : "Not specified";

                    String message = "Job Title: " + job.getJobTitle() + "\n" +
                            "Company: " + job.getCompany() + "\n" +
                            "Location: " + job.getLocation() + "\n" +
                            "Salary: " + job.getSalary() + "\n" +
                            "Type: " + job.getJobType() + "\n" +
                            "Industry: " + industry + "\n\n" +
                            "Description:\n" + job.getDescription();

                    JOptionPane.showMessageDialog(this, message, "Job Details", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error fetching job details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomSection.add(viewBtn);

        // Accepted status -> Upload or Update Work
        if ("Accepted".equalsIgnoreCase(app.getStatus())) {
            JButton uploadBtn = new JButton(app.getFilePath().isEmpty() ? "Upload Work" : "Update Work");
            uploadBtn.setBackground(new Color(24, 119, 242));
            uploadBtn.setForeground(Color.WHITE);
            uploadBtn.setFocusPainted(false);
            uploadBtn.setPreferredSize(new Dimension(140, 35));
            uploadBtn.addActionListener(e -> handleFileUpload(app));
            bottomSection.add(uploadBtn);
        }

        // Feedback status -> Resubmit Work
        if ("Feedback".equalsIgnoreCase(app.getStatus())) {
            JButton resubmitBtn = new JButton("Resubmit Work");
            resubmitBtn.setBackground(new Color(24, 119, 242));
            resubmitBtn.setForeground(Color.WHITE);
            resubmitBtn.setFocusPainted(false);
            resubmitBtn.setPreferredSize(new Dimension(140, 35));
            resubmitBtn.addActionListener(e -> handleFileUpload(app));
            bottomSection.add(resubmitBtn);
        }

        // Completed -> View Work
        if ("Completed".equalsIgnoreCase(app.getStatus())) {
            JButton viewWorkBtn = new JButton("View Work");
            viewWorkBtn.setBackground(new Color(108, 117, 125));
            viewWorkBtn.setForeground(Color.WHITE);
            viewWorkBtn.setFocusPainted(false);
            viewWorkBtn.setPreferredSize(new Dimension(140, 35));
            viewWorkBtn.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(new File(app.getFilePath()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cannot open file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            bottomSection.add(viewWorkBtn);
        }

        card.add(topSection, BorderLayout.NORTH);
        card.add(bottomSection, BorderLayout.SOUTH);
        return card;
    }

    private void handleFileUpload(ApplicationData app) {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String destPath = NETWORK_FOLDER + selectedFile.getName();

            try {
                Files.copy(selectedFile.toPath(), new File(destPath).toPath(), StandardCopyOption.REPLACE_EXISTING);

                if (ApplicationDAO.updateApplicationFile(app.getId(), destPath)) {
                    app.setFilePath(destPath);

                    // If Feedback, reset status back to Accepted
                    if ("Feedback".equalsIgnoreCase(app.getStatus())) {
                        if (ApplicationDAO.updateApplicationStatus(app.getId(), "Accepted")) {
                            app.setStatus("Accepted");
                        }
                    }

                    JOptionPane.showMessageDialog(this, "File uploaded/resubmitted successfully!");
                    main.refreshAllScreens();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update application file in DB.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "File upload failed:\n" + ex.getMessage());
            }
        }
    }

    private List<ApplicationData> filterByStatusForTabs(List<ApplicationData> apps, String tab) {
        List<ApplicationData> filtered = new ArrayList<>();
        for (ApplicationData app : apps) {
            String status = app.getStatus();
            if (status == null) continue;

            switch (tab) {
                case "Pending" -> { if ("Pending".equalsIgnoreCase(status)) filtered.add(app); }
                case "Accepted" -> { if ("Accepted".equalsIgnoreCase(status)) filtered.add(app); }
                case "Feedback" -> { if ("Feedback".equalsIgnoreCase(status)) filtered.add(app); }
                case "Completed" -> { if ("Completed".equalsIgnoreCase(status)) filtered.add(app); }
                case "Rejected" -> { if ("Rejected".equalsIgnoreCase(status)) filtered.add(app); }
            }
        }
        return filtered;
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "Pending" -> new Color(255, 193, 7);
            case "Accepted" -> new Color(40, 167, 69);
            case "Feedback" -> new Color(23, 162, 184);
            case "Rejected" -> new Color(220, 53, 69);
            case "Completed" -> new Color(108, 117, 125);
            default -> new Color(108, 117, 125);
        };
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
        JLabel user = new JLabel(main.currentUser + " (Job Seeker)");
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

        String[] items = {"🏠 Home", "💼 Browse Jobs", "🎓 Student Quick Gigs", "📋 My Applications", "👤 Profile"};

        for (String item : items) {
            JButton btn = createMenuButton(item);
            if (item.contains("Home")) btn.addActionListener(e -> main.switchScreen("jobseeker"));
            else if (item.contains("Browse Jobs")) btn.addActionListener(e -> main.switchScreen("browsejobs"));
            else if (item.contains("Student Quick Gigs")) btn.addActionListener(e -> main.switchScreen("studentgigs"));
            else if (item.contains("My Applications")) btn.addActionListener(e -> main.switchScreen("myapplications"));
            else if (item.contains("Profile")) btn.addActionListener(e -> main.switchScreen("profile"));

            leftPanel.add(btn);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        leftPanel.add(Box.createVerticalGlue());

        JButton btnLogout = createMenuButton("🚪 Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
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
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230, 235, 240)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    public void refreshApplications() {
        System.out.println("🔄 Refreshing applications for: " + main.currentUser);
        allApplications = ApplicationDAO.getApplicationsBySeeker(main.currentUser);
        System.out.println("📊 Loaded " + allApplications.size() + " applications");
        showApplications(filterByStatusForTabs(allApplications, "Pending"));
        updateFeedbackBadge();
    }
}
