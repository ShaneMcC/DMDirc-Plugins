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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.AboutDialogModel;
import com.dmdirc.ui.core.about.Licence;
import com.dmdirc.ui.core.about.LicensedComponent;

import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Licences panel.
 */
public class LicencesPanel extends JPanel implements TreeSelectionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    private final AboutDialogModel model;
    /** Config manager. */
    private final AggregateConfigProvider config;
    /** Licence scroll pane. */
    private JScrollPane scrollPane;
    /** Licence list model */
    private DefaultTreeModel listModel;
    /** Licence textpane. */
    private JEditorPane licence;
    /** Licence list. */
    private JTree list;

    public LicencesPanel(final AboutDialogModel model,
            final AggregateConfigProvider globalConfig) {
        this.model = model;
        config = globalConfig;
        initComponents();
        addListeners();
        initLicenses();
        layoutComponents();
    }

    /**
     * Adds the listeners to the components.
     */
    private void addListeners() {
        list.addTreeSelectionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("ins rel, fill"));
        add(new JScrollPane(list), "growy, pushy, w 150!");
        add(scrollPane, "grow, push");
    }

    /** Initialises the components. */
    private void initComponents() {
        setOpaque(UIUtilities.getTabbedPaneOpaque());
        listModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        list = new JTree(listModel) {
            /**
             * A version number for this class.
             */
            private static final long serialVersionUID = 1;

            @Override
            public void scrollRectToVisible(final Rectangle aRect) {
                final Rectangle rect = new Rectangle(0, aRect.y, aRect.width,
                        aRect.height);
                super.scrollRectToVisible(rect);
            }
        };
        list.setBorder(BorderFactory.createEmptyBorder(
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue()));
        list.setCellRenderer(new LicenceRenderer());
        list.setRootVisible(false);
        list.setOpaque(false);
        list.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        new TreeScroller(list);
        licence = new JEditorPane();
        licence.setEditorKit(new HTMLEditorKit());
        final Font font = UIManager.getFont("Label.font");
        ((HTMLDocument) licence.getDocument()).getStyleSheet().addRule("body " + "{ font-family: "
                + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }");
        licence.setEditable(false);
        scrollPane = new JScrollPane(licence);
    }

    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        if (list.getSelectionCount() == 0) {
            list.setSelectionPath(e.getOldLeadSelectionPath());
        }
        list.scrollPathToVisible(e.getPath());
        final Object userObject = ((DefaultMutableTreeNode) e.getPath().
                getLastPathComponent()).getUserObject();
        if (userObject instanceof Licence) {
            licence.setText(
                    "<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>"
                            + ((Licence) userObject).getName() + "</h3>"
                            + ((Licence) userObject).getBody().replaceAll("\\n", "<br>"));
        } else if (userObject instanceof LicensedComponent) {
            final LicensedComponent lc = (LicensedComponent) userObject;
            licence.setText("<b>Name:</b> "
                    + lc.getName() + "<br>");
        } else {
            licence.setText("<b>Name:</b> DMDirc<br>"
                    + "<b>Version:</b> " + config
                    .getOption("version", "version") + "<br>"
                    + "<b>Description:</b> The intelligent IRC client");
        }
        UIUtilities.resetScrollPane(scrollPane);
    }

    private void initLicenses() {
        model.getLicensedComponents().forEach(this::addLicensedComponent);
        listModel.nodeStructureChanged((TreeNode) listModel.getRoot());
        for (int i = 0; i < list.getRowCount(); i++) {
            list.expandRow(i);
        }
        list.setSelectionRow(0);
    }



    private void addLicensedComponent(final LicensedComponent component) {
        final MutableTreeNode componentNode = new DefaultMutableTreeNode(component);
        listModel.insertNodeInto(componentNode, (MutableTreeNode) listModel.getRoot(),
                listModel.getChildCount(listModel.getRoot()));
        component.getLicences().forEach(l -> listModel.insertNodeInto(
                new DefaultMutableTreeNode(l), componentNode,
                listModel.getChildCount(componentNode)));
    }

}
