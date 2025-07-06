package tgw.wolf_tweaks.util.collection.sets;

public interface SetExtension {

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
