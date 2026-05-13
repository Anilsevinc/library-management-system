package com.anils.library.service;

import com.anils.library.core.Library;
import com.anils.library.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ödünç / iade iş kuralları: kitap müsaitliği, kullanıcı limiti, kim hangi kitapta (Map), ücret kesme ve iade.
 */
public class LibraryService {

    /** Varsayılan sabit ücret; {@link FixedFeeCalculator} ile kullanılır. */
    public static final double DEFAULT_BORROW_FEE = 50.0;

    private final Library library;
    private final FeeCalculator feeCalculator;
    /** Ödünçteki kitap id → kullanıcı id */
    private final Map<Long, Long> borrowedBookToUser = new HashMap<>();

    public LibraryService(Library library, FeeCalculator feeCalculator) {
        this.library = Objects.requireNonNull(library);
        this.feeCalculator = Objects.requireNonNull(feeCalculator);
    }

    public Optional<Long> findBorrowerUserId(long bookId) {
        return Optional.ofNullable(borrowedBookToUser.get(bookId));
    }

    public boolean isBookAvailable(long bookId) {
        if (library.findBookById(bookId).isEmpty()) {
            return false;
        }
        return !borrowedBookToUser.containsKey(bookId);
    }

    public BorrowOutcome borrow(long userId, long bookId) {
        Optional<User> userOpt = library.findUserById(userId);
        if (userOpt.isEmpty()) {
            return BorrowOutcome.USER_NOT_FOUND;
        }
        if (library.findBookById(bookId).isEmpty()) {
            return BorrowOutcome.BOOK_NOT_FOUND;
        }
        if (borrowedBookToUser.containsKey(bookId)) {
            return BorrowOutcome.BOOK_NOT_AVAILABLE;
        }

        User user = userOpt.get();
        if (!user.borrowBook(bookId)) {
            return BorrowOutcome.USER_LIMIT_OR_ALREADY_HAS_BOOK;
        }

        borrowedBookToUser.put(bookId, userId);
        user.applyBorrowFee(feeCalculator.borrowFeeAmount());
        return BorrowOutcome.OK;
    }

    public ReturnOutcome returnBook(long userId, long bookId) {
        Optional<User> userOpt = library.findUserById(userId);
        if (userOpt.isEmpty()) {
            return ReturnOutcome.USER_NOT_FOUND;
        }
        if (library.findBookById(bookId).isEmpty()) {
            return ReturnOutcome.BOOK_NOT_FOUND;
        }

        Long holderId = borrowedBookToUser.get(bookId);
        if (holderId == null) {
            return ReturnOutcome.BOOK_NOT_BORROWED;
        }
        if (holderId != userId) {
            return ReturnOutcome.WRONG_USER;
        }

        User user = userOpt.get();
        borrowedBookToUser.remove(bookId);
        user.returnBook(bookId);
        user.refundBorrowFee(feeCalculator.borrowFeeAmount());
        return ReturnOutcome.OK;
    }
}
