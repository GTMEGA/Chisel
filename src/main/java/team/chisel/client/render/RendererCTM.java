package team.chisel.client.render;

import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.common.Optional;
import team.chisel.api.rendering.ClientUtils;
import team.chisel.ctmlib.CTMRenderer;
import cpw.mods.fml.client.registry.RenderingRegistry;


@Optional.Interface(modid = "falsetweaks", iface = "com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer")
public class RendererCTM extends CTMRenderer implements ThreadSafeBlockRenderer {

	public RendererCTM() {
		super(RenderingRegistry.getNextAvailableRenderId());
		ClientUtils.renderCTMId = getRenderId();
	}

	@Optional.Method(modid = "falsetweaks")
	@Override
	public ISimpleBlockRenderingHandler forCurrentThread() {
		return this;
	}
}
