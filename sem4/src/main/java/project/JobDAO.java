package project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

    // Add a new job or gig
    public static boolean addJob(JobData job) {
        String sql = "INSERT INTO jobs (title, company, location, salary, type, category, industry, description, posted_by, status, posted_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, job.getJobTitle());
            ps.setString(2, job.getCompany());
            ps.setString(3, job.getLocation());
            ps.setString(4, job.getSalary());
            ps.setString(5, job.getJobType());
            ps.setString(6, job.getCategory());
            ps.setString(7, job.getIndustry()); // NEW
            ps.setString(8, job.getDescription());
            ps.setString(9, job.getEmployer());
            ps.setString(10, job.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update existing job
    public static boolean updateJob(JobData job) {
        String sql = "UPDATE jobs SET title=?, company=?, location=?, salary=?, type=?, category=?, industry=?, description=?, status=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, job.getJobTitle());
            ps.setString(2, job.getCompany());
            ps.setString(3, job.getLocation());
            ps.setString(4, job.getSalary());
            ps.setString(5, job.getJobType());
            ps.setString(6, job.getCategory());
            ps.setString(7, job.getIndustry()); // NEW
            ps.setString(8, job.getDescription());
            ps.setString(9, job.getStatus());
            ps.setInt(10, job.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete job by ID
    public static boolean deleteJob(int id) {
        String sql = "DELETE FROM jobs WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get job by ID
    public static JobData getJobById(int id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToJob(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all jobs
    public static List<JobData> getAllJobs() {
        return getJobsByCategory(null);
    }

    // Get all student gigs
    public static List<JobData> getAllGigs() {
        return getJobsByCategory("gig");
    }

    // Get jobs/gigs posted by a specific employer
    public static List<JobData> getJobsByEmployer(String employer) {
        String sql = "SELECT * FROM jobs WHERE posted_by = ?";
        List<JobData> jobs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapResultSetToJob(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }

    // Helper: Get jobs by category
    private static List<JobData> getJobsByCategory(String category) {
        String sql = "SELECT * FROM jobs";
        if (category != null) {
            sql += " WHERE category = ?";
        }
        List<JobData> jobs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (category != null) {
                ps.setString(1, category);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapResultSetToJob(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }

    // FIXED: Updated to match latest JobData constructor (with industry)
    private static JobData mapResultSetToJob(ResultSet rs) throws SQLException {
        return new JobData(
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
                rs.getString("industry") // NEW
        );
    }
}
