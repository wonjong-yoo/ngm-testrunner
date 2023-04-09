package com.github.wonjongyoo.testrunner.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch

class ReferenceSearchUtils {
    companion object {
        fun searchReferences(element: PsiElement, project: Project): Collection<PsiReference> {
            val searchScope: GlobalSearchScope = GlobalSearchScope.projectScope(project)
            return ReferencesSearch.search(element, searchScope).findAll()
        }
    }
}