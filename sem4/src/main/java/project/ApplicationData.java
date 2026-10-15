package project;

import java.sql.Timestamp;

public class ApplicationData {
    private int id;
    private int jobId;
    private String username;
    private String fullName;
    private String jobTitle;
    private String company;
    private String status; // e.g., "Pending", "Interview", "Hired", "Rejected", "Completed"
    private Timestamp appliedOn;
    private String resumeLink;
    private String email;
    private String phone;
    private String filePath;     // uploaded work / proof of completion
    private String feedback;     // feedback from employer

    // Full constructor
    public ApplicationData(int id, int jobId, String username, String fullName, String jobTitle,
                           String company, String status, Timestamp appliedOn,
                           String resumeLink, String email, String phone,
                           String filePath, String feedback) {
        this.id = id;
        this.jobId = jobId;
        this.username = username != null ? username : "";
        this.fullName = fullName != null ? fullName : "";
        this.jobTitle = jobTitle != null ? jobTitle : "";
        this.company = company != null ? company : "";
        this.status = status != null ? status : "Pending";
        this.appliedOn = appliedOn;
        this.resumeLink = resumeLink != null ? resumeLink : "";
        this.email = email != null ? email : "";
        this.phone = phone != null ? phone : "";
        this.filePath = filePath != null ? filePath : "";
        this.feedback = feedback != null ? feedback : "";
    }

    // Constructor without optional fields
    public ApplicationData(int id, int jobId, String username, String fullName,
                           String jobTitle, String company, String status, Timestamp appliedOn) {
        this(id, jobId, username, fullName, jobTitle, company, status, appliedOn, "", "", "", "", "");
    }

    // Getters
    public int getId() { return id; }
    public int getJobId() { return jobId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public String getStatus() { return status; }
    public Timestamp getAppliedOn() { return appliedOn; }
    public String getResumeLink() { return resumeLink; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getFilePath() { return filePath; }
    public String getFeedback() { return feedback; }

    // Setters
    public void setStatus(String status) { this.status = status != null ? status : ""; }
    public void setResumeLink(String resumeLink) { this.resumeLink = resumeLink != null ? resumeLink : ""; }
    public void setFilePath(String filePath) { this.filePath = filePath != null ? filePath : ""; }
    public void setFeedback(String feedback) { this.feedback = feedback != null ? feedback : ""; }

    // Convenience methods
    public String getAppliedDate() {
        return appliedOn != null ? appliedOn.toLocalDateTime().toLocalDate().toString() : "";
    }

    public boolean hasUploadedWork() {
        return filePath != null && !filePath.trim().isEmpty();
    }

    public boolean hasFeedback() {
        return feedback != null && !feedback.trim().isEmpty();
    }

    /**
     * Checks if the completed work is accepted by the employer.
     * A job is considered accepted if status is "Completed" and the employee uploaded work/proof.
     */
    public boolean isAcceptedByEmployer() {
        return "completed".equalsIgnoreCase(this.status) && hasUploadedWork();
    }

    @Override
    public String toString() {
        return "ApplicationData{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
