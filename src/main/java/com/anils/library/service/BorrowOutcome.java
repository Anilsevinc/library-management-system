package com.anils.library.service;

public enum BorrowOutcome {
    OK,
    USER_NOT_FOUND,
    BOOK_NOT_FOUND,
    BOOK_NOT_AVAILABLE,
    USER_LIMIT_OR_ALREADY_HAS_BOOK
}
