package tgw.wolf_tweaks.util.collection.lists;

public interface ListExtension {
    
    void clear();

    default void reset() {
        this.clear();
        this.trim();
    }

    void trim();
}
