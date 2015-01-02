/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs.error;

import com.dmdirc.addons.ui_swing.components.PackingTable;
import com.dmdirc.ui.IconManager;

import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

/**
 * Table listing ProgramErrors in the client.
 */
public class ErrorTable extends PackingTable {

    /** Serial version UID. */
    private static final long serialVersionUID = 3994014806819705247L;
    /** Icon manager. */
    private final IconManager iconManager;

    /**
     * Table listing ProgramErrors in the client.
     *
     * @param iconManager Icon manager
     * @param tableModel  Table model
     * @param scrollPane  Parent scrollpane
     */
    public ErrorTable(final IconManager iconManager,
            final ErrorTableModel tableModel, final JScrollPane scrollPane) {
        super(tableModel, scrollPane, true);
        this.iconManager = iconManager;

        setAutoCreateRowSorter(true);
        setAutoCreateColumnsFromModel(true);
        setColumnSelectionAllowed(false);
        setCellSelectionEnabled(false);
        setDragEnabled(false);
        setFillsViewportHeight(false);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getRowSorter().toggleSortOrder(0);
        getTableHeader().setReorderingAllowed(false);
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row,
            final int column) {
        switch (column) {
            case 1:
                return new ErrorLevelIconCellRenderer(iconManager);
            default:
                return super.getCellRenderer(row, column);
        }
    }

}
