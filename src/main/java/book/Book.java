package book;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Book {
    public String name;
    public String author;
    public String description;
    public String uri;
    @JsonIgnore
    @ManyToOne
    private Account account;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    Book() { // jpa only
    }

    public Book(Account account, String name, String author, String description) {
        this.name = name;
        this.author = author;
        this.account = account;
        this.description = description;
        this.uri = "http://localhost:8090/" + account.getId() + "/books/" + id;
    }

    public Account getAccount() {
        return account;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    void setAccount (Account account){
        this.account = account;
    }

    void updateFrom(Book source) {
        this.name = source.getName();
        this.author = source.getAuthor();
        this.description = source.getDescription();
    }
}