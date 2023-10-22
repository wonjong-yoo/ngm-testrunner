package com.github.wonjongyoo.ngm.utils

import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor
import com.github.wonjongyoo.ngm.node.ClassMethodNodeDescriptor
import com.github.wonjongyoo.ngm.node.TestMethodNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.KtNamedFunction

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
        val controllerTestMethods = methodWrapper.let {
            val classFqName = it.getContainingClassFqName()
            if (classFqName.contains("Controller")) {
                val psiFacade = JavaPsiFacade.getInstance(project)
                val targetControllerTestClassName = "${classFqName}Test"
                val javaControllerTestClass = psiFacade.findClass(
                    targetControllerTestClassName,
                    project.projectScope()
                )

                val target: List<MethodWrapper> = javaControllerTestClass?.methods
                    ?.filter { method ->
                        method.name.contains(it.getMethodName())
                    }
                    ?.filter { method ->
                        method !is KtLightMethod
                    }
                    ?.map { method -> method.toWrapper() } ?: listOf()
                val target2: List<MethodWrapper> = KotlinFullClassNameIndex.get(targetControllerTestClassName, project, project.projectScope())
                    .firstOrNull()
                    ?.declarations
                    ?.filterIsInstance<KtNamedFunction>()
                    ?.filter {  function ->
                        function.name?.contains(it.getMethodName()) ?: false
                    }
                    ?.map { function ->
                        function.toWrapper()
                    } ?: listOf()

                return@let (target + target2)
            }

            listOf()
        }

        // 1. 기준 메서드에 대한 노드
        val newNode = ClassMethodNodeDescriptor(methodWrapper)
        // 2. 기준 메서드를 호출하는 테스트 메서드를 자식 노드로 먼저 추가
        newNode.addChildren(
            (testMethods + controllerTestMethods).map {
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
