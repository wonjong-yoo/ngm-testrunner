package com.github.wonjongyoo.ngm.marker

import com.github.wonjongyoo.ngm.icons.NgmTestRunnerIcons
import com.github.wonjongyoo.ngm.model.TestRunnerDataHolder
import com.github.wonjongyoo.ngm.node.visitor.FindingTestMethodVisitor
import com.github.wonjongyoo.ngm.testrunner.JunitTestRunner
import com.github.wonjongyoo.ngm.utils.MethodInvocationFinder
import com.github.wonjongyoo.ngm.utils.MethodWrapper
import com.github.wonjongyoo.ngm.utils.ToolWindowUtils
import com.github.wonjongyoo.ngm.utils.toWrapper
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass

class KotlinMethodLineMarkerProvider: RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
        forNavigation: Boolean
    ) {
        elements
            .filterIsInstance<KtNamedFunction>()
            .filterNot {
                it.annotationEntries.any { annotation ->
                    annotation.shortName.toString().contains("Test")
                }
            }
            .forEach { element ->
                val navigationHandler = createIconNavigationHandler(element)

                val marker = NavigationGutterIconBuilder.create(NgmTestRunnerIcons.icon)
                    .setTooltipText("Run all affected tests by ${(element).containingClass()?.name}#${element.name}")
                    .setTarget(element)
                    .setPopupTitle("Run all affected tests by ${(element).containingClass()?.name}#${element.name}")
                    .createLineMarkerInfo(element, navigationHandler)

                result.add(marker)
            }
    }

    private fun createIconNavigationHandler(methodElement: PsiElement): GutterIconNavigationHandler<PsiElement> = GutterIconNavigationHandler<PsiElement> { e, elt ->
        ActionManager.getInstance().createActionPopupMenu("TEST", createActionGroup(methodElement, elt)).component.show(e.component, e.x, e.y)
    }

    private fun createActionGroup(methodElement: PsiElement, eventElement: PsiElement): ActionGroup {
        val defaultActionGroup = DefaultActionGroup("Test Action Group", true)

        val action1 = object : AnAction({ "Run all affected tests in this method" }, AllIcons.Actions.RunAll) {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project!!
                JunitTestRunner.runTestMethods(project, makeTestTreeAndRender(methodElement, eventElement), "Run all affected tests")

                ToolWindowUtils.activateNgmTestRunnerToolWindow(project)
            }
        }

        val action2 = object : AnAction({ "Show invocation tree only" }, AllIcons.Actions.ShowAsTree) {
            override fun actionPerformed(e: AnActionEvent) {
                makeTestTreeAndRender(methodElement, eventElement)
            }
        }

        defaultActionGroup.addAction(action1)
        defaultActionGroup.addAction(action2)

        return defaultActionGroup
    }

    private fun makeTestTreeAndRender(methodElement: PsiElement, eventElement: PsiElement): Set<MethodWrapper> {
        val elementAtCurrentOffset = methodElement as KtNamedFunction

        val methodWrapper: MethodWrapper = elementAtCurrentOffset.toWrapper()

        val finder = MethodInvocationFinder(eventElement.project)
        val rootBaseNode = finder.buildInvocationTree(methodWrapper)
        if (rootBaseNode == null) {
            println("invocation tree is null")
            return setOf()
        }

        val visitor = FindingTestMethodVisitor()
        rootBaseNode.accept(visitor)

        val testRunnerDataHolder = eventElement.project.getService(TestRunnerDataHolder::class.java)
        testRunnerDataHolder.treeModel.setRoot(rootBaseNode.toTreeNode())
        testRunnerDataHolder.treeModel.reload()

        val testMethodWrappers = visitor.getTestMethodWrappers()
        testRunnerDataHolder.testMethods = testMethodWrappers

        return testMethodWrappers
    }
}