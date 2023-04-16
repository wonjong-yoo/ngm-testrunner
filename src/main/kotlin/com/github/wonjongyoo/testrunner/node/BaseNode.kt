package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.node.visitor.NodeRecursiveWalkingVisitor
import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.ui.treeStructure.SimpleNode

abstract class BaseNode(
    val methodWrapper: MethodWrapper
): SimpleNode(methodWrapper.getElement().project) {
    private val children: MutableSet<BaseNode> = mutableSetOf()

    abstract override fun getName(): String?

    override fun toString(): String {
        return methodWrapper.getMethodName()
    }

    override fun getChildren(): Array<BaseNode> {
        return children.toTypedArray()
    }

    fun addChildren(baseNode: List<BaseNode>) {
        children.addAll(baseNode)
    }

    fun accept(visitor: NodeRecursiveWalkingVisitor) {
        visitor.visitNode(this)
    }

    fun printNodeRecursively(tab: Int = 0) {
        (1 .. tab).forEach { print("\t") }
        println(this.name)
        children.forEach {
            it.printNodeRecursively(tab + 1)
        }
    }
}
