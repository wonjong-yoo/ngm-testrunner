package com.github.wonjongyoo.testrunner.node.visitor

import com.github.wonjongyoo.testrunner.node.BaseNode

abstract class NodeRecursiveWalkingVisitor {
    open fun visitNode(baseNode: BaseNode) {
        for (child in baseNode.children) {
            visitNode(child)
        }
    }
}