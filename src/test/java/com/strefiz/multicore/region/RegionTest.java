package com.strefiz.multicore.region;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Region}.
 */
class RegionTest {

	@Test
	void newRegionIsReady() {
		Region region = new Region();

		assertEquals(RegionState.READY, region.state());
		assertNull(region.ownerThread());
		assertEquals(0, region.sectionCount());
	}

	@Test
	void tryMarkTickingSetsOwner() throws InterruptedException {
		Region region = new Region();
		Thread owner = Thread.currentThread();

		assertTrue(region.tryMarkTicking());

		assertEquals(RegionState.TICKING, region.state());
		assertSame(owner, region.ownerThread());
	}

	@Test
	void tryMarkTickingFailsWhenAlreadyTicking() throws InterruptedException {
		Region region = new Region();
		Thread owner = Thread.currentThread();

		assertTrue(region.tryMarkTicking());
		assertFalse(region.tryMarkTicking());
		assertSame(owner, region.ownerThread());
	}

	@Test
	void markNotTickingByOwnerReturnsToReady() throws InterruptedException {
		Region region = new Region();
		Thread owner = Thread.currentThread();

		region.tryMarkTicking();
		assertTrue(region.markNotTicking());

		assertEquals(RegionState.READY, region.state());
		assertNull(region.ownerThread());
	}

	@Test
	void markNotTickingByOtherThreadFails() throws InterruptedException {
		Region region = new Region();
		Thread owner = Thread.currentThread();
		AtomicReference<Boolean> result = new AtomicReference<>();

		region.tryMarkTicking();
		Thread other = new Thread(() -> result.set(region.markNotTicking()));
		other.start();
		other.join();

		assertFalse(result.get());
		assertEquals(RegionState.TICKING, region.state());
		assertSame(owner, region.ownerThread());
	}

	@Test
	void addRemoveSectionRejectedWhileTicking() throws InterruptedException {
		Region region = new Region();
		Thread owner = Thread.currentThread();
		RegionSection section = RegionSection.fromChunk(100, 200, 4);

		region.tryMarkTicking();

		assertThrows(IllegalStateException.class, () -> region.addSection(section));
		assertThrows(IllegalStateException.class, () -> region.removeSection(section));
		assertEquals(0, region.sectionCount());
	}

	@Test
	void addRemoveSectionWorksWhenNotTicking() {
		Region region = new Region();
		// chunkShift = 4 => 16x16 chunk sections; use chunks in different sections
		RegionSection a = RegionSection.fromChunk(0, 0, 4);
		RegionSection b = RegionSection.fromChunk(32, 0, 4);

		region.addSection(a);
		region.addSection(b);

		assertEquals(2, region.sectionCount());
		assertTrue(region.sections().contains(a));
		assertTrue(region.sections().contains(b));

		region.removeSection(a);

		assertEquals(1, region.sectionCount());
		assertFalse(region.sections().contains(a));
		assertTrue(region.sections().contains(b));
	}

	@Test
	void markDeadWorksFromReadyAndTransient() {
		Region ready = new Region();
		ready.markDead();
		assertEquals(RegionState.DEAD, ready.state());

		Region transientRegion = new Region();
		transientRegion.markTransient();
		transientRegion.markDead();
		assertEquals(RegionState.DEAD, transientRegion.state());
	}

	@Test
	void markDeadFailsWhileTicking() throws InterruptedException {
		Region region = new Region();
		Thread.currentThread(); // owner
		region.tryMarkTicking();

		assertThrows(IllegalStateException.class, region::markDead);
	}

	@Test
	void markTransientWorksFromReady() {
		Region region = new Region();
		region.markTransient();
		assertEquals(RegionState.TRANSIENT, region.state());
	}

	@Test
	void markTransientFailsWhileTicking() throws InterruptedException {
		Region region = new Region();
		region.tryMarkTicking();

		assertThrows(IllegalStateException.class, region::markTransient);
	}

	@Test
	void regionLocalDataIsThreadLocalToOwner() throws InterruptedException {
		Region region = new Region();
		Thread owner = Thread.currentThread();
		String key = "my-data";

		region.tryMarkTicking();
		region.putRegionLocal(key, 42);

		assertEquals(Integer.valueOf(42), region.getRegionLocal(key));
		assertNull(region.getRegionLocal("other-key"));

		region.markNotTicking();
	}

	@Test
	void regionsAreEqualById() {
		Region a = new Region();
		Region b = new Region();

		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}
}
