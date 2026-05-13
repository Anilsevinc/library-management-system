package com.anils.library;

import com.anils.library.core.Library;
import com.anils.library.service.FeeCalculator;
import com.anils.library.service.LibraryService;

import java.util.Objects;

/**
 * Konsol katmanına kütüphane verisi ve servisleri bir arada verir (composition).
 */
public final class LibrarySession {

    private final Library catalog;
    private final LibraryService libraryService;
    private final FeeCalculator feeCalculator;

    public LibrarySession(Library catalog, LibraryService libraryService, FeeCalculator feeCalculator) {
        this.catalog = Objects.requireNonNull(catalog);
        this.libraryService = Objects.requireNonNull(libraryService);
        this.feeCalculator = Objects.requireNonNull(feeCalculator);
    }

    public Library catalog() {
        return catalog;
    }

    public LibraryService library() {
        return libraryService;
    }

    public FeeCalculator fees() {
        return feeCalculator;
    }
}
