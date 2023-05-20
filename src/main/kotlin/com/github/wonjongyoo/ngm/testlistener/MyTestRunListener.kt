package com.github.wonjongyoo.ngm.testlistener

import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor
import com.github.wonjongyoo.ngm.node.visitor.ChangingTestMethodIconVisitor
import com.github.wonjongyoo.ngm.utils.TestRunResult
import com.github.wonjongyoo.ngm.utils.ToolWindowUtils
import com.github.wonjongyoo.ngm.window.TreeModelHolder
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.openapi.project.Project
import javax.swing.tree.DefaultMutableTreeNode

class MyTestRunListener : TestStatusListener() {

    override fun testSuiteFinished(root: AbstractTestProxy?) {
        // Nothing..
    }

    // 테스트 실행이 종료되었을 때 이 callback 호출
    override fun testSuiteFinished(root: AbstractTestProxy?, project: Project?) {

        println("finished")
        if (root == null) {
            return
        }

        val testRunResults = root.allTests
            .mapNotNull {
                // locationUrl =  java:test://com.naver.fin.payment.core.payment.pay.service.SnapshotServiceTest/restore_inserted
                val locationUrl = it.locationUrl ?: ""
                val regex = """java:test://(.+)/(.+)""".toRegex()
                val matchResult = regex.find(locationUrl) ?: return@mapNotNull null

                val (className, methodName) = matchResult.destructured
                TestRunResult("${className.split(".").last()}.$methodName", it.isPassed)
            }.toList()

        val treeModelHolder = project?.getService(TreeModelHolder::class.java) ?: return
        val defaultMutableTreeNode = treeModelHolder.treeModel.root as DefaultMutableTreeNode
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

        treeModelHolder.treeModel.reload()
        ToolWindowUtils.activateNgmTestRunnerToolWindow(project)
    }
}