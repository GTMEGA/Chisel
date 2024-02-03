package team.chisel;

import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;

import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.common.Loader;

public class ClientCompat {
    private static boolean falseTweaksPresent = false;
    public static void init() {
        falseTweaksPresent = Loader.isModLoaded("falsetweaks");
    }
    public static Tessellator getTessellator() {
        if (falseTweaksPresent && ThreadedChunkUpdates.isEnabled())
            return ThreadedChunkUpdates.getThreadTessellator();

        return Tessellator.instance;
    }
}
