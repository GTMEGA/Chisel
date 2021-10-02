package team.chisel.client.render;

import team.chisel.ctmlib.RenderBlocksCTM;

public class SubmapManagerEmissiveCTM extends SubmapManagerBaseExtraCTM {
    public SubmapManagerEmissiveCTM(String texturePath) {
        super(texturePath + "-glow");
    }

    protected RenderBlocksCTM getRenderBlocks() {
        return new SubmapManagerEmissive.RenderBlocksCTMFullbright();
    }
}
