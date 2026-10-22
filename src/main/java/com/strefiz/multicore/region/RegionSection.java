package com.strefiz.multicore.region;

import java.util.Objects;

/**
 * A coordinate in the region section grid. A region section groups {@code 1 << chunkShift} chunks on
 * each axis (a power of two), so regions own sections rather than raw chunks. This is a performance
 * tradeoff: approximating chunks to sections allows the regioniser to treat NxN chunks as a single
 * unit.
 *
 * <p>Conversion: {@code sectionCoord = chunkCoord >> chunkShift}.
 */
public final class RegionSection {

	private final int x;
	private final int z;

	/**
	 * Creates a region section coordinate.
	 *
	 * @param x the section x coordinate
	 * @param z the section z coordinate
	 */
	public RegionSection(int x, int z) {
		this.x = x;
		this.z = z;
	}

	/** @return the section x coordinate. */
	public int x() {
		return x;
	}

	/** @return the section z coordinate. */
	public int z() {
		return z;
	}

	/**
	 * Converts a chunk coordinate to its containing section coordinate.
	 *
	 * @param chunkX the chunk x coordinate
	 * @param chunkZ the chunk z coordinate
	 * @param chunkShift log2 of the section size in chunks
	 * @return the containing region section
	 */
	public static RegionSection fromChunk(int chunkX, int chunkZ, int chunkShift) {
		return new RegionSection(chunkX >> chunkShift, chunkZ >> chunkShift);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RegionSection that)) {
			return false;
		}
		return x == that.x && z == that.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, z);
	}

	@Override
	public String toString() {
		return "RegionSection(" + x + ", " + z + ")";
	}
}
