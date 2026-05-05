package com.example.superheroes.client;

import com.example.superheroes.item.infinity.InfinityStoneType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientThanosState {
	private static final EnumSet<InfinityStoneType> STONES = EnumSet.noneOf(InfinityStoneType.class);
	private static volatile boolean LOCAL_BROKEN = false;
	private static final Map<UUID, RemoteState> REMOTE = new HashMap<>();

	private ClientThanosState() {
	}

	public static synchronized void updateLocal(int bitmask, boolean broken) {
		STONES.clear();
		for (InfinityStoneType t : InfinityStoneType.values()) {
			if ((bitmask & (1 << t.ordinal())) != 0) {
				STONES.add(t);
			}
		}
		LOCAL_BROKEN = broken;
	}

	public static synchronized void updateRemote(UUID playerId, int bitmask, boolean broken) {
		EnumSet<InfinityStoneType> stones = EnumSet.noneOf(InfinityStoneType.class);
		for (InfinityStoneType t : InfinityStoneType.values()) {
			if ((bitmask & (1 << t.ordinal())) != 0) {
				stones.add(t);
			}
		}
		REMOTE.put(playerId, new RemoteState(bitmask, broken, stones));
	}

	public static synchronized boolean hasStone(InfinityStoneType type) {
		return STONES.contains(type);
	}

	public static synchronized int count() {
		return STONES.size();
	}

	public static synchronized boolean hasAllStones() {
		return STONES.size() >= InfinityStoneType.values().length;
	}

	public static synchronized int localBitmask() {
		int mask = 0;
		for (InfinityStoneType t : STONES) {
			mask |= (1 << t.ordinal());
		}
		return mask;
	}

	public static boolean isLocalBroken() {
		return LOCAL_BROKEN;
	}

	public static synchronized int bitmaskFor(UUID playerId) {
		RemoteState state = REMOTE.get(playerId);
		return state != null ? state.bitmask : 0;
	}

	public static synchronized boolean isBroken(UUID playerId) {
		RemoteState state = REMOTE.get(playerId);
		return state != null && state.broken;
	}

	public static synchronized void clear() {
		STONES.clear();
		LOCAL_BROKEN = false;
		REMOTE.clear();
	}

	public static synchronized void clearRemote(UUID playerId) {
		REMOTE.remove(playerId);
	}

	private record RemoteState(int bitmask, boolean broken, EnumSet<InfinityStoneType> stones) {
	}
}
