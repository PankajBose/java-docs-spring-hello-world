package uline.emma.addresslookup;

import java.util.Date;

public class MatchedBean {
    private final String email;
    private final String name;
    private final Date lastUsed;

    public MatchedBean(String email, String name, Date lastUsed) {
        this.email = email;
        this.name = name;
        this.lastUsed = lastUsed;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    @Override
    public String toString() {
        return "{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", lastUsed=" + lastUsed +
                '}';
    }
}