package com.periodicelements.atlas;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The rocks the Atlas's geological layer knows, one byte per column. 0 means not yet seen; SOIL means the column was
 * looked into and no rock lies within reach under the soil. The colours follow the tints of 19th-century geological maps.
 */
public final class Rocks {
	public static final int UNKNOWN = 0;
	public static final String[] NAMES = {"", "stone", "granite", "diorite", "andesite", "tuff", "basalt", "sandstone",
			"terracotta", "calcite", "deepslate", "dripstone_block", "soil"};
	public static final int SOIL = NAMES.length - 1;
	/** RGB of each rock on the map. */
	public static final int[] COLORS = {0, 0x9A9A9A, 0xD9826A, 0xE8E4DA, 0x8C8F86, 0x7A8270, 0x4A4A55, 0xE8D49A,
			0xC27A4E, 0xF4F0E2, 0x55575F, 0xA9846C, 0xC8B88A};
	/** How far under the surface the Atlas looks for rock, in blocks. */
	public static final int DEPTH = 24;

	private static final Map<Block, Integer> BY_BLOCK = new IdentityHashMap<>();

	static {
		put(1, Blocks.STONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
		put(2, Blocks.GRANITE);
		put(3, Blocks.DIORITE);
		put(4, Blocks.ANDESITE);
		put(5, Blocks.TUFF);
		put(6, Blocks.BASALT, Blocks.SMOOTH_BASALT, Blocks.POLISHED_BASALT);
		put(7, Blocks.SANDSTONE, Blocks.RED_SANDSTONE);
		put(8, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
				Blocks.BROWN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA);
		put(9, Blocks.CALCITE);
		put(10, Blocks.DEEPSLATE);
		put(11, Blocks.DRIPSTONE_BLOCK);
	}

	private Rocks() {
	}

	private static void put(int id, Block... blocks) {
		for (Block b : blocks) {
			BY_BLOCK.put(b, id);
		}
	}

	/** The rock index of a block, or UNKNOWN when it is not rock (soil, plants, water, ores...). */
	public static int of(Block block) {
		Integer id = BY_BLOCK.get(block);
		return id == null ? UNKNOWN : id;
	}

	/** Translation key of a rock's name: the block's own name, or the Atlas's word for deep soil. */
	public static String key(int rock) {
		if (rock == SOIL) {
			return "gui.periodic_elements.atlas.rock.soil";
		}
		if (rock <= UNKNOWN || rock >= NAMES.length) {
			return "gui.periodic_elements.atlas.rock.unknown";
		}
		return "block.minecraft." + NAMES[rock];
	}
}
