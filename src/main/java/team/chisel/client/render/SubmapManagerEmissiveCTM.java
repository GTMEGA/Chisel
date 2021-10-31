package team.chisel.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import team.chisel.ctmlib.RenderBlocksCTM;

public class SubmapManagerEmissiveCTM extends SubmapManagerBaseExtraCTM {
    public SubmapManagerEmissiveCTM(String texturePath) {
        super(texturePath + "-glow");
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlocksCTM getRenderBlocks() {
        return new SubmapManagerEmissive.RenderBlocksCTMFullbright();
    }
}
