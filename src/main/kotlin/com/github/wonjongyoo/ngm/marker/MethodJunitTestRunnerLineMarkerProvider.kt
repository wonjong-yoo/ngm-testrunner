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

    private fun createIconNavigationHandler(element: PsiElement): GutterIconNavigationHandler<PsiElement> = GutterIconNavigationHandler<PsiElement> { _, elt ->
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

}