package com.github.wonjongyoo.ngm.window

import com.github.wonjongyoo.ngm.model.NgmTestRunnerDataKeys
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.tree.TreeUtil
import javax.swing.JTree

class NgmTestRunnerToolWindowActions {
    companion object {
        private fun getTree(e: AnActionEvent): JTree? {
            return e.getData(NgmTestRunnerDataKeys.TEST_METHOD_TREE_MODEL)
        }
    }

    class ExpandAllAction : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val tree = getTree(e)
            if (tree == null) {
                println("tree is null")
                return
            }

            TreeUtil.expandAll(tree)
        }
    }

    class CollapseAllAction : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val tree = getTree(e)
            if (tree == null) {
                println("tree is null")
                return
            }

            TreeUtil.collapseAll(tree, -1)
        }
    }
}