package com.periodicelements.atlas;

import net.minecraft.nbt.NbtCompound;

/**
 * 128 x 128 columns of the Atlas. Per column: the map colour id of the top block (0 = never drawn), the top block's Y,
 * and the rock under it (Rocks; 0 = not looked at). {@code rev} goes up on every change so clients fetch only new tiles.
 */
public final class AtlasTile {
	public static final int SHIFT = 7;
	public static final int SIZE = 1 << SHIFT;
	public static final int AREA = SIZE * SIZE;

	public final int tx, tz;
	public final byte[] color = new byte[AREA];
	public final short[] height = new short[AREA];
	public final byte[] rock = new byte[AREA];
	public int rev;

	public AtlasTile(int tx, int tz) {
		this.tx = tx;
		this.tz = tz;
	}

	public static long key(int tx, int tz) {
		return ((long) tx << 32) | (tz & 0xFFFFFFFFL);
	}

	public static int keyX(long key) {
		return (int) (key >> 32);
	}

	public static int keyZ(long key) {
		return (int) key;
	}

	public long key() {
		return key(tx, tz);
	}

	public static int index(int x, int z) {
		return (z & (SIZE - 1)) * SIZE + (x & (SIZE - 1));
	}

	public int drawn() {
		int n = 0;
		for (byte c : color) {
			if (c != 0) n++;
		}
		return n;
	}

	/** Heights as big-endian bytes, for packets and saving. */
	public byte[] heightBytes() {
		byte[] out = new byte[AREA * 2];
		for (int i = 0; i < AREA; i++) {
			out[2 * i] = (byte) (height[i] >> 8);
			out[2 * i + 1] = (byte) height[i];
		}
		return out;
	}

	public static void readHeights(byte[] in, short[] out) {
		for (int i = 0; i < out.length && 2 * i + 1 < in.length; i++) {
			out[i] = (short) (((in[2 * i] & 0xFF) << 8) | (in[2 * i + 1] & 0xFF));
		}
	}

	/** Copies in every column this tile lacks (colour or rock); returns how many columns gained something. */
	public int mergeFrom(AtlasTile other) {
		int gained = 0;
		for (int i = 0; i < AREA; i++) {
			boolean got = false;
			if (color[i] == 0 && other.color[i] != 0) {
				color[i] = other.color[i];
				height[i] = other.height[i];
				got = true;
			}
			if (rock[i] == 0 && other.rock[i] != 0) {
				rock[i] = other.rock[i];
				got = true;
			}
			if (got) gained++;
		}
		if (gained > 0) rev++;
		return gained;
	}

	public NbtCompound toNbt() {
		NbtCompound t = new NbtCompound();
		t.putInt("x", tx);
		t.putInt("z", tz);
		t.putInt("rev", rev);
		t.putByteArray("color", color);
		t.putByteArray("height", heightBytes());
		t.putByteArray("rock", rock);
		return t;
	}

	public static AtlasTile fromNbt(NbtCompound t) {
		AtlasTile tile = new AtlasTile(t.getInt("x"), t.getInt("z"));
		tile.rev = t.getInt("rev");
		System.arraycopy(t.getByteArray("color"), 0, tile.color, 0, Math.min(AREA, t.getByteArray("color").length));
		readHeights(t.getByteArray("height"), tile.height);
		System.arraycopy(t.getByteArray("rock"), 0, tile.rock, 0, Math.min(AREA, t.getByteArray("rock").length));
		return tile;
	}
}
