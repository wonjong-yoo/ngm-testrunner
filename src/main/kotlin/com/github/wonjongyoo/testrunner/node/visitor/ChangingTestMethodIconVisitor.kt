package com.github.wonjongyoo.testrunner.node.visitor

import com.github.wonjongyoo.testrunner.node.BaseNodeDescriptor
import com.github.wonjongyoo.testrunner.node.TestMethodNodeDescriptor
import com.github.wonjongyoo.testrunner.utils.TestRunResult

class ChangingTestMethodIconVisitor(
    private val testRunResults: List<TestRunResult>
): NodeRecursiveWalkingVisitor() {
    override fun visitNode(baseNodeDescriptor: BaseNodeDescriptor) {
        if (baseNodeDescriptor is TestMethodNodeDescriptor) {
            // TODO: 개선 요망
            testRunResults.firstOrNull { it.testName == baseNodeDescriptor.name }?.let {
                baseNodeDescriptor.setIconByTestResult(it.success)
            }
        }

        super.visitNode(baseNodeDescriptor)
    }
}