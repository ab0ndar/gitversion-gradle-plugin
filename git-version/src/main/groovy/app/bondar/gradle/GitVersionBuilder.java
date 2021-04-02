package app.bondar.gradle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

public class GitVersionBuilder {
  // Example: 1.0.0-13-g68ff004-SNAPSHOT
  private static final Pattern DESCRIBE_PATTERN =
      Pattern.compile("^([0-9]+\\.[0-9]+\\.[0-9]+)-([0-9]+)-(g.+)(-SNAPSHOT)?$");

  protected final Git git;
  protected volatile String gitDescribeOutput = null;

  protected static final Set<String> SHORT_VERSION_BRANCH_NAMES = new HashSet<>();
  static {
    SHORT_VERSION_BRANCH_NAMES.add("main");
    SHORT_VERSION_BRANCH_NAMES.add("master");
    SHORT_VERSION_BRANCH_NAMES.add("release");
    SHORT_VERSION_BRANCH_NAMES.add("hotfix");
  }

  GitVersionBuilder(Git git) {
    this.git = git;
  }

  String getVersion() {
    describe();
    if (gitDescribeOutput == null || gitDescribeOutput.trim().length() == 0) {
      throw new GitVersionException("Git describe command returns empty result");
    }

    final String branchName = getBranchName();
    final Matcher matcher = DESCRIBE_PATTERN.matcher(gitDescribeOutput);
    if (!matcher.matches()) {
      throw new GitVersionException("Git describe command returns incorrect result\n" +
          gitDescribeOutput + "\nExpected pattern: " + DESCRIBE_PATTERN.pattern());
    }

    if (SHORT_VERSION_BRANCH_NAMES.contains(branchName.toLowerCase())) {
      return matcher.group(1);
    }

    return gitDescribeOutput;
  }

  private String describe() {
    if (gitDescribeOutput != null) {
      return gitDescribeOutput;
    }

    try {
      final List<Ref> tags = git.tagList().call();
      if (tags == null || tags.isEmpty()) {
        throw new GitVersionException("Git repo has no tags, unable to generate a project version");
      }

      gitDescribeOutput = GitVersionUtils.executeGitCommand(git.getRepository().getWorkTree(),
          "describe", "--tags", "--long", "--dirty=-SNAPSHOT");
    } catch (GitVersionException e) {
      throw e;
    } catch (Exception e) {
      throw new GitVersionException("Failed to execute git describe command", e);
    }

    return gitDescribeOutput;
  }

  String getBranchName() {
    Ref ref;
    try {
      ref = git.getRepository().findRef(git.getRepository().getBranch());
    } catch (Exception e) {
      throw new GitVersionException("Failed to retrieve git branch name", e);
    }

    return (ref == null)?null:ref.getName().substring(Constants.R_HEADS.length());
  }
}
