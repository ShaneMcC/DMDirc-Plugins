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

package com.dmdirc.addons.windowstatus;

import com.dmdirc.addons.ui_swing.injection.SwingModule;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;

import dagger.Module;
import dagger.Provides;

/**
 * DI module for this plugin.
 */
@Module(injects = WindowStatusManager.class, addsTo = SwingModule.class)
public class WindowStatusModule {

    /** The plugin's plugin info. */
    private final PluginInfo pluginInfo;

    public WindowStatusModule(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    /**
     * Provides the domain that the settings should be stored under.
     *
     * @return The settings domain for the plugin.
     */
    @Provides
    @PluginDomain(WindowStatusPlugin.class)
    public String getSettingsDomain() {
        return pluginInfo.getDomain();
    }

    @Provides
    @PluginDomain(WindowStatusPlugin.class)
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

}
