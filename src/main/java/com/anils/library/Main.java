package com.anils.library;

import com.anils.library.core.Library;
import com.anils.library.model.Book;
import com.anils.library.model.LibraryItem;
import com.anils.library.model.User;
import com.anils.library.service.BorrowOutcome;
import com.anils.library.service.FixedFeeCalculator;
import com.anils.library.service.LibraryService;
import com.anils.library.service.ReturnOutcome;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Library catalog = new Library();

        FixedFeeCalculator feeCalculator = new FixedFeeCalculator(LibraryService.DEFAULT_BORROW_FEE);
        LibraryService libraryService = new LibraryService(catalog, feeCalculator);
        LibrarySession session = new LibrarySession(catalog, libraryService, feeCalculator);

        try (Scanner scanner = new Scanner(System.in)) {
            runMenuLoop(scanner, session);
        }
    }

    /** Polimorfizm: {@link LibraryItem} alt tipi raftaki bölümü kendi kurallarıyla döner. */
    private static void printShelfInfo(LibraryItem item) {
        System.out.println(item.getTitle() + " → raf/bölüm: " + item.getShelfSection());
    }

    private static void runMenuLoop(Scanner scanner, LibrarySession session) {
        boolean running = true;
        while (running) {
            printMainMenu(session);
            String choice = scanner.nextLine().trim();
            MenuOption option = MenuOption.fromCode(choice);
            if (option == null) {
                System.out.println("Geçersiz seçim.\n");
                continue;
            }
            running = option.handle(scanner, session);
            System.out.println();
        }
    }

    private static void printMainMenu(LibrarySession session) {
        System.out.println("=== Kütüphane otomasyonu ===");
        System.out.println("Geçerli ödünç ücreti: " + session.fees().borrowFeeAmount());
        for (MenuOption option : MenuOption.values()) {
            System.out.println(option.code + " — " + option.label);
        }
        System.out.print("Seçiminiz: ");
    }

    /**
     * Menü seçenekleri: enum ile soyut metot → çalışma zamanında doğru davranış (polimorfizm).
     */
    private enum MenuOption {
        EXIT("0", "Çıkış") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                System.out.println("Güle güle.");
                return false;
            }
        },
        ADD_BOOK("1", "Kitap ekle") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                Library c = s.catalog();
                long suggested = c.suggestNextBookId();
                System.out.print("Kitap id (Enter = önerilen " + suggested + "): ");
                String idLine = sc.nextLine().trim();
                long id = idLine.isEmpty() ? suggested : readLong(idLine, sc);
                System.out.print("Başlık: ");
                String title = sc.nextLine().trim();
                System.out.print("Yazar: ");
                String author = sc.nextLine().trim();
                System.out.print("Kategori: ");
                String category = sc.nextLine().trim();
                Book book = new Book(id, title, author, category);
                c.saveBook(book);
                printShelfInfo(book);
                System.out.println("Kitap kaydedildi.");
                return true;
            }
        },
        FIND_BOOK("2", "Kitap ara (id / başlık / yazar)") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                Library c = s.catalog();
                System.out.println("a) id  b) başlıkta ara  c) yazar");
                System.out.print("Alt seçim: ");
                String sub = sc.nextLine().trim().toLowerCase(Locale.ROOT);
                switch (sub) {
                    case "a" -> {
                        System.out.print("Kitap id: ");
                        long id = readLong(sc.nextLine().trim(), sc);
                        Optional<Book> book = c.findBookById(id);
                        book.ifPresentOrElse(System.out::println, () -> System.out.println("Bulunamadı."));
                    }
                    case "b" -> {
                        System.out.print("Başlık parçası: ");
                        printBooks(c.findBooksByTitleContaining(sc.nextLine().trim()));
                    }
                    case "c" -> {
                        System.out.print("Yazar adı: ");
                        printBooks(c.findBooksByAuthor(sc.nextLine().trim()));
                    }
                    default -> System.out.println("Geçersiz alt seçim.");
                }
                return true;
            }
        },
        UPDATE_BOOK("3", "Kitap güncelle") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                Library c = s.catalog();
                System.out.print("Güncellenecek kitap id: ");
                long id = readLong(sc.nextLine().trim(), sc);
                Optional<Book> opt = c.findBookById(id);
                if (opt.isEmpty()) {
                    System.out.println("Kitap yok.");
                    return true;
                }
                Book book = opt.get();
                System.out.print("Yeni başlık (boş = değiştirme): ");
                String title = sc.nextLine();
                System.out.print("Yeni yazar (boş = değiştirme): ");
                String author = sc.nextLine();
                System.out.print("Yeni kategori (boş = değiştirme): ");
                String category = sc.nextLine();
                if (!title.trim().isEmpty()) {
                    book.setTitle(title.trim());
                }
                if (!author.trim().isEmpty()) {
                    book.setAuthor(author.trim());
                }
                if (!category.trim().isEmpty()) {
                    book.setCategory(category.trim());
                }
                c.saveBook(book);
                System.out.println("Güncellendi: " + book);
                return true;
            }
        },
        DELETE_BOOK("4", "Kitap sil") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                Library c = s.catalog();
                System.out.print("Silinecek kitap id: ");
                long id = readLong(sc.nextLine().trim(), sc);
                if (s.library().findBorrowerUserId(id).isPresent()) {
                    System.out.println("Ödünçte olan kitap silinemez.");
                    return true;
                }
                boolean removed = c.deleteBookById(id);
                System.out.println(removed ? "Silindi." : "Kitap bulunamadı.");
                return true;
            }
        },
        LIST_BY_CATEGORY("5", "Kategoriye göre listele") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                System.out.print("Kategori: ");
                printBooks(s.catalog().findBooksByCategory(sc.nextLine().trim()));
                return true;
            }
        },
        LIST_BY_AUTHOR("6", "Yazara göre listele") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                System.out.print("Yazar: ");
                printBooks(s.catalog().findBooksByAuthor(sc.nextLine().trim()));
                return true;
            }
        },
        LIST_ALL_BOOKS("7", "Tüm kitapları listele") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                printBooks(s.catalog().findAllBooks());
                return true;
            }
        },
        ADD_USER("8", "Kullanıcı ekle") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                Library c = s.catalog();
                long suggested = c.suggestNextUserId();
                System.out.print("Kullanıcı id (Enter = önerilen " + suggested + "): ");
                String idLine = sc.nextLine().trim();
                long id = idLine.isEmpty() ? suggested : readLong(idLine, sc);
                System.out.print("İsim: ");
                String name = sc.nextLine().trim();
                c.saveUser(new User(id, name));
                System.out.println("Kullanıcı kaydedildi.");
                return true;
            }
        },
        BORROW("9", "Kitap ödünç al") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                System.out.print("Kullanıcı id: ");
                long userId = readLong(sc.nextLine().trim(), sc);
                System.out.print("Kitap id: ");
                long bookId = readLong(sc.nextLine().trim(), sc);
                BorrowOutcome outcome = s.library().borrow(userId, bookId);
                switch (outcome) {
                    case OK -> System.out.println("Ödünç verildi. Kesilen ücret: " + s.fees().borrowFeeAmount());
                    case USER_NOT_FOUND -> System.out.println("Kullanıcı bulunamadı.");
                    case BOOK_NOT_FOUND -> System.out.println("Kitap bulunamadı.");
                    case BOOK_NOT_AVAILABLE -> System.out.println("Kitap başka kullanıcıda.");
                    case USER_LIMIT_OR_ALREADY_HAS_BOOK -> System.out.println("Limit dolu veya kitap zaten bu kullanıcıda.");
                }
                return true;
            }
        },
        RETURN_BOOK("10", "Kitap iade et") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                System.out.print("Kullanıcı id: ");
                long userId = readLong(sc.nextLine().trim(), sc);
                System.out.print("Kitap id: ");
                long bookId = readLong(sc.nextLine().trim(), sc);
                ReturnOutcome outcome = s.library().returnBook(userId, bookId);
                switch (outcome) {
                    case OK -> System.out.println("İade alındı. Ücret iade edildi: " + s.fees().borrowFeeAmount());
                    case USER_NOT_FOUND -> System.out.println("Kullanıcı bulunamadı.");
                    case BOOK_NOT_FOUND -> System.out.println("Kitap bulunamadı.");
                    case BOOK_NOT_BORROWED -> System.out.println("Kitap ödünçte değil.");
                    case WRONG_USER -> System.out.println("Bu kitap bu kullanıcıda kayıtlı değil.");
                }
                return true;
            }
        },
        WHO_HAS_BOOK("11", "Kitabın ödünçteki kullanıcısı") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                System.out.print("Kitap id: ");
                long bookId = readLong(sc.nextLine().trim(), sc);
                Optional<Long> borrower = s.library().findBorrowerUserId(bookId);
                if (borrower.isEmpty()) {
                    System.out.println("Kitap rafta veya kayıtlı değil.");
                    return true;
                }
                long uid = borrower.get();
                s.catalog().findUserById(uid).ifPresentOrElse(
                        u -> System.out.println("Ödünçte: kullanıcı id=" + uid + ", ad=" + u.getName()),
                        () -> System.out.println("Ödünç kaydı var ama kullanıcı bulunamadı (id=" + uid + ")."));
                return true;
            }
        },
        STATS("12", "Özet (depo boyutları)") {
            @Override
            boolean handle(Scanner sc, LibrarySession s) {
                Library c = s.catalog();
                System.out.println("Kitap sayısı: " + c.bookCount());
                System.out.println("Kullanıcı sayısı: " + c.userCount());
                return true;
            }
        };

        private final String code;
        private final String label;

        MenuOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        static MenuOption fromCode(String code) {
            for (MenuOption o : values()) {
                if (o.code.equals(code)) {
                    return o;
                }
            }
            return null;
        }

        abstract boolean handle(Scanner scanner, LibrarySession session);
    }

    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("(Kayıt yok)");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static long readLong(String line, Scanner scanner) {
        try {
            return Long.parseLong(line.trim());
        } catch (NumberFormatException ex) {
            System.out.print("Sayı geçersiz, tekrar: ");
            return readLong(scanner.nextLine(), scanner);
        }
    }
}
