package com.github.wonjongyoo.testrunner.window

import com.github.wonjongyoo.testrunner.node.BaseNodeDescriptor
import com.github.wonjongyoo.testrunner.node.visitor.FindingTestMethodVisitor
import com.github.wonjongyoo.testrunner.runner.JunitTestRunner
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.PsiElement
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
    val tree: SimpleTree

    init {
        val project = toolWindow.project
        val treeModelHolder = project.getService(TreeModelHolder::class.java)
        treeModelHolder.treeModel = DefaultTreeModel(DefaultMutableTreeNode())

        tree = MySimpleTree(treeModelHolder.treeModel)
        tree.cellRenderer = CustomTreeCellRenderer()
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                // 마우스 오른쪽 버튼 클릭시 Popup Menu 노출
                if (SwingUtilities.isRightMouseButton(e)) {
                    val row = tree.getRowForLocation(e.x, e.y)
                    if (row != -1) {
                        // tree.selectionModel.setSelectionInterval(row, row)
                        showPopupMenu(tree, e.x, e.y)
                    }
                }

                // 더블 클릭시 해당 PsiElement 위치로 이동
                if (e.clickCount == 2) {
                    val row = tree.getRowForLocation(e.x, e.y)
                    if (row != -1) {
                        val selectedNode = tree.selectedNode

                        if (selectedNode is BaseNodeDescriptor) {
                            val psiElement = selectedNode.methodWrapper.getElement()
                            navigateToPsiElement(project, psiElement)
                        }
                    }
                }
            }
        })

        mainPanel = JPanel(BorderLayout())
        mainPanel.add(JBScrollPane(tree), BorderLayout.CENTER)
    }

    private fun navigateToPsiElement(project: Project, psiElement: PsiElement) {
        val virtualFile = psiElement.containingFile.virtualFile
        if (virtualFile != null) {
            val fileEditorManager = FileEditorManager.getInstance(project)
            val editor: Editor? =
                fileEditorManager.openTextEditor(OpenFileDescriptor(project, virtualFile, psiElement.textOffset), true)
            if (editor != null) {
                editor.getCaretModel().moveToOffset(psiElement.textOffset)
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER)
            }
        }
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
                (baseNode as BaseNodeDescriptor).accept(visitor)
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