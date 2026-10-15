package project;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PostjobScreen extends JPanel {

    private JobData jobToEdit = null; // currently editing job, null if posting new
    private JTextField jobTitleField;
    private JTextField companyField;
    private JTextField locationField;
    private JTextField salaryField;
    private JTextArea descArea;
    private JComboBox<String> industryCombo; // NEW INDUSTRY FIELD
    private JRadioButton fullTimeRadio;
    private JRadioButton partTimeRadio;
    private JRadioButton contractRadio;
    private JRadioButton gigsRadio;
    private Main main;

    public PostjobScreen(Main main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Top navbar
        add(createNavbar(main), BorderLayout.NORTH);

        // Center content with form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 242, 245));

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBackground(Color.WHITE);
        formBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        formBox.setPreferredSize(new Dimension(700, 780));

        // Title
        JLabel titleLabel = new JLabel(" Post New Job");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(24, 119, 242));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Fill in the details to create a new job posting");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 100, 100));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        jobTitleField = createInputField();
        companyField = createInputField();
        locationField = createInputField();
        salaryField = createInputField();

        // Industry Dropdown (NEW)
        String[] industries = {"IT", "Marketing", "HR", "Design", "Finance", "Education"};
        industryCombo = new JComboBox<>(industries);
        industryCombo.setMaximumSize(new Dimension(600, 40));
        industryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Job Type Panel
        JPanel jobTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        jobTypePanel.setBackground(Color.WHITE);
        jobTypePanel.setMaximumSize(new Dimension(600, 40));

        fullTimeRadio = createRadioButton("Full Time", true);
        partTimeRadio = createRadioButton("Part Time", false);
        contractRadio = createRadioButton("Contract", false);
        gigsRadio = createRadioButton("Gigs", false);

        ButtonGroup jobTypeGroup = new ButtonGroup();
        jobTypeGroup.add(fullTimeRadio);
        jobTypeGroup.add(partTimeRadio);
        jobTypeGroup.add(contractRadio);
        jobTypeGroup.add(gigsRadio);

        jobTypePanel.add(fullTimeRadio);
        jobTypePanel.add(partTimeRadio);
        jobTypePanel.add(contractRadio);
        jobTypePanel.add(gigsRadio);

        // Job Description
        descArea = new JTextArea(5, 20);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(600, 100));
        descScroll.setPreferredSize(new Dimension(600, 100));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setMaximumSize(new Dimension(600, 50));

        JButton postBtn = createStyledButton("Post Job", new Color(40, 167, 69));
        postBtn.setPreferredSize(new Dimension(150, 45));
        postBtn.addActionListener(e -> handlePostOrEdit());

        JButton cancelBtn = createStyledButton("Cancel", new Color(108, 117, 125));
        cancelBtn.setPreferredSize(new Dimension(150, 45));
        cancelBtn.addActionListener(e -> {
            clearForm();
            main.switchScreen("employer");
        });

        buttonsPanel.add(postBtn);
        buttonsPanel.add(cancelBtn);

        // Add all components
        formBox.add(titleLabel);
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(subtitle);
        formBox.add(Box.createRigidArea(new Dimension(0, 30)));

        formBox.add(createLabel("Job Title *"));
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(jobTitleField);
        formBox.add(Box.createRigidArea(new Dimension(0, 15)));

        formBox.add(createLabel("Company Name *"));
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(companyField);
        formBox.add(Box.createRigidArea(new Dimension(0, 15)));

        formBox.add(createLabel("Location *"));
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(locationField);
        formBox.add(Box.createRigidArea(new Dimension(0, 15)));

        formBox.add(createLabel("Salary *"));
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(salaryField);
        formBox.add(Box.createRigidArea(new Dimension(0, 15)));

        formBox.add(createLabel("Industry")); // NEW LABEL
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(industryCombo); // ADD DROPDOWN
        formBox.add(Box.createRigidArea(new Dimension(0, 15)));

        formBox.add(createLabel("Job Type *"));
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(jobTypePanel);
        formBox.add(Box.createRigidArea(new Dimension(0, 15)));

        formBox.add(createLabel("Job Description"));
        formBox.add(Box.createRigidArea(new Dimension(0, 5)));
        formBox.add(descScroll);
        formBox.add(Box.createRigidArea(new Dimension(0, 25)));

        formBox.add(buttonsPanel);

        centerPanel.add(formBox);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void handlePostOrEdit() {
        String jobTitle = jobTitleField.getText().trim();
        String company = companyField.getText().trim();
        String location = locationField.getText().trim();
        String salary = salaryField.getText().trim();
        String description = descArea.getText().trim();
        String industry = (String) industryCombo.getSelectedItem();

        if (jobTitle.isEmpty() || company.isEmpty() || location.isEmpty() || salary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String type = fullTimeRadio.isSelected() ? "Full Time" :
                partTimeRadio.isSelected() ? "Part Time" :
                        contractRadio.isSelected() ? "Contract" : "Gigs";
        String category = type.equals("Gigs") ? "gigs" : "job";

        try (Connection con = DBConnection.getConnection()) {
            if (jobToEdit == null) {
                // POST NEW JOB
                String sql = "INSERT INTO jobs (title, company, location, salary, type, category, description, posted_by, industry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, jobTitle);
                ps.setString(2, company);
                ps.setString(3, location);
                ps.setString(4, salary);
                ps.setString(5, type);
                ps.setString(6, category);
                ps.setString(7, description);
                ps.setString(8, main.currentUser);
                ps.setString(9, industry);
                ps.executeUpdate();
                ps.close();

                JOptionPane.showMessageDialog(this, "Job posted successfully!\nIndustry: " + industry, "Success", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // EDIT EXISTING JOB
                String sql = "UPDATE jobs SET title=?, company=?, location=?, salary=?, type=?, category=?, description=?, industry=? WHERE id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, jobTitle);
                ps.setString(2, company);
                ps.setString(3, location);
                ps.setString(4, salary);
                ps.setString(5, type);
                ps.setString(6, category);
                ps.setString(7, description);
                ps.setString(8, industry);
                ps.setInt(9, jobToEdit.getId());
                ps.executeUpdate();
                ps.close();

                JOptionPane.showMessageDialog(this, "Job updated successfully!\nIndustry: " + industry, "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            clearForm();

            // ✅ REFRESH ALL SCREENS AFTER POSTING/EDITING
            main.refreshAllScreens();

            main.switchScreen("myjobposts");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void setJobForEdit(JobData job) {
        this.jobToEdit = job;

        if (job == null) {
            // NEW JOB: clear all fields
            jobTitleField.setText("");
            companyField.setText("");
            locationField.setText("");
            salaryField.setText("");
            descArea.setText("");
            fullTimeRadio.setSelected(true);
            industryCombo.setSelectedIndex(0);
            return; // skip the rest
        }

        // EDIT EXISTING JOB: populate fields
        jobTitleField.setText(job.getJobTitle());
        companyField.setText(job.getCompany());
        locationField.setText(job.getLocation());
        salaryField.setText(job.getSalary());
        descArea.setText(job.getDescription());

        switch (job.getJobType()) {
            case "Full Time" -> fullTimeRadio.setSelected(true);
            case "Part Time" -> partTimeRadio.setSelected(true);
            case "Contract" -> contractRadio.setSelected(true);
            case "Gigs" -> gigsRadio.setSelected(true);
        }

        if (job.getIndustry() != null) {
            industryCombo.setSelectedItem(job.getIndustry());
        }
    }

    private void clearForm() {
        jobToEdit = null;
        jobTitleField.setText("");
        companyField.setText("");
        locationField.setText("");
        salaryField.setText("");
        descArea.setText("");
        fullTimeRadio.setSelected(true);
        industryCombo.setSelectedIndex(0); // RESET INDUSTRY
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

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(24, 119, 242));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            clearForm();
            main.switchScreen("employer");
        });

        rightTop.add(backBtn);
        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);

        return topBar;
    }

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

    private JRadioButton createRadioButton(String text, boolean selected) {
        JRadioButton radio = new JRadioButton(text);
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        radio.setBackground(Color.WHITE);
        radio.setSelected(selected);
        return radio;
    }
}
