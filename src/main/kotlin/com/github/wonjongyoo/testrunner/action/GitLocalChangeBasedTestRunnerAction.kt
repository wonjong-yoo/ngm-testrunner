package com.github.wonjongyoo.testrunner.action

import com.github.wonjongyoo.testrunner.runner.JunitTestRunner
import com.github.wonjongyoo.testrunner.utils.ReferenceSearchUtils
import com.github.wonjongyoo.testrunner.utils.TextRangeBasedMethodVisitor
import com.intellij.codeInsight.actions.VcsFacade
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class GitLocalChangeBasedTestRunnerAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val changeListManager = ChangeListManager.getInstance(project)
        val psiManager = PsiManager.getInstance(project)

        // 1. 변경사항 내의 모든 PsiMethod 를 찾는다.
        // 2. PsiMethod 내의 모든 참조를 찾는다.
        //   2-1. 만약 그 참조가 테스트라면(여기서는 KtSimpleNameReference) 테스트 대상에 추가
        //   2-2 만약 그 참조가 일반 메서드라면 (2)를 반복
        // 3. (2-1) 의 결과를 묶어서 테스트 실행
        val visitedPsiMethods: MutableSet<PsiMethod> = mutableSetOf()
        val targets = changeListManager.changeLists
            .flatMap { it.changes }
            .map { change ->
                val virtualFile = change.afterRevision?.file?.virtualFile ?: return@map listOf<PsiMethod>()
                val psiFile: PsiFile = psiManager.findFile(virtualFile) ?: return@map listOf<PsiMethod>()
                val allChangedRanges = VcsFacade.getInstance().getChangedRangesInfo(psiFile)?.allChangedRanges ?: return@map listOf<PsiMethod>()

                val visitor = TextRangeBasedMethodVisitor(allChangedRanges)
                psiFile.accept(visitor)

                visitor.psiMethods
            }
            .flatten()
            .map { targetMethods ->
                val target: Map<KtClass, List<KtNamedFunction>> = findTestMethodsRecursively(project, targetMethods, visitedPsiMethods)

                println("target : $target")
                return@map target
            }
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.flatten().distinct() }

        JunitTestRunner.runTestMethod(project, targets)
    }

    private fun findTestMethodsRecursively(
        project: Project,
        psiMethod: PsiMethod,
        visitedPsiMethods: MutableSet<PsiMethod>
    ): Map<KtClass, List<KtNamedFunction>> {
        if (visitedPsiMethods.contains(psiMethod)) {
            println("already visited PsiMethod : ${psiMethod.name}")
            return mapOf()
        }

        val references = ReferenceSearchUtils.searchReferences(psiMethod, project)

        // Resovle refereces
        val testMethods: List<KtNamedFunction> = references
            .filterIsInstance<KtSimpleNameReference>()
            .mapNotNull { PsiTreeUtil.getParentOfType(it.element, KtNamedFunction::class.java) }

        if (testMethods.isEmpty()) {
            println("no test method exist in ${psiMethod.name}")
            return mapOf()
        }

        val testClass = PsiTreeUtil.getParentOfType(testMethods.first(), KtClass::class.java) ?: return mapOf()

        val referenceMethods = references
            .filterIsInstance<PsiReferenceExpression>()
            .mapNotNull { PsiTreeUtil.getParentOfType(it.element, PsiMethod::class.java) }

        visitedPsiMethods.add(psiMethod)
        return if (referenceMethods.isNotEmpty()) {
            mapOf(testClass to testMethods) + referenceMethods.flatMap { findTestMethodsRecursively(project, it, visitedPsiMethods).entries }.associate { it.key to it.value }
        } else {
            mapOf(testClass to testMethods)
        }
    }
}