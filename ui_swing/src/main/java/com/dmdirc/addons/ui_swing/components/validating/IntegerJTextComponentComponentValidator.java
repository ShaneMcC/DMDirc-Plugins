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

package com.dmdirc.addons.ui_swing.components.validating;

import com.dmdirc.util.validators.Validator;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * JTextComponent for integer input.
 */
public class IntegerJTextComponentComponentValidator
        extends ComponentValidator<Integer, JTextComponent> implements DocumentListener {

    /**
     * Creates a new validator.
     *
     * @param textArea  Text component to validate
     * @param validator Validator to validate against
     */
    public IntegerJTextComponentComponentValidator(final JTextComponent textArea,
            final Validator<Integer> validator) {
        super(textArea, validator);
    }

    @Override
    public Integer getValidatable() {
        try {
            return Integer.parseInt(getComponent().getText());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    @Override
    public void addHooks() {
        getComponent().getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        validate();
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        validate();
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        validate();
    }

}
