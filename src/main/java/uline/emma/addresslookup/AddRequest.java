package uline.emma.addresslookup;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class AddRequest {
    @NotEmpty(message = "Site cannot be empty")
    private String site;
    @NotEmpty(message = "Email cannot be empty")
    @Email
    private String email;
    private String firstname;
    private String lastname;

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "{" +
                "site='" + site + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}