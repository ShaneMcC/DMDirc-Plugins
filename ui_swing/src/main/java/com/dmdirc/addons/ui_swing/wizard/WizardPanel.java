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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.EtchedLineBorder;
import com.dmdirc.addons.ui_swing.components.EtchedLineBorder.BorderSide;
import com.dmdirc.addons.ui_swing.components.TitlePanel;
import com.dmdirc.addons.ui_swing.components.XScrollablePanel;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

/**
 * Wizard panel.
 */
public class WizardPanel extends JPanel implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Wizard title. */
    private final String title;
    /** Step Listeners. */
    private final ListenerList stepListeners;
    /** List of steps. */
    private final List<Step> steps;
    /** Step scroll pane. */
    private JScrollPane sp;
    /** Step panel. */
    private JPanel stepsPanel;
    /** Title panel. */
    private TitlePanel titleLabel;
    /** Current step. */
    private int currentStep;
    /** Previous step button. */
    private JButton prev;
    /** Next step button. */
    private JButton next;
    /** Progress label. */
    private JLabel progressLabel;

    /**
     * Creates a new instance of WizardPanel.
     *
     * @param title Title for the wizard
     * @param steps Steps for the wizard
     */
    public WizardPanel(final String title, final List<Step> steps) {

        stepListeners = new ListenerList();
        this.steps = new ArrayList<>(steps);

        this.title = title;

        initComponents();
        layoutComponents();

        steps.forEach(this::addStep);
    }

    /** Initialises the components. */
    private void initComponents() {
        titleLabel = new TitlePanel(new EtchedLineBorder(EtchedBorder.LOWERED,
                BorderSide.BOTTOM), title);
        stepsPanel = new XScrollablePanel(new MigLayout("fillx"));
        sp = new JScrollPane(stepsPanel);

        progressLabel = new JLabel();

        next = new JButton();

        prev = new JButton("\u00AB Previous");
        next.setText("Next \u00BB");

        next.addActionListener(this);
        prev.addActionListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        final JPanel progressPanel = new JPanel(new MigLayout("fill"));
        progressPanel.add(progressLabel, "growx, pushx");
        progressPanel.add(prev, "sg button");
        progressPanel.add(next, "sg button");
        progressPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                Color.BLACK));
        progressPanel.setBorder(new EtchedLineBorder(EtchedBorder.LOWERED,
                BorderSide.TOP));

        setLayout(new MigLayout("fill, wrap 1, ins 0"));
        add(titleLabel, "growx, pushx");
        add(sp, "grow, push");
        add(progressPanel, "growx, pushx");
    }

    /** Displays the wizard. */
    public void display() {
        if (!steps.isEmpty()) {
            stepsPanel.removeAll();
            stepsPanel.add(steps.get(0));
            currentStep = 0;
            titleLabel.setText(steps.get(currentStep).getTitle());
            UIUtilities.resetScrollPane(sp);

            prev.setEnabled(false);
            if (steps.size() == 1) {
                next.setText("Finish");
            }

            updateProgressLabel();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == next) {
            nextStep();
        } else if (e.getSource() == prev) {
            prevStep();
        }
    }

    /**
     * Adds a step to the wizard.
     *
     * @param step Step to add
     */
    public void addStep(final Step step) {
        steps.add(step);
    }

    /**
     * Enables or disables the "next step" button.
     *
     * @param newValue boolean true to make "next" button enabled, else false
     */
    public void enableNextStep(final boolean newValue) {
        next.setEnabled(newValue);
    }

    /**
     * Enables or disables the "previous step" button.
     *
     * @param newValue boolean true to make "previous" button enabled, else false
     */
    public void enablePreviousStep(final boolean newValue) {
        prev.setEnabled(newValue);
    }

    /** Moves to the next step. */
    protected void nextStep() {
        switch (next.getText()) {
            case "Next \u00BB":
                prev.setEnabled(true);
                fireStepAboutToBeDisplayed(steps.get(currentStep + 1));
                stepsPanel.setVisible(false);
                stepsPanel.removeAll();
                stepsPanel.add(steps.get(currentStep + 1));
                stepsPanel.setVisible(true);
                fireStepHidden(steps.get(currentStep));
                currentStep++;
                if (currentStep == steps.size() - 1) {
                    next.setText("Finish");
                }
                titleLabel.setText(steps.get(currentStep).getTitle());
                updateProgressLabel();
                break;
            case "Finish":
                fireWizardFinished();
                break;
        }
    }

    /** Moves to the previous step. */
    protected void prevStep() {
        fireStepAboutToBeDisplayed(steps.get(currentStep - 1));
        stepsPanel.setVisible(false);
        stepsPanel.removeAll();
        stepsPanel.add(steps.get(currentStep - 1));
        stepsPanel.setVisible(true);
        fireStepHidden(steps.get(currentStep));
        currentStep--;
        if (currentStep == 0) {
            prev.setEnabled(false);
        }
        next.setText("Next \u00BB");
        titleLabel.setText(steps.get(currentStep).getTitle());
        updateProgressLabel();
    }

    /**
     * Returns the step at the specified index.
     *
     * @param stepNumber step number
     *
     * @return Specified step.
     */
    public Step getStep(final int stepNumber) {
        return steps.get(stepNumber);
    }

    /**
     * Returns the current step.
     *
     * @return Current step number
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /** Updates the progress label. */
    private void updateProgressLabel() {
        progressLabel.setText("Step " + (currentStep + 1) + " of "
                + steps.size());
    }

    /**
     * Adds a step listener to the list.
     *
     * @param listener Listener to add
     */
    public void addStepListener(final StepListener listener) {
        stepListeners.add(StepListener.class, listener);
    }

    /**
     * Removes a step listener from the list.
     *
     * @param listener Listener to remove
     */
    public void removeStepListener(final StepListener listener) {
        stepListeners.remove(StepListener.class, listener);
    }

    /**
     * Adds a wizard listener to the list.
     *
     * @param listener Listener to add
     */
    public void addWizardListener(final WizardListener listener) {
        stepListeners.add(WizardListener.class, listener);
    }

    /**
     * Removes a wizard listener from the list.
     *
     * @param listener Listener to remove
     */
    public void removeWizardListener(final WizardListener listener) {
        stepListeners.remove(WizardListener.class, listener);
    }

    /**
     * Fires step about to be displayed events.
     *
     * @param step Step to be displayed
     */
    private void fireStepAboutToBeDisplayed(final Step step) {
        for (StepListener listener : stepListeners.get(StepListener.class)) {
            listener.stepAboutToDisplay(step);
        }
    }

    /**
     * Fires step hidden events.
     *
     * @param step step that's been hidden
     */
    private void fireStepHidden(final Step step) {
        for (StepListener listener : stepListeners.get(StepListener.class)) {
            listener.stepHidden(step);
        }
    }

    /**
     * Fires wizard finished events.
     */
    private void fireWizardFinished() {
        stepListeners.get(WizardListener.class)
                .forEach(WizardListener::wizardFinished);
    }

    /**
     * Fires wizard cancelled events.
     */
    protected void fireWizardCancelled() {
        stepListeners.get(WizardListener.class)
                .forEach(WizardListener::wizardCancelled);
    }

}
