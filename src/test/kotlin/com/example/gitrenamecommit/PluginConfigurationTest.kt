package com.example.gitrenamecommit

import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PluginConfigurationTest : BasePlatformTestCase() {

    public override fun setUp() {
        super.setUp()
    }

    fun testPluginActionRegistered() {
        val actionManager = ActionManager.getInstance()
        val action = actionManager.getAction("com.example.gitrenamecommit.RenameCurrentCommitAction")

        assertNotNull("Action should be registered", action)
        assertTrue("Action should be an instance of RenameCurrentCommitAction", action is RenameCurrentCommitAction)
    }

    fun testActionAddedToGitMenu() {
        val actionManager = ActionManager.getInstance()
        val gitMenu = actionManager.getAction("Git.MainMenu")

        assertNotNull("Git menu should exist", gitMenu)

        val action = actionManager.getAction("com.example.gitrenamecommit.RenameCurrentCommitAction")
        assertNotNull("Our action should be registered", action)
    }

    fun testActionAddedToGitLogContextMenu() {
        val actionManager = ActionManager.getInstance()
        val gitLogContextMenu = actionManager.getAction("Git.Log.ContextMenu")

        assertNotNull("Git log context menu should exist", gitLogContextMenu)

        val action = actionManager.getAction("com.example.gitrenamecommit.RenameCurrentCommitAction")
        assertNotNull("Our action should be registered", action)
    }

    fun testNotificationGroupRegistered() {
        val notificationGroup = NotificationGroupManager.getInstance()
            .getNotificationGroup("Git Commit Rename")

        assertNotNull("Notification group should be registered", notificationGroup)
    }
}
