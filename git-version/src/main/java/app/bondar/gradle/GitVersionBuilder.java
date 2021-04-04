package app.bondar.gradle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

public class GitVersionBuilder {
  // Example: 1.0.0-13-g68ff004-SNAPSHOT
  private static final Pattern DESCRIBE_PATTERN =
      Pattern.compile("^\\w?(?P<version>[0-9]+\\.[0-9]+\\.[0-9]+)-(?P<distance>[0-9]+)-(?P<revision>g[\\w]+)(?P<snapshot>-SNAPSHOT)?$");

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
    final String gitVersion = describe();
    if (gitVersion == null || gitVersion.trim().length() == 0) {
      throw new GitVersionException("Git describe command returns empty result");
    }

    final String branchName = getBranchName();
    final Matcher matcher = DESCRIBE_PATTERN.matcher(gitVersion);
    if (!matcher.matches()) {
      throw new GitVersionException("Git describe command returns incorrect result\n" +
          gitVersion + "\nExpected pattern: " + DESCRIBE_PATTERN.pattern());
    }

    if (SHORT_VERSION_BRANCH_NAMES.contains(branchName.toLowerCase())) {
      return matcher.group(1);
    }

    return
        matcher.group("version") + "-" +
        matcher.group("distance") + "-" +
        matcher.group("revision") +
        ((matcher.group("snapshot") == null) ? "" : matcher.group("snapshot"));
  }

  String describe() {
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
