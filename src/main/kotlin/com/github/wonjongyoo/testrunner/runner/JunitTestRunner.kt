package com.github.wonjongyoo.testrunner.runner

import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.testframework.TestSearchScope
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.first
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class JunitTestRunner {
    companion object {
        fun runTestMethod(
            project: Project,
            testMethodListByTestClass: Map<KtClass, List<KtNamedFunction>>
        ) {
            val testMethodName = "Run all tests in locally change files"

            val runManager = RunManager.getInstance(project)
            val setting = runManager.createConfiguration(
                testMethodName, JUnitConfigurationType::class.java
            )

            val runConfiguration = setting.configuration
            if (runConfiguration is JUnitConfiguration) {
                val jUnitConfiguration = runConfiguration

                val firstTestClass = testMethodListByTestClass.first().key
                val module = ModuleUtilCore.findModuleForPsiElement(firstTestClass)
                if (module != null) {
                    jUnitConfiguration.setSearchScope(TestSearchScope.WHOLE_PROJECT)
                    jUnitConfiguration.setModule(module)
                }

                val persistentData = jUnitConfiguration.persistentData
                persistentData.TEST_OBJECT = JUnitConfiguration.TEST_PATTERN

                persistentData.setPatterns(generateTestPattern(testMethodListByTestClass))

                val executor = DefaultRunExecutor.getRunExecutorInstance()
                ExecutionUtil.runConfiguration(setting, executor)
            }
        }

        fun runTestMethod(
            project: Project,
            testMethods: Set<MethodWrapper>,
            junitRunMessage: String
        ) {
            val runManager = RunManager.getInstance(project)
            val setting = runManager.createConfiguration(junitRunMessage, JUnitConfigurationType::class.java)

            val runConfiguration = setting.configuration
            if (runConfiguration is JUnitConfiguration) {
                val jUnitConfiguration = runConfiguration

                val firstTestMethod = testMethods.first().getElement()
                val module = ModuleUtilCore.findModuleForPsiElement(firstTestMethod)
                if (module != null) {
                    jUnitConfiguration.setSearchScope(TestSearchScope.WHOLE_PROJECT)
                    jUnitConfiguration.setModule(module)
                }

                val persistentData = jUnitConfiguration.persistentData
                persistentData.TEST_OBJECT = JUnitConfiguration.TEST_PATTERN

                persistentData.setPatterns(generateTestPattern(testMethods))

                val executor = DefaultRunExecutor.getRunExecutorInstance()
                ExecutionUtil.runConfiguration(setting, executor)
            }
        }

        private fun generateTestPattern(
            testMethodListByTestClass: Map<KtClass, List<KtNamedFunction>>
        ): LinkedHashSet<String> {
            val testPatterns = testMethodListByTestClass.map {
                val testClass = it.key
                val testMethods = it.value


                val testClassFqName = testClass.fqName.toString()
                testMethods.map {
                    "$testClassFqName,${it.name}"
                }
            }
                .flatMap { it }

            return LinkedHashSet(testPatterns)
        }

        private fun generateTestPattern(
            testMethodWrappers: Set<MethodWrapper>
        ): LinkedHashSet<String> {
            val testPatterns = testMethodWrappers.map {
                val testClassFqName = it.getContainingClassFqName()

                "$testClassFqName,${it.getMethodName()}"
            }

            return LinkedHashSet(testPatterns)
        }
    }
}