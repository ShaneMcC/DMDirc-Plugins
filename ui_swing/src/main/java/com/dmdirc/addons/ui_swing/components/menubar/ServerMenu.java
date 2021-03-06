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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.dialogs.newserver.NewServerDialog;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.util.system.LifecycleController;
import com.dmdirc.interfaces.WindowModel;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * A menu providing server related commands to the menu bar.
 */
@Singleton
public class ServerMenu extends JMenu implements MenuListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Lifecycle controller. */
    private final LifecycleController lifecycleController;
    /** Provider to use to retrieve NSD instances. */
    private final DialogProvider<NewServerDialog> newServerProvider;
    /** Provider for server settings dialogs. */
    private final KeyedDialogProvider<Connection, ServerSettingsDialog> ssdProvider;
    /** Menu items which can be enabled/disabled. */
    private JMenuItem ssd;
    private JMenuItem disconnect;

    /**
     * Creates a new Server menu.
     *
     * @param activeFrameManager  Active frame manager.
     * @param lifecycleController Lifecycle controller
     * @param newServerProvider   Provider to use to retrieve NSD instances.
     * @param ssdProvider         Provider to get SSD instances
     */
    @Inject
    public ServerMenu(
            final ActiveFrameManager activeFrameManager,
            final LifecycleController lifecycleController,
            final DialogProvider<NewServerDialog> newServerProvider,
            final KeyedDialogProvider<Connection, ServerSettingsDialog> ssdProvider) {
        super("Server");
        this.activeFrameManager = activeFrameManager;
        this.lifecycleController = lifecycleController;
        this.newServerProvider = newServerProvider;
        this.ssdProvider = ssdProvider;

        setMnemonic('s');
        addMenuListener(this);
        initServerMenu();
        menuSelected(null);
    }

    /**
     * Initialises the server menu.
     */
    private void initServerMenu() {
        add(JMenuItemBuilder.create()
                .setText("New Server...")
                .setMnemonic('n')
                .addActionMethod(newServerProvider::displayOrRequestFocus)
                .build());

        disconnect = JMenuItemBuilder.create()
                .setText("Disconnect")
                .setMnemonic('d')
                .addActionMethod(() ->
                        activeFrameManager.getActiveFrame().map(TextFrame::getContainer)
                                .flatMap(WindowModel::getConnection)
                                .ifPresent(Connection::disconnect))
                .build();
        add(disconnect);

        ssd = JMenuItemBuilder.create()
                .setMnemonic('s')
                .setText("Server settings")
                .addActionMethod(() -> activeFrameManager.getActiveFrame().ifPresent(
                        f -> f.getContainer().getConnection()
                                .ifPresent(ssdProvider::displayOrRequestFocus)))
                .build();
        add(ssd);

        if (!Apple.isAppleUI()) {
            add(JMenuItemBuilder.create()
                    .setText("Exit")
                    .setMnemonic('x')
                    .addActionMethod(
                            () -> UIUtilities.invokeOffEDTNoLogging(lifecycleController::quit))
                    .build());
        }
    }

    @Override
    public final void menuSelected(final MenuEvent e) {
        final Optional<ServerState> activeConnectionState = activeFrameManager.getActiveFrame()
                .map(TextFrame::getContainer)
                .flatMap(WindowModel::getConnection)
                .map(Connection::getState);
        final boolean connected = activeConnectionState.equals(Optional.of(ServerState.CONNECTED));

        ssd.setEnabled(connected);
        disconnect.setEnabled(connected);
    }

    @Override
    public void menuDeselected(final MenuEvent e) {
        // Do nothing
    }

    @Override
    public void menuCanceled(final MenuEvent e) {
        // Do nothing
    }

}
