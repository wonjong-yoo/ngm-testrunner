package com.github.wonjongyoo.testrunner.window

import com.github.wonjongyoo.testrunner.node.BaseNode
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

class CustomTreeCellRenderer : TreeCellRenderer {
    private val panel = JPanel(BorderLayout())
    private val titleLabel = JLabel()

    init {
        panel.add(titleLabel, BorderLayout.NORTH)
    }

    override fun getTreeCellRendererComponent(
        tree: JTree?,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        if (value is DefaultMutableTreeNode) {
            val baseNode = value.userObject as? BaseNode

            if (baseNode != null) {
                titleLabel.text = baseNode.name
                titleLabel.icon = baseNode.icon
            }
        }

        return panel
    }
}