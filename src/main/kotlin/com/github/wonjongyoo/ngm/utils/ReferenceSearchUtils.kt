package com.github.wonjongyoo.ngm.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.kotlin.idea.base.util.projectScope

class ReferenceSearchUtils {
    companion object {
        fun searchReferences(methodWrapper: MethodWrapper, project: Project): Collection<PsiReference> {
            val searchScope: SearchScope = project.projectScope()
            val references = ReferencesSearch.search(methodWrapper.getElement(), searchScope).findAll()
            // 만약 인터페이스 구현체이고 탐색결과가 하나도 없다면 interface 메서드로 부터 레퍼런스 탐색
            if (references.isEmpty()) {
                val interfaceMethod = methodWrapper.getInterfaceMethod() ?: return listOf()
                return ReferencesSearch.search(interfaceMethod, searchScope).findAll()
            }

            return references
        }
    }
}
