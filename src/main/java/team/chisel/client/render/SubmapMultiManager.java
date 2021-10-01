package team.chisel.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import team.chisel.ctmlib.ISubmapManager;
import team.chisel.ctmlib.RenderBlocksCTM;

import java.util.Arrays;
import java.util.List;

public class SubmapMultiManager implements ISubmapManager {
    protected final List<ISubmapManager> managers;
    protected RenderBlocksCTM rendererWrapper;

    public SubmapMultiManager(ISubmapManager... managers) {
        this.managers = Arrays.asList(managers);

        if (managers.length < 1)
            throw new IllegalArgumentException("SubmapMultiManager needs at least one ISubmapManager!");
    }

    @Override
    public void registerIcons(String modName, Block block, IIconRegister register) {
        for (ISubmapManager manager : managers)
            manager.registerIcons(modName, block, register);
    }

    @Override
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        initRenderContext();
        return rendererWrapper;
    }

    protected void initRenderContext() {
        if (rendererWrapper == null) {
            rendererWrapper = new RenderBlocksWrapper();
        }
    }

    @SideOnly(Side.CLIENT)
    private class RenderBlocksWrapper extends RenderBlocksCTM {
        @Override
        public boolean renderStandardBlock(Block block, int x, int y, int z) {
            boolean ret = false;
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.blockAccess = blockAccess;
                if (lockBlockBounds) {
                    renderBlocks.overrideBlockBounds(renderMinX, renderMinY, renderMinZ, renderMaxX, renderMaxY, renderMaxZ);
                }
                if (renderBlocks instanceof RenderBlocksCTM) {
                    RenderBlocksCTM renderBlocksCTM = (RenderBlocksCTM) renderBlocks;
                    renderBlocksCTM.manager = renderBlocksCTM.manager == null ? manager : renderBlocksCTM.manager;
                    renderBlocksCTM.rendererOld = renderBlocksCTM.rendererOld == null ? rendererOld : renderBlocksCTM.rendererOld;
                }
                ret |= renderBlocks.renderStandardBlock(block, x, y, z);
                renderBlocks.unlockBlockBounds();
            }
            unlockBlockBounds();
            return ret;
        }

        @Override
        public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.renderFaceXNeg(block, x, y, z, manager.getIcon(ForgeDirection.WEST.ordinal(), 0));
            }
        }

        @Override
        public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.renderFaceXPos(block, x, y, z, manager.getIcon(ForgeDirection.EAST.ordinal(), 0));
            }
        }

        @Override
        public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.renderFaceZNeg(block, x, y, z, manager.getIcon(ForgeDirection.NORTH.ordinal(), 0));
            }
        }

        @Override
        public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.renderFaceZPos(block, x, y, z, manager.getIcon(ForgeDirection.SOUTH.ordinal(), 0));
            }
        }

        @Override
        public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon) {
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.renderFaceYNeg(block, x, y, z, manager.getIcon(ForgeDirection.DOWN.ordinal(), 0));
            }
        }

        @Override
        public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
            for (ISubmapManager manager : managers) {
                RenderBlocks renderBlocks = manager.createRenderContext(rendererOld, block, blockAccess);
                renderBlocks.renderFaceYPos(block, x, y, z, manager.getIcon(ForgeDirection.UP.ordinal(), 0));
            }
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return managers.get(0).getIcon(side, meta);
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return managers.get(0).getIcon(world, x, y, z, side);
    }

    @Override
    public void preRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
    }

    @Override
    public void postRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
    }
}
