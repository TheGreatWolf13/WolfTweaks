package tgw.wolf_tweaks.patches;

public interface PatchAbstractMinecart {

    boolean isChunkLoader();

    void setChunkLoaderName(String name);

    void setChunkLoaderNameFromInventory();

    void startChunkLoader();

    void stopChunkLoader();
}
