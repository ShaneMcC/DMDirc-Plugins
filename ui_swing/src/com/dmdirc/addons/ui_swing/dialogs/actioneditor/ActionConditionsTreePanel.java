/*
 * Copyright (c) 2006-2014 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.ConditionTree;
import com.dmdirc.actions.ConditionTreeFactory;
import com.dmdirc.actions.ConditionTreeFactory.ConditionTreeFactoryType;
import com.dmdirc.actions.validators.ConditionRuleValidator;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions tree panel.
 */
public class ActionConditionsTreePanel extends JPanel implements ActionListener,
        PropertyChangeListener, DocumentListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Button group. */
    private ButtonGroup group;
    /** All triggers button. */
    private JRadioButton allButton;
    /** One trigger button. */
    private JRadioButton oneButton;
    /** Custom rule button. */
    private JRadioButton customButton;
    /** Custom rule field. */
    private ValidatingJTextField rule;
    /** Condition tree factory. */
    private ConditionTreeFactory treeFactory;
    /** Condition count. */
    private int conditionCount = 0;
    /** Condition rule validator. */
    private ConditionRuleValidator treeValidator;
    /** validates. */
    private boolean validates = true;

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon manager
     */
    public ActionConditionsTreePanel(final IconManager iconManager) {

        this.iconManager = iconManager;
        initComponents();
        addListeners();
        layoutComponents();

        selectTreeButton();
    }

    /** Initialises the components. */
    private void initComponents() {
        group = new ButtonGroup();
        allButton = new JRadioButton("All of the conditions are true");
        oneButton = new JRadioButton("At least one of the conditions is true");
        customButton = new JRadioButton("The conditions match a custom rule");
        treeValidator = new ConditionRuleValidator(conditionCount);

        rule = new ValidatingJTextField(iconManager, treeValidator);

        group.add(allButton);
        group.add(oneButton);
        group.add(customButton);
    }

    /** Adds the listeners. */
    private void addListeners() {
        allButton.addActionListener(this);
        oneButton.addActionListener(this);
        customButton.addActionListener(this);
        rule.addPropertyChangeListener("validationResult", this);
        rule.getDocument().addDocumentListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, pack, hidemode 3"));
        add(new TextLabel("Only execute this action if..."), "growx, pushx");
        add(allButton, "growx, pushx");
        add(oneButton, "growx, pushx");
        add(customButton, "growx, pushx");
        add(rule, "growx, pushx");
    }

    /**
     * Selects the appropriate radio button for the tree.
     */
    private void selectTreeButton() {
        group.clearSelection();
        final ConditionTreeFactoryType type;
        if (treeFactory == null) {
            type = ConditionTreeFactoryType.CONJUNCTION;
        } else {
            type = treeFactory.getType();
        }

        switch (type) {
            case CONJUNCTION:
                allButton.setSelected(true);
                rule.setText("");
                rule.setEnabled(false);
                break;
            case DISJUNCTION:
                oneButton.setSelected(true);
                rule.setText("");
                rule.setEnabled(false);
                break;
            case CUSTOM:
                customButton.setSelected(true);
                rule.setText(treeFactory.getConditionTree(conditionCount).
                        toString());
                rule.setEnabled(true);
                break;
            default:
                allButton.setSelected(true);
                rule.setText("");
                rule.setEnabled(false);
                break;
        }

        sortTreeFactory();
    }

    /** Sorts the tree factory out. */
    private void sortTreeFactory() {
        if (group.getSelection().equals(allButton.getModel())) {
            treeFactory = new ConditionTreeFactory.ConjunctionFactory();
            firePropertyChange("validationResult", validates, true);
        } else if (group.getSelection().equals(oneButton.getModel())) {
            treeFactory = new ConditionTreeFactory.DisjunctionFactory();
            firePropertyChange("validationResult", validates, true);
            validates = true;
        } else {
            treeFactory = new ConditionTreeFactory.CustomFactory(ConditionTree.parseString(rule.
                    getText()));
            rule.checkError();
        }

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        rule.setEnabled(e.getSource().equals(customButton));
        sortTreeFactory();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        allButton.setEnabled(enabled);
        oneButton.setEnabled(enabled);
        customButton.setEnabled(enabled);
    }

    /**
     * Returns the selected rule type.
     *
     * @param conditionCount Condition count
     *
     * @return Selected rule type
     */
    public ConditionTreeFactoryType getRuleType(final int conditionCount) {
        return treeFactory.getType();
    }

    /**
     * Returns the current custom rule.
     *
     * @param conditionCount number of conditions
     *
     * @return Custom rule
     */
    public ConditionTree getRule(final int conditionCount) {
        treeFactory.getConditionTree(conditionCount);
        return treeFactory.getConditionTree(conditionCount);
    }

    /**
     * Sets the tree rule.
     *
     * @param conditionCount condition count
     * @param tree           new condition tree
     */
    public void setRule(final int conditionCount, final ConditionTree tree) {
        if (tree != null) {
            this.conditionCount = conditionCount;
            treeFactory = ConditionTreeFactory.getFactory(tree, conditionCount);
            selectTreeButton();
        }
    }

    /**
     * Sets the new condition count.
     *
     * @param conditionCount new condition count.
     */
    public void setConditionCount(final int conditionCount) {
        this.conditionCount = conditionCount;
        treeValidator.setArgs(conditionCount);
        rule.checkError();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        firePropertyChange("validationResult", validates, evt.getNewValue());
        validates = (Boolean) evt.getNewValue();
    }

    /** Validates the conditions. */
    public void validateConditions() {
        selectTreeButton();
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        treeFactory = new ConditionTreeFactory.CustomFactory(ConditionTree.parseString(rule.
                getText()));
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        treeFactory = new ConditionTreeFactory.CustomFactory(ConditionTree.parseString(rule.
                getText()));
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }

}