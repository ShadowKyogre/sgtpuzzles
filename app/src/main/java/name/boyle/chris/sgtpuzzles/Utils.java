package name.boyle.chris.sgtpuzzles;

import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Utils {

	static String readAllOf(InputStream s) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s), 8096);
		String line;
		StringBuilder log = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			log.append(line);
			log.append("\n");
		}
		return log.toString();
	}

	static void closeQuietly(@Nullable Closeable c)
	{
		if (c == null) return;
		try {
			c.close();
		} catch (IOException ignored) {}
	}

	static void setExecutable(File executablePath) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			if (!executablePath.setExecutable(true)) {
				throw new IOException("Can't make game binary executable: File.setExecutable failed");
			}
		} else {
			Set<String> dirs = new LinkedHashSet<String>();
			dirs.add("/system/bin");
			dirs.add("/system/xbin");
			final String path = System.getenv("PATH");
			if (path != null) {
				Collections.addAll(dirs, path.split(":"));
			}
			Set<File> tried = new LinkedHashSet<File>();
			boolean ok = false;
			for (String dir : dirs) {
				final File chmod = new File(dir, "chmod");
				if (chmod.exists()) {
					final String[] chmodArgs = {
							chmod.getAbsolutePath(), "755", executablePath.getAbsolutePath()};
					Log.d(GamePlay.TAG, "exec: " + Arrays.toString(chmodArgs));
					final int chmodExit = waitForProcess(Runtime.getRuntime().exec(chmodArgs));
					if (chmodExit == 0) {
						ok = true;
						break;
					}
					tried.add(chmod);
				}
			}
			if (! ok) {
				throw new IOException("Can't make game binary executable, tried " + tried);
			}
		}
	}

	static int waitForProcess(Process process) {
		if (process == null) return -1;
		try {
			while (true) {
				try {
					return process.waitFor();
				} catch (InterruptedException ignored) {}
			}
		} finally {
			process.destroy();
		}
	}
}
