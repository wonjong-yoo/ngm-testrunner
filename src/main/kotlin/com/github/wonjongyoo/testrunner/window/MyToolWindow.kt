package com.github.wonjongyoo.testrunner.window

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.github.wonjongyoo.testrunner.node.visitor.FindingTestMethodVisitor
import com.github.wonjongyoo.testrunner.runner.JunitTestRunner
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class MyToolWindow(
    toolWindow: ToolWindow
) {
    val mainPanel: JPanel
    private val tree: SimpleTree

    init {
        val project = toolWindow.project
        val treeModelHolder = project.getService(TreeModelHolder::class.java)
        treeModelHolder.treeModel = DefaultTreeModel(DefaultMutableTreeNode())
        tree = SimpleTree(treeModelHolder.treeModel)
        tree.cellRenderer = CustomTreeCellRenderer()
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val row = tree.getRowForLocation(e.x, e.y)
                    if (row != -1) {
                        // tree.selectionModel.setSelectionInterval(row, row)
                        showPopupMenu(tree, e.x, e.y)
                    }
                }
            }
        })

        mainPanel = JPanel(BorderLayout())
        mainPanel.add(JBScrollPane(tree), BorderLayout.CENTER)
    }

    private fun showPopupMenu(component: JComponent, x: Int, y: Int) {
        val popupMenu = JPopupMenu()

        val testRunAction = JMenuItem("Run tests")
        testRunAction.addActionListener {
            val tree = tree
            val selectedNodes = tree.selectedNodesIfUniform as? Array<SimpleNode> ?: return@addActionListener
            if (selectedNodes.isEmpty()) {
                return@addActionListener
            }

            val project = selectedNodes.first().project

            val visitor = FindingTestMethodVisitor()

            val targetSet = selectedNodes.map { baseNode ->
                (baseNode as BaseNode).accept(visitor)
                visitor.getTestMethodWrappers()
            }
                .flatten()
                .toSet()

            JunitTestRunner.runTestMethods(
                project,
                targetSet,
                "Run Tests"
            )
            // val newName = JOptionPane.showInputDialog(component, "Enter new name:", node.name)
            // if (newName != null && newName.isNotBlank()) {
            //     renameNode(node, newName)
            // }
        }

        popupMenu.add(testRunAction)
        popupMenu.show(component, x, y)
    }
}