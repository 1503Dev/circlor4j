package dev1503.circlor4j.client.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
	private static final String API_URL = "https://circlor.1503dev.top/circlor4j/api/latest/?version=";
	private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

	private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "circlor4j-update-checker");
		t.setDaemon(true);
		return t;
	});

	private static volatile UpdateInfo latestInfo;
	private static volatile boolean checked = false;

	public record UpdateInfo(String latest, String url) {
	}

	public static void checkForUpdates(String currentVersion) {
		if (checked) {
			return;
		}
		checked = true;
		System.out.println("[Circlor4J] Checking updates for version: [" + currentVersion + "]");

		CompletableFuture.runAsync(() -> {
			try {
				fetchUpdateInfo(currentVersion);
			} catch (Exception e) {
				System.out.println("[Circlor4J] Update check failed: " + e);
				latestInfo = null;
			}
		}, EXECUTOR).orTimeout(10, TimeUnit.SECONDS).exceptionally(throwable -> {
			System.out.println("[Circlor4J] Update check timed out: " + throwable);
			latestInfo = null;
			return null;
		});
	}

	private static void fetchUpdateInfo(String currentVersion) {
		HttpURLConnection conn = null;
		try {
			URI uri = URI.create(API_URL + currentVersion);
			conn = (HttpURLConnection) uri.toURL().openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setRequestProperty("User-Agent", "Circlor4J/" + currentVersion);

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				return;
			}

			StringBuilder response = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
			}

			String json = response.toString().trim();
			String latest = extractJsonField(json, "latest");
			String url = extractJsonField(json, "url");
			System.out.println("[Circlor4J] Response: " + json);
			System.out.println("[Circlor4J] latest=" + latest + " url=" + url);

			if (latest != null && !latest.isEmpty() && isNewer(currentVersion, latest)) {
				latestInfo = new UpdateInfo(latest, url);
				System.out.println("[Circlor4J] Update available!");
			} else {
				System.out.println("[Circlor4J] No update (latest=" + latest + ")");
			}
		} catch (Exception e) {
			latestInfo = null;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private static String extractJsonField(String json, String field) {
		String search = "\"" + field + "\"";
		int idx = json.indexOf(search);
		if (idx < 0) {
			return null;
		}
		int colonIdx = json.indexOf(':', idx + search.length());
		if (colonIdx < 0) {
			return null;
		}
		int quoteStart = json.indexOf('"', colonIdx + 1);
		if (quoteStart < 0) {
			return null;
		}
		int quoteEnd = json.indexOf('"', quoteStart + 1);
		if (quoteEnd < 0) {
			return null;
		}
		String raw = json.substring(quoteStart + 1, quoteEnd);
		return unescapeJsonString(raw);
	}

	private static String unescapeJsonString(String raw) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			if (c == '\\' && i + 1 < raw.length()) {
				char next = raw.charAt(i + 1);
				switch (next) {
					case '\\' -> sb.append('\\');
					case '"' -> sb.append('"');
					case '/' -> sb.append('/');
					case 'n' -> sb.append('\n');
					case 'r' -> sb.append('\r');
					case 't' -> sb.append('\t');
					case 'b' -> sb.append('\b');
					case 'f' -> sb.append('\f');
					case 'u' -> {
						if (i + 5 < raw.length()) {
							try {
								sb.append((char) Integer.parseInt(raw.substring(i + 2, i + 6), 16));
								i += 4;
							} catch (NumberFormatException e) {
								sb.append('u');
							}
						} else {
							sb.append('u');
						}
					}
					default -> sb.append(next);
				}
				i++;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static int[] parseVersion(String version) {
		if (version == null) {
			return null;
		}
		Matcher m = VERSION_PATTERN.matcher(version.trim());
		if (!m.matches()) {
			return null;
		}
		int[] parts = new int[5];
		parts[0] = Integer.parseInt(m.group(1));
		parts[1] = Integer.parseInt(m.group(2));
		parts[2] = Integer.parseInt(m.group(3));
		parts[3] = Integer.parseInt(m.group(4));
		parts[4] = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
		return parts;
	}

	private static boolean isNewer(String current, String latest) {
		int[] cur = parseVersion(current);
		int[] lat = parseVersion(latest);
		if (cur == null || lat == null) {
			return false;
		}
		for (int i = 0; i < 5; i++) {
			if (lat[i] > cur[i]) {
				return true;
			}
			if (lat[i] < cur[i]) {
				return false;
			}
		}
		return false;
	}

	public static UpdateInfo getUpdateInfo() {
		return latestInfo;
	}

	public static boolean hasUpdate() {
		return latestInfo != null;
	}

	public static String getUpdateUrl() {
		return latestInfo != null ? latestInfo.url() : null;
	}
}
