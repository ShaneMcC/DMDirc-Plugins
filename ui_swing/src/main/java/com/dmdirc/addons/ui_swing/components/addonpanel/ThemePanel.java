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

package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.addonbrowser.DataLoaderWorkerFactory;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.UserConfig;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.ui.themes.Theme;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.manager.CachingUpdateManager;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Lists known themes, enabling the end user to enable/disable these as well as download new ones.
 */
public class ThemePanel extends AddonPanel {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Manager to retrieve themes from. */
    private final ThemeManager themeManager;
    /** Manager to use to retrieve addon-related icons. */
    private final IconManager iconManager;
    /** Manager to use to retrieve update information. */
    private final CachingUpdateManager updateManager;
    /** Configuration to write update-related settings to. */
    private final ConfigProvider userConfig;

    /**
     * Creates a new instance of ThemePanel.
     *
     * @param parentWindow  Parent window
     * @param themeManager  Manager to retrieve themes from.
     * @param workerFactory Factory to use to create data workers.
     * @param iconManager   Manager to use to retrieve addon-related icons.
     * @param updateManager Manager to use to retrieve update information.
     * @param userConfig    Configuration to write update-related settings to.
     * @param eventBus      The event bus to post errors to.
     */
    @Inject
    public ThemePanel(
            @MainWindow final Window parentWindow,
            final ThemeManager themeManager,
            final DataLoaderWorkerFactory workerFactory,
            final IconManager iconManager,
            final CachingUpdateManager updateManager,
            @UserConfig final ConfigProvider userConfig,
            final EventBus eventBus) {
        super(parentWindow, workerFactory, eventBus);
        this.themeManager = themeManager;
        this.iconManager = iconManager;
        this.updateManager = updateManager;
        this.userConfig = userConfig;
        load();
    }

    @Override
    protected JTable populateList(final JTable table) {
        final List<Theme> list = new ArrayList<>(themeManager.getAllThemes().values());
        Collections.sort(list);

        UIUtilities.invokeLater(() -> {
            ((DefaultTableModel) addonList.getModel()).setRowCount(0);
            for (final Theme theme : list) {
                ((DefaultTableModel) addonList.getModel()).addRow(
                        new AddonCell[]{
                            new AddonCell(
                                    new AddonToggle(
                                            updateManager,
                                            userConfig,
                                            themeManager,
                                            theme),
                                    iconManager),});
            }

            addonList.repaint();
        });
        return addonList;
    }

    @Override
    protected String getTypeName() {
        return "themes";
    }

}
