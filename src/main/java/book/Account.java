package book;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


@Entity
public class Account {

    @JsonIgnore
    public String password;
    public String username;
    @OneToMany(mappedBy = "account")
    private Set<Book> books = new HashSet<>();
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Account(String name, String password) {
        this.username = name;
        this.password = password;
    }

    Account() { // jpa only
    }

    public Set<Book> getBooks() {
        return books;
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    void updateFrom(Account source) {
        this.username = source.getUsername();
        this.password = source.getPassword();
    }
}