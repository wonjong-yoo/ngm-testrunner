package com.github.wonjongyoo.ngm.node

import com.github.wonjongyoo.ngm.utils.MethodWrapper
import com.intellij.icons.AllIcons

class TestMethodNodeDescriptor(methodWrapper: MethodWrapper) : BaseNodeDescriptor(methodWrapper) {
    var isPass: Boolean? = null
    init {
        // 초기 값은 Junit Test Icon 으로 설정. 테스트 실행 이후 성공 혹은 실패로 변경
        this.icon = AllIcons.Nodes.JunitTestMark
    }

    fun setIconByTestResult(isSuccess: Boolean) {
        if (isSuccess) {
            this.isPass = true
            this.icon = AllIcons.RunConfigurations.TestPassed
        } else {
            this.isPass = false
            this.icon = AllIcons.RunConfigurations.TestFailed
        }
    }
}