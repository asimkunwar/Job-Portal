package project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class   Main extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // Data storage
    public static List<JobData> gigs = new ArrayList<>();
    public static Map<String, UserData> users = new HashMap<>();
    public static List<JobData> jobs = new ArrayList<>();
    public String currentUser = "";
    public int currentUserId = 0;
    public String userType = "";

    // Screens
    private LoginScreen loginScreen;
    private SignupScreen signupScreen;
    public JobSeekerDashboard jobSeekerDashboard;
    public EmployerDashboard employerDashboard;
    public PostjobScreen postjobScreen;
    public BrowseJobScreen browseJobScreen;
    public MyJobPostsScreen myJobPostsScreen;
    public MyApplicationsScreen myApplicationsScreen;
    public ProfileScreen profileScreen;
    public StudentGigsscreen studentGigsscreen;
    public EmployerApplicationScreen employerApplicationScreen;
    public EmployerSettingScreen employerSettingScreen;

    // Loading overlay dialog
    private JDialog loadingDialog;

    public Main() {
        setTitle("Job Portal");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLoadingDialog();
        initScreens();

        add(mainPanel);
        switchScreen("login");
        setVisible(true);
    }

    private void initScreens() {
        loginScreen = new LoginScreen(this);
        signupScreen = new SignupScreen(this);
        jobSeekerDashboard = new JobSeekerDashboard(this);
        employerDashboard = new EmployerDashboard(this);
        postjobScreen = new PostjobScreen(this);
        browseJobScreen = new BrowseJobScreen(this);
        myJobPostsScreen = new MyJobPostsScreen(this);
        myApplicationsScreen = new MyApplicationsScreen(this);
        profileScreen = new ProfileScreen(this);
        studentGigsscreen = new StudentGigsscreen(this);
        employerSettingScreen = new EmployerSettingScreen(this);
        employerApplicationScreen = new EmployerApplicationScreen(this);

        mainPanel.add(loginScreen, "login");
        mainPanel.add(signupScreen, "signup");
        mainPanel.add(jobSeekerDashboard, "jobseeker");
        mainPanel.add(employerDashboard, "employer");
        mainPanel.add(postjobScreen, "postjob");
        mainPanel.add(browseJobScreen, "browsejobs");
        mainPanel.add(myJobPostsScreen, "myjobposts");
        mainPanel.add(myApplicationsScreen, "myapplications");
        mainPanel.add(profileScreen, "profile");
        mainPanel.add(studentGigsscreen, "studentgigs");
        mainPanel.add(employerApplicationScreen, "employerapplications");
        mainPanel.add(employerSettingScreen, "employersettings");
    }

    private void initLoadingDialog() {
        loadingDialog = new JDialog(this, "Loading...", true);
        JLabel label = new JLabel("Please wait, loading data...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        loadingDialog.add(label);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    // Switch screen with background refresh
    public void switchScreen(String screenName) {
        if ("login".equals(screenName)) {
            currentUser = "";
            userType = "";
            if (loginScreen != null) loginScreen.clearFields();
        }

        // Show the panel immediately
        cardLayout.show(mainPanel, screenName);

        // Refresh data in background
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    switch (screenName) {
                        case "browsejobs":
                            if (browseJobScreen != null) browseJobScreen.refreshJobs(); // fetch and update safely inside screen
                            break;
                        case "studentgigs":
                            if (studentGigsscreen != null) studentGigsscreen.loadGigs();
                            break;
                        case "myapplications":
                            if (myApplicationsScreen != null) myApplicationsScreen.refreshApplications();
                            break;
                        case "myjobposts":
                            if (myJobPostsScreen != null) myJobPostsScreen.refreshJobPosts();
                            break;
                        case "employerapplications":
                            if (employerApplicationScreen != null) employerApplicationScreen.refreshApplications();
                            break;
                        case "profile":
                            if (profileScreen != null) profileScreen.refresh();
                            break;
                        case "employersettings":
                            if (employerSettingScreen != null) employerSettingScreen.refresh();
                            break;
                        case "jobseeker":
                            if (jobSeekerDashboard != null) jobSeekerDashboard.refreshDashboard();
                            break;
                        case "employer":
                            if (employerDashboard != null) employerDashboard.refreshDashboard();
                            break;
                        case "postjob":
                            // lightweight screen, no heavy fetch
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error refreshing screen: " + screenName);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                // Done: nothing extra needed since all screen updates use existing safe methods
            }
        }.execute();
    }

    public void refreshAllScreens() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (browseJobScreen != null) browseJobScreen.refreshJobs();
                    if (studentGigsscreen != null) studentGigsscreen.loadGigs();
                    if (myJobPostsScreen != null) myJobPostsScreen.refreshJobPosts();
                    if (myApplicationsScreen != null) myApplicationsScreen.refreshApplications();
                    if (employerApplicationScreen != null) employerApplicationScreen.refreshApplications();
                    if (jobSeekerDashboard != null) jobSeekerDashboard.refreshDashboard();
                    if (employerDashboard != null) employerDashboard.refreshDashboard();

                    System.out.println("✅ All screens refreshed successfully");
                } catch (Exception e) {
                    System.err.println("❌ Error during global refresh");
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public void showJobDetails(int jobId) {
        try {
            JobData job = JobDAO.getJobById(jobId);
            if (job != null) {
                String message = "Job Title: " + job.getJobTitle() + "\n" +
                        "Company: " + job.getCompany() + "\n" +
                        "Location: " + job.getLocation() + "\n" +
                        "Salary: " + job.getSalary() + "\n" +
                        "Type: " + job.getJobType() + "\n" +
                        "Industry: " + (job.getIndustry().isEmpty() ? "Not specified" : job.getIndustry()) + "\n\n" +
                        "Description:\n" + job.getDescription();
                JOptionPane.showMessageDialog(this, message, "Job Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Job not found!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching job details.");
        }
    }

    public void showProfileDialog(String username, String filePath) {
        String message = "Username: " + username + "\nUploaded File: " + (filePath == null || filePath.isEmpty() ? "None" : filePath);
        JOptionPane.showMessageDialog(this, message, "Applicant Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
