package com.bookadaisical.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bookadaisical.dto.requests.BookIdDto;
import com.bookadaisical.dto.requests.BookSearchFiltersDto;
import com.bookadaisical.dto.requests.CreateNewBookDto;
import com.bookadaisical.dto.requests.UsernameDto;
import com.bookadaisical.dto.responses.AuthorsDto;
import com.bookadaisical.dto.responses.BookDto;
import com.bookadaisical.dto.responses.PopularGenreDto;
import com.bookadaisical.enums.Genre;
import com.bookadaisical.exceptions.BookNotFoundException;
import com.bookadaisical.exceptions.UserNotFoundException;
import com.bookadaisical.model.Book;
import com.bookadaisical.model.Image;
import com.bookadaisical.model.User;
import com.bookadaisical.repository.BookRepository;
import com.bookadaisical.repository.UserRepository;
import com.bookadaisical.repository.specifications.BookSpecification;
import com.bookadaisical.service.interfaces.IBookService;
import com.bookadaisical.utils.GenreCount;

import jakarta.security.auth.message.config.AuthConfig;

@Service
public class BookService implements IBookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookService(BookRepository bookRepository,
                        UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<BookDto> getTopTenBooks() {
        List<Book> books = new ArrayList<>();
        int daysBack = 30;
        Pageable topTen = PageRequest.of(0, 10);

        while (books.size() < 10 && daysBack <= 180) {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysBack);
            books = bookRepository.findBooksByUploadDateAndViews(startDate, topTen);
            daysBack *= 2;
        }

        return books.stream().map(book -> new BookDto(book)).collect(Collectors.toList());
    }

    @Override
    public List<BookDto> getRecentlyAddedBooks(){
        List<Book> books = bookRepository.findTop10ByIsActiveOrderByCreatedOnDesc(
            true,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdOn"))
        );

        return books.stream().map(book -> new BookDto(book)).collect(Collectors.toList());
    }

    @Override
    public List<BookDto> getAllBooks() {
        List<Book> books = bookRepository.findAll();

        return books.stream().map(book -> new BookDto(book)).collect(Collectors.toList());
    }

    public List<BookDto> getFilteredBooks(BookSearchFiltersDto filters) {
        BookSpecification spec = new BookSpecification(filters);
        List<Book> books = bookRepository.findAll(spec);

        return books.stream().map(book -> new BookDto(book)).collect(Collectors.toList());
    }

    @Override
    public List<BookDto> getBooksByUploaderUsername(String username) {
        List<Book> books = bookRepository.findAllBooksByUploaderUsername(username);

        return books.stream().map(book -> new BookDto(book)).collect(Collectors.toList());
    }

    @Override
    public List<PopularGenreDto> getMostPopularCategories(){

        List<GenreCount> topGenres = bookRepository.findTop4GenresByPopularity(PageRequest.of(0, 3));

        return topGenres.stream()
                        .map(genreCount -> {
                            Genre genre = genreCount.getGenre();
                            return bookRepository.findMostPopularActiveBookByGenre(genre)
                                                 .stream()
                                                 .findFirst()
                                                 .map(book -> new PopularGenreDto(genre, new BookDto(book)))
                                                 .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    @Override
    public void uploadNewBook(CreateNewBookDto createNewBookDto) throws Exception {
        Book book = createNewBookDtoToBook(createNewBookDto);

        bookRepository.save(book);
    }

    private Book createNewBookDtoToBook(CreateNewBookDto createNewBookDto) throws Exception {
        Book book = new Book();

        Optional<User> user = userRepository.findByUsername(createNewBookDto.getUploaderUsername()); 
        if(user.isPresent()){
            book.setUploader(user.get());
        } else {
            throw new UserNotFoundException();
        }

        book.setTitle(createNewBookDto.getTitle());
        book.setAuthor(createNewBookDto.getAuthor());
        book.setNumViews(0);
        book.setDescription(createNewBookDto.getDescription());
        book.setCreatedOn(LocalDateTime.now());
        book.setLastModified(book.getCreatedOn());
        book.setYearOfPublication(createNewBookDto.getYearOfPublication());
        book.setArtisticMovement(createNewBookDto.getArtisticMovement());
        book.setTargetAudience(createNewBookDto.getTargetAudience());
        book.setBookCondition(createNewBookDto.getCondition());
        book.setActive(true);
        for(String imageData : createNewBookDto.getImages())
        {
            Image image = new Image();
            image.setImageData(Base64.getDecoder().decode(imageData));
            book.addImage(image);
        }

        book.setGenres(createNewBookDto.getGenres());
        book.setTradingOptions(createNewBookDto.getTradingOptions());

        return book;
    }

    @Override
    public List<BookDto> getRecommendedBooks(UsernameDto usernameDto) throws Exception
    {
        List<BookDto> books = new ArrayList<>();
        Optional<User> user = userRepository.findByUsername(usernameDto.getUsername());

        if(user.isPresent())
        {
            List<Book> userBooks = bookRepository.findAllBooksByUploaderUsername(user.get().getUsername());
            for(Book book : userBooks)
            {
                books.add(new BookDto(book));
                if(books.size() == 10) break;
            }
        }

        if(books.size() < 10)
        {
            List<Book> allBooks = bookRepository.findAll();
            for(Book book : allBooks)
            {
                books.add(new BookDto(book));
                if(books.size() == 10) break;
            }
        }

        return books;
    }

    @Override
    public void updateView(BookIdDto bookIdDto) throws Exception
    {
        Optional<Book> book = bookRepository.findById(bookIdDto.getId());
        if(book.isEmpty()) throw new BookNotFoundException();

        book.get().setNumViews(book.get().getNumViews() + 1);
        bookRepository.save(book.get());
    }

    @Override
    public AuthorsDto getAuthors() throws Exception
    {
        AuthorsDto autors = new AuthorsDto();
        List<Book> books = bookRepository.findAll();

        for(Book book : books)
        {
            autors.getAuthors().add(book.getAuthor());
        }

        return autors;
    }
}
