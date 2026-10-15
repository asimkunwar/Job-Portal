package project;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class JobData {
    private int id = -1; // -1 means not yet saved in DB
    private String jobTitle;
    private String company;
    private String location;
    private String salary;
    private String jobType;
    private String category; // "job" or "gig"
    private String industry; // NEW: Industry field
    private String description;
    private String employer; // username of the poster (posted_by)
    private String status;   // e.g., "active", "closed", "expired"
    private Timestamp posted_date; // keep DB column name

    // Constructor for new jobs (no ID yet)
    public JobData(String jobTitle, String company, String location, String salary,
                   String jobType, String category, String industry, String description, String employer) {
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
        this.category = category;
        this.industry = industry;
        this.description = description;
        this.employer = employer;
        this.status = "active"; // default
        this.posted_date = new Timestamp(System.currentTimeMillis());
    }

    // Constructor for jobs loaded from DB (with ID)
    public JobData(int id, String jobTitle, String company, String location, String salary,
                   String jobType, String category, String description, String employer,
                   String status, Timestamp posted_date, String industry) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
        this.category = category;
        this.description = description;
        this.employer = employer;
        this.status = status != null ? status : "active";
        this.posted_date = posted_date;
        this.industry = industry;
    }

    // Getters
    public int getId() { return id; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getSalary() { return salary; }
    public String getJobType() { return jobType; }
    public String getCategory() { return category; }
    public String getIndustry() { return industry; }
    public String getDescription() { return description; }
    public String getEmployer() { return employer; }
    public String getPostedBy() { return employer; }
    public String getStatus() { return status; }
    public Timestamp getPosted_date() { return posted_date; }

    // Setters
    public void setJobTitle(String jobTitle) {
        this.jobTitle = (jobTitle != null && !jobTitle.trim().isEmpty()) ? jobTitle.trim() : "Untitled Job";
    }

    public void setCompany(String company) { this.company = company != null ? company.trim() : ""; }
    public void setLocation(String location) { this.location = location != null ? location.trim() : ""; }
    public void setSalary(String salary) { this.salary = salary != null ? salary.trim() : ""; }
    public void setJobType(String jobType) { this.jobType = jobType != null ? jobType.trim() : ""; }
    public void setCategory(String category) { this.category = "gig".equalsIgnoreCase(category) ? "gig" : "job"; }
    public void setIndustry(String industry) { this.industry = industry != null ? industry.trim() : ""; }
    public void setDescription(String description) { this.description = description != null ? description.trim() : ""; }
    public void setEmployer(String employer) { this.employer = employer != null ? employer.trim() : ""; }
    public void setStatus(String status) { this.status = status != null ? status.trim() : "active"; }
    public void setPosted_date(Timestamp posted_date) { this.posted_date = posted_date; }

    // Convenience method: Days ago posted
    public long getPostedDaysAgo() {
        if (posted_date == null) return 0;
        return ChronoUnit.DAYS.between(posted_date.toLocalDateTime(), LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "JobData{" +
                "id=" + id +
                ", title='" + jobTitle + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", salary='" + salary + '\'' +
                ", type='" + jobType + '\'' +
                ", category='" + category + '\'' +
                ", industry='" + industry + '\'' +
                ", employer='" + employer + '\'' +
                ", status='" + status + '\'' +
                ", posted_date=" + posted_date +
                ", postedDaysAgo=" + getPostedDaysAgo() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobData jobData = (JobData) o;
        return id == jobData.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
