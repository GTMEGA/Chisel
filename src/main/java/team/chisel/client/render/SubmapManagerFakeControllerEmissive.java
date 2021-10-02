package team.chisel.client.render;

import team.chisel.ctmlib.RenderBlocksCTM;

public class SubmapManagerFakeControllerEmissive extends SubmapManagerEmissiveCTM {
    public SubmapManagerFakeControllerEmissive(String texturePath) {
        super(texturePath);
    }

    @Override
    protected RenderBlocksCTM getRenderBlocks() {
        RenderBlocksCTM renderBlocks = super.getRenderBlocks();
        renderBlocks.ctm = SubmapManagerFakeControllerExtra.CTMFakeController.getInstance();
        return renderBlocks;
    }
}
