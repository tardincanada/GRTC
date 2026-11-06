package com.periodicelements.crops;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

/**
 * A crop plant's genes (3.23.0, extended in 3.37.0), packed in an int. Seven genes, two alleles each:
 * <ul>
 * <li>C cycle: <b>cc</b> (recessive) ripens early, grows faster;</li>
 * <li>P yield: <b>P_</b> (dominant) gives more at harvest;</li>
 * <li>R disease resistance: <b>R_</b> resists rust;</li>
 * <li>D drought: <b>dd</b> (recessive) grows at full speed in dry country;</li>
 * <li>F cold: <b>F_</b> grows at full speed where it is too cold for its kind;</li>
 * <li>I pests: <b>I_</b> resists caterpillars;</li>
 * <li>S stature (3.37.0, wheat and rice): <b>ss</b> (recessive) is semi-dwarf and makes the most of NPK.</li>
 * </ul>
 * Bits 0-11 hold genes C to I (gene g at bits 2g and 2g+1, set = the capital letter). Bits 16-17 hold S the other way
 * round (set = the dwarf allele s), so that seeds saved before 3.37.0 read as SS. Bits 18-21 are the transgenes
 * (Bt, EPSPS, golden rice, DREB), which no wild plant has. Bit 14 marks an F1 hybrid of two different pure lines.
 * On an item, bits 12-13 and 15 say how far it was analysed: 0 not, 1 traits with the Seed Lens, 2 genotype after
 * Mendel's Laws, 3 every trait after the DNA double helix, 4 the whole genome from the DNA Sequencer.
 */
public final class Genome {
	public static final String LETTERS = "CPRDFIS";
	public static final int CYCLE = 0, YIELD = 1, DISEASE = 2, DROUGHT = 3, COLD = 4, PEST = 5, STATURE = 6;
	public static final int GENES = 7;
	/** Genes C and P, the ones Mendel's work shows; the rest show after the DNA double helix. */
	public static final int MENDEL_GENES = 2;
	public static final int BT = 0, EPSPS = 1, GOLDEN = 2, DREB = 3;
	public static final String[] TRANSGENES = {"bt", "epsps", "golden", "dreb"};
	public static final int HYBRID = 1 << 14;
	private static final int TRANSGENE_SHIFT = 18;
	/** Everything a plant passes on or is: genes, transgenes and the hybrid mark (not the analysis stamp). */
	public static final int MASK = 0xFFF | HYBRID | 0x3F << 16;
	/** The ordinary plant: CC pp rr DD ff ii SS (no useful trait). Seeds with no genome on them are this. */
	public static final int DEFAULT = 0b11 | 0b11 << 6;
	/** One mutation in this many alleles passed on. */
	public static final int MUTATION = 500;
	public static final int STAMP_SHIFT = 12;
	public static final int STAMP_TRAITS = 1, STAMP_GENOTYPE = 2, STAMP_DNA = 3, STAMP_SEQUENCE = 4;

	private Genome() {
	}

	/** The two alleles of a gene, bit set = the capital letter. */
	public static int alleles(int genome, int gene) {
		return gene == STATURE ? ~genome >> 16 & 3 : genome >> 2 * gene & 3;
	}

	private static int setAlleles(int genome, int gene, int bits) {
		if (gene == STATURE) {
			return genome & ~(3 << 16) | (~bits & 3) << 16;
		}
		return genome & ~(3 << 2 * gene) | (bits & 3) << 2 * gene;
	}

	/** Number of capital alleles (0, 1 or 2). */
	public static int capitals(int genome, int gene) {
		return Integer.bitCount(alleles(genome, gene));
	}

	/** Is the useful form of this gene showing? */
	public static boolean has(int genome, int gene) {
		int caps = capitals(genome, gene);
		return switch (gene) {
			case CYCLE, DROUGHT, STATURE -> caps == 0; // recessive traits: only the homozygote shows them
			default -> caps > 0;
		};
	}

	public static int with(int genome, int gene, int capitals) {
		return setAlleles(genome, gene, capitals == 0 ? 0 : capitals == 1 ? 1 : 3);
	}

	/** The gene set to the homozygous useful form (what CRISPR writes). */
	public static int useful(int genome, int gene) {
		return with(genome, gene, switch (gene) {
			case CYCLE, DROUGHT, STATURE -> 0;
			default -> 2;
		});
	}

	public static boolean transgene(int genome, int t) {
		return (genome >> TRANSGENE_SHIFT + t & 1) != 0;
	}

	public static int withTransgene(int genome, int t) {
		return genome | 1 << TRANSGENE_SHIFT + t;
	}

	public static boolean hybrid(int genome) {
		return (genome & HYBRID) != 0;
	}

	/** Homozygous at every gene: a pure line, which breeds true. */
	public static boolean pure(int genome) {
		for (int g = 0; g < GENES; g++) {
			if (capitals(genome, g) == 1) {
				return false;
			}
		}
		return true;
	}

