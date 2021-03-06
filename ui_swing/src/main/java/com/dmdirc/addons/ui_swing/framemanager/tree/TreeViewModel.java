/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.framemanager.tree;

import com.dmdirc.GlobalWindow;
import com.dmdirc.addons.ui_swing.WindowComparator;
import com.dmdirc.config.provider.AggregateConfigProvider;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A simple sorted tree data model based on DefaultTreeModel.
 */
public class TreeViewModel extends DefaultTreeModel {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Window comparator. */
    private final WindowComparator comparator;
    /** Configuration provider to read settings from. */
    private final AggregateConfigProvider globalConfig;

    /**
     * Creates a tree in which any node can have children.
     *
     * @param globalConfig The configuration provider to read settings from.
     * @param root         a TreeNode object that is the root of the tree.
     */
    public TreeViewModel(final AggregateConfigProvider globalConfig, final TreeNode root) {
        super(root, false);

        this.globalConfig = globalConfig;
        comparator = new WindowComparator();
    }

    /**
     * Inserts a new node into the tree and fires the appropriate events.
     *
     * @param newChild child to be added.
     * @param parent   parent child is to be added too.
     */
    public final void insertNodeInto(final TreeViewNode newChild, final MutableTreeNode parent) {
        insertNodeInto(newChild, parent, getIndex(newChild, parent));
    }

    /**
     * Compares the new child with the existing children or parent to decide where it needs to be
     * inserted.
     *
     * @param newChild new node to be inserted.
     * @param parent   node the new node will be inserted into.
     *
     * @return index where new node is to be inserted.
     */
    private int getIndex(final TreeViewNode newChild, final TreeNode parent) {
        if (newChild.getWindow().getContainer() instanceof GlobalWindow) {
            return 0;
        }

        if (parent.equals(root) && !globalConfig.getOptionBool("ui", "sortrootwindows")) {
            return parent.getChildCount();
        }

        if (globalConfig.getOptionBool("ui", "sortchildwindows")) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                final TreeViewNode child = (TreeViewNode) parent.getChildAt(i);
                if (sortBefore(newChild, child)
                        || !sortAfter(newChild, child)
                        && newChild.getUserObject().toString().compareToIgnoreCase(
                        child.getUserObject().toString()) < 0) {
                    return i;
                }
            }
        }

        return parent.getChildCount();
    }

    /**
     * Compares the types of the specified nodes' objects to see if the new node should be sorted
     * before the other.
     *
     * @param newChild The new child to be tested
     * @param child    The existing child that it's being tested against
     *
     * @return True iff newChild should be sorted before child
     */
    private boolean sortBefore(final TreeViewNode newChild, final TreeViewNode child) {
        return comparator.compare(newChild.getWindow(), child.getWindow()) <= -1;
    }

    /**
     * Compares the types of the specified nodes' objects to see if the new node should be sorted
     * after the other.
     *
     * @param newChild The new child to be tested
     * @param child    The existing child that it's being tested against
     *
     * @return True iff newChild should be sorted before child
     */
    private boolean sortAfter(final TreeViewNode newChild, final TreeViewNode child) {
        return comparator.compare(newChild.getWindow(), child.getWindow()) >= 1;
    }

    /**
     * Returns the root node for this model.
     *
     * @return Root node
     */
    public TreeViewNode getRootNode() {
        return (TreeViewNode) getRoot();
    }

}
