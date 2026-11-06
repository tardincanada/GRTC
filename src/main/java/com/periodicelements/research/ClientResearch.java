package com.periodicelements.research;

import java.util.Set;

/** The local player's research, as last sent by the server. Only filled on the client. */
public final class ClientResearch {
	private static volatile Set<String> known = Set.of();

	private ClientResearch() {
	}

	public static Set<String> known() {
		return known;
	}

	public static void set(Iterable<String> ids) {
		java.util.Set<String> s = new java.util.LinkedHashSet<>();
		ids.forEach(s::add);
		known = java.util.Collections.unmodifiableSet(s);
	}
}
