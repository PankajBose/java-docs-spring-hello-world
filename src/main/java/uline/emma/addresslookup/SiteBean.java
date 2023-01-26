package uline.emma.addresslookup;

public class SiteBean {
    private String sitename;
    private String displayname;
    private String emailaddress;

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    @Override
    public String toString() {
        return "SiteBean{" +
                "sitename='" + sitename + '\'' +
                ", displayname='" + displayname + '\'' +
                ", emailaddress='" + emailaddress + '\'' +
                '}';
    }
}