	/** One allele of each gene from the mother and one from the father, by chance; now and then one mutates. */
	public static int child(int mother, int father, Random random) {
		return child(mother, father, random, 1.0 / MUTATION);
	}

	/**
	 * A seed of these parents: one allele of each gene from each, each allele mutating with chance {@code mutation}.
	 * Transgenes pass from either parent. Two different pure lines give an F1 hybrid.
	 */
	public static int child(int mother, int father, Random random, double mutation) {
		int out = 0;
		for (int g = 0; g < GENES; g++) {
			int a = alleles(mother, g) >> random.nextInt(2) & 1;
			int b = alleles(father, g) >> random.nextInt(2) & 1;
			if (random.nextDouble() < mutation) {
				a ^= 1;
			}
			if (random.nextDouble() < mutation) {
				b ^= 1;
			}
			out = setAlleles(out, g, a | b << 1);
		}
		out |= (mother | father) & 0xF << TRANSGENE_SHIFT;
		if (pure(mother) && pure(father) && genes(mother) != genes(father)) {
			out |= HYBRID;
		}
		return out;
	}

	/** The plant itself with each allele mutated with chance {@code mutation} (X-rays, gamma rays). */
	public static int mutate(int genome, Random random, double mutation) {
		int out = genome;
		for (int g = 0; g < GENES; g++) {
			int bits = alleles(out, g);
			for (int k = 0; k < 2; k++) {
				if (random.nextDouble() < mutation) {
					bits ^= 1 << k;
				}
			}
			out = setAlleles(out, g, bits);
		}
		return out;
	}

	/** The genes alone (no transgenes, hybrid mark or stamp). */
	public static int genes(int genome) {
		return genome & (0xFFF | 3 << 16);
	}

	/** A wild plant: capital alleles at each gene's natural frequency. */
	public static int wild(Random random, boolean arid, boolean cold) {
		double[] capital = {0.8, 0.2, 0.25, arid ? 0.4 : 0.8, cold ? 0.5 : 0.2, 0.25};
		int out = 0;
		for (int g = 0; g < GENES - 1; g++) {
			for (int k = 0; k < 2; k++) {
				if (random.nextDouble() < capital[g]) {
					out |= 1 << 2 * g + k;
				}
			}
		}
		return out;
	}

	public static int stamp(int value) {
		return (value >> STAMP_SHIFT & 3) | (value >> 15 & 1) << 2;
	}

	public static int stamped(int genome, int stamp) {
		int s = Math.max(stamp, stamp(genome));
		return genome & MASK | (s & 3) << STAMP_SHIFT | (s >> 2 & 1) << 15;
	}

	/** Genes shown at this level of analysis: C and P from Mendel's Laws on, all of them with the Sequencer. */
	private static int shown(int level) {
		return level >= STAMP_SEQUENCE ? GENES : level >= STAMP_GENOTYPE ? MENDEL_GENES : 0;
	}

	/** "Cc PP rr dd Ff ii", with Ss for wheat and rice. */
	public static String genotype(int genome) {
		return genotype(genome, STAMP_SEQUENCE, false);
	}

	public static String genotype(int genome, int level, boolean stature) {
		StringBuilder sb = new StringBuilder();
		for (int g = 0; g < shown(level); g++) {
			if (g == STATURE && !stature) {
				continue;
			}
			if (!sb.isEmpty()) {
				sb.append(' ');
			}
			char up = LETTERS.charAt(g);
			int caps = capitals(genome, g);
			sb.append(caps > 0 ? up : Character.toLowerCase(up)).append(caps > 1 ? up : Character.toLowerCase(up));
		}
		return sb.toString();
	}

	/** The useful traits that show, as text; null if none. */
	public static Text traits(int genome) {
		return traits(genome, STAMP_SEQUENCE, true);
	}

	/** The traits a reader at this level can see: cycle and yield by eye; the rest, and transgenes, after the DNA. */
	public static Text traits(int genome, int level, boolean stature) {
		MutableText out = null;
		int genes = level >= STAMP_DNA ? GENES : MENDEL_GENES;
		for (int g = 0; g < genes; g++) {
			if (g == STATURE && !stature) {
				continue;
			}
			if (has(genome, g)) {
				Text t = Text.translatable("gene.periodic_elements." + LETTERS.charAt(g) + ".good");
				out = out == null ? Text.empty().append(t) : out.append(", ").append(t);
			}
		}
		if (level >= STAMP_DNA) {
			for (int t = 0; t < TRANSGENES.length; t++) {
				if (transgene(genome, t)) {
					Text x = Text.translatable("gene.periodic_elements." + TRANSGENES[t]);
					out = out == null ? Text.empty().append(x) : out.append(", ").append(x);
				}
			}
		}
		return out;
	}
}
