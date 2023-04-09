package com.github.wonjongyoo.testrunner.runner

import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.testframework.TestSearchScope
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project

class JunitTestRunner {
    companion object {
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

        fun runTestMethods(
            project: Project,
            testMethods: Set<MethodWrapper>,
            junitRunMessage: String
        ) {
            val runManager = RunManager.getInstance(project)

            val a = testMethods.map {
                val module = ModuleUtilCore.findModuleForPsiElement(it.getElement())

                module to it
            }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, value) -> LinkedHashSet(value) }

            val settings = a.map { (module, target) ->
                val newMessage = "[${module?.name}] $junitRunMessage"
                val setting = runManager.createConfiguration(newMessage, JUnitConfigurationType::class.java)
                println("module : ${module?.name} -> tests : ${target.map { it.getMethodName() }}")
                val runConfiguration = setting.configuration
                if (runConfiguration is JUnitConfiguration) {
                    val jUnitConfiguration = runConfiguration

                    if (module != null) {
                        jUnitConfiguration.setSearchScope(TestSearchScope.WHOLE_PROJECT)
                        jUnitConfiguration.setModule(module)
                    }

                    val persistentData = jUnitConfiguration.persistentData
                    persistentData.TEST_OBJECT = JUnitConfiguration.TEST_PATTERN

                    persistentData.setPatterns(generateTestPattern(target))

                    // DefaultRunExecutor.getRunExecutorInstance()
                    // ExecutionUtil.runConfiguration(setting, executor)
                }
                setting.isTemporary = true

                setting
            }

            runModuleTests(settings, project)
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

        private fun runModuleTests(
            settings: List<RunnerAndConfigurationSettings>,
            project: Project
        ) {
            settings.forEach { setting ->
                val environment = ExecutionEnvironmentBuilder
                    .create(DefaultRunExecutor.getRunExecutorInstance(), setting)
                    .build()

                environment?.let {
                    ExecutionManager.getInstance(project).restartRunProfile(it)
                }
            }
        }
    }
}