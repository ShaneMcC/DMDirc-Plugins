/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.harness.ui;

import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.util.validators.Validator;
import javax.swing.JTextField;
import org.fest.swing.core.GenericTypeMatcher;

public class ValidatingJTextFieldFinder extends GenericTypeMatcher<JTextField> {

    private final Class<? extends Validator> validator;

    public ValidatingJTextFieldFinder(final Class<? extends Validator> validator) {
        super(JTextField.class);
        this.validator = validator;
    }
    
    @Override
    protected boolean isMatching(JTextField arg0) {
        return arg0.getParent() instanceof ValidatingJTextField &&
                validator.isAssignableFrom(((ValidatingJTextField) arg0.getParent()).getValidator()
                .getClass());
    }

}
