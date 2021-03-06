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

package com.dmdirc.addons.ui_swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Handles redo's on text components.
 */
public final class RedoAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(RedoAction.class);
    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Undo manager. */
    private final UndoManager undoManager;

    /**
     * Creates a new instance of RedoAction.
     *
     * @param undoManager UndoManager to use for this redo action
     */
    public RedoAction(final UndoManager undoManager) {
        super("Undo");

        this.undoManager = undoManager;
    }

    @Override
    public void actionPerformed(final ActionEvent evt) {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotUndoException ex) {
            LOG.info(USER_ERROR, "Unable to redo", ex);
        }
    }

}
