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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		super.updateAnimation();
		try {
			updateAnimationInterpolated(true);
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
	 * Generates an interpolated frame from the current tick and frame counters, and optionally uploads it to the gpu.
	 * @param upload True if the generated texture should be uploaded. False if the method is being used just for pre-generation.
	 */
	private void updateAnimationInterpolated(boolean upload) throws IllegalArgumentException {

		int subFrameCount = animationMetadata.getFrameTimeSingle(frameCounter);
		int firstFrameIndex = animationMetadata.getFrameIndex(frameCounter);
		int totalFrameCount = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
		int secondFrameIndex = animationMetadata.getFrameIndex((frameCounter + 1) % totalFrameCount);

		double alpha = 1.0D - tickCounter / (double) subFrameCount;

		if (interpolatedFrameData == null || interpolatedFrameData.length != totalFrameCount) {
			interpolatedFrameData = new int[totalFrameCount][][][];
		}

		if (firstFrameIndex != secondFrameIndex && secondFrameIndex >= 0 && secondFrameIndex < framesTextureData.size()) {
			int[][][] subFrames = interpolatedFrameData[firstFrameIndex];
			if (subFrames == null || subFrames.length != subFrameCount) {
				interpolatedFrameData[firstFrameIndex] = subFrames = new int[subFrameCount][][];
			}
			int[][] subFrame = subFrames[tickCounter];
			if (subFrame == null) {
				int[][] firstFrame = (int[][]) framesTextureData.get(firstFrameIndex);
				int[][] secondFrame = (int[][]) framesTextureData.get(secondFrameIndex);
				subFrame = subFrames[tickCounter] = interpolate(firstFrame, secondFrame, alpha, null);
			}

			if (upload)
				TextureUtil.uploadTextureMipmap(subFrame, width, height, originX, originY, false, false);
		}
	}

	/**
	 * Linearly interpolates between two animation frames, and stores the result in the output. If the output has an incorrect size, or is null, a new array is returned.
	 */
	private int[][] interpolate(int[][] first, int[][] second, double alpha, int[][] output) {
		if (output == null || output.length != first.length)
			output = new int[first.length][];

		for (int y = 0; y < first.length; y++) {
			if (output[y] == null)
				output[y] = new int[first[y].length];

			if (y < second.length && second[y].length == first[y].length)
				for (int x = 0; x < first[y].length; ++x) {
					int firstPixel = first[y][x];
					int secondPixel = second[y][x];
					int interpolated = interpolatePixel(firstPixel, secondPixel, alpha);
					output[y][x] = interpolated;
				}
		}
		return output;
	}

	/**
	 * Linearly interpolates between RGB colors.
	 */
	private int interpolatePixel(int first, int second, double alpha) {
		int r = (int) (((first & 16711680) >> 16) * alpha + ((second & 16711680) >> 16) * (1.0D - alpha));
		int g = (int) (((first & 65280) >> 8) * alpha + ((second & 65280) >> 8) * (1.0D - alpha));
		int b = (int) ((first & 255) * alpha + (second & 255) * (1.0D - alpha));
		return first & -16777216 | r << 16 | g << 8 | b;
	}

	/**
	 * Nukes the frame cache and regenerates it from scratch.
	 */
	private void regenerateInterpolationBuffer() {
		interpolatedFrameData = null;
		int tickCounterBak = tickCounter;
		int frameCounterBak = frameCounter;

		int frameCount = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
		for (frameCounter = 0; frameCounter < frameCount; frameCounter++) {
			int subFrameCount = animationMetadata.getFrameTimeSingle(frameCounter);
			for (tickCounter = 0; tickCounter < subFrameCount; tickCounter++) {
				updateAnimationInterpolated(false);
			}
		}

		tickCounter = tickCounterBak;
		frameCounter = frameCounterBak;
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