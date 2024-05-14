package team.chisel.block;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import team.chisel.api.carving.CarvableHelper;

import java.util.Random;

public class BlockSlowcrete extends BlockCarvable {
	public BlockSlowcrete() {
		super(Material.rock);
		carverHelper = new CarvableHelper(this);
		FMLCommonHandler.instance().bus().register(this);
		needsRandomTick = true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		float f = 0.01f;
		return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1 - f, z + 1);
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	public void updateTick(World world, int x, int y, int z, Random rng) {
		if (!world.isRemote) {
			float chance = world.isRaining() ? 0.01f : 0.005f;
			if (rng.nextFloat() > chance)
				return;
			if (this.tryToFall(world, x, y, z, x, y, z))
				return;
			if (this.tryToFall(world, x, y, z, x-1, y, z))
				return;
			if (this.tryToFall(world, x, y, z, x+1, y, z))
				return;
			if (this.tryToFall(world, x, y, z, x, y, z-1))
				return;
			if (this.tryToFall(world, x, y, z, x, y, z+1))
				return;
		}
	}

	private boolean tryToFall(World world, int sourceX, int sourceY, int sourceZ, int testX, int testY, int testZ) {
		boolean matching = sourceX == testX && sourceY == testY && sourceZ == testZ;
		if ((matching || BlockFalling.func_149831_e(world, testX, testY, testZ)) && BlockFalling.func_149831_e(world, testX, testY - 1, testZ) && sourceY >= 0) {
			if (world.checkChunksExist(sourceX - 32, sourceY - 32, sourceZ - 32, sourceX + 32, sourceY + 32, sourceZ + 32)) {
				if (!world.isRemote) {
					EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, testX + 0.5F, testY + 0.5F, testZ + 0.5F,
																				   this,
																				   world.getBlockMetadata(sourceX, sourceY, sourceZ));

					if (!matching) {
						world.setBlockToAir(sourceX, sourceY, sourceZ);
						entityfallingblock.field_145812_b++;
					}
					world.spawnEntityInWorld(entityfallingblock);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * How many world ticks before ticking
	 */
	public int tickRate(World world) {
		return 2;
	}

	public void onEntityCollidedWithBlock(World p_149670_1_, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity p_149670_5_) {
		p_149670_5_.motionX *= 0.85D;
		p_149670_5_.motionZ *= 0.85D;
	}
}
