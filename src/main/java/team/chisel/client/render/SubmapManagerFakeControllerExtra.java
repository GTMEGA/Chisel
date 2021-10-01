package team.chisel.client.render;

import net.minecraft.world.IBlockAccess;
import team.chisel.ctmlib.CTM;
import team.chisel.ctmlib.RenderBlocksCTM;
import team.chisel.init.ChiselBlocks;

import static team.chisel.ctmlib.Dir.*;
import static team.chisel.ctmlib.Dir.RIGHT;

public class SubmapManagerFakeControllerExtra extends SubmapManagerBaseExtra {
    public SubmapManagerFakeControllerExtra(String texturePath) {
        super(texturePath);
    }

    @Override
    protected RenderBlocksCTM getRenderBlocks() {
        RenderBlocksCTM renderBlocks = super.getRenderBlocks();
        renderBlocks.ctm = CTMFakeController.getInstance();
        return renderBlocks;
    }

    public static class CTMFakeController extends CTM {
        public static CTMFakeController getInstance() {
            return new CTMFakeController();
        }

        @Override
        public int[] getSubmapIndices(IBlockAccess world, int x, int y, int z, int side) {
            int meta = world.getBlockMetadata(x, y, z);
            buildConnectionMap(world, x, y, z, side, ChiselBlocks.futura, meta);
            if (connectedAnd(TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT)) {
                return new int[]{14, 15, 11, 10};
            } else if (connectedAnd(TOP, BOTTOM)) {
                return new int[]{12, 13, 9, 8};
            } else if (connectedAnd(LEFT, RIGHT)) {
                return new int[]{6, 7, 3, 2};
            } else {
                return new int[]{4, 5, 1, 0};
            }
        }
    }
}
