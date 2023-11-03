package com.github.wonjongyoo.ngm.marker

import com.github.wonjongyoo.ngm.icons.NgmTestRunnerIcons
import com.github.wonjongyoo.ngm.node.visitor.FindingTestMethodVisitor
import com.github.wonjongyoo.ngm.testrunner.JunitTestRunner
import com.github.wonjongyoo.ngm.utils.MethodInvocationFinder
import com.github.wonjongyoo.ngm.utils.MethodWrapper
import com.github.wonjongyoo.ngm.utils.ToolWindowUtils
import com.github.wonjongyoo.ngm.utils.toWrapper
import com.github.wonjongyoo.ngm.window.TreeModelHolder
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
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod

class MethodJunitTestRunnerLineMarkerProvider: RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
        forNavigation: Boolean
    ) {
        elements
            .filter { it is PsiIdentifier && it.parent is PsiMethod }
            .filterNot {
                (it.parent as PsiMethod).modifierList.annotations.any { annotation ->
                    annotation.qualifiedName?.contains("org.junit") ?: false
                }
            }
            .forEach { element ->
                val navigationHandler = createIconNavigationHandler(element)

                val marker = NavigationGutterIconBuilder.create(NgmTestRunnerIcons.icon)
                    .setTooltipText("Run all affected tests by ${(element.parent as PsiMethod).containingClass?.name}#${element.text}")
                    .setTarget(element)
                    .setPopupTitle("Run all affected tests by ${(element.parent as PsiMethod).containingClass?.name}#${element.text}")
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
                runTests(methodElement, eventElement, renderTree(methodElement, eventElement))
            }
        }

        val action2 = object : AnAction({ "Show invocation tree only" }, AllIcons.Actions.ShowAsTree) {
            override fun actionPerformed(e: AnActionEvent) {
                renderTree(methodElement, eventElement)
            }
        }

        defaultActionGroup.addAction(action1)
        defaultActionGroup.addAction(action2)

        return defaultActionGroup
    }

    private fun renderTree(methodElement: PsiElement, eventElement: PsiElement): Set<MethodWrapper> {
        val elementAtCurrentOffset = methodElement.parent as PsiMethod

        val methodWrapper: MethodWrapper = elementAtCurrentOffset.toWrapper()

        val finder = MethodInvocationFinder(eventElement.project)
        val rootBaseNode = finder.buildInvocationTree(methodWrapper)
        if (rootBaseNode == null) {
            println("invocation tree is null")
            return setOf()
        }

        val visitor = FindingTestMethodVisitor()
        rootBaseNode.accept(visitor)

        val treeModelHolder = eventElement.project.getService(TreeModelHolder::class.java)
        treeModelHolder.treeModel.setRoot(rootBaseNode.toTreeNode())
        treeModelHolder.treeModel.reload()

        return visitor.getTestMethodWrappers()
    }

    private fun runTests(methodElement: PsiElement, eventElement: PsiElement, testMethodWrappers: Set<MethodWrapper>) {
        JunitTestRunner.runTestMethods(eventElement.project, testMethodWrappers, "Run all affected tests in ${methodElement.text}")

        ToolWindowUtils.activateNgmTestRunnerToolWindow(methodElement.project)
    }
}
