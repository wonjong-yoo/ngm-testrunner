package com.github.wonjongyoo.ngm.node.visitor

import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor
import com.github.wonjongyoo.ngm.node.TestMethodNodeDescriptor
import com.github.wonjongyoo.ngm.utils.MethodWrapper

class FindingTestMethodVisitor : NodeRecursiveWalkingVisitor() {
    val testMethodNodes: MutableSet<TestMethodNodeDescriptor> = mutableSetOf()
    val visited: MutableSet<BaseNodeDescriptor> = mutableSetOf()

    override fun visitNode(baseNodeDescriptor: BaseNodeDescriptor) {
        if (visited.contains(baseNodeDescriptor)) {
            return
        }
        visited.add(baseNodeDescriptor)

        if (baseNodeDescriptor is TestMethodNodeDescriptor) {
            testMethodNodes.add(baseNodeDescriptor)
        }

        super.visitNode(baseNodeDescriptor)
    }

    fun getTestMethodWrappers(): Set<MethodWrapper> {
        return testMethodNodes.map { it.methodWrapper }.toSet()
    }
}