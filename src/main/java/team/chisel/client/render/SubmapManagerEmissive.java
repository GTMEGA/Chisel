package team.chisel.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import team.chisel.ctmlib.RenderBlocksCTM;

public class SubmapManagerEmissive extends SubmapManagerBaseExtra {
    public SubmapManagerEmissive(String texturePath) {
        super(texturePath + "-glow");
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlocksCTM getRenderBlocks() {
        RenderBlocksCTM renderBlocks = new RenderBlocksCTMFullbright();
        renderBlocks.ctm = CTMPlain.getInstance();
        return renderBlocks;
    }

    @SideOnly(Side.CLIENT)
    public static class RenderBlocksCTMFullbright extends RenderBlocksCTM {
        @Override
        protected void fillLightmap(int bottomLeft, int bottomRight, int topRight, int topLeft) {
            ao();
            int maxLight = 0xF000F0;
            super.fillLightmap(maxLight, maxLight, maxLight, maxLight);
        }

        @Override
        protected void fillColormap(float bottomLeft, float bottomRight, float topRight, float topLeft, float[][] map) {
            ao();
            int color = 0xFFFFFF;
            super.fillColormap(color, color, color, color, map);
        }

        @Override
        public boolean renderStandardBlock(Block block, int x, int y, int z) {
            boolean ret = super.renderStandardBlock(block, x, y, z);
            this.enableAO = false;
            return ret;
        }

        private void ao() {
            if (this.inWorld) {
                this.enableAO = true;
            }
        }
    }
}
