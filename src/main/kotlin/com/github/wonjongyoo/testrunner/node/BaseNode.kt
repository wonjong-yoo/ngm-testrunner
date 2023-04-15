package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.github.wonjongyoo.testrunner.utils.TestRunResult
import com.intellij.ui.treeStructure.SimpleNode

abstract class BaseNode(
    val methodWrapper: MethodWrapper
): SimpleNode(methodWrapper.getElement().project) {
    val children: MutableSet<BaseNode> = mutableSetOf()

    fun addChildren(baseNode: List<BaseNode>) {
        children.addAll(baseNode)
    }

    override fun toString(): String {
        return methodWrapper.getMethodName()
    }

    fun printRecursively(tab: Int = 0) {
        (1 .. tab).forEach { print("\t") }
        println(this.name)
        children.forEach {
            it.printRecursively(tab + 1)
        }
    }

    fun findAllTestMethodsForAllChild(): Set<MethodWrapper> {
        val testMethods: MutableSet<MethodWrapper> = mutableSetOf()

        if (this is TestNode) {
            testMethods.add(this.methodWrapper)
            return testMethods
        }

        for (child in children) {
            testMethods.addAll(child.findAllTestMethodsForAllChild())
        }

        return testMethods
    }

    override fun getChildren(): Array<SimpleNode> {
        return children.toTypedArray()
    }

    abstract override fun getName(): String?

    fun setIconRecursively(testRunResults: List<TestRunResult>) {
        if (this is TestNode) {
            // TODO: 개선 요망
            testRunResults.firstOrNull { it.testName == this.name }?.let {
                this.setIcon(it.success)
            }
        }

        for (child in this.children) {
            child.setIconRecursively(testRunResults)
        }
    }
}