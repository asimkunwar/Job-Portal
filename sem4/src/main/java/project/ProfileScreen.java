package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileScreen extends JPanel {

    private JTextField nameField, emailField, phoneField, locationField, resumeField;
    private JTextArea bioArea;
    private JLabel picLabel;
    private File selectedPicFile = null;
    private File selectedResumeFile = null;
    private Main main; // reference to main

    public ProfileScreen(Main main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Top navbar
        add(createNavbar(main), BorderLayout.NORTH);

        // Left menu
        JPanel leftMenu = createLeftMenu(main);

        // Center panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 242, 245));

        JPanel profileBox = new JPanel();
        profileBox.setLayout(new BoxLayout(profileBox, BoxLayout.Y_AXIS));
        profileBox.setBackground(Color.WHITE);
        profileBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        profileBox.setPreferredSize(new Dimension(700, 800));

        // Header
        JLabel titleLabel = new JLabel("👤 My Profile");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        titleLabel.setForeground(new Color(24, 119, 242));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Manage your account information");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 100, 100));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileBox.add(titleLabel);
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        profileBox.add(subtitle);
        profileBox.add(Box.createRigidArea(new Dimension(0, 30)));

        // Profile picture
        picLabel = new JLabel("👤", SwingConstants.CENTER);
        picLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        picLabel.setForeground(new Color(150, 150, 150));
        picLabel.setOpaque(true);
        picLabel.setBackground(new Color(230, 235, 240));
        picLabel.setPreferredSize(new Dimension(120, 120));
        picLabel.setMaximumSize(new Dimension(120, 120));
        picLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton changePicBtn = new JButton("Change Picture");
        changePicBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changePicBtn.setForeground(new Color(24, 119, 242));
        changePicBtn.setBackground(Color.WHITE);
        changePicBtn.setFocusPainted(false);
        changePicBtn.setBorderPainted(false);
        changePicBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePicBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePicBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                selectedPicFile = fileChooser.getSelectedFile();
                picLabel.setIcon(new ImageIcon(new ImageIcon(selectedPicFile.getAbsolutePath())
                        .getImage().getScaledInstance(120,120, Image.SCALE_SMOOTH)));
                picLabel.setText("");
            }
        });

        profileBox.add(picLabel);
        profileBox.add(Box.createRigidArea(new Dimension(0, 10)));
        profileBox.add(changePicBtn);
        profileBox.add(Box.createRigidArea(new Dimension(0, 30)));

        // Full Name (non-editable)
        profileBox.add(createLabel("Full Name"));
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        nameField = createInputField();
        nameField.setEditable(false);
        profileBox.add(nameField);
        profileBox.add(Box.createRigidArea(new Dimension(0, 15)));

        // Email (non-editable)
        profileBox.add(createLabel("Email Address"));
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        emailField = createInputField();
        emailField.setEditable(false);
        profileBox.add(emailField);
        profileBox.add(Box.createRigidArea(new Dimension(0, 15)));

        // Phone
        profileBox.add(createLabel("Phone Number"));
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        phoneField = createInputField();
        profileBox.add(phoneField);
        profileBox.add(Box.createRigidArea(new Dimension(0, 15)));

        // Location
        profileBox.add(createLabel("Location"));
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        locationField = createInputField();
        profileBox.add(locationField);
        profileBox.add(Box.createRigidArea(new Dimension(0, 15)));

        // About Me
        profileBox.add(createLabel("About Me"));
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        bioArea = new JTextArea(6, 30);
        bioArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setPreferredSize(new Dimension(600, 120));
        profileBox.add(bioScroll);
        profileBox.add(Box.createRigidArea(new Dimension(0, 15)));

        // Resume
        profileBox.add(createLabel("Resume Link"));
        profileBox.add(Box.createRigidArea(new Dimension(0, 5)));
        JPanel resumePanel = new JPanel(new BorderLayout(5,0));
        resumeField = createInputField();
        JButton uploadResumeBtn = new JButton("Upload");
        uploadResumeBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                selectedResumeFile = fileChooser.getSelectedFile();
                resumeField.setText(selectedResumeFile.getAbsolutePath());
            }
        });
        resumePanel.add(resumeField, BorderLayout.CENTER);
        resumePanel.add(uploadResumeBtn, BorderLayout.EAST);
        resumePanel.setMaximumSize(new Dimension(600,40));
        profileBox.add(resumePanel);
        profileBox.add(Box.createRigidArea(new Dimension(0, 25)));

        // Save button
        JButton saveBtn = new JButton("💾 Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        saveBtn.addActionListener(e -> saveProfile());

        profileBox.add(saveBtn);

        // Scrollable panel
        JScrollPane scrollPane = new JScrollPane(profileBox);
        scrollPane.setBorder(null);
        add(leftMenu, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    // **New public method** to refresh data every time the user opens Profile
    public void refresh() {
        fetchUserData(main.currentUser);
    }

    private void fetchUserData(String username){
        try(Connection con = DBConnection.getConnection()){
            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                nameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                locationField.setText(rs.getString("location") != null ? rs.getString("location") : "");
                bioArea.setText(rs.getString("bio") != null ? rs.getString("bio") : "");
                resumeField.setText(rs.getString("resume_link") != null ? rs.getString("resume_link") : "");
                String pic = rs.getString("profile_pic");
                if(pic != null && !pic.isEmpty()){
                    picLabel.setIcon(new ImageIcon(new ImageIcon(pic)
                            .getImage().getScaledInstance(120,120, Image.SCALE_SMOOTH)));
                    picLabel.setText("");
                }
            }
            rs.close();
            ps.close();
        }catch(Exception e){ e.printStackTrace(); }
    }

    private void saveProfile(){
        try(Connection con = DBConnection.getConnection()){
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET phone=?, location=?, bio=?, resume_link=?, profile_pic=? WHERE username=?"
            );
            ps.setString(1, phoneField.getText().trim());
            ps.setString(2, locationField.getText().trim());
            ps.setString(3, bioArea.getText().trim());
            ps.setString(4, resumeField.getText().trim());
            ps.setString(5, selectedPicFile != null ? selectedPicFile.getAbsolutePath() : null);
            ps.setString(6, main.currentUser);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // ✅ REFRESH PROFILE DATA
            refresh();

        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving profile: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // NAVBAR, LEFT MENU, INPUT FIELD, LABEL, MENU BUTTON methods remain exactly as before
    private JPanel createNavbar(Main main){
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(100,60));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(220,220,220)));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT,15,15));
        leftTop.setBackground(Color.WHITE);
        JLabel logo = new JLabel("💼 JobPortal");
        logo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));

        logo.setForeground(new Color(24,119,242));
        leftTop.add(logo);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT,15,15));
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


    private JTextField createInputField(){
        JTextField field = new JTextField(20);
        field.setMaximumSize(new Dimension(600,40));
        field.setFont(new Font("Segoe UI", Font.PLAIN,14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1),
                BorderFactory.createEmptyBorder(5,10,5,10)
        ));
        return field;
    }

    private JLabel createLabel(String text){
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD,13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
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

        btn.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ btn.setBackground(new Color(230,235,240)); }
            public void mouseExited(MouseEvent e){ btn.setBackground(Color.WHITE); }
        });
        return btn;
    }
}
