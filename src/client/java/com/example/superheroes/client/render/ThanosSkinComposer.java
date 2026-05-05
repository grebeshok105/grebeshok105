package com.example.superheroes.client.render;

import com.example.superheroes.ModId;
import com.example.superheroes.item.infinity.InfinityStoneType;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Composes Thanos skin variants on-the-fly based on inserted stones and gauntlet-broken state.
 *
 * Stone pixel layout on the right-arm OVERLAY layer (back-of-hand row at y=46, plus Mind at y=44):
 *   POWER   (40, 46) — 0xFFB44CFF
 *   SPACE   (41, 46) — 0xFF4AA6FF
 *   REALITY (42, 46) — 0xFFE03030
 *   SOUL    (43, 46) — 0xFFFFA040
 *   TIME    (47, 46) — 0xFF40D87A
 *   MIND    (41, 44) — 0xFFFFE048
 */
public final class ThanosSkinComposer {
	private static final ResourceLocation BASE_TEXTURE = ModId.of("textures/entity/hero/thanos.png");
	private static final ResourceLocation WOUNDED_TEXTURE = ModId.of("textures/entity/hero/thanos_wounded.png");

	private static final int[][] STONE_PIXELS = new int[InfinityStoneType.values().length][];

	static {
		STONE_PIXELS[InfinityStoneType.POWER.ordinal()] = new int[]{40, 46};
		STONE_PIXELS[InfinityStoneType.SPACE.ordinal()] = new int[]{41, 46};
		STONE_PIXELS[InfinityStoneType.REALITY.ordinal()] = new int[]{42, 46};
		STONE_PIXELS[InfinityStoneType.SOUL.ordinal()] = new int[]{43, 46};
		STONE_PIXELS[InfinityStoneType.TIME.ordinal()] = new int[]{47, 46};
		STONE_PIXELS[InfinityStoneType.MIND.ordinal()] = new int[]{41, 44};
	}

	private static final Map<Long, ResourceLocation> CACHE = new HashMap<>();

	private ThanosSkinComposer() {
	}

	public static ResourceLocation getTextureFor(int stoneMask, boolean broken) {
		long key = (((long) stoneMask) << 1) | (broken ? 1L : 0L);
		ResourceLocation cached = CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		ResourceLocation generated = compose(stoneMask, broken);
		if (generated != null) {
			CACHE.put(key, generated);
			return generated;
		}
		return broken ? WOUNDED_TEXTURE : BASE_TEXTURE;
	}

	private static ResourceLocation compose(int stoneMask, boolean broken) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation source = broken ? WOUNDED_TEXTURE : BASE_TEXTURE;
		NativeImage img = readNativeImage(mc, source);
		if (img == null) {
			return null;
		}
		paintStones(img, stoneMask);
		DynamicTexture dyn = new DynamicTexture(img);
		ResourceLocation rl = ModId.of("dynamic/thanos/" + Integer.toHexString(stoneMask) + (broken ? "_broken" : ""));
		mc.getTextureManager().register(rl, dyn);
		return rl;
	}

	private static NativeImage readNativeImage(Minecraft mc, ResourceLocation rl) {
		try {
			Resource res = mc.getResourceManager().getResource(rl).orElse(null);
			if (res == null) {
				return null;
			}
			try (InputStream is = res.open()) {
				return NativeImage.read(is);
			}
		} catch (IOException e) {
			return null;
		}
	}

	private static void paintStones(NativeImage img, int stoneMask) {
		for (InfinityStoneType type : InfinityStoneType.values()) {
			if ((stoneMask & (1 << type.ordinal())) == 0) {
				continue;
			}
			int[] pos = STONE_PIXELS[type.ordinal()];
			int color = type.getColor(); // 0xAARRGGBB
			// NativeImage uses 0xAABBGGRR format internally — swap RR and BB.
			int abgr = toAbgr(color);
			img.setPixelRGBA(pos[0], pos[1], abgr);
		}
	}

	private static int toAbgr(int argb) {
		int a = (argb >>> 24) & 0xFF;
		int r = (argb >>> 16) & 0xFF;
		int g = (argb >>> 8) & 0xFF;
		int b = argb & 0xFF;
		return (a << 24) | (b << 16) | (g << 8) | r;
	}

	public static void invalidate() {
		Minecraft mc = Minecraft.getInstance();
		for (ResourceLocation rl : CACHE.values()) {
			mc.getTextureManager().release(rl);
		}
		CACHE.clear();
	}
}
