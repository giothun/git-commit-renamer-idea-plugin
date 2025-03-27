package com.example.gitrenamecommit

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

class RenameCurrentCommitAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    companion object {
        private val LOG = Logger.getInstance(RenameCurrentCommitAction::class.java)

        private val NOTIFICATION_GROUP by lazy {
            NotificationGroupManager.getInstance().getNotificationGroup("Git Commit Rename")
                ?: NotificationGroupManager.getInstance().getNotificationGroup("Vcs Messages")
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val repositories = GitRepositoryManager.getInstance(project).repositories

        val repository = when {
            repositories.isEmpty() -> {
                project.showNotification("No Git repository found.", NotificationType.ERROR)
                return
            }

            repositories.size == 1 -> repositories.first()
            else -> project.selectRepository(repositories) ?: return
        }

        if (!repository.canAmend()) {
            project.showNotification("Cannot rename commit in current repository state.", NotificationType.ERROR)
            return
        }

        val newMessage = Messages.showInputDialog(
            project, "Enter new commit message:", "Rename Current Commit", Messages.getQuestionIcon()
        )?.takeIf { it.isNotBlank() } ?: run {
            project.showNotification("Commit rename cancelled or empty message provided.", NotificationType.WARNING)
            return
        }

        project.renameCommit(repository, newMessage)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible =
            event.project?.let { GitRepositoryManager.getInstance(it).repositories.isNotEmpty() } == true
    }

    private fun Project.renameCommit(repository: GitRepository, newMessage: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            runCatching {
                val handler = GitLineHandler(this, repository.root, GitCommand.COMMIT).apply {
                    addParameters("--amend", "-m", newMessage)
                }
                Git.getInstance().runCommand(handler)
            }.onSuccess { result ->
                if (result.success()) {
                    repository.update()
                    VcsDirtyScopeManager.getInstance(this).markEverythingDirty()

                    ApplicationManager.getApplication().invokeLater {
                        showNotification("Commit renamed successfully.", NotificationType.INFORMATION)
                    }
                } else {
                    LOG.warn("Git amend failed: ${result.errorOutputAsJoinedString}")
                    ApplicationManager.getApplication().invokeLater {
                        showNotification(
                            "Failed to rename commit: ${result.errorOutputAsJoinedString}", NotificationType.ERROR
                        )
                    }
                }
            }.onFailure { e ->
                LOG.error("Unexpected error while renaming commit", e)
                ApplicationManager.getApplication().invokeLater {
                    showNotification("Unexpected error while renaming commit: ${e.message}", NotificationType.ERROR)
                }
            }
        }
    }

    private fun Project.showNotification(content: String, type: NotificationType) {
        NOTIFICATION_GROUP.createNotification(content, type).notify(this)
    }

    private fun Project.selectRepository(repositories: List<GitRepository>): GitRepository? {
        val repoNames = repositories.map { it.root.name }
        val selectedName = Messages.showEditableChooseDialog(
            "Select a repository to rename the commit:",
            "Select Repository",
            Messages.getQuestionIcon(),
            repoNames.toTypedArray(),
            repoNames.firstOrNull(),
            null
        )
        return repositories.find { it.root.name == selectedName }
    }

    private fun GitRepository.canAmend(): Boolean = isOnBranch && !isRebaseInProgress
}
