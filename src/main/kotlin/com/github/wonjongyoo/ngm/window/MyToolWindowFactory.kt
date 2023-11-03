package com.github.wonjongyoo.ngm.window

import com.github.wonjongyoo.ngm.icons.NgmTestRunnerIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = toolWindow.contentManager.factory.createContent(myToolWindow, "All Affected Methods And Tests", false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setIcon(NgmTestRunnerIcons.toolWindowIcon)
    }
}
