package com.github.wonjongyoo.testrunner.node.visitor

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.github.wonjongyoo.testrunner.node.TestMethodNode
import com.github.wonjongyoo.testrunner.utils.MethodWrapper

class FindingTestMethodVisitor : NodeRecursiveWalkingVisitor() {
    val testMethodNodes: MutableSet<TestMethodNode> = mutableSetOf()
    val visited: MutableSet<BaseNode> = mutableSetOf()

    override fun visitNode(baseNode: BaseNode) {
        if (visited.contains(baseNode)) {
            return
        }
        visited.add(baseNode)

        if (baseNode is TestMethodNode) {
            testMethodNodes.add(baseNode)
        }

        super.visitNode(baseNode)
    }

    fun getTestMethodWrappers(): Set<MethodWrapper> {
        return testMethodNodes.map { it.methodWrapper }.toSet()
    }
}