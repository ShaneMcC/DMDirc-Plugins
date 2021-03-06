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

package com.dmdirc.addons.ui_swing.wizard;

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.ui.CoreUIUtils;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;

/**
 * Basic wizard container.
 */
public class WizardDialog extends StandardDialog implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Wizard. */
    private final WizardPanel wizard;
    /** Parent container. */
    private final Window parentWindow;

    /**
     * Creates a new instance of WizardFrame that requires a mainframe.
     *
     * @param title        Title for the wizard
     * @param steps        Steps for the wizard
     * @param parentWindow Parent component
     * @param modality     Modality
     */
    public WizardDialog(final String title, final List<Step> steps, final Window parentWindow,
            final ModalityType modality) {
        super(parentWindow, modality);

        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        orderButtons(new JButton(), new JButton());
        this.wizard = new WizardPanel(title, steps);
        this.parentWindow = parentWindow;
        layoutComponents();
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setContentPane(wizard);
    }

    /** Displays the wizard. */
    @Override
    public void display() {
        wizard.display();
        if (parentWindow == null) {
            CoreUIUtils.centreWindow(this);
        } else {
            setLocationRelativeTo(parentWindow);
        }
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(final WindowEvent e) {
                removeWindowListener(this);
                wizard.fireWizardCancelled();
            }
        });
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void validate() {
        super.validate();

        setLocationRelativeTo(parentWindow);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            wizard.nextStep();
        } else if (e.getSource() == getCancelButton()) {
            wizard.fireWizardCancelled();
        }
    }

    /**
     * Adds a step to the wizard.
     *
     * @param step Step to add
     */
    public void addStep(final Step step) {
        wizard.addStep(step);
    }

    /**
     * Returns the step at the specified index.
     *
     * @param stepNumber step number
     *
     * @return Specified step.
     */
    public Step getStep(final int stepNumber) {
        return wizard.getStep(stepNumber);
    }

    /**
     * Returns the current step.
     *
     * @return Current step number
     */
    public int getCurrentStep() {
        return wizard.getCurrentStep();
    }

    /**
     * Enables or disables the "next step" button.
     *
     * @param newValue boolean true to make "next" button enabled, else false
     */
    public void enableNextStep(final boolean newValue) {
        wizard.enableNextStep(newValue);
    }

    /**
     * Enables or disables the "previous step" button.
     *
     * @param newValue boolean true to make "previous" button enabled, else false
     */
    public void enablePreviousStep(final boolean newValue) {
        wizard.enablePreviousStep(newValue);
    }

    /**
     * Adds a step listener to the list.
     *
     * @param listener Listener to add
     */
    public void addStepListener(final StepListener listener) {
        wizard.addStepListener(listener);
    }

    /**
     * Removes a step listener from the list.
     *
     * @param listener Listener to remove
     */
    public void removeStepListener(final StepListener listener) {
        wizard.removeStepListener(listener);
    }

    /**
     * Adds a wizard listener to the list.
     *
     * @param listener Listener to add
     */
    public void addWizardListener(final WizardListener listener) {
        wizard.addWizardListener(listener);
    }

    /**
     * Removes a wizard listener from the list.
     *
     * @param listener Listener to remove
     */
    public void removeWizardListener(final WizardListener listener) {
        wizard.removeWizardListener(listener);
    }

}
