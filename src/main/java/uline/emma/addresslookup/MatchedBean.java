package uline.emma.addresslookup;

import java.util.Date;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchedBean that = (MatchedBean) o;
        return email.equalsIgnoreCase(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email.toLowerCase());
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