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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.UpdateStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Update component table model.
 */
public class UpdateTableModel extends AbstractTableModel {

    /** A version number for this class. */
    private static final long serialVersionUID = 3;
    /** Update component list. */
    private final List<UpdateComponent> updates;
    /** Enabled list. */
    private final Map<UpdateComponent, Boolean> enabled;

    /**
     * Instantiates a new table model.
     *
     * @param updateManager The update manager to use to retrieve component status.
     * @param updates       Update components to show
     */
    public UpdateTableModel(
            final CachingUpdateManager updateManager,
            final Collection<UpdateComponent> updates) {

        this.updates = new ArrayList<>(updates);
        this.enabled = new HashMap<>();

        for (UpdateComponent update : this.updates) {
            enabled.put(update,
                    updateManager.getStatus(update) != UpdateStatus.CHECKING_NOT_PERMITTED);
        }
    }

    @Override
    public int getRowCount() {
        synchronized (updates) {
            return updates.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Update Component";
            case 1:
                return "Enabled?";
            case 2:
                return "Version";
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Boolean.class;
            case 2:
                return Integer.class;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        synchronized (updates) {
            if (updates.size() <= rowIndex) {
                throw new IndexOutOfBoundsException(rowIndex + " >= "
                        + updates.size());
            }
            if (rowIndex < 0) {
                throw new IllegalArgumentException(
                        "Must specify a positive integer");
            }
            switch (columnIndex) {
                case 0:
                    return updates.get(rowIndex).getFriendlyName();
                case 1:
                    return enabled.get(updates.get(rowIndex));
                case 2:
                    return updates.get(rowIndex).getFriendlyVersion();
                default:
                    throw new IllegalArgumentException("Unknown column: "
                            + columnIndex);
            }
        }
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        synchronized (updates) {
            if (updates.size() <= rowIndex) {
                throw new IndexOutOfBoundsException(rowIndex + " >= "
                        + updates.size());
            }
            if (rowIndex < 0) {
                throw new IllegalArgumentException(
                        "Must specify a positive integer");
            }
            switch (columnIndex) {
                case 1:
                    enabled.put(updates.get(rowIndex), (Boolean) aValue);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown column: "
                            + columnIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * Adds a update component to the model.
     *
     * @param component update component to add
     */
    public void add(final UpdateComponent component) {
        synchronized (updates) {
            updates.add(component);
            fireTableRowsInserted(updates.size() - 1, updates.size() - 1);
        }
    }

    /**
     * Removes a update component to the model.
     *
     * @param component update component to remove
     */
    public void remove(final UpdateComponent component) {
        synchronized (updates) {
            remove(updates.indexOf(component));
        }
    }

    /**
     * Removes a update component to the model.
     *
     * @param index Index of the update component to remove
     */
    public void remove(final int index) {
        synchronized (updates) {
            if (index != -1) {
                updates.remove(index);
                fireTableRowsDeleted(index, index);
            }
        }
    }

    /**
     * Retrieves the update component that corresponds to the specified row.
     *
     * @since 0.6.3m2
     * @param row The row number
     *
     * @return The corresponding update component
     */
    public UpdateComponent getComponent(final int row) {
        return updates.get(row);
    }

}
