package com.github.wonjongyoo.ngm.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.BindingContext

interface MethodWrapper {
    fun isJunitTestMethod(): Boolean

    fun getElement(): PsiElement

    fun getContainingClassFqName(): String

    fun getContainingClassName(): String

    fun getMethodName(): String

    fun getArgumentTypes(): List<String>

    fun getInterfaceMethod(): PsiElement?
}

class KtNamedFunctionWrapper(val ktNamedFunction: KtNamedFunction) : MethodWrapper {
    var isJunitTestMethodCached: Boolean? = null

    override fun getElement(): KtNamedFunction {
        return ktNamedFunction
    }

    override fun isJunitTestMethod(): Boolean {
        if (isJunitTestMethodCached == null) {
            val annotationEntries = ktNamedFunction.annotationEntries

            isJunitTestMethodCached = annotationEntries.any {
                val bindingContext = it.analyze()
                val fqName = bindingContext.get(BindingContext.ANNOTATION, it)?.fqName?.asString() ?: return@any false

                fqName.contains("Test")
            }

            return isJunitTestMethodCached!!
        }

        return isJunitTestMethodCached!!
    }

    override fun getContainingClassFqName(): String {
        // 확장함수
        val receiverTypeReference = this.ktNamedFunction.receiverTypeReference
        if (receiverTypeReference != null) {
            return receiverTypeReference.kotlinFqName.toString()
        }

        val ktClass = this.ktNamedFunction.getParentOfType<KtClass>(true) ?: throw RuntimeException("There is no containing class")
        return ktClass.fqName.toString()
    }

    override fun getContainingClassName(): String {
        // 확장함수
        val receiverTypeReference = this.ktNamedFunction.receiverTypeReference
        if (receiverTypeReference != null) {
            return receiverTypeReference.text ?: "NO CLASS"
        }

        val ktClass = this.ktNamedFunction.getParentOfType<KtClass>(true) ?: throw RuntimeException("There is no containing class")
        return ktClass.name ?: "NO CLASS"
    }

    override fun getMethodName(): String {
        return ktNamedFunction.name ?: return ""
    }

    override fun getArgumentTypes(): List<String> {
        if (ktNamedFunction.valueParameters.isEmpty()) {
            return listOf()
        }

        return ktNamedFunction.valueParameters.map {
            (it.resolveToDescriptorIfAny() as? CallableDescriptor)?.returnType?.toString() ?: "NULL"
        }
    }

    override fun getInterfaceMethod(): PsiElement? {
        val ktClass = PsiTreeUtil.getParentOfType(this.ktNamedFunction, KtClass::class.java) ?: return null
        val superTypes = ktClass.superTypeListEntries
        return superTypes
            .mapNotNull {
                PsiTreeUtil.findChildOfType(it, KtNameReferenceExpression::class.java)
            }
            .mapNotNull {
                it.mainReference.resolve()
            }
            .flatMap {
                PsiTreeUtil.findChildrenOfType(it, KtNamedFunction::class.java)
            }
            .firstOrNull {
                it.name == ktNamedFunction.name
                    && it.valueParameters.size == ktNamedFunction.valueParameters.size
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtNamedFunctionWrapper

        if (ktNamedFunction != other.ktNamedFunction) return false

        return true
    }

    override fun hashCode(): Int {
        return ktNamedFunction.hashCode()
    }
}

fun KtNamedFunction.toWrapper() = KtNamedFunctionWrapper(this)
class PsiMethodWrapper(val psiMethod: PsiMethod) : MethodWrapper {
    private var isJunitTestMethodCached: Boolean? = null

    override fun getElement(): PsiMethod {
        return psiMethod
    }

    override fun isJunitTestMethod(): Boolean {
        if (isJunitTestMethodCached == null) {
            isJunitTestMethodCached = psiMethod.annotations.any {
                it.qualifiedName?.contains("Test") ?: false
            }

            return isJunitTestMethodCached!!
        }

        return isJunitTestMethodCached!!
    }

    override fun getContainingClassFqName(): String {
        val psiClass = psiMethod.containingClass ?: throw RuntimeException("There is no containing class")
        return psiClass.qualifiedName ?: return ""
    }

    override fun getContainingClassName(): String {
        val psiClass = psiMethod.containingClass ?: throw RuntimeException("There is no containing class")
        return psiClass.name ?: return ""
    }

    override fun getMethodName(): String {
        return psiMethod.name
    }

    override fun getArgumentTypes(): List<String> {
        if (psiMethod.parameters.isEmpty()) {
            return listOf()
        }

        return psiMethod.parameters.map {
            (it as PsiParameterImpl).typeElement?.text ?: "NULL"
        }
    }

    override fun getInterfaceMethod(): PsiElement? {
        val parent = psiMethod.parent
        if (parent !is PsiClass || parent.implementsList?.referenceElements?.isEmpty() == true) {
            return null
        }

        val interfaceReferences = parent.implementsList!!.referenceElements

        return interfaceReferences.mapNotNull { it.resolve() }
            .filterIsInstance<PsiClass>()
            .flatMap { it.allMethods.toList() }
            .firstOrNull { it.name == psiMethod.name && it.typeParameters.size == psiMethod.typeParameters.size } ?: return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PsiMethodWrapper

        return psiMethod == other.psiMethod
    }

    override fun hashCode(): Int {
        return psiMethod.hashCode()
    }
}

fun PsiMethod.toWrapper() = PsiMethodWrapper(this)

fun PsiReference.searchPsiFunctionElement(): MethodWrapper? {
    if (this is KtSimpleNameReference) {
        return PsiTreeUtil.getParentOfType(this.element, KtNamedFunction::class.java)?.toWrapper()
    }

    if (this is PsiJavaCodeReferenceElement) {
        return this.getParentOfType<PsiMethod>(true)?.toWrapper()
    }

    return null
}
