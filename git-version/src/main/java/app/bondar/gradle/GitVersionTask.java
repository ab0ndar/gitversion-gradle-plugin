package app.bondar.gradle;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class GitVersionTask extends DefaultTask {
  public GitVersionTask() {
    setDescription("Print project version based on git tag and revision");
    setGroup("git-version");
  }

  @TaskAction
  @SuppressWarnings("java:S106")
  public void process() {
    System.out.println(this.getProject().getVersion());
  }
}
