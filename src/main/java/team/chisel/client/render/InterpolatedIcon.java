/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 15, 2015, 5:11:16 PM (GMT)]
 */
package team.chisel.client.render;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.val;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// This is all vanilla code from 1.8, thanks to ganymedes01 porting it to 1.7 :D
@SideOnly(Side.CLIENT)
public class InterpolatedIcon extends TextureAtlasSprite {
	/**
	 * This is really just a GpuTexture[][], but minecraft uses int[][] as gpu textures
	 */
	private int[][][][] interpolatedFrameData;
	private TextureAtlasSprite wrapped;

	public InterpolatedIcon(String name, TextureAtlasSprite wrapped) {
		super(name);
		this.wrapped = wrapped;
	}

	@Override
	public void updateAnimation() {
		if (init != null) {
			if (!init.isDone()) {
				super.updateAnimation();
				return;
			}
			init = null;
		}
		++this.tickCounter;
		if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter)) {
			int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
			this.frameCounter = (this.frameCounter + 1) % j;
			this.tickCounter = 0;
		}
		try {
			val frame = generateFrame(frameCounter, tickCounter);
			if (frame != null)
				TextureUtil.uploadTextureMipmap(frame, width, height, originX, originY, false, false);
		} catch (Exception e) {
			// NO-OP
		}
	}

	@Override
	public void initSprite(int p_110971_1_, int p_110971_2_, int p_110971_3_, int p_110971_4_, boolean p_110971_5_) {
		super.initSprite(p_110971_1_, p_110971_2_, p_110971_3_, p_110971_4_, p_110971_5_);
		wrapped.copyFrom(this);
		regenerateInterpolationBuffer();
	}

	/**
	 * Linearly interpolates between two animation frames, and stores the result in the output. If the output has an incorrect size, or is null, a new array is returned.
	 */
	private static int[][] interpolate(int[][] first, int[][] second, float alpha, int[][] output) {
		if (output == null || output.length != first.length)
			output = new int[first.length][];

		for (int mipMapIndex = 0; mipMapIndex < first.length; mipMapIndex++) {
			int[] firstIMG;
			int[] secondIMG;
			int[] outputIMG;
			int length;

			firstIMG = first[mipMapIndex];
			length = firstIMG.length;
			if ((outputIMG = output[mipMapIndex]) == null || outputIMG.length != length)
				outputIMG = output[mipMapIndex] = new int[length];

			if (mipMapIndex < second.length && (secondIMG = second[mipMapIndex]).length == length) {
				for (int pixel = 0; pixel < length; ++pixel) {
					outputIMG[pixel] = interpolatePixel(firstIMG[pixel], secondIMG[pixel], alpha);
				}
			} else {
				//Fallback if something weird happened
				System.arraycopy(firstIMG, 0, outputIMG, 0, length);
			}
		}
		return output;
	}

	private static final double GAMMA = 2.2;

	/**
	 * Linearly interpolates between RGB colors.
	 */
	private static int interpolatePixel(int first, int second, float alpha) {
		int r = interpolate((first & 0x00FF0000) >>> 16, (second & 0x00FF0000) >>> 16, alpha);
		int g = interpolate((first & 0x0000FF00) >>> 8, (second & 0x0000FF00) >>> 8, alpha);
		int b = interpolate(first & 0x000000FF, second & 0x000000FF, alpha);
		return first & 0xFF000000 | r << 16 | g << 8 | b;
	}

	private static final int GAMMA_RES = 2048;
	private static final float[] UNGAMMA_TABLE = new float[GAMMA_RES + 1];
	private static final float[] REGAMMA_TABLE = new float[GAMMA_RES + 1];

	static {
		for (int i = 0; i < GAMMA_RES + 1; i++) {
			double unit = i / (double)GAMMA_RES;
			UNGAMMA_TABLE[i] = (float) Math.pow(unit, GAMMA);
			REGAMMA_TABLE[i] = (float) Math.pow(unit, 1d / GAMMA);
		}
	}

	private static float ungamma(float value) {
		int index = Math.min(GAMMA_RES, Math.max(0, (int) (value * GAMMA_RES)));
		return UNGAMMA_TABLE[index];
	}
	private static float regamma(float value) {
		int index = Math.min(GAMMA_RES, Math.max(0, (int) (value * GAMMA_RES)));
		return REGAMMA_TABLE[index];
	}


	private static int interpolate(int a, int b, float alpha) {
		return Math.round(255f * regamma(ungamma(a / 255f) * alpha + ungamma(b / 255f) * (1f - alpha)));
	}

	private static final Semaphore MUTEX = new Semaphore(1);
	private static final BlockingQueue<FutureTask<?>> tasks = new LinkedBlockingQueue<>();
	private static Thread workerThread;

	private static Future<?> submit(Runnable task) {
		val future = new FutureTask<>(task, null);
		MUTEX.acquireUninterruptibly();
		tasks.add(future);
		if (workerThread == null) {
			workerThread = new Thread(() -> {
				boolean acquired = false;
				long lastWorkTime = System.currentTimeMillis();
				try {
					MUTEX.acquire();
					acquired = true;
					while (!tasks.isEmpty() || System.currentTimeMillis() - lastWorkTime < 500) {
						MUTEX.release();
						acquired = false;
						FutureTask<?> task1;
						task1 = tasks.poll(500, TimeUnit.MILLISECONDS);
						if (task1 == null)
							continue;

						task1.run();
						lastWorkTime = System.currentTimeMillis();
						MUTEX.acquire();
						acquired = true;
					}
				} catch (InterruptedException ignored) {

				} finally {
					workerThread = null;
					if (acquired)
						MUTEX.release();
				}
			});
			workerThread.start();
		}
		MUTEX.release();
		return future;
	}

	/**
	 * Nukes the frame cache and regenerates it from scratch.
	 */
	private void regenerateInterpolationBuffer() {
		init = submit(() -> {
			interpolatedFrameData = null;
			generateFrame(-1, -1);
		});
	}

	private Future<?> init;

	private int[][] generateFrame(int frameIndex, int tickIndex) {
		int frameCount = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();

		if (interpolatedFrameData == null || interpolatedFrameData.length != frameCount) {
			interpolatedFrameData = new int[frameCount][][][];
		}
		if (frameIndex == -1) {
			for (frameIndex = 0; frameIndex < frameCount; frameIndex++) {
				generateFrame0(frameIndex, frameCount, tickIndex);
			}
			return null;
		} else {
			return generateFrame0(frameIndex, frameCount, tickIndex);
		}
	}

	private int[][] generateFrame0(int frameIndex, int frameCount, int tickIndex) {
		int firstFrameIndex = animationMetadata.getFrameIndex(frameIndex);
		int secondFrameIndex = animationMetadata.getFrameIndex((frameIndex + 1) % frameCount);
		if (firstFrameIndex != secondFrameIndex && secondFrameIndex >= 0 && secondFrameIndex < framesTextureData.size()) {
			return generateSubFrame(frameIndex, firstFrameIndex, secondFrameIndex, tickIndex);
		}
		return null;
	}

	private int[][] generateSubFrame(int frameIndex, int firstFrameIndex, int secondFrameIndex, int tickIndex) {
		int subFrameCount = animationMetadata.getFrameTimeSingle(frameCounter);
		int[][][] subFrames = interpolatedFrameData[frameIndex];
		if (subFrames == null)
			interpolatedFrameData[frameIndex] = subFrames = new int[subFrameCount][][];
		if (tickIndex == -1) {
			for (tickIndex = 0; tickIndex < subFrameCount; tickIndex++) {
				generateSubFrame0(subFrames, tickIndex, subFrameCount, firstFrameIndex, secondFrameIndex);
			}
			return null;
		} else {
			return generateSubFrame0(subFrames, tickIndex, subFrameCount, firstFrameIndex, secondFrameIndex);
		}
	}

	private int[][] generateSubFrame0(int[][][] subFrames, int tickIndex, int subFrameCount, int firstFrameIndex, int secondFrameIndex) {
		int[][] subFrame = subFrames[tickIndex];
		if (subFrame == null) {
			float alpha = 1.0f - tickIndex / (float) subFrameCount;
			int[][] firstFrame = (int[][]) framesTextureData.get(firstFrameIndex);
			int[][] secondFrame = (int[][]) framesTextureData.get(secondFrameIndex);
			subFrame = subFrames[tickIndex] = interpolate(firstFrame, secondFrame, alpha, null);
		}
		return subFrame;
	}

	public static class RegistrationHandler {

		@SuppressWarnings("unchecked")
		@SubscribeEvent
		public void onTexturesStitchedPre(TextureStitchEvent.Pre event) {
			for (Entry<String, TextureAtlasSprite> e : (Set<Entry<String, TextureAtlasSprite>>) event.map.mapRegisteredSprites.entrySet()) {
				ResourceLocation baseResource = new ResourceLocation(e.getKey());
				ResourceLocation res = event.map.completeResourceLocation(baseResource, 0);
				try {
					IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(res);
					iresource.getMetadata("dummy");
					JsonObject mcmeta = ((SimpleResource) iresource).mcmetaJson;
					if (mcmeta != null) {
						JsonElement interp = mcmeta.getAsJsonObject("animation").get("interpolate");
						if (interp != null && interp.getAsBoolean()) {
							e.setValue(new InterpolatedIcon(e.getKey(), e.getValue()));
						}
					}
				} catch (Exception ignored) {
				}
			}
		}
	}
}