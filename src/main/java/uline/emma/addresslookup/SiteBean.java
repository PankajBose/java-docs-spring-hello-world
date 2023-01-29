package uline.emma.addresslookup;

public class SiteBean {
    private String sitename;
    private String firstname;
    private String lastname;
    private String emailaddress;

    public SiteBean() {
    }

    public SiteBean(String sitename, String firstname, String lastname, String emailaddress) {
        this.sitename = sitename;
        this.firstname = firstname;
        this.lastname = lastname;
        this.emailaddress = emailaddress;
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
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
}