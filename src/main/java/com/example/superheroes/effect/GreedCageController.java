package com.example.superheroes.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public final class GreedCageController {
	private static final List<Cage> ACTIVE = new CopyOnWriteArrayList<>();
	private static final int RADIUS = 6;

	private GreedCageController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(GreedCageController::tick);
	}

	public static void create(ServerLevel level, Vec3 center, int durationTicks) {
		Map<BlockPos, BlockState> saved = new HashMap<>();
		double r = RADIUS;
		for (int dx = -RADIUS - 1; dx <= RADIUS + 1; dx++) {
			for (int dy = -1; dy <= RADIUS + 1; dy++) {
				for (int dz = -RADIUS - 1; dz <= RADIUS + 1; dz++) {
					double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (dist < r - 0.6 || dist > r + 0.6) continue;
					BlockPos pos = BlockPos.containing(center.x + dx, center.y + dy, center.z + dz);
					BlockState orig = level.getBlockState(pos);
					if (orig.isAir() || orig.canBeReplaced() || !orig.getFluidState().isEmpty()) {
						saved.put(pos.immutable(), orig);
						level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
					}
				}
			}
		}
		long deadline = level.getGameTime() + durationTicks;
		ACTIVE.add(new Cage(level.dimension(), center, saved, deadline));

		spawnRingParticles(level, center, true);
	}

	private static void tick(MinecraftServer server) {
		if (ACTIVE.isEmpty()) return;
		for (int i = ACTIVE.size() - 1; i >= 0; i--) {
			Cage cage = ACTIVE.get(i);
			ServerLevel level = server.getLevel(cage.dim);
			if (level == null) {
				ACTIVE.remove(i);
				continue;
			}
			long now = level.getGameTime();
			spawnRingParticles(level, cage.center, false);
			if (now >= cage.deadline) {
				restore(level, cage);
				ACTIVE.remove(i);
			}
		}
	}

	private static void spawnRingParticles(ServerLevel level, Vec3 center, boolean burst) {
		int density = burst ? 240 : 16;
		double r = RADIUS;
		for (int i = 0; i < density; i++) {
			double theta = level.getRandom().nextDouble() * Math.PI * 2.0;
			double phi = (level.getRandom().nextDouble() - 0.05) * Math.PI * 0.55;
			double sinPhi = Math.sin(phi);
			double cosPhi = Math.cos(phi);
			double x = center.x + r * cosPhi * Math.cos(theta);
			double y = center.y + r * sinPhi;
			double z = center.z + r * cosPhi * Math.sin(theta);
			level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
			if (burst || i % 3 == 0) {
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.0, 0.0, 0.0, 0.02);
			}
		}
		if (burst) {
			level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 0.5, center.z, 4, 1.5, 1.0, 1.5, 0.0);
		}
	}

	private static void restore(ServerLevel level, Cage cage) {
		for (Map.Entry<BlockPos, BlockState> e : cage.saved.entrySet()) {
			BlockPos pos = e.getKey();
			BlockState current = level.getBlockState(pos);
			if (current.is(Blocks.BARRIER)) {
				level.setBlock(pos, e.getValue(), 3);
			}
		}
		level.sendParticles(ParticleTypes.POOF, cage.center.x, cage.center.y + 1.0, cage.center.z, 60, 4.0, 1.5, 4.0, 0.05);
		level.sendParticles(ParticleTypes.FLASH, cage.center.x, cage.center.y + 1.0, cage.center.z, 1, 0, 0, 0, 0);
	}

	private record Cage(ResourceKey<Level> dim, Vec3 center, Map<BlockPos, BlockState> saved, long deadline) {
	}
}
