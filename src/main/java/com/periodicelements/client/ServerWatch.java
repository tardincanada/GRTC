package com.periodicelements.client;

import com.periodicelements.PeriodicElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

/**
 * 3.68.3 (diagnostic): watches the single-player server from the client. When it has not finished a tick for
 * {@link #STALL_MS} while not paused, every thread's stack goes to the log once, so a frozen world can be traced from
 * latest.log alone. A second dump follows a minute later if it is still frozen.
 */
public final class ServerWatch {
	static final long STALL_MS = 15_000;

	private ServerWatch() {
	}

	public static void start() {
		Thread t = new Thread(ServerWatch::run, "Elementar server watch");
		t.setDaemon(true);
		t.start();
	}

	private static void run() {
		int lastTicks = -1;
		long since = System.currentTimeMillis();
		int dumps = 0;
		IntegratedServer lastServer = null;
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
			MinecraftClient client = MinecraftClient.getInstance();
			IntegratedServer server = client == null ? null : client.getServer();
			long now = System.currentTimeMillis();
			if (server == null || server != lastServer || client.isPaused()) {
				lastServer = server;
				lastTicks = server == null ? -1 : server.getTicks();
				since = now;
				dumps = 0;
				continue;
			}
			int ticks = server.getTicks();
			if (ticks != lastTicks) {
				lastTicks = ticks;
				since = now;
				dumps = 0;
				continue;
			}
			long stalled = now - since;
			if ((dumps == 0 && stalled >= STALL_MS) || (dumps == 1 && stalled >= STALL_MS + 60_000)) {
				dumps++;
				dump(stalled);
			}
		}
	}

	private static void dump(long stalled) {
		StringBuilder sb = new StringBuilder();
		sb.append("[Elementar] The world's server has not ticked for ").append(stalled / 1000)
				.append(" s. Thread dump follows (send this log to the mod's author).\n");
		for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
			sb.append('"').append(info.getThreadName()).append("\" ").append(info.getThreadState());
			if (info.getLockName() != null) sb.append(" on ").append(info.getLockName());
			if (info.getLockOwnerName() != null) sb.append(" owned by \"").append(info.getLockOwnerName()).append('"');
			sb.append('\n');
			for (StackTraceElement e : info.getStackTrace()) {
				sb.append("\tat ").append(e).append('\n');
			}
			sb.append('\n');
		}
		PeriodicElements.LOGGER.warn(sb.toString());
	}
}
