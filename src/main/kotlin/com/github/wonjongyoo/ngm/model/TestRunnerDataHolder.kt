package com.github.wonjongyoo.ngm.model

import com.github.wonjongyoo.ngm.utils.MethodWrapper
import com.intellij.openapi.components.Service
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

@Service(Service.Level.PROJECT)
class TestRunnerDataHolder {
     var treeModel: DefaultTreeModel = DefaultTreeModel(DefaultMutableTreeNode())
     var testMethods: Set<MethodWrapper> = setOf()
}
