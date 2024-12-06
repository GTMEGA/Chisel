package team.chisel.client.render;

import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import cpw.mods.fml.common.Optional;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import lombok.val;
import org.lwjgl.opengl.GL11;

import team.chisel.Chisel;
import team.chisel.ClientCompat;
import team.chisel.block.BlockCarvableGlow;
import team.chisel.config.Configurations;
import team.chisel.ctmlib.Drawing;
import team.chisel.utils.GeneralClient;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

@Optional.Interface(modid = "falsetweaks", iface = "com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer")
public class RendererLayeredGlow implements ISimpleBlockRenderingHandler, ThreadSafeBlockRenderer {

	public RendererLayeredGlow() {
		Chisel.renderGlowId = RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GeneralClient.setGLColorFromInt(Configurations.configColors[metadata]);
		GL11.glDisable(GL11.GL_LIGHTING);
		Drawing.drawBlock(block, ((BlockCarvableGlow) block).getGlowTexture(), renderer);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glColor3f(1, 1, 1);
		Drawing.drawBlock(block, metadata, renderer);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		val tess = ClientCompat.getTessellator();
		tess.setColorOpaque_I(Configurations.configColors[world.getBlockMetadata(x, y, z)]);
		tess.setBrightness(0xF000F0);
		Drawing.renderAllFaces(renderer, block, x, y, z, ((BlockCarvableGlow) block).getGlowTexture());
		renderer.renderStandardBlock(block, x, y, z);
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return Chisel.renderGlowId;
	}

	@Optional.Method(modid = "falsetweaks")
	@Override
	public ISimpleBlockRenderingHandler forCurrentThread() {
		return this;
	}
}
