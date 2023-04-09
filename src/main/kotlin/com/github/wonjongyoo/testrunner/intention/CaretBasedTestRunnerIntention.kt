package com.github.wonjongyoo.testrunner.intention

import com.github.wonjongyoo.testrunner.runner.JunitTestRunner
import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.github.wonjongyoo.testrunner.utils.TestMethodSearcher
import com.github.wonjongyoo.testrunner.utils.toWrapper
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtNamedFunction

class CaretBasedTestRunnerIntention: IntentionAction {
    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getText(): String = "Run all affected test on this method"

    override fun getFamilyName(): String = "Ninja Guru Magician"

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val elementAtCurrentOffset = getElementAtCurrentCaretOffset(editor, file) ?: return false

        if (file.fileType is JavaFileType) {
            return PsiTreeUtil.getParentOfType(elementAtCurrentOffset, PsiMethod::class.java) != null
        }

        if (file.fileType is KotlinFileType) {
            return PsiTreeUtil.getParentOfType(elementAtCurrentOffset, KtNamedFunction::class.java) != null
        }

        return false
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val elementAtCurrentOffset = getElementAtCurrentCaretOffset(editor, file)

        val methodWrapper: MethodWrapper = when (file.fileType) {
            is JavaFileType -> PsiTreeUtil.getParentOfType(elementAtCurrentOffset, PsiMethod::class.java)?.toWrapper() ?: return
            is KotlinFileType -> PsiTreeUtil.getParentOfType(elementAtCurrentOffset, KtNamedFunction::class.java)?.toWrapper() ?: return
            else -> return
        }

        val searcher = TestMethodSearcher(project)
        val testMethods = searcher.search(methodWrapper)

        JunitTestRunner.runTestMethods(project, testMethods, "Run all affected tests based on caret method")
    }

    private fun getElementAtCurrentCaretOffset(editor: Editor, file: PsiFile): PsiElement? {
        val currentCaretOffset = editor.caretModel.currentCaret.offset
        val nowElement = file.findElementAt(currentCaretOffset)

        return nowElement
    }
}