package com.github.wonjongyoo.ngm.testlistener

import com.github.wonjongyoo.ngm.model.TestRunnerDataHolder
import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor
import com.github.wonjongyoo.ngm.node.visitor.ChangingTestMethodIconVisitor
import com.github.wonjongyoo.ngm.utils.TestRunResult
import com.github.wonjongyoo.ngm.utils.ToolWindowUtils
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy.SMRootTestProxy
import com.intellij.openapi.project.Project
import javax.swing.tree.DefaultMutableTreeNode

class MyTestRunListener : TestStatusListener() {

    override fun testSuiteFinished(root: AbstractTestProxy?) {
        // Nothing..
    }

    // 테스트 실행이 종료되었을 때 이 callback 호출
    override fun testSuiteFinished(root: AbstractTestProxy?, project: Project?) {

        println("finished")
        if (root == null
            || (root is SMRootTestProxy
                && root.testConsoleProperties?.configuration?.name?.contains("(run by NGM)") == false)) {
            return
        }

        val testRunResults = root.allTests
            .mapNotNull {
                val locationUrl = it.locationUrl ?: ""
                val regex = """java:test://(.+)/(.+)""".toRegex()
                val matchResult = regex.find(locationUrl) ?: return@mapNotNull null

                val (className, methodName) = matchResult.destructured
                TestRunResult("${className.split(".").last()}.$methodName", it.isPassed)
            }.toList()

        val testRunnerDataHolder = project?.getService(TestRunnerDataHolder::class.java) ?: return
        val defaultMutableTreeNode = testRunnerDataHolder.treeModel.root as DefaultMutableTreeNode
        val targetNodes = when (val userObject = defaultMutableTreeNode.userObject) {
            is String -> defaultMutableTreeNode.children().asSequence().map { (it as DefaultMutableTreeNode).userObject as BaseNodeDescriptor }.toList()
            is BaseNodeDescriptor -> listOf(userObject)
            else -> null
        }

        targetNodes?.forEach { targetNode ->
            val visitor = ChangingTestMethodIconVisitor(testRunResults)
            targetNode.accept(visitor)

            targetNode.applyTestResult()
        }

        testRunnerDataHolder.treeModel.reload()
        ToolWindowUtils.activateNgmTestRunnerToolWindow(project)
    }
}
