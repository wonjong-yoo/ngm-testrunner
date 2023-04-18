package com.github.wonjongyoo.testrunner.window

import com.github.wonjongyoo.testrunner.node.BaseNode
import com.intellij.icons.AllIcons
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class CustomTreeCellRenderer : NodeRenderer() {

    init {
        isOpaque = false
        isIconOpaque = false
        isTransparentIconBackground = true
    }

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        if (value is DefaultMutableTreeNode) {
            val userObject = value.userObject
            if (userObject != null) {
                when (userObject) {
                    is BaseNode -> {
                        append(userObject.name ?: "NULL", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, null, JBColor.YELLOW))
                        this.icon = userObject.icon
                    }
                    is String -> {
                        append("ROOT", SimpleTextAttributes.ERROR_ATTRIBUTES)
                        this.icon = AllIcons.Nodes.Folder
                    }
                }

            }

            // val userObject = value.userObject
            // when (userObject) {
            //     is BaseNode -> {
            //         titleLabel.text = userObject.name
            //         titleLabel.icon = userObject.icon
            //     }
            //     is String -> {
            //         titleLabel.text = "Root"
            //         titleLabel.icon = AllIcons.Nodes.Folder
            //     }
            // }
        }

        // return panel
    }


}