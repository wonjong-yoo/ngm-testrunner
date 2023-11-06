package com.github.wonjongyoo.ngm.window

import com.github.wonjongyoo.ngm.model.NgmTestRunnerDataKeys
import com.github.wonjongyoo.ngm.model.TestRunnerDataHolder
import com.github.wonjongyoo.ngm.testrunner.JunitTestRunner
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

    class RerunAllTestAction : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val testRunnerDataHolder = project.getService(TestRunnerDataHolder::class.java)

            JunitTestRunner.runTestMethods(e.project!!, testRunnerDataHolder.testMethods, "Rerun all affected tests")
        }
    }

    class RerunAllFailedTestAction : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val tree: JTree? = getTree(e)
            if (tree == null) {
                println("tree is null")
                return
            }

        }
    }
}
