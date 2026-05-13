package com.anils.library.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User implements Identifiable {

    public static final int MAX_BORROWED_BOOKS = 5;

    private long id;
    private String name;
    private double balance;
    private final Set<Long> borrowedBookIds = new HashSet<>();

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    /** Ödünç alırken kesilen ücret (bakiyeden düşülür). */
    public void applyBorrowFee(double fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("fee must be non-negative");
        }
        balance -= fee;
    }

    /** İade sırasında ücretin geri yüklenmesi. */
    public void refundBorrowFee(double fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("fee must be non-negative");
        }
        balance += fee;
    }

    /**
     * Dışarıya doğrudan Set referansı verilmez; liste değiştirilemez görünüm.
     */
    public Set<Long> getBorrowedBookIds() {
        return Collections.unmodifiableSet(borrowedBookIds);
    }

    public int getBorrowedCount() {
        return borrowedBookIds.size();
    }

    public boolean isBorrowing(long bookId) {
        return borrowedBookIds.contains(bookId);
    }

    /**
     * @return false limit doluysa veya kitap zaten bu kullanıcıdaysa
     */
    public boolean borrowBook(long bookId) {
        if (borrowedBookIds.size() >= MAX_BORROWED_BOOKS) {
            return false;
        }
        return borrowedBookIds.add(bookId);
    }

    public boolean returnBook(long bookId) {
        return borrowedBookIds.remove(bookId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", borrowedBookIds=" + borrowedBookIds +
                '}';
    }
}
