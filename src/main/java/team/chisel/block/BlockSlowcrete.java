package team.chisel.block;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import team.chisel.api.carving.CarvableHelper;
import team.chisel.config.Configurations;

public class BlockSlowcrete extends BlockCarvable {
	public BlockSlowcrete() {
		super(Material.circuits);
		carverHelper = new CarvableHelper(this);
//		this.slipperiness = 1f;
		FMLCommonHandler.instance().bus().register(this);
	}

	@SideOnly(Side.CLIENT)
	private static MovementInput manualInputCheck;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void speedupPlayer(PlayerTickEvent event) {
		if (event.phase == Phase.START && event.side.isClient() && event.player.onGround && event.player instanceof EntityPlayerSP) {
			if (manualInputCheck == null) {
				manualInputCheck = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
			}
			EntityPlayerSP player = (EntityPlayerSP) event.player;
			Block below = player.worldObj.getBlock(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY) - 2, MathHelper.floor_double(player.posZ));
			if (below == this) {
				manualInputCheck.updatePlayerMoveState();
				if (manualInputCheck.moveForward != 0 || manualInputCheck.moveStrafe != 0) {
					player.motionX *= 0.95f ;
					player.motionZ *= 0.95f ;
				}
			}
		}
	}
}
