package uline.emma.addresslookup;

import java.util.Calendar;
import java.util.Date;

public class NameBean {
    private String firstname;
    private String lastname;
    private Date lastusedtime;
    public static final Date defualtDate;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        defualtDate = calendar.getTime();
    }

    public NameBean() {
    }

    public NameBean(String firstname, String lastname, Date lastusedtime) {
        this.firstname = firstname == null ? "" : firstname.trim();
        this.lastname = lastname == null ? "" : lastname.trim();
        this.lastusedtime = lastusedtime == null ? defualtDate : lastusedtime;
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