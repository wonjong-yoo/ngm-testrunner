package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.icons.AllIcons

class MethodNode(methodWrapper: MethodWrapper) : BaseNode(methodWrapper) {
    init {
        this.icon = AllIcons.Nodes.Method
    }

    override fun getName(): String {
        return "${methodWrapper.getContainingClassName()}#${methodWrapper.getMethodName()}"
    }
}