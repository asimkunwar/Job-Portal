package project;

public class UserData {
    private String username; // added - primary key
    private String password;
    private String email;
    private String type; // "jobseeker" or "employer"
    private String fullName;
    private String phone;
    private String location;
    private String bio;
    private String resumeLink;
    private String profilePic;
    private String company;
    private String website;

    // Full constructor
    public UserData(String username, String password, String email, String type, String fullName,
                    String phone, String location, String bio, String resumeLink,
                    String profilePic, String company, String website) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.type = type;
        this.fullName = fullName;
        this.phone = phone;
        this.location = location;
        this.bio = bio;
        this.resumeLink = resumeLink;
        this.profilePic = profilePic;
        this.company = company;
        this.website = website;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getType() { return type; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getResumeLink() { return resumeLink; }
    public String getProfilePic() { return profilePic; }
    public String getCompany() { return company; }
    public String getWebsite() { return website; }

    // Setters
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setType(String type) { this.type = type; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
    public void setBio(String bio) { this.bio = bio; }
    public void setResumeLink(String resumeLink) { this.resumeLink = resumeLink; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    public void setCompany(String company) { this.company = company; }
    public void setWebsite(String website) { this.website = website; }

    @Override
    public String toString() {
        return "UserData{" +
                "username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", fullName='" + fullName + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}