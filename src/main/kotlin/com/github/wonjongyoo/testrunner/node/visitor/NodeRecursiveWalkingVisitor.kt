package com.github.wonjongyoo.testrunner.node.visitor

import com.github.wonjongyoo.testrunner.node.BaseNodeDescriptor

abstract class NodeRecursiveWalkingVisitor {
    open fun visitNode(baseNodeDescriptor: BaseNodeDescriptor) {
        for (child in baseNodeDescriptor.children) {
            visitNode(child)
        }
    }
}