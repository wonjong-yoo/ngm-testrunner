package com.github.wonjongyoo.ngm.window

import com.github.wonjongyoo.ngm.node.BaseNodeDescriptor
import com.intellij.ui.treeStructure.SimpleTree
import java.awt.Color
import javax.swing.tree.TreeModel

class MySimpleTree(treeModel: TreeModel) : SimpleTree(treeModel) {
    override fun getFileColorFor(node: Any?): Color? {
        if (node is BaseNodeDescriptor) {
            return node.backgroundColorCached
        }

        return null
    }

    override fun isFileColorsEnabled(): Boolean {
        return true
    }
}
