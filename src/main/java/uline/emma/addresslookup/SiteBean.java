package uline.emma.addresslookup;

import java.util.Date;

public class SiteBean {
    private String id;
    private String sitename;
    private String firstname;
    private String lastname;
    private String displayname;
    private String emailaddress;
    private Date lastusedtime;

    public SiteBean() {
    }

    public SiteBean(String id, String sitename, String firstname, String lastname, String displayname, String emailaddress, Date lastusedtime) {
        this.id = id;
        this.sitename = sitename;
        this.firstname = firstname;
        this.lastname = lastname;
        this.displayname = displayname;
        this.emailaddress = emailaddress;
        this.lastusedtime = lastusedtime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public Date getLastusedtime() {
        return lastusedtime;
    }

    public void setLastusedtime(Date lastusedtime) {
        this.lastusedtime = lastusedtime;
    }
}