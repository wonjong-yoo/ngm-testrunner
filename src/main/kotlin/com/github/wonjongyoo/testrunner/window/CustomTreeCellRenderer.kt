package com.github.wonjongyoo.testrunner.window

import com.github.wonjongyoo.testrunner.node.BaseNodeDescriptor
import com.github.wonjongyoo.testrunner.node.ClassMethodNodeDescriptor
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
                    is ClassMethodNodeDescriptor -> {
                        append(userObject.name, SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, null, JBColor.YELLOW))
                        this.icon = userObject.icon
                        if (userObject.allTestCount != null) {
                            if (userObject.allTestCount != 0) {
                                append(" (${userObject.passedTestCount} / ${userObject.allTestCount})", SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null))
                            } else {
                                append(" (no test)", SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null))
                            }
                        }
                    }
                    is BaseNodeDescriptor -> {
                        append(userObject.name, SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, null, JBColor.YELLOW))
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