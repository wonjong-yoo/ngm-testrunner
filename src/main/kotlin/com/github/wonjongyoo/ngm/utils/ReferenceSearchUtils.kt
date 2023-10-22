package com.github.wonjongyoo.ngm.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.kotlin.idea.base.util.projectScope

class ReferenceSearchUtils {
    companion object {
        fun searchReferences(element: PsiElement, project: Project): Collection<PsiReference> {
            val searchScope: SearchScope = project.projectScope()
            return ReferencesSearch.search(element, searchScope).findAll()
        }
    }
}
