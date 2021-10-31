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
public class SubmapManagerBaseExtraCTM extends SubmapManagerBaseExtra {
    @SideOnly(Side.CLIENT)
    protected TextureSubmap submap;

    public SubmapManagerBaseExtraCTM(String texturePath) {
        super(texturePath);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(String modName, Block block, IIconRegister register) {
        super.registerIcons(modName, block, register);
        submap = new TextureSubmap(register.registerIcon(modName + ":" + texturePath + "-ctm"), 4, 4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        RenderBlocksCTM renderBlocks = (RenderBlocksCTM)super.createRenderContext(rendererOld, block, world);
        renderBlocks.submap = submap;
        return renderBlocks;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlocksCTM getRenderBlocks() {
        return new RenderBlocksCTM();
    }
}
