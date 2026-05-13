package com.anils.library.model;

/**
 * Kütüphanedeki takip edilen öğeler için ortak soyut taban (inheritance).
 * {@link Book} bu hiyerarşiyi genişletir.
 */
public abstract class LibraryItem implements Identifiable {

    private long id;
    private String title;

    protected LibraryItem(long id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** Alt sınıflar raflama / bölüm bilgisini polimorfik olarak verir. */
    public abstract String getShelfSection();
}
