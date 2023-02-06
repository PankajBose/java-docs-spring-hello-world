package uline.emma.addresslookup;

public class NameBean {
    private String firstname;
    private String lastname;

    public NameBean() {
    }

    public NameBean(String firstname, String lastname) {
        this.firstname = firstname == null ? "" : firstname.trim();
        this.lastname = lastname == null ? "" : lastname.trim();
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

    public String getName() {
        int firstnameLength = firstname.length();
        int lastnameLength = lastname.length();

        if (firstnameLength == 0 && lastnameLength == 0) return "";

        if (lastnameLength == 0) return firstname;

        if (firstnameLength == 0) return lastname;

        return firstname + " " + lastname;
    }
}