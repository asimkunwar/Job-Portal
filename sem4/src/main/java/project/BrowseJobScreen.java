package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BrowseJobScreen extends JPanel {

    private JPanel jobsContainer; // container for dynamic refresh
    private JPanel rightPanel;    // stats panel
    private List<JobData> finalJobsList = new ArrayList<>();
    private Main mainRef;

    public BrowseJobScreen(Main main) {
        this.mainRef = main;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Top navbar
        add(createNavbar(main), BorderLayout.NORTH);

        // Left menu
        JPanel leftMenu = createLeftMenu(main);

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(new Color(240, 242, 245));

        JLabel welcomeLabel = new JLabel("Browse All Jobs 💼");
        welcomeLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(welcomeLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Search/filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setBackground(new Color(240, 242, 245));
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JButton searchBtn = createStyledButton("🔍 Search", new Color(24, 119, 242));
        searchBtn.setPreferredSize(new Dimension(120, 38));

        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All Types", "Full Time", "Part Time", "Contract"});
        typeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeFilter.setPreferredSize(new Dimension(130, 38));

        filterPanel.add(new JLabel("🔎 Search:"));
        filterPanel.add(searchField);
        filterPanel.add(searchBtn);
        filterPanel.add(typeFilter);

        centerPanel.add(filterPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Jobs container
        jobsContainer = new JPanel();
        jobsContainer.setLayout(new BoxLayout(jobsContainer, BoxLayout.Y_AXIS));
        jobsContainer.setBackground(new Color(240, 242, 245));
        centerPanel.add(jobsContainer);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Right stats panel
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(280, 100));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        rightPanel.setBackground(new Color(240, 242, 245));

        add(leftMenu, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Load jobs initially
        loadJobs();

        // Search/filter listeners
        Runnable filterJobs = () -> {
            String searchText = searchField.getText().toLowerCase();
            String selectedType = (String) typeFilter.getSelectedItem();
            List<JobData> filteredJobs = new ArrayList<>();
            for (JobData job : finalJobsList) {
                boolean matchesSearch = searchText.isEmpty() ||
                        job.getJobTitle().toLowerCase().contains(searchText) ||
                        job.getCompany().toLowerCase().contains(searchText) ||
                        job.getLocation().toLowerCase().contains(searchText);

                boolean matchesType = selectedType.equals("All Types") ||
                        job.getJobType().equalsIgnoreCase(selectedType);

                if (matchesSearch && matchesType) filteredJobs.add(job);
            }
            refreshJobList(filteredJobs);
        };

        searchBtn.addActionListener(e -> filterJobs.run());
        typeFilter.addActionListener(e -> filterJobs.run());
    }

    // =================== REFRESH METHOD ===================
    public void refreshJobs() {
        // Reload all jobs from database
        loadJobs();
    }

    private void loadJobs() {
        try {
            List<JobData> allJobs = JobDAO.getAllJobs();
            finalJobsList.clear();
            for (JobData job : allJobs) {
                if ("job".equalsIgnoreCase(job.getCategory())) finalJobsList.add(job);
            }
            refreshJobList(finalJobsList);
            refreshStats();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading jobs from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshJobList(List<JobData> jobs) {
        jobsContainer.removeAll();
        if (jobs.isEmpty()) {
            JLabel noJobsLabel = new JLabel("No jobs match your search criteria.");
            noJobsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noJobsLabel.setForeground(new Color(100, 100, 100));
            jobsContainer.add(noJobsLabel);
        } else {
            for (JobData job : jobs) {
                jobsContainer.add(createJobCard(job, mainRef));
                jobsContainer.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        jobsContainer.revalidate();
        jobsContainer.repaint();
    }

    private void refreshStats() {
        rightPanel.removeAll();
        rightPanel.add(createStatsPanel(finalJobsList));
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // =================== YOUR ORIGINAL METHODS BELOW ===================
    private JPanel createJobCard(JobData job, Main main) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Top section
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel(job.getJobTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel companyLabel = new JLabel(job.getCompany());
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        companyLabel.setForeground(new Color(100, 100, 100));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(companyLabel);
        topSection.add(titlePanel, BorderLayout.WEST);

        // Mid section
        JPanel midSection = new JPanel(new GridLayout(1, 3, 15, 0));
        midSection.setBackground(Color.WHITE);
        midSection.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        midSection.add(createInfoLabel("🏭 " + (job.getIndustry() == null || job.getIndustry().isEmpty() ? "Not specified" : job.getIndustry())));
        midSection.add(createInfoLabel("💰 " + job.getSalary()));
        midSection.add(createInfoLabel("⏰ " + job.getJobType()));

        // Bottom buttons
        JPanel bottomSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomSection.setBackground(Color.WHITE);

        JButton viewBtn = createStyledButton("View Details", new Color(108, 117, 125));
        viewBtn.setPreferredSize(new Dimension(130, 35));
        viewBtn.addActionListener(e -> {
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
        });

        JButton applyBtn = createStyledButton("Apply Now", new Color(24, 119, 242));
        applyBtn.setPreferredSize(new Dimension(120, 35));
        applyBtn.addActionListener(e -> {
            try {
                boolean success = ApplicationDAO.applyForJob(job.getId(), main.currentUser);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Application submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // ✅ REFRESH ALL SCREENS
                    main.refreshAllScreens();
                } else {
                    JOptionPane.showMessageDialog(this, "You have already applied for this job.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error submitting application.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton saveBtn = createStyledButton("💾 Save", new Color(245, 245, 245));
        saveBtn.setForeground(new Color(50, 50, 50));
        saveBtn.setPreferredSize(new Dimension(100, 35));

        bottomSection.add(viewBtn);
        bottomSection.add(applyBtn);
        bottomSection.add(saveBtn);

        card.add(topSection, BorderLayout.NORTH);
        card.add(midSection, BorderLayout.CENTER);
        card.add(bottomSection, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createStatsPanel(List<JobData> jobsList) {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(240, 242, 245));

        JLabel statsTitle = new JLabel("📊 Quick Stats");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(statsTitle);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        rightPanel.add(createStatBox("Total Jobs", String.valueOf(jobsList.size()), "💼"));

        return rightPanel;
    }

    private JPanel createStatBox(String title, String value, String emoji) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Noto Color Emoji", Font.PLAIN, 28));

        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
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

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.PLAIN, 13));   // safest cross-platform
        label.setForeground(new Color(80, 80, 80));
        return label;
    }


}
