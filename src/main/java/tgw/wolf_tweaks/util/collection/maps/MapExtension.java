package tgw.wolf_tweaks.util.collection.maps;

public interface MapExtension {

    void clear();

    default boolean hasNextIteration(long it) {
        return (int) it != 0;
    }

    default void reset() {
        this.clear();
        this.trim();
    }

    boolean trim();
}
