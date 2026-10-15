package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyJobPostsScreen extends JPanel {

    private Main main;
    private JPanel jobsContainer;  // ⭐ Store reference to just the jobs container

    public MyJobPostsScreen(Main main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Top navbar
        add(createNavbar(main), BorderLayout.NORTH);

        // Left menu
        add(createLeftMenu(main), BorderLayout.WEST);

        // Center panel - CREATE ONCE, never rebuild
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    // ⭐ NEW: Create center panel structure ONCE
    private JScrollPane createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("My Job Posts 💼");
        titleLabel.setFont(new Font("Segoe UI ", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // ⭐ Jobs container - only THIS gets cleared and refilled
        jobsContainer = new JPanel();
        jobsContainer.setLayout(new BoxLayout(jobsContainer, BoxLayout.Y_AXIS));
        jobsContainer.setBackground(new Color(240, 242, 245));
        centerPanel.add(jobsContainer);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    // ⭐ FIXED: Only refresh the jobs container, not entire UI
    public void refreshJobPosts() {
        System.out.println("🔄 Refreshing job posts for: " + main.currentUser);

        // Clear ONLY the jobs container
        jobsContainer.removeAll();

        // Fetch jobs from DB
        List<JobData> jobsFromDB = fetchJobsFromDB(main.currentUser);
        System.out.println("📊 Found " + jobsFromDB.size() + " jobs posted by " + main.currentUser);

        if (jobsFromDB.isEmpty()) {
            JLabel noJobsLabel = new JLabel("You have not posted any jobs yet.");
            noJobsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noJobsLabel.setForeground(new Color(100, 100, 100));
            noJobsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            jobsContainer.add(Box.createVerticalGlue());
            jobsContainer.add(noJobsLabel);
            jobsContainer.add(Box.createVerticalGlue());
        } else {
            for (JobData job : jobsFromDB) {
                jobsContainer.add(createJobCard(job));
                jobsContainer.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        // Refresh ONLY the jobs container
        jobsContainer.revalidate();
        jobsContainer.repaint();
    }

    private List<JobData> fetchJobsFromDB(String employer) {
        List<JobData> jobs = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            String sql = "SELECT id, title, company, location, salary, type, category, description, posted_by, status, posted_date, industry " +
                    "FROM jobs WHERE posted_by = ?";

            ps = con.prepareStatement(sql);
            ps.setString(1, employer);
            rs = ps.executeQuery();

            while (rs.next()) {
                JobData job = new JobData(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("location"),
                        rs.getString("salary"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("posted_by"),
                        rs.getString("status"),
                        rs.getTimestamp("posted_date"),
                        rs.getString("industry")
                );
                jobs.add(job);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Database error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return jobs;
    }

    private JPanel createJobCard(JobData job) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        // Job info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(job.getJobTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel companyLabel = new JLabel(job.getCompany() + " | " + job.getLocation());
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        companyLabel.setForeground(new Color(80, 80, 80));

        JLabel typeSalary = new JLabel(job.getJobType() + " | " + job.getSalary());
        typeSalary.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeSalary.setForeground(new Color(80, 80, 80));

        JLabel industryLabel = new JLabel("Industry: " + (job.getIndustry() != null ? job.getIndustry() : "N/A"));
        industryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        industryLabel.setForeground(new Color(100, 100, 100));

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(companyLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(typeSalary);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(industryLabel);

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton editBtn = new JButton("Edit");
        editBtn.setBackground(new Color(24, 119, 242));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.setPreferredSize(new Dimension(80, 30));
        editBtn.addActionListener(e -> {
            main.postjobScreen.setJobForEdit(job); // load existing job details
            main.switchScreen("postjob");          // switch screen without clearing form
        });


        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setPreferredSize(new Dimension(80, 30));
        deleteBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this job?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                deleteJobFromDB(job.getId());
                refreshJobPosts();
            }
        });

        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    private void deleteJobFromDB(int jobId) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "DELETE FROM jobs WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, jobId);
            ps.executeUpdate();
            ps.close();
            con.close();

            System.out.println("✅ Deleted job ID: " + jobId);

            // ✅ REFRESH ALL SCREENS AFTER DELETION
            main.refreshAllScreens();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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