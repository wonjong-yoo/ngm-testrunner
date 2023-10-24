package com.github.wonjongyoo.ngm.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

class ToolWindowUtils {
    companion object {
        val ngmTestRunnerToolWindowId = "NgmTestRunner"
        fun activateNgmTestRunnerToolWindow(project: Project) {
            val toolWindowManager = ToolWindowManager.getInstance(project)

            val ngmTestRunnerToolWindow = toolWindowManager.getToolWindow(ngmTestRunnerToolWindowId)
            if (ngmTestRunnerToolWindow == null) {
                println("There is no ngm test runner tool window.")
                return
            }

            ngmTestRunnerToolWindow.activate(null)
        }
    }
}