package com.github.wonjongyoo.ngm.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // val button = JButton(AllIcons.Actions.Execute)
        // val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        // buttonPanel.add(button)
        //
        // val panel = JPanel(BorderLayout())
        // panel.add(buttonPanel, BorderLayout.NORTH)

        // button.addActionListener {
        //     println("Button Clicked")
        // }
        //
        // val treeModelHolder = ServiceManager.getService(project, TreeModelHolder::class.java)
        // treeModelHolder?.treeModel = DefaultTreeModel(rootNode)
        // val tree = SimpleTree(treeModelHolder?.treeModel)
        // // tree.cellRenderer = CustomTreeCellRenderer()
        //
        // panel.add(tree, BorderLayout.CENTER)
        // val content = toolWindow.contentManager.factory.createContent(panel, "", false)
        // toolWindow.contentManager.addContent(content)

        // toolWindow.component.parent.add(tree)

        val myToolWindow = MyToolWindow(toolWindow)
        val content = toolWindow.contentManager.factory.createContent(myToolWindow.mainPanel, "All Affected Methods And Tests", false)
        toolWindow.contentManager.addContent(content)
    }


}
