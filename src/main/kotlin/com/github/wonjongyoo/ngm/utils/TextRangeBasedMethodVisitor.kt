package com.github.wonjongyoo.ngm.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtNamedFunction

// 파일 내의 모든 PsiMethod(Java), KtNamedFunction(Kotlin) 을 찾는다.
class TextRangeBasedMethodVisitor(private val textRanges: List<TextRange>) : PsiRecursiveElementWalkingVisitor() {
    val methodWrappers: MutableSet<MethodWrapper> = mutableSetOf()
    override fun visitElement(element: PsiElement) {
        if (element is PsiMethod && textRanges.any { element.textRange.contains(it) }) {
            val wrapper = element.toWrapper()
            if (!methodWrappers.contains(wrapper)) {
                methodWrappers.add(wrapper)
            }
        }

        if (element is KtNamedFunction && textRanges.any { element.textRange.contains(it) }) {
            val wrapper = element.toWrapper()
            if (!methodWrappers.contains(wrapper)) {
                methodWrappers.add(wrapper)
            }
        }

        super.visitElement(element)
    }
}
