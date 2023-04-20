package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.node.visitor.NodeRecursiveWalkingVisitor
import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.ide.projectView.impl.ProjectViewTree
import com.intellij.ui.treeStructure.SimpleNode
import java.awt.Color
import javax.swing.tree.DefaultMutableTreeNode

abstract class BaseNodeDescriptor(
    val methodWrapper: MethodWrapper
): SimpleNode(methodWrapper.getElement().project) {
    private val children: MutableSet<BaseNodeDescriptor> = mutableSetOf()
    val backgroundColorCached: Color? = ProjectViewTree.getColorForElement(methodWrapper.getElement())

    abstract override fun getName(): String?

    override fun toString(): String {
        return methodWrapper.getMethodName()
    }

    override fun getChildren(): Array<BaseNodeDescriptor> {
        return children.toTypedArray()
    }

    fun addChildren(baseNodeDescriptor: List<BaseNodeDescriptor>) {
        children.addAll(baseNodeDescriptor)
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

    fun toTreeNode(): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(this)

        for (child in this.children) {
            node.add(child.toTreeNode())
        }

        return node
    }
}
