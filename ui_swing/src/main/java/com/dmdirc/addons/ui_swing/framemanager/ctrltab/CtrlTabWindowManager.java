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

package com.dmdirc.addons.ui_swing.framemanager.ctrltab;

import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.NextFrameAction;
import com.dmdirc.addons.ui_swing.actions.PreviousFrameAction;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingActiveWindowChangeRequestEvent;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewModel;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewNode;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.util.LogUtils;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Window manager to handle ctrl[+shift]+tab switching between windows.
 */
@Singleton
public class CtrlTabWindowManager {

    private static final Logger LOG = LoggerFactory.getLogger(CtrlTabWindowManager.class);
    /** Node storage, used for adding and deleting nodes correctly. */
    private final Map<TextFrame, TreeViewNode> nodes;
    /** Data model. */
    private final TreeViewModel model;
    /** Tree Scroller. */
    private final TreeScroller treeScroller;
    /** Selection model for the tree scroller. */
    private final TreeSelectionModel selectionModel;

    @Inject
    public CtrlTabWindowManager(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final MainFrame mainFrame,
            final SwingEventBus swingEventBus) {
        nodes = new HashMap<>();
        model = new TreeViewModel(globalConfig, new TreeViewNode(null, null));
        selectionModel = new DefaultTreeSelectionModel();
        treeScroller = new TreeScroller(model, selectionModel, false) {

            @Override
            protected void setPath(final TreePath path) {
                super.setPath(path);
                swingEventBus.publishAsync(new SwingActiveWindowChangeRequestEvent(Optional
                        .ofNullable(((TreeViewNode) path.getLastPathComponent()).getWindow())));
            }
        };

        swingEventBus.subscribe(this);

        if (mainFrame.getRootPane().getActionMap() != null) {
            mainFrame.getRootPane().getActionMap()
                    .put("prevFrameAction", new PreviousFrameAction(treeScroller));
            mainFrame.getRootPane().getActionMap()
                    .put("nextFrameAction", new NextFrameAction(treeScroller));
        }
        final InputMap ancestorFocusedMap = mainFrame.getRootPane().getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (ancestorFocusedMap != null) {
            ancestorFocusedMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "prevFrameAction");
            ancestorFocusedMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_DOWN_MASK),
                    "nextFrameAction");
        }
    }

    @Handler
    public void windowAdded(final SwingWindowAddedEvent event) {
        final Optional<TextFrame> parent = event.getParentWindow();
        final TextFrame window = event.getChildWindow();
        final TreeViewNode parentNode;
        parentNode = parent.map(nodes::get).orElseGet(model::getRootNode);

        UIUtilities.invokeAndWait(() -> {
            final TreeViewNode node = new TreeViewNode(null, window);
            synchronized (nodes) {
                nodes.put(window, node);
            }
            node.setUserObject(window);
            model.insertNodeInto(node, parentNode);
        });
    }

    @Handler
    public void windowDeleted(final SwingWindowDeletedEvent event) {
        final TextFrame window = event.getChildWindow();
        UIUtilities.invokeAndWait(() -> {
            if (nodes.get(window) == null) {
                return;
            }
            final TreeViewNode node = nodes.get(window);
            if (node.getLevel() == 0) {
                LOG.warn(LogUtils.USER_ERROR, "delServer triggered for root node {}",
                        node, new IllegalArgumentException());
            } else {
                model.removeNodeFromParent(nodes.get(window));
            }
            nodes.remove(window);
        });
    }

    /** Scrolls up. */
    public void scrollUp() {
        treeScroller.changeFocus(true);
    }

    /** Scrolls down. */
    public void scrollDown() {
        treeScroller.changeFocus(false);
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void selectionChanged(final SwingWindowSelectedEvent event) {
        if (event.getWindow().isPresent()) {
            final TreeNode[] path = model.getPathToRoot(nodes.get(event.getWindow().get()));
            if (path != null && path.length > 0) {
                selectionModel.setSelectionPath(new TreePath(path));
            }
        } else {
            selectionModel.setSelectionPath(null);
        }
    }

}
