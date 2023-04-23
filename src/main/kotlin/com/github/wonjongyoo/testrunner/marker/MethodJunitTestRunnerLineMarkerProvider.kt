package com.github.wonjongyoo.testrunner.marker

import com.github.wonjongyoo.testrunner.node.visitor.FindingTestMethodVisitor
import com.github.wonjongyoo.testrunner.runner.JunitTestRunner
import com.github.wonjongyoo.testrunner.utils.MethodInvocationFinder
import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.github.wonjongyoo.testrunner.utils.ToolWindowUtils
import com.github.wonjongyoo.testrunner.utils.toWrapper
import com.github.wonjongyoo.testrunner.window.TreeModelHolder
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons.RunConfigurations.TestState
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod

class MethodJunitTestRunnerLineMarkerProvider: RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
        forNavigation: Boolean
    ) {
        for (element in elements) {
            if (element is PsiIdentifier && element.parent is PsiMethod) {

                val navigationHandler = GutterIconNavigationHandler<PsiElement> { event, elt ->
                    val elementAtCurrentOffset = element.parent as PsiMethod

                    val methodWrapper: MethodWrapper = elementAtCurrentOffset.toWrapper()

                    val finder = MethodInvocationFinder(elt.project)
                    val rootBaseNode = finder.buildInvocationTree(methodWrapper)
                    if (rootBaseNode == null) {
                        println("invocation tree is null")
                        return@GutterIconNavigationHandler
                    }

                    val visitor = FindingTestMethodVisitor()
                    rootBaseNode.accept(visitor)

                    val treeModelHolder = elt.project.getService(TreeModelHolder::class.java)
                    treeModelHolder.treeModel.setRoot(rootBaseNode.toTreeNode())
                    treeModelHolder.treeModel.reload()

                    JunitTestRunner.runTestMethods(elt.project, visitor.getTestMethodWrappers(), "Run all affected tests in ${element.text}")

                    ToolWindowUtils.activateNgmTestRunnerToolWindow(element.project)
                }

                val marker = NavigationGutterIconBuilder.create(TestState.Run)
                    .setTooltipText("Run all affected tests by ${(element.parent as PsiMethod).containingClass?.name}#${element.text}")
                    .setTarget(element)
                    .createLineMarkerInfo(element, navigationHandler)

                result.add(marker)
            }
        }
    }
}