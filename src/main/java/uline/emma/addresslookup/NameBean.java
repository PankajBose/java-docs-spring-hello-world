package uline.emma.addresslookup;

import java.util.Date;

public class NameBean {
    private String firstname;
    private String lastname;
    private Date lastusedtime;

    public NameBean() {
    }

    public NameBean(String firstname, String lastname, Date lastusedtime) {
        this.firstname = firstname == null ? "" : firstname.trim();
        this.lastname = lastname == null ? "" : lastname.trim();
        this.lastusedtime = lastusedtime;
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

    public Date getLastusedtime() {
        return lastusedtime;
    }

    public void setLastusedtime(Date lastusedtime) {
        this.lastusedtime = lastusedtime;
    }

    public String getName() {
        int firstnameLength = firstname.length();
        int lastnameLength = lastname.length();

        if (firstnameLength == 0 && lastnameLength == 0) return "";

        if (lastnameLength == 0) return firstname;

        if (firstnameLength == 0) return lastname;

        return firstname + " " + lastname;
    }
}