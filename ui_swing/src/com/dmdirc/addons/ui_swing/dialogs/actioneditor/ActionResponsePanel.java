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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.inputfields.TextAreaInputField;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.MutableComboBoxModel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Action response panel.
 */
public class ActionResponsePanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Response text area. */
    private JTextArea response;
    /** Formatter combo box. */
    private JComboBox<String> formatter;
    /** Response scrollpane. */
    private JScrollPane scrollPane;

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon manager
     * @param config      Config
     */
    public ActionResponsePanel(final IconManager iconManager,
            final ColourManagerFactory colourManagerFactory, final AggregateConfigProvider config) {
        initComponents(iconManager, colourManagerFactory, config);
        addListeners();
        layoutComponents();
    }

    private void initComponents(final IconManager iconManager,
            final ColourManagerFactory colourManagerFactory, final AggregateConfigProvider config) {
        response = new TextAreaInputField(iconManager, colourManagerFactory, config, "");
        scrollPane = new JScrollPane(response);
        response.setRows(4);
        formatter = new JComboBox<>(new DefaultComboBoxModel<String>());

        final MutableComboBoxModel<String> model =
                (MutableComboBoxModel<String>) formatter.getModel();
        model.addElement("No change");
        model.addElement("No response");

        final Collection<String> formatters = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        formatters.addAll(config.getOptions("formatter").keySet());

        for (final String format : formatters) {
            model.addElement(format);
        }
    }

    /** Adds the listeners. */
    private void addListeners() {
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Response"));
        setLayout(new MigLayout("fill, wrap 1"));

        add(new JLabel("Execute these commands: "));
        add(scrollPane, "grow, push");
        add(new JLabel("Alter the event's formatter"));
        add(formatter, "growx, pushx");
    }

    /**
     * Sets the response.
     *
     * @param response new response
     */
    public void setResponse(final String... response) {
        if (response == null) {
            this.response.setText("");
        } else {
            final StringBuilder sb = new StringBuilder();
            for (String responseLine : response) {
                responseLine = responseLine.replace("\n", "\\n");
                sb.append(responseLine).append('\n');
            }
            if (sb.length() > 0) {
                this.response.setText(sb.substring(0, sb.length() - 1));
            }
        }
        UIUtilities.resetScrollPane(scrollPane);
    }

    /**
     * Sets the new formatter for the response panel.
     *
     * @param newFormat new formatter.
     */
    public void setFormatter(final String newFormat) {
        if (newFormat == null) {
            formatter.setSelectedIndex(0);
        } else if (newFormat.isEmpty()) {
            formatter.setSelectedIndex(1);
        } else {
            formatter.setSelectedItem(newFormat);
        }
    }

    /**
     * Returns the current response.
     *
     * @return Response text
     */
    public String[] getResponse() {
        final String[] text = response.getText().split("\n");
        for (int i = 0; i < text.length; i++) {
            text[i] = text[i].replace("\\n", "\n");
        }
        return text;
    }

    /**
     * Returns the current formatter.
     *
     * @return Formatter text
     */
    public String getFormatter() {
        final String newFormat = (String) formatter.getSelectedItem();
        switch (newFormat) {
            case "No change":
                return null;
            case "No response":
                return "";
            default:
                return newFormat;
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        response.setEnabled(enabled);
        formatter.setEnabled(enabled);
    }

}
