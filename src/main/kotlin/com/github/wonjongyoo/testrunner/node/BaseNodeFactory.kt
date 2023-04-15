package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.utils.MethodWrapper

class BaseNodeFactory {
    companion object {
        fun createNode(methodWrapper: MethodWrapper) : BaseNode {
            if (methodWrapper.isJunitTestMethod()) {
                return TestNode(methodWrapper)
            } else {
                return MethodNode(methodWrapper)
            }
        }
    }
}