package com.github.wonjongyoo.ngm.utils

import com.intellij.execution.testframework.sm.runner.SMTestProxy.SMRootTestProxy

class TestResultParseUtils {
    companion object {
        fun parseTestResult(root :SMRootTestProxy): List<TestRunResult> {
            return root.allTests
                .mapNotNull {
                    val locationUrl = it.locationUrl ?: ""
                    val regex = """java:test://(.+)/(.+)""".toRegex()
                    val matchResult = regex.find(locationUrl) ?: return@mapNotNull null

                    val (className, methodName) = matchResult.destructured
                    TestRunResult("${className.split(".").last()}.$methodName", it.isPassed)
                }.toList()
        }
    }


    data class TestRunResult(
        val testName: String,
        val success: Boolean
    )
}