package com.periodicelements.client;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Search in the Field Guide and the compendiums (3.27.0): case and accents are ignored ("acido" finds "Ácido"), and a
 * query of several words matches text that holds all of them, in any order.
 */
public final class SearchText {
	private SearchText() {
	}

	public static String normalize(String s) {
		String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return n.toLowerCase(Locale.ROOT);
	}

	/** The query's words, normalized; empty for a blank query. */
	public static List<String> words(String query) {
		List<String> out = new ArrayList<>();
		for (String w : normalize(query).trim().split("\\s+")) {
			if (!w.isEmpty()) {
				out.add(w);
			}
		}
		return out;
	}

	/** Does the (already normalized) text hold every word? */
	public static boolean all(String normalizedText, List<String> words) {
		if (words.isEmpty()) {
			return false;
		}
		for (String w : words) {
			if (!normalizedText.contains(w)) {
				return false;
			}
		}
		return true;
	}

	/** Does the (already normalized) text hold any of the words? */
	public static boolean any(String normalizedText, List<String> words) {
		for (String w : words) {
			if (normalizedText.contains(w)) {
				return true;
			}
		}
		return false;
	}

	/** How many times the words occur in the text, for ranking results. */
	public static int count(String normalizedText, List<String> words) {
		int n = 0;
		for (String w : words) {
			for (int i = normalizedText.indexOf(w); i >= 0; i = normalizedText.indexOf(w, i + w.length())) {
				n++;
			}
		}
		return n;
	}
}
