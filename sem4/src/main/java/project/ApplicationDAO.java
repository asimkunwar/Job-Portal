package project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    // Apply for a job
    public static boolean applyForJob(int jobId, String username) {
        try (Connection con = DBConnection.getConnection()) {
            // Check if already applied
            String checkSql = "SELECT 1 FROM applications WHERE job_id = ? AND username = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, jobId);
                psCheck.setString(2, username);
                if (psCheck.executeQuery().next()) {
                    return false; // already applied
                }
            }

            // Insert new application
            String sql = "INSERT INTO applications (job_id, username, status, applied_on) VALUES (?, ?, 'Pending', NOW())";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, jobId);
                ps.setString(2, username);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all applications for jobs posted by an employer
    public static List<ApplicationData> getApplicationsByEmployer(String employerUsername) {
        List<ApplicationData> list = new ArrayList<>();
        String sql = "SELECT a.id, a.job_id, a.username, u.full_name, " +
                "j.title AS job_title, j.company, a.status, a.applied_on, " +
                "IFNULL(a.resume_link, '') AS resume_link, " +
                "IFNULL(a.email, '') AS email, " +
                "IFNULL(a.phone, '') AS phone, " +
                "IFNULL(a.feedback, '') AS feedback, " +
                "IFNULL(sw.file_path, '') AS file_path " +
                "FROM applications a " +
                "JOIN jobs j ON a.job_id = j.id " +
                "JOIN users u ON a.username = u.username " +
                "LEFT JOIN submitted_work sw ON a.id = sw.application_id " +
                "WHERE j.posted_by = ? " +
                "ORDER BY a.applied_on DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, employerUsername);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get applications for a job seeker
    public static List<ApplicationData> getApplicationsBySeeker(String username) {
        List<ApplicationData> list = new ArrayList<>();
        String sql = "SELECT a.id, a.job_id, a.username, u.full_name, " +
                "j.title AS job_title, j.company, a.status, a.applied_on, " +
                "IFNULL(a.resume_link, '') AS resume_link, " +
                "IFNULL(a.email, '') AS email, " +
                "IFNULL(a.phone, '') AS phone, " +
                "IFNULL(a.feedback, '') AS feedback, " +
                "IFNULL(sw.file_path, '') AS file_path " +
                "FROM applications a " +
                "JOIN jobs j ON a.job_id = j.id " +
                "JOIN users u ON a.username = u.username " +
                "LEFT JOIN submitted_work sw ON a.id = sw.application_id " +
                "WHERE a.username = ? " +
                "ORDER BY a.applied_on DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Withdraw an application
    public static boolean withdrawApplication(int applicationId) {
        String sql = "DELETE FROM applications WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update uploaded work (initial upload or resubmit)
    public static boolean updateApplicationFile(int applicationId, String filePath) {
        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT 1 FROM submitted_work WHERE application_id = ?";
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setInt(1, applicationId);
                if (check.executeQuery().next()) {
                    // Update
                    String update = "UPDATE submitted_work SET file_path = ?, uploaded_on = NOW() WHERE application_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(update)) {
                        ps.setString(1, filePath);
                        ps.setInt(2, applicationId);
                        return ps.executeUpdate() > 0;
                    }
                } else {
                    // Insert
                    String insert = "INSERT INTO submitted_work (application_id, file_path, uploaded_on) VALUES (?, ?, NOW())";
                    try (PreparedStatement ps = con.prepareStatement(insert)) {
                        ps.setInt(1, applicationId);
                        ps.setString(2, filePath);
                        return ps.executeUpdate() > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update status
    public static boolean updateApplicationStatus(int applicationId, String newStatus) {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update feedback
    public static boolean updateApplicationFeedback(int applicationId, String feedback) {
        String sql = "UPDATE applications SET feedback = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, feedback != null ? feedback : "");
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update both status and feedback
    public static boolean updateApplicationStatusAndFeedback(int applicationId, String status, String feedback) {
        String sql = "UPDATE applications SET status = ?, feedback = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, feedback != null ? feedback : "");
            ps.setInt(3, applicationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get uploaded file path
    public static String getUploadedWorkPath(int applicationId) {
        String sql = "SELECT file_path FROM submitted_work WHERE application_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file_path") != null ? rs.getString("file_path") : "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}