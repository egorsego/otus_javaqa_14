// tag::runner[]
package book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Optional;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// end::runner[]

@RestController
@RequestMapping("/users")
class UserRestController {

    private final AccountRepository accountRepository;

    private final BookRepository bookRepository;

    @Autowired
    UserRestController(AccountRepository accountRepository, BookRepository bookRepository) {
        this.accountRepository = accountRepository;
        this.bookRepository = bookRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@RequestBody Account account) {
        Account result = this.accountRepository.saveAndFlush(account);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("")
                .buildAndExpand(result.getId()).toUri());
        return new ResponseEntity<>(result, httpHeaders, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Account> readUsers() {
        return this.accountRepository.findAll();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    ResponseEntity<?> remove(@PathVariable Long userId) {
        Optional<Account> result = this.accountRepository.findById(userId);
        Collection<Book> books = this.bookRepository.findByAccountId(userId);
        this.bookRepository.deleteAll(books);
        this.accountRepository.delete(result.get());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("")
                .buildAndExpand(result.get().getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    ResponseEntity<?> replace(@PathVariable Long userId, @RequestBody Account newAccount) {
        Optional<Account> existingAccount = this.accountRepository.findById(userId);
        existingAccount.get().updateFrom(newAccount);
        Collection<Book> books = this.bookRepository.findByAccountId(userId);
        this.bookRepository.deleteAll(books);
        for (Book b: newAccount.getBooks()) {
            this.bookRepository.save(new Book(existingAccount.get(), b.getName(), b.getAuthor(), b.getDescription()));
        }
        this.accountRepository.saveAndFlush(existingAccount.get());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("")
                .buildAndExpand(existingAccount.get().getId()).toUri());
               return new ResponseEntity<>(existingAccount.get(), httpHeaders, HttpStatus.CREATED);
    }
}

@RestController
@RequestMapping("/{userId}/books")
class BookRestController {

    private final BookRepository bookRepository;

    private final AccountRepository accountRepository;

    @Autowired
    BookRestController(BookRepository bookRepository,
                       AccountRepository accountRepository) {
        this.bookRepository = bookRepository;
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable Long userId, @RequestBody Book input) {
        this.validateUser(userId);
        return this.accountRepository.findById(userId).map(account -> {
            Book result = this.bookRepository.saveAndFlush(new Book(account, input.getName(), input.getAuthor(), input.getDescription()));
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }).get();
    }

    @RequestMapping(value = "/{bookId}", method = RequestMethod.DELETE)
    ResponseEntity<?> remove(@PathVariable Long bookId) {
        Book result = this.bookRepository.getOne(bookId);
        this.bookRepository.delete(result);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("")
                .buildAndExpand(result.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/{bookId}", method = RequestMethod.PUT)
    ResponseEntity<?> replace(@PathVariable Long userId, @PathVariable Long bookId, @RequestBody Book newBook) {
        this.validateUser(userId);
        Book book = this.bookRepository.getOne(bookId);
        book.updateFrom(newBook);

        return this.accountRepository.findById(userId).map(account -> {
            this.bookRepository.saveAndFlush(book);
            return new ResponseEntity<>(null, HttpStatus.CREATED);
        }).get();
    }

    @RequestMapping(value = "/{bookId}", method = RequestMethod.GET)
    Optional<Book> readBookmark(@PathVariable Long userId, @PathVariable Long bookId) {
        this.validateUser(userId);
        return this.bookRepository.findById(bookId);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Book> readBookmarks(@PathVariable Long userId) {
        this.validateUser(userId);
        //return this.bookRepository.findByAccountUsername(userId);
        return this.bookRepository.findByAccountId(userId);
    }

    private void validateUser(Long userId) {
        this.accountRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId));
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("could not find user '" + userId + "'.");
    }
}
