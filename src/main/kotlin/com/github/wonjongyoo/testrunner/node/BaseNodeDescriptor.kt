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

    override fun getName(): String {
        val argumentTypes = methodWrapper.getArgumentTypes()
        argumentTypes.joinToString(", ")

        return "${methodWrapper.getContainingClassName()}.${methodWrapper.getMethodName()}"
    }

    fun getMethodSignature(): String {
        val argumentTypes = methodWrapper.getArgumentTypes()
        argumentTypes.joinToString(", ")

        return "${this.name}(${argumentTypes.joinToString(", ")})"
    }

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

    fun applyTestResult(): Pair<Int, Int> {
        var allTest = 0
        var passedTest = 0

        if (this is TestMethodNodeDescriptor) {
            allTest += 1
            passedTest += if (this.isPass == true) 1 else 0
        } else if (this is ClassMethodNodeDescriptor) {
            for (child in this.children) {
                val test = child.applyTestResult()
                allTest += test.first
                passedTest += test.second
            }

            this.allTestCount = allTest
            this.passedTestCount = passedTest
        }

        return allTest to passedTest
    }
}
