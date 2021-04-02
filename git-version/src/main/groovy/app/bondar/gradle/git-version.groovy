package app.bondar.gradle


import static GitVersionUtils.findGitRepo

import org.gradle.api.Plugin
import org.gradle.api.Project

// See Writing Custom Plugins at
// https://docs.gradle.org/current/userguide/custom_plugins.html#custom_plugins

class GitVersionPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        // Find Git repo by exploring Gradle modules tree up to the root module
        def git = findGitRepo(project.getProjectDir())
        if (git == null) {
            throw new IllegalStateException("Git repository wasn't found")
        }

        // Declare gradle gitVersion property as Groovy closure,
        // so accessing property as gitVersion() calculates project version based on git tag and revision
        def properties = project.getExtensions().getExtraProperties()
        properties.set("gitVersion", { args -> return new GitVersionBuilder(git).getVersion() })

        // Declare gradle task gitVersion that prints project version based on git tasg and revision to stdout
        project.getTasks().create("gitVersion", GitVersionTask.class)
    }
}
