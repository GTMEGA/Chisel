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
import team.chisel.ctmlib.TextureSubmap;

//Base class for self-contained renderers and sub managers etc
public class SubmapManagerBaseExtra implements ISubmapManager {
    private final String texturePath;

    @SideOnly(Side.CLIENT)
    protected RenderBlocksCTM renderBlocks;
    protected TextureSubmap submap;
    protected TextureSubmap submapSmall;

    public SubmapManagerBaseExtra(String texturePath) {
        this.texturePath = texturePath;
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return submapSmall.getBaseIcon();
    }

    @Override
    public void registerIcons(String modName, Block block, IIconRegister register) {
        submap = new TextureSubmap(register.registerIcon(modName + ":" + texturePath + "-ctm"), 4, 4);
        submapSmall = new TextureSubmap(register.registerIcon(modName + ":" + texturePath), 2, 2);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return submapSmall.getBaseIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        initRenderContext();
        renderBlocks.setRenderBoundsFromBlock(block);
        renderBlocks.submap = submap;
        renderBlocks.submapSmall = submapSmall;
        return renderBlocks;
    }

    protected void initRenderContext() {
        if (renderBlocks == null) {
            renderBlocks = getRenderBlocks();
        }
    }

    protected RenderBlocksCTM getRenderBlocks() {
        return new RenderBlocksCTM();
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
