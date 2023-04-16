package com.github.wonjongyoo.testrunner.intention

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.github.wonjongyoo.testrunner.utils.MethodInvocationFinder
import com.github.wonjongyoo.testrunner.utils.MethodWrapper
import com.github.wonjongyoo.testrunner.utils.toWrapper
import com.github.wonjongyoo.testrunner.window.TreeModelHolder
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
import javax.swing.tree.DefaultMutableTreeNode

class CaretBasedTestRunnerIntention: IntentionAction {
    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getText(): String = "Run all affected test on this method"

    override fun getFamilyName(): String = "Ninja guru magician"

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

        val searcher = MethodInvocationFinder(project)
        val node = searcher.buildInvocationTree(methodWrapper)

        updateTree(project, node!!)

        node.printNodeRecursively()
    }

    private fun updateTree(project: Project, baseNode: BaseNode) {
        val treeModelHolder = project.getService(TreeModelHolder::class.java)

        treeModelHolder?.treeModel?.setRoot(makeTree(baseNode))
        treeModelHolder?.treeModel?.reload()
    }

    private fun makeTree(baseNode: BaseNode): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(baseNode)

        for (child in baseNode.children) {
            node.add(makeTree(child))
        }

        return node
    }

    private fun getElementAtCurrentCaretOffset(editor: Editor, file: PsiFile): PsiElement? {
        val currentCaretOffset = editor.caretModel.currentCaret.offset
        val nowElement = file.findElementAt(currentCaretOffset)

        return nowElement
    }
}