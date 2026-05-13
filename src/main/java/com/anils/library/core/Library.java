package com.anils.library.core;

import com.anils.library.model.Book;
import com.anils.library.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Kütüphane envanteri: kitap ve kullanıcı kayıtları bellekte {@link Map} ile tutulur.
 */
public final class Library {

    private final Map<Long, Book> booksById = new HashMap<>();
    private final Map<Long, User> usersById = new HashMap<>();

    public void saveBook(Book book) {
        Objects.requireNonNull(book, "book");
        booksById.put(book.getId(), book);
    }

    public Optional<Book> findBookById(long id) {
        return Optional.ofNullable(booksById.get(id));
    }

    public boolean deleteBookById(long id) {
        return booksById.remove(id) != null;
    }

    public List<Book> findAllBooks() {
        return new ArrayList<>(booksById.values());
    }

    public List<Book> findBooksByCategory(String category) {
        String needle = normalize(category);
        List<Book> result = new ArrayList<>();
        for (Book book : booksById.values()) {
            if (normalize(book.getCategory()).equals(needle)) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> findBooksByAuthor(String author) {
        String needle = normalize(author);
        List<Book> result = new ArrayList<>();
        for (Book book : booksById.values()) {
            if (normalize(book.getAuthor()).equals(needle)) {
                result.add(book);
            }
        }
        return result;
    }

    /**
     * Başlıkta geçen metne göre (büyük/küçük harf duyarsız) arar.
     */
    public List<Book> findBooksByTitleContaining(String fragment) {
        String needle = normalize(fragment);
        List<Book> result = new ArrayList<>();
        if (needle.isEmpty()) {
            return result;
        }
        for (Book book : booksById.values()) {
            if (normalize(book.getTitle()).contains(needle)) {
                result.add(book);
            }
        }
        return result;
    }

    public boolean bookExists(long id) {
        return booksById.containsKey(id);
    }

    public void saveUser(User user) {
        Objects.requireNonNull(user, "user");
        usersById.put(user.getId(), user);
    }

    public Optional<User> findUserById(long id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public boolean deleteUserById(long id) {
        return usersById.remove(id) != null;
    }

    public List<User> findAllUsers() {
        return new ArrayList<>(usersById.values());
    }

    public boolean userExists(long id) {
        return usersById.containsKey(id);
    }

    public long suggestNextBookId() {
        return booksById.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1L;
    }

    public long suggestNextUserId() {
        return usersById.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1L;
    }

    public int bookCount() {
        return booksById.size();
    }

    public int userCount() {
        return usersById.size();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
