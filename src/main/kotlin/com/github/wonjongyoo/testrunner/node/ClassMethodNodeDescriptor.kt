package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.icons.AllIcons

/**
 * 테스트 메서드가 아닌 일반적인 클래스에 존재하는 메서드
 */
class ClassMethodNodeDescriptor(methodWrapper: MethodWrapper) : BaseNodeDescriptor(methodWrapper) {
    var allTestCount: Int? = null
    var passedTestCount: Int? = null
    init {
        this.icon = AllIcons.Nodes.Method
    }
}
