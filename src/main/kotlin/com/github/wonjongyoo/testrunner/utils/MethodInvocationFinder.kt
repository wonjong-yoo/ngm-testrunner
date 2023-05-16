package com.github.wonjongyoo.testrunner.utils

import com.github.wonjongyoo.testrunner.node.BaseNodeDescriptor
import com.github.wonjongyoo.testrunner.node.ClassMethodNodeDescriptor
import com.github.wonjongyoo.testrunner.node.TestMethodNodeDescriptor
import com.intellij.openapi.project.Project

class MethodInvocationFinder(
    val project: Project
) {
    private val visited: MutableSet<MethodWrapper> = mutableSetOf()

    fun buildInvocationTree(
        methodWrapper: MethodWrapper,
    ): BaseNodeDescriptor? {
        if (visited.contains(methodWrapper)) {
            println("already visited : ${methodWrapper.getContainingClassFqName()}.${methodWrapper.getMethodName()}")
            return null
        }
        visited.add(methodWrapper)

        println("visit : ${methodWrapper.getContainingClassFqName()}.${methodWrapper.getMethodName()}")

        val psiFunctionWrappers = ReferenceSearchUtils.searchReferences(methodWrapper.getElement(), project)
            .mapNotNull {
                it.searchPsiFunctionElement()
            }

        // 테스트 메서드 탐색
        val testMethods = psiFunctionWrappers.filter {
            it.isJunitTestMethod()
        }.toSet()

        // 1. 기준 메서드에 대한 노드
        val newNode = ClassMethodNodeDescriptor(methodWrapper)
        // 2. 기준 메서드를 호출하는 테스트 메서드를 자식 노드로 먼저 추가
        newNode.addChildren(
            testMethods.map {
                TestMethodNodeDescriptor(it)
            }
        )

        // 3. 기준 메서드를 호출하는 테스트 메서드를 제외하고 나머지 메서드는 재귀 호출하여 나식 노드에 추가
        val childNodes = psiFunctionWrappers.filter { !it.isJunitTestMethod() }
            .mapNotNull {
                buildInvocationTree(it)
            }
        newNode.addChildren(childNodes)

        return newNode
    }
}

data class TestRunResult(
    val testName: String,
    val success: Boolean
)
