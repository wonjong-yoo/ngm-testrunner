package com.github.wonjongyoo.testrunner.node.visitor

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.github.wonjongyoo.testrunner.node.TestMethodNode
import com.github.wonjongyoo.testrunner.utils.TestRunResult

class ChangingTestMethodIconVisitor(
    private val testRunResults: List<TestRunResult>
): NodeRecursiveWalkingVisitor() {
    override fun visitNode(baseNode: BaseNode) {
        if (baseNode is TestMethodNode) {
            // TODO: 개선 요망
            testRunResults.firstOrNull { it.testName == baseNode.name }?.let {
                baseNode.setIconByTestResult(it.success)
            }
        }

        super.visitNode(baseNode)
    }
}