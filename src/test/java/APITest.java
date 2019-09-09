import book.Account;
import book.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class APITest {
    private static final Faker faker = new Faker();

    @Test
    public void completeFlowTest(){
        Account userA = new Account(faker.name().username(), faker.internet().password());
        Long userAId = ((Integer)addUser(userA)
                                        .then()
                                        .extract()
                                        .path("id"))
                                        .longValue();

        Account userB = new Account(faker.name().username(), faker.internet().password());

        Book bookA = new Book(userA, "Book A", "Author A", "Book A Description");
        Long bookAId = ((Integer)addBook(userAId, bookA)
                                        .then()
                                        .extract()
                                        .path("id"))
                                        .longValue();

        Book bookB = new Book(userA, "Book B", "Author B", "Book B Description");
        Long bookBId = ((Integer)addBook(userAId, bookB)
                                        .then()
                                        .extract()
                                        .path("id"))
                                        .longValue();
        Book bookC = new Book(userA, "Book C", "Author C", "Book C Description");

        removeBook(userAId, bookBId);
        replaceBook(userAId, bookAId, bookC);

        replaceUser(userAId, userB);

        Book bookD = new Book(userA, "Book D", "Author D", "Book D Description");
        addBook(userAId, bookD);

        removeUser(userAId);
    }

    @Test
    public void addUserTest(){
        Account user = new Account(faker.name().username(), faker.internet().password());
        Response response = addUser(user);
        response.then().statusCode(SC_CREATED).log().all();

        Long userId = ((Integer) response.then().extract().path("id")).longValue();
        removeUser(userId);
    }

    @Test
    public void removeUserTest(){
        Account user = new Account(faker.name().username(), faker.internet().password());
        Long userId = ((Integer)addUser(user).then().extract().path("id")).longValue();

        Response response = removeUser(userId);
        response.then().statusCode(SC_OK).log().all();
    }

    @Test
    public void addBookTest(){
        Account user = new Account(faker.name().username(), faker.internet().password());
        Long userId = ((Integer)addUser(user).then().extract().path("id")).longValue();

        Book book = new Book(user, "War and Peace", "Leo Tolstoy", "War and Peace Description");
        Response response = addBook(userId, book);
        response.then().statusCode(SC_CREATED).log();

        removeUser(userId);
    }

    @Test
    public void removeBookTest(){
        Account user = new Account(faker.name().username(), faker.internet().password());
        Long userId = ((Integer)addUser(user).then().extract().path("id")).longValue();

        Book book = new Book(user, "Fyodor Dostoevsky", "Crime and Punishment", "Crime and Punishment Description");
        Long bookId = ((Integer)addBook(userId, book).then().extract().path("id")).longValue();

        Response response = removeBook(userId, bookId);
        response.then().statusCode(SC_OK).log();

        removeUser(userId);
    }

    @Test
    public void replaceUserTest(){
        Account user = new Account(faker.name().username(), faker.internet().password());
        Long userId = ((Integer)addUser(user).then().extract().path("id")).longValue();

        Book book = new Book(user, "Alexander Pushkin", "Eugene Onegin", "Eugene Onegin Description");
        addBook(userId, book);

        Account newUser = new Account(faker.name().username(), faker.internet().password());

        Response response = replaceUser(userId, newUser);
        response.then().statusCode(SC_CREATED).log();

        removeUser(userId);
    }

    @Test
    public void replaceBookTest(){
        Account user = new Account(faker.name().username(), faker.internet().password());
        Long userId = ((Integer)addUser(user).then().extract().path("id")).longValue();

        Book book = new Book(user, "Anton Chekhov", "The Seagull", "The Seagull Description");
        Long bookId = ((Integer)addBook(userId, book).then().extract().path("id")).longValue();

        Book newBook = new Book(user, "Nikolai Gogol", "Dead Souls", "Dead Souls Description");

        Response response = replaceBook(userId, bookId, newBook);
        response.then().statusCode(SC_CREATED).log().all();

        removeUser(userId);
    }

    private Response addUser(Account user){
        String url = "http://localhost:8090/users";
        ObjectMapper mapper = new ObjectMapper();
        RequestSpecification requestSpecification = null;

        try {
            requestSpecification = given().contentType(ContentType.JSON).body(mapper.writeValueAsBytes(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return requestSpecification.post(url);
    }

    private Response removeUser(Long userId){
        String url = "http://localhost:8090/users/" + userId;
        RequestSpecification requestSpecification = given().contentType(ContentType.JSON);
        return requestSpecification.delete(url);
    }

    private Response addBook(Long userId, Book book){
        ObjectMapper mapper = new ObjectMapper();
        String url = String.format("http://localhost:8090/%d/books", userId);
        RequestSpecification requestSpecification = null;
        try {
            requestSpecification = given().contentType(ContentType.JSON).body(mapper.writeValueAsBytes(book));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return requestSpecification.post(url);
    }

    private Response removeBook(Long userId, Long bookId){
        String url = String.format("http://localhost:8090/%d/books/%d", userId, bookId);
        RequestSpecification requestSpecification = given().contentType(ContentType.JSON);
        return requestSpecification.delete(url);
    }

    private Response replaceUser(Long userId, Account newUser){
        ObjectMapper mapper = new ObjectMapper();
        String url = "http://localhost:8090/users/" + userId;

        RequestSpecification requestSpecification = null;
        try {
            requestSpecification = given().contentType(ContentType.JSON).body(mapper.writeValueAsBytes(newUser));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return requestSpecification.put(url);
    }

    private Response replaceBook(Long userId, Long bookId, Book newBook) {
        ObjectMapper mapper = new ObjectMapper();
        String url = String.format("http://localhost:8090/%d/books/%d", userId, bookId);

        RequestSpecification requestSpecification = null;
        try {
            requestSpecification = given().contentType(ContentType.JSON).body(mapper.writeValueAsBytes(newBook));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return requestSpecification.put(url);
    }
}