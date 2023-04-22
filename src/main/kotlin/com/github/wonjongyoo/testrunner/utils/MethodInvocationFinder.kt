package com.github.wonjongyoo.testrunner.utils

import com.github.wonjongyoo.testrunner.node.BaseNodeDescriptor
import com.github.wonjongyoo.testrunner.node.ClassMethodNodeDescriptor
import com.github.wonjongyoo.testrunner.node.TestMethodNodeDescriptor
import com.intellij.openapi.project.Project

class MethodInvocationFinder(
    val project: Project
) {
    private val visited: MutableSet<MethodWrapper> = mutableSetOf()

    fun searchByMethodWrapper(methodWrapper: MethodWrapper): Set<MethodWrapper> {
        if (visited.contains(methodWrapper)) {
            return mutableSetOf()
        }
        visited.add(methodWrapper)

        val references = ReferenceSearchUtils.searchReferences(methodWrapper.getElement(), project)

        val psiFunctionWrappers = references.mapNotNull {
            it.searchPsiFunctionElement()
        }

        // 테스트 메서드 탐색
        val testMethods = psiFunctionWrappers.filter {
            it.isJunitTestMethod()
        }.toSet()

        // 그 외 메서드는 재귀적으로 테스트 메서드를 탐색
        val testMethodsFromRecursivelySearched = psiFunctionWrappers.filter { !it.isJunitTestMethod() }
            .map {
                searchByMethodWrapper(it)
            }
            .flatten()
            .distinct()

        return testMethods + testMethodsFromRecursivelySearched
    }

    fun buildInvocationTree(
        methodWrapper: MethodWrapper,
    ): BaseNodeDescriptor? {
        if (visited.contains(methodWrapper)) {
            println("already visited : ${methodWrapper.getMethodName()}")
            return null
        }
        visited.add(methodWrapper)

        println("visit : ${methodWrapper.getMethodName()}")

        val references = ReferenceSearchUtils.searchReferences(methodWrapper.getElement(), project)

        val psiFunctionWrappers = references.mapNotNull {
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
