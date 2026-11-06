package com.periodicelements;

/**
 * One row of the periodic table, loaded from {@code periodic_elements/elements.json}.
 *
 * @param level      historical tech level (0 Stone Age to 7 Accelerator age) when it was first isolated
 * @param discovered year it was first isolated, or "ancient"
 */
public record Element(
		String id,
		int number,
		String symbol,
		String name,
		String state,
		String category,
		int color,
		int level,
		String discovered
) {
	public boolean isAncient() {
		return "ancient".equals(discovered);
	}
}
