package com.github.wonjongyoo.ngm.node.visitor

import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor

abstract class NodeRecursiveWalkingVisitor {
    open fun visitNode(baseNodeDescriptor: BaseNodeDescriptor) {
        for (child in baseNodeDescriptor.children) {
            visitNode(child)
        }
    }
}
