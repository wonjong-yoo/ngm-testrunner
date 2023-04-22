package com.github.wonjongyoo.testrunner.node

import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.icons.AllIcons

class TestMethodNodeDescriptor(methodWrapper: MethodWrapper) : BaseNodeDescriptor(methodWrapper) {
    init {
        // 초기 값은 Junit Test Icon 으로 설정. 테스트 실행 이후 성공 혹은 실패로 변경
        this.icon = AllIcons.Nodes.JunitTestMark
    }

    override fun getName(): String {
        val argumentTypes = methodWrapper.getArgumentTypes()
        argumentTypes.joinToString(", ")

        return "${methodWrapper.getContainingClassName()}.${methodWrapper.getMethodName()}(${argumentTypes.joinToString(", ")})"
    }

    fun setIconByTestResult(isSuccess: Boolean) {
        if (isSuccess) {
            this.icon = AllIcons.RunConfigurations.TestPassed
        } else {
            this.icon = AllIcons.RunConfigurations.TestFailed
        }
    }
}