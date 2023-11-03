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

        val psiFunctionWrappers = ReferenceSearchUtils.searchReferences(methodWrapper, project)
            .mapNotNull {
                it.searchPsiFunctionElement()
            }

        // 테스트 메서드 탐색
        val testMethods: Set<MethodWrapper>  = psiFunctionWrappers.filter { it.isJunitTestMethod() }.toSet()
        // 컨트롤러 테스트 메서드 탐색, 컨트롤러 메서드는 보통 직접 대상 메서드를 호출 하지 않고 MockMvc 를 쓰기 때문에 클래스 이름으로부터 간접적으로 테스트 메서드를 유추한다.
        val controllerTestMethods: Set<MethodWrapper>  = getControllerTestMethods(methodWrapper)

        // 1. 기준 메서드에 대한 노드
        val newNode = ClassMethodNodeDescriptor(methodWrapper)
        // 2. 기준 메서드를 호출하는 테스트 메서드를 자식 노드로 먼저 추가
        newNode.addChildren(
            (testMethods + controllerTestMethods).map {
                TestMethodNodeDescriptor(it)
            }
        )

        // 3. 기준 메서드를 호출하는 테스트 메서드를 제외하고 나머지 메서드는 **재귀 호출**하여 나식 노드에 추가
        val childNodes = psiFunctionWrappers.filter { !it.isJunitTestMethod() }
            .mapNotNull {
                buildInvocationTree(it)
            }
        newNode.addChildren(childNodes)

        return newNode
    }

    private fun getControllerTestMethods(methodWrapper: MethodWrapper): Set<MethodWrapper> {
        val classFqName = methodWrapper.getContainingClassFqName()
        if (!classFqName.contains("Controller")) {
            return setOf()
        }

        val psiFacade = JavaPsiFacade.getInstance(project)
        val targetControllerTestClassName = "${classFqName}Test"
        val javaControllerTestClass = psiFacade.findClass(
            targetControllerTestClassName,
            project.projectScope()
        )

        val target: List<MethodWrapper> = javaControllerTestClass?.methods
            ?.filter { psiMethod ->
                psiMethod.name.contains(methodWrapper.getMethodName())
            }
            ?.filter { psiMethod ->
                psiMethod !is KtLightMethod
            }
            ?.map { method -> method.toWrapper() } ?: listOf()
        val target2: List<MethodWrapper> =
            KotlinFullClassNameIndex[targetControllerTestClassName, project, project.projectScope()]
                .firstOrNull()
                ?.declarations
                ?.filterIsInstance<KtNamedFunction>()
                ?.filter { function ->
                    function.name?.contains(methodWrapper.getMethodName()) ?: false
                }
                ?.map { function ->
                    function.toWrapper()
                } ?: listOf()

        return (target + target2).toSet()
    }
}

data class TestRunResult(
    val testName: String,
    val success: Boolean
)
