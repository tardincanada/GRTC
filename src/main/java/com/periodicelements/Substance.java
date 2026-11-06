package com.periodicelements;

import java.util.Map;

/**
 * A compound from {@code periodic_elements/substances.json}, generated from data/substances.csv.
 *
 * @param formula     display formula with subscripts, e.g. "H₂SO₄"
 * @param sector      "industry", "agriculture", or "both"
 * @param tier        complexity tier, 1 (simple molecules) to 6 (polymers)
 * @param level       historical tech level (0 to 7) at which it can first be made
 * @param composition element symbol to atom count for one formula unit (or polymer repeat unit)
 */
public record Substance(
		String id,
		String name,
		String formula,
		String state,
		String sector,
		int tier,
		int level,
		int color,
		Map<String, Integer> composition
) {
}
