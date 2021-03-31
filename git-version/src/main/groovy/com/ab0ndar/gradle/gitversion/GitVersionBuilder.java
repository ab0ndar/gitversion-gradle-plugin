package com.ab0ndar.gradle.gitversion;

import static com.ab0ndar.gradle.gitversion.GitVersionUtils.executeGitCommand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils;

public class GitVersionBuilder {
  private static final Pattern DESCRIBE_PATTERN =
      Pattern.compile("^([0-9]+\\.[0-9]+\\.[0-9]+)-([0-9]+)-(g.+)(-SNAPSHOT)?$");

  protected final Git git;
  protected volatile String gitDescribeOutput = null;

  protected static final Set<String> EXCLUDE_SET = new HashSet<>();
  static {
    EXCLUDE_SET.add("master");
    EXCLUDE_SET.add("release");
    EXCLUDE_SET.add("hotfix");
  }

  GitVersionBuilder(Git git) {
    this.git = git;
  }

  String getVersion() {
    describe();
    if (StringUtils.isBlank(gitDescribeOutput)) {
      throw new GitVersionException("Git describe command returns empty result");
    }

    final String branchName = getBranchName();
    final Matcher matcher = DESCRIBE_PATTERN.matcher(gitDescribeOutput);
    if (!matcher.matches()) {
      throw new GitVersionException("Git describe command returns incorrect result\n" +
          gitDescribeOutput + "\nExpected pattern: " + DESCRIBE_PATTERN.pattern());
    }

    if (!EXCLUDE_SET.contains(branchName.toLowerCase())) {
      final String distance = matcher.group(2);
      return matcher.replaceFirst("(1)-"+distance+"-(3)");
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

      gitDescribeOutput = executeGitCommand(git.getRepository().getWorkTree(),
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
      throw new GitVersionException("Failed to obtain git branch", e);
    }

    return (ref == null)?null:ref.getName().substring(Constants.R_HEADS.length());
  }
}
