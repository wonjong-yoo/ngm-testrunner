package com.github.wonjongyoo.ngm.model

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.ui.treeStructure.SimpleTree

class NgmTestRunnerDataKeys {
    companion object {
        val TEST_METHOD_TREE_MODEL = DataKey.create<SimpleTree>("testmethod.treemodel")
    }
}
