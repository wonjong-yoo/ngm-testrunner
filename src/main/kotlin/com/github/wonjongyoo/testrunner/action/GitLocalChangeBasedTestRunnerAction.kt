package com.github.wonjongyoo.testrunner.action

import com.github.wonjongyoo.testrunner.runner.JunitTestRunner
import com.github.wonjongyoo.testrunner.utils.TestMethodSearcher
import com.github.wonjongyoo.testrunner.utils.TextRangeBasedMethodVisitor
import com.intellij.codeInsight.actions.VcsFacade
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

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
        val targetTestMethods = changeListManager.changeLists
            .flatMap { it.changes }
            .asSequence()
            .map { change ->
                val virtualFile = change.afterRevision?.file?.virtualFile ?: return@map setOf()
                val psiFile: PsiFile = psiManager.findFile(virtualFile) ?: return@map setOf()
                val allChangedRanges = VcsFacade.getInstance().getChangedRangesInfo(psiFile)?.allChangedRanges ?: return@map setOf()

                val visitor = TextRangeBasedMethodVisitor(allChangedRanges)
                psiFile.accept(visitor)

                return@map visitor.methodWrappers
            }
            .flatten()
            .map { methodWrapper ->
                val searcher = TestMethodSearcher(project)

                val testMethods = searcher.search(methodWrapper)
                println("target : ${testMethods.map { it.getMethodName() }}")

                testMethods
            }
            .flatten()
            .toSet()

        JunitTestRunner.runTestMethods(project, targetTestMethods, "Run all tests in locally change files")
    }
}