package com.github.wonjongyoo.testrunner.utils

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.github.wonjongyoo.testrunner.node.BaseNodeFactory
import com.intellij.openapi.project.Project

class TestMethodSearcher(
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

    fun search2(
        methodWrapper: MethodWrapper,
    ): BaseNode? {
        if (visited.contains(methodWrapper)) {
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

        val newNode = BaseNodeFactory.createNode(methodWrapper)
        newNode.addChildren(
            testMethods.map {
                BaseNodeFactory.createNode(it)
            }
        )

        // 그 외 메서드는 재귀적으로 테스트 메서드를 탐색
        val childNodes = psiFunctionWrappers.filter { !it.isJunitTestMethod() }
            .mapNotNull {
                search2(it)
            }

        newNode.addChildren(childNodes)

        return if (testMethods.isEmpty() && childNodes.isEmpty()) null else newNode
    }
}

data class TestRunResult(
    val testName: String,
    val success: Boolean
)
