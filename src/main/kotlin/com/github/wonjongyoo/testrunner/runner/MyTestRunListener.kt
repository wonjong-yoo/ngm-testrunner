package com.github.wonjongyoo.testrunner.runner

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.github.wonjongyoo.testrunner.utils.TestRunResult
import com.github.wonjongyoo.testrunner.window.TreeModelHolder
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.openapi.project.Project
import javax.swing.tree.DefaultMutableTreeNode

class MyTestRunListener : TestStatusListener() {
    companion object {
        fun getInstance(project: Project): MyTestRunListener {
            return project.getService(MyTestRunListener::class.java)
        }
    }

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
                TestRunResult("${className.split(".").last()}#$methodName", it.isPassed)
            }.toList()

        val treeModelHolder = project?.getService(TreeModelHolder::class.java) ?: return

        val rootNode = ((treeModelHolder.treeModel.root as DefaultMutableTreeNode).userObject) as BaseNode
        rootNode.setIconRecursively(testRunResults)
        treeModelHolder.treeModel.reload()
    }
}