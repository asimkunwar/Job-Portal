package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class EmployerDashboard extends JPanel {

    private Main main;

    private JPanel cardsPanel;
    private JPanel chartContainer;
    private JPanel rightPanel;
    private JScrollPane scrollPane;

    private JLabel welcomeLabel;
    private JLabel navbarUserLabel;

    public EmployerDashboard(Main main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        add(createNavbar(main), BorderLayout.NORTH);
        add(createLeftMenu(main), BorderLayout.WEST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(new Color(240, 242, 245));

        // ✅ Welcome label - centered
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(welcomeLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // ✅ Tagline - centered
        JLabel taglineLabel = new JLabel("💼 We didn't wait, we built it for you!");
        taglineLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        taglineLabel.setForeground(new Color(24, 119, 242));
        taglineLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(taglineLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Stats cards
        cardsPanel = createStatsCards();
        centerPanel.add(cardsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Chart container
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        updateChartPanel();
        centerPanel.add(chartContainer);

        scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);

        // Right panel - top companies
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(300, 100));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(240, 242, 245));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        updateTopCompaniesPanel();

        add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void refreshDashboard() {
        SwingUtilities.invokeLater(() -> {
            welcomeLabel.setText("👋 Welcome back, " + main.currentUser + "!");
            if (navbarUserLabel != null) {
                navbarUserLabel.setText(main.currentUser.isEmpty() ? "Guest" : main.currentUser + " (Employer)");
            }
            updateStatsCards();
            updateChartPanel();
            updateTopCompaniesPanel();
            revalidate();
            repaint();
        });
    }

    private void updateStatsCards() {
        Container parent = cardsPanel.getParent();
        int index = -1;
        if (parent != null) {
            Component[] components = parent.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == cardsPanel) {
                    index = i;
                    break;
                }
            }
            parent.remove(cardsPanel);
        }
        cardsPanel = createStatsCards();
        if (parent != null && index >= 0) {
            parent.add(cardsPanel, index);
        }
    }

    private void updateChartPanel() {
        chartContainer.removeAll();
        chartContainer.add(createApplicationsByIndustryChart(), BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void updateTopCompaniesPanel() {
        rightPanel.removeAll();
        JLabel title = new JLabel("🏢 Top Companies");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        rightPanel.add(title);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        java.util.List<String[]> topCompanies = fetchTopCompanies();
        int rank = 1;
        for (String[] comp : topCompanies) {
            if (rank > 10) break;

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JLabel nameLabel = new JLabel("⭐ " + rank + ". " + comp[0]);
            nameLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
            nameLabel.setForeground(new Color(24, 119, 242));

            JLabel detailLabel = new JLabel("✅ Completed: " + comp[1]);
            detailLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            detailLabel.setForeground(new Color(80, 80, 80));

            card.add(nameLabel);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(detailLabel);

            rightPanel.add(card);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rank++;
        }

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JPanel createStatsCards() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.setBackground(new Color(240, 242, 245));

        int jobs = getCountJobs();
        int jobseekers = getUserCount("jobseeker");
        int employers = getUserCount("employer");
        int completed = countCompletedJobs();

        cards.add(createCard("🧑‍💼 Jobs", jobs, new Color(173, 216, 230)));
        cards.add(createCard("🧑‍🎓 Jobseekers", jobseekers, new Color(144, 238, 144)));
        cards.add(createCard("🏢 Employers", employers, new Color(255, 165, 0)));
        cards.add(createCard("✅ Completed Jobs", completed, new Color(255, 99, 71)));

        return cards;
    }

    private JPanel createCard(String title, int value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(valueLabel, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createApplicationsByIndustryChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> industryData = fetchApplicationsByIndustry();
        for (Map.Entry<String, Integer> entry : industryData.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "📊 Applications by Industry",
                dataset,
                true, true, false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setSimpleLabels(false);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

        Color[] colors = {new Color(135, 206, 235), new Color(144, 238, 144),
                new Color(255, 165, 0), new Color(238, 130, 238),
                new Color(255, 215, 0), new Color(100, 149, 237)};
        int i = 0;
        for (Object keyObj : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) keyObj, colors[i % colors.length]);
            i++;
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 300));
        chartPanel.setBackground(Color.WHITE);

        JPanel container = new JPanel(new BorderLayout());
        container.add(chartPanel, BorderLayout.CENTER);
        container.setBackground(Color.WHITE);

        return container;
    }

    private Map<String, Integer> fetchApplicationsByIndustry() {
        Map<String, Integer> data = new HashMap<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT j.industry, COUNT(*) as total " +
                    "FROM applications a " +
                    "JOIN jobs j ON a.job_id = j.id " +
                    "GROUP BY j.industry";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String industry = rs.getString("industry");
                if (industry == null || industry.trim().isEmpty()) industry = "Not specified";
                data.put(industry, rs.getInt("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private java.util.List<String[]> fetchTopCompanies() {
        java.util.List<String[]> top = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT j.company, COUNT(*) as total_completed " +
                    "FROM applications a " +
                    "JOIN jobs j ON a.job_id = j.id " +
                    "WHERE a.status='Completed' " +
                    "GROUP BY j.company " +
                    "ORDER BY total_completed DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                top.add(new String[]{rs.getString("company"), String.valueOf(rs.getInt("total_completed"))});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return top;
    }

    private int getCountJobs() {
        int count = 0;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM jobs";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    private int getUserCount(String type) {
        int count = 0;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE type=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    private int countCompletedJobs() {
        int count = 0;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM applications WHERE status='Completed'";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
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

        navbarUserLabel = new JLabel(main.currentUser.isEmpty() ? "Guest" : main.currentUser + " (Employer)");
        navbarUserLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        rightTop.add(navbarUserLabel);

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
            btn.addActionListener(e -> {
                switch (item) {
                    case "🏠 Dashboard" -> main.switchScreen("employer");
                    case "➕ Post New Job" -> {
                        if (main.postjobScreen != null) main.postjobScreen.setJobForEdit(null);
                        main.switchScreen("postjob");
                    }
                    case "💼 My Job Posts" -> main.switchScreen("myjobposts");
                    case "📊 Applications" -> main.switchScreen("employerapplications");
                    case "⚙ Settings" -> main.switchScreen("employersettings");
                }
            });
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
        btn.setPreferredSize(new Dimension(190,42));
        btn.setMaximumSize(new Dimension(190,42));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50,50,50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230,235,240)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

}
