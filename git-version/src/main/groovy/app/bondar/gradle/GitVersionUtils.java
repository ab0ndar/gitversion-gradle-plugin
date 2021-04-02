package app.bondar.gradle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.gradle.api.Project;

public class GitVersionUtils {
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  public static final int EXECUTION_TIMEOUT = 15;

  private GitVersionUtils() {}

  static String executeGitCommand(final File workDir, final String... commands) throws IOException, InterruptedException {
    final List<String> cmdInput = new ArrayList<>();
    cmdInput.add("git");
    cmdInput.addAll(Arrays.asList(commands));

    final ProcessBuilder pb = new ProcessBuilder(cmdInput);
    pb.directory(workDir);
    pb.redirectErrorStream(true);

    final String output;
    final Process gitProcess = pb.start();
    try (final InputStreamReader in = new InputStreamReader(gitProcess.getInputStream(), StandardCharsets.UTF_8);
        final BufferedReader reader = new BufferedReader(in))
    {
      final StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
        builder.append(LINE_SEPARATOR);
      }
      output = builder.toString().trim();
    }

    if (!gitProcess.waitFor(EXECUTION_TIMEOUT, TimeUnit.SECONDS)) {
      throw new GitVersionException("Failed to execute git command, execution timeout exceeded");
    }

    if (gitProcess.exitValue() != 0) {
      throw new GitVersionException("Failed to execute git command\n" + output);
    }

    return output;
  }

  // Find Git repo by exploring Gradle modules tree up to the root module
  static Git findGitRepo(Project project) throws IOException {
    do {
      File gitDir = new File(project.getProjectDir(), ".git");
      if (gitDir.exists()) {
        return Git.wrap(new FileRepository(gitDir));
      }

      project = project.getParent();
    } while (project != null);

    return null;
  }

  static Git findGitRepo(File dir) throws IOException {
    do {
      File gitDir = new File(dir, ".git");
      if (gitDir.exists()) {
        return Git.wrap(new FileRepository(gitDir));
      }

      dir = dir.getParentFile();
    } while (dir != null);

    return null;
  }
}
