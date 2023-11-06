package com.github.wonjongyoo.ngm.node.visitor

import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor
import com.github.wonjongyoo.ngm.node.TestMethodNodeDescriptor

class FindingFailedTestMethodVisitor: NodeRecursiveWalkingVisitor() {
    val failedTestMethods: MutableSet<TestMethodNodeDescriptor> = mutableSetOf()
    val visited: MutableSet<BaseNodeDescriptor> = mutableSetOf()
    override fun visitNode(baseNodeDescriptor: BaseNodeDescriptor) {
        if (visited.contains(baseNodeDescriptor)) {
            return
        }
        visited.add(baseNodeDescriptor)

        if (baseNodeDescriptor is TestMethodNodeDescriptor
            && baseNodeDescriptor.isPass != null
            && baseNodeDescriptor.isPass == false) {
            failedTestMethods.add(baseNodeDescriptor)
        }

        super.visitNode(baseNodeDescriptor)
    }
}