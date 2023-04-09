package com.github.wonjongyoo.testrunner.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.BindingContext

interface MethodWrapper {
    fun isJunitTestMethod(): Boolean

    fun getElement(): PsiElement

    fun getContainingClassFqName(): String

    fun getMethodName(): String
}

class KtNamedFunctionWrapper(val ktNamedFunction: KtNamedFunction) : MethodWrapper {
    override fun getElement(): KtNamedFunction {
        return ktNamedFunction
    }

    override fun isJunitTestMethod(): Boolean {
        val annotationEntries = ktNamedFunction.annotationEntries

        return annotationEntries.any {
            val bindingContext = it.analyze()
            val fqName = bindingContext.get(BindingContext.ANNOTATION, it)?.fqName?.asString() ?: return@any false

            fqName.contains("junit") && fqName.contains("Test")
        }
    }

    override fun getContainingClassFqName(): String {
        val ktClass = this.ktNamedFunction.getParentOfType<KtClass>(true) ?: throw RuntimeException("There is no containing class")
        return ktClass.fqName.toString()
    }

    override fun getMethodName(): String {
        return ktNamedFunction.name ?: return ""
    }
}

fun KtNamedFunction.toWrapper() = KtNamedFunctionWrapper(this)
class PsiMethodWrapper(val psiMethod: PsiMethod) : MethodWrapper {
    override fun getElement(): PsiMethod {
        return psiMethod
    }

    override fun isJunitTestMethod(): Boolean {
        return false
    }

    override fun getContainingClassFqName(): String {
        val psiClass = psiMethod.containingClass ?: throw RuntimeException("There is no containing class")
        return psiClass.qualifiedName ?: return ""
    }

    override fun getMethodName(): String {
        return psiMethod.name
    }
}

fun PsiMethod.toWrapper() = PsiMethodWrapper(this)

fun PsiReference.searchPsiFunctionElement(): MethodWrapper? {
    if (this is KtSimpleNameReference) {
        return PsiTreeUtil.getParentOfType(this.element, KtNamedFunction::class.java)?.toWrapper()
    }

    if (this is PsiReferenceExpression) {
        return this.getParentOfType<PsiMethod>(true)?.toWrapper()
    }

    return null
}