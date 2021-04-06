package app.bondar.gradle;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class GitVersionTask extends DefaultTask {

  private final GitVersionBuilder builder;

  @Inject
  public GitVersionTask(Git git) {
    builder = new GitVersionBuilder(git);
    setDescription("Print project version based on git tag and revision");
    setGroup("git-version");
  }

  @TaskAction
  @SuppressWarnings("java:S106")
  public void process() {
    System.out.println(builder.getVersion());
  }
}
