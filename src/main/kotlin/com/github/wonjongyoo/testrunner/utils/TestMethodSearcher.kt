package com.github.wonjongyoo.testrunner.utils

import com.intellij.openapi.project.Project

class TestMethodSearcher(
    val project: Project
) {
    private val visited: MutableSet<MethodWrapper> = mutableSetOf()

    fun search(methodWrapper: MethodWrapper): Set<MethodWrapper> {
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
                search(it)
            }
            .flatten()
            .distinct()

        return testMethods + testMethodsFromRecursivelySearched
    }
}