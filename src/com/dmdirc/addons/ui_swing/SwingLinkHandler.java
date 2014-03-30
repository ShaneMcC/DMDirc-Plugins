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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.LinkChannelClickedEvent;
import com.dmdirc.events.LinkNicknameClickedEvent;
import com.dmdirc.events.LinkUrlClickedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.core.util.URLHandler;

import com.google.common.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handles response to users clicking on links within the Swing UI.
 */
@Singleton
public class SwingLinkHandler {

    private final URLHandler urlHandler;
    private final ActiveFrameManager activeFrameManager;
    private final SwingWindowFactory windowFactory;

    @Inject
    public SwingLinkHandler(
            final ActiveFrameManager activeFrameManager,
            final URLHandler urlHandler,
            final SwingWindowFactory windowFactory) {
        this.urlHandler = urlHandler;
        this.activeFrameManager = activeFrameManager;
        this.windowFactory = windowFactory;
    }

    @Subscribe
    public void handleChannelClick(final LinkChannelClickedEvent event) {
        final FrameContainer container = event.getWindow().getContainer();
        final Connection connection = container.getConnection();
        if (connection != null) {
            connection.join(new ChannelJoinRequest(event.getTarget()));
        }
    }

    @Subscribe
    public void handleLinkClick(final LinkUrlClickedEvent event) {
        urlHandler.launchApp(event.getTarget());
    }

    @Subscribe
    public void handleNicknameClick(final LinkNicknameClickedEvent event) {
        final FrameContainer container = event.getWindow().getContainer();
        final Connection connection = container.getConnection();
        if (connection != null) {
            activeFrameManager.setActiveFrame(
                    windowFactory.getSwingWindow(connection.getQuery(event.getTarget())));
        }
    }

}