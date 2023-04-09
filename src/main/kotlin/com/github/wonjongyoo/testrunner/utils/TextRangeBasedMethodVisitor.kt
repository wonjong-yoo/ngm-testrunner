package com.github.wonjongyoo.testrunner.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtNamedFunction

// 파일 내의 모든 PsiMethod(Java), KtNamedFunction(Kotlin) 을 찾는다.
class TextRangeBasedMethodVisitor(private val textRanges: List<TextRange>) : PsiRecursiveElementWalkingVisitor() {
    val psiMethods: MutableList<PsiMethod> = mutableListOf()
    val ktNamedFunctions: MutableList<KtNamedFunction> = mutableListOf()
    override fun visitElement(element: PsiElement) {
        if (element is PsiMethod && textRanges.any { element.textRange.contains(it) }) {
            if (!psiMethods.contains(element)) {
                psiMethods.add(element)
            }
        }

        if (element is KtNamedFunction && textRanges.any { element.textRange.contains(it) }) {
            if (!ktNamedFunctions.contains(element)) {
                ktNamedFunctions.add(element)
            }
        }

        super.visitElement(element)
    }
}