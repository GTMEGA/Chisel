package team.chisel.client.render;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import lombok.val;
import team.chisel.ctmlib.CTM;
import team.chisel.ctmlib.ISubmapManager;
import team.chisel.ctmlib.RenderBlocksCTM;
import team.chisel.ctmlib.TextureSubmap;

//Base class for self-contained renderers and sub managers etc
public class SubmapManagerBaseExtra implements ISubmapManager {
    protected final String texturePath;
    @SideOnly(Side.CLIENT)
    private ThreadLocal<Object> renderBlocks;

    @SideOnly(Side.CLIENT)
    protected RenderBlocksCTM renderBlocks() {
        return (RenderBlocksCTM) renderBlocks.get();
    }

    @SideOnly(Side.CLIENT)
    protected void renderBlocks(RenderBlocksCTM rb) {
        renderBlocks.set(rb);
    }

    @SideOnly(Side.CLIENT)
    private void createThreadLocals() {
        renderBlocks = ThreadLocal.withInitial(this::getRenderBlocks);
    }

    {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            createThreadLocals();
        }
    }


    @SideOnly(Side.CLIENT)
    protected TextureSubmap submapSmall;

    public SubmapManagerBaseExtra(String texturePath) {
        this.texturePath = texturePath;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return submapSmall.getBaseIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(String modName, Block block, IIconRegister register) {
        submapSmall = new TextureSubmap(register.registerIcon(modName + ":" + texturePath), 2, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return submapSmall.getBaseIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        val renderBlocks = this.renderBlocks();
        renderBlocks.setRenderBoundsFromBlock(block);
        renderBlocks.submapSmall = submapSmall;
        renderBlocks.submap = submapSmall;// hacky work around to use the renderer i want
        return renderBlocks;
    }

    @SideOnly(Side.CLIENT)
    protected RenderBlocksCTM getRenderBlocks() {
        RenderBlocksCTM renderBlocks = new RenderBlocksCTM();
        renderBlocks.ctm = CTMPlain.getInstance();
        return renderBlocks;
    }

    @SideOnly(Side.CLIENT)
    public static class CTMPlain extends CTM {
        public static CTMPlain getInstance() {
            return new CTMPlain();
        }

        private final int[] submapIndices = new int[]{18, 19, 17, 16};

        @Override
        public int[] getSubmapIndices(IBlockAccess world, int x, int y, int z, int side) {
            return submapIndices;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void preRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void postRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
    }
}
