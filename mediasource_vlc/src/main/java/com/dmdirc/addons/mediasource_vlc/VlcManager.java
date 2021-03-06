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

package com.dmdirc.addons.mediasource_vlc;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.util.io.Downloader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Retrieves information from VLC using its HTTP interface.
 */
public class VlcManager implements MediaSource {

    /** The information obtained from VLC. */
    private final Map<String, String> information = new HashMap<>();
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** The identity controller to read settings from. */
    private final IdentityController identityController;
    /** Downloader to download files. */
    private final Downloader downloader;

    @Inject
    public VlcManager(
            @PluginDomain(VlcMediaSourcePlugin.class) final PluginInfo pluginInfo,
            final IdentityController identityController) {
        this.pluginInfo = pluginInfo;
        this.identityController = identityController;
        this.downloader = new Downloader();
    }

    @Override
    public MediaSourceState getState() {
        if (fetchInformation()) {
            final String output = information.get("state");
            if (output.equalsIgnoreCase("stop")) {
                return MediaSourceState.STOPPED;
            } else if (output.equalsIgnoreCase("playing")) {
                return MediaSourceState.PLAYING;
            } else if (output.equalsIgnoreCase("paused")) {
                return MediaSourceState.PAUSED;
            } else {
                return MediaSourceState.NOTKNOWN;
            }
        } else {
            return MediaSourceState.CLOSED;
        }
    }

    @Override
    public String getAppName() {
        return "VLC";
    }

    @Override
    public String getArtist() {
        return information.containsKey("artist") ? information.get("artist") : getFallbackArtist();
    }

    /**
     * Retrieves the fallback artist (parsed from the file name).
     *
     * @return The fallback artist
     */
    private String getFallbackArtist() {
        String result = "unknown";

        if (information.containsKey("playlist_current")) {
            try {
                final int item = Integer.parseInt(information.get(
                        "playlist_current"));
                String[] bits = information.get("playlist_item_" + item).split(
                        (File.separatorChar == '\\' ? "\\\\" : File.separator));
                result = bits[bits.length - 1];
                bits = result.split("-");
                if (bits.length > 1) {
                    result = bits[0];
                } else {
                    // Whole filename is the title, so no artist is known.
                    result = "unknown";
                }
            } catch (NumberFormatException nfe) {
                // DO nothing
            }
        }

        return result;
    }

    @Override
    public String getTitle() {
        return information.containsKey("title") ? information.get("title")
                : getFallbackTitle();
    }

    /**
     * Retrieves the fallback title (parsed from the file name).
     *
     * @return The fallback title
     */
    private String getFallbackTitle() {
        String result = "unknown";

        // Title is unknown, lets guess using the filename
        if (information.containsKey("playlist_current")) {
            try {
                final int item = Integer.parseInt(information.get(
                        "playlist_current"));
                result = information.get("playlist_item_" + item);

                final int sepIndex = result.lastIndexOf(File.separatorChar);
                final int extIndex = result.lastIndexOf('.');
                result = result.substring(sepIndex,
                        extIndex > sepIndex ? extIndex : result.length());

                final int offset = result.indexOf('-');
                if (offset > -1) {
                    result = result.substring(offset + 1).trim();
                }
            } catch (NumberFormatException nfe) {
                // Do nothing
            }
        }

        return result;
    }

    @Override
    public String getAlbum() {
        return information.containsKey("album/movie/show title")
                ? information.get("album/movie/show title") : "unknown";
    }

    @Override
    public String getLength() {
        // This is just seconds, could do with formatting.
        return information.containsKey("length") ? information.get("length")
                : "unknown";
    }

    @Override
    public String getTime() {
        // This is just seconds, could do with formatting.
        return information.containsKey("time") ? information.get("time")
                : "unknown";
    }

    @Override
    public String getFormat() {
        return information.containsKey("codec") ? information.get("codec")
                : "unknown";
    }

    @Override
    public String getBitrate() {
        return information.containsKey("bitrate") ? information.get("bitrate")
                : "unknown";
    }

    @Handler
    public void showConfig(final ClientPrefsOpenedEvent event) {
        final PreferencesDialogModel manager = event.getModel();
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "VLC Media Source",
                "", "category-vlc");

        final PreferencesSetting setting = new PreferencesSetting(
                PreferencesType.LABEL, pluginInfo.getDomain(), "", "Instructions",
                "Instructions", manager.getConfigManager(), manager.getIdentity());
        setting.setValue("<html><p>"
                + "The VLC media source requires that VLC's web interface is"
                + " enabled. To do this, follow the steps below:</p>"
                + "<ol style='margin-left: 20px; padding-left: 0px;'>"
                + "<li>Open VLC's preferences dialog (found in the 'Tools' "
                + "menu)"
                + "<li>Set the 'Show settings' option to 'All'"
                + "<li>Expand the 'Interface' category by clicking on the plus "
                + "sign next to it"
                + "<li>Select the 'Main interfaces' category"
                + "<li>Check the box next to 'HTTP remote control interface'"
                + "<li>Expand the 'Main interfaces' category"
                + "<li>Select the 'HTTP' category"
                + "<li>In the 'Host address' field, enter 'localhost:8082'"
                + "<li>In the 'Source directory' field enter the path to VLC's"
                + " http directory<ul style='margin-left: 5px; padding-left: "
                + "0px; list-style-type: none;'>"
                + "<li style='padding-bottom: 5px'>For Linux users this may be "
                + "/usr/share/vlc/http/"
                + "<li>For Windows users this will be under the main VLC "
                + "directory, e.g. C:\\Program Files\\VLC\\http</ul><li>Click "
                + "'Save'<li>Restart VLC</ol></html>");
        general.addSetting(setting);
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                pluginInfo.getDomain(), "host", "Hostname and port",
                "The host and port that VLC listens on for web connections",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(general);
    }

    /**
     * Attempts to fetch information from VLC's web interface.
     *
     * @return True on success, false otherwise
     */
    private boolean fetchInformation() {
        information.clear();
        final List<String> res;
        final List<String> res2;

        try {
            final String host = identityController.getGlobalConfiguration()
                    .getOption(pluginInfo.getDomain(), "host");
            res = downloader.getPage("http://" + host + "/old/info.html");
            res2 = downloader.getPage("http://" + host + "/old/");
            parseInformation(res, res2);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Parses the information from the two pages obtained from VLC's web interface.
     *
     * @param res  The first page of VLC info (/old/info.html)
     * @param res2 The second page of VLC info (/old/)
     */
    protected void parseInformation(final List<String> res,
            final List<String> res2) {
        for (String line : res) {
            final String tline = line.trim();

            if (tline.startsWith("<li>")) {
                final int colon = tline.indexOf(':');
                final String key = tline.substring(5, colon).trim()
                        .toLowerCase();
                final String value = tline.substring(colon + 1, tline.length()
                        - 5).trim();

                information.put(key, value);
            }
        }

        boolean isPlaylist = false;
        boolean isCurrent = false;
        boolean isItem = false;
        int playlistItem = 0;
        for (String line : res2) {
            final String tline = line.trim();

            if (isPlaylist) {
                if (tline.startsWith("</ul>")) {
                    isPlaylist = false;
                    information.put("playlist_items", Integer.toString(
                            playlistItem));
                } else if (tline.equalsIgnoreCase("<strong>")) {
                    isCurrent = true;
                } else if (tline.equalsIgnoreCase("</strong>")) {
                    isCurrent = false;
                } else if (tline.startsWith("<a href=\"?control=play&amp")) {
                    isItem = true;
                } else if (isItem) {
                    String itemname = tline;
                    if (itemname.endsWith("</a>")) {
                        itemname = itemname.substring(0, itemname.length() - 4);
                    }
                    if (!itemname.isEmpty()) {
                        if (isCurrent) {
                            information.put("playlist_current", Integer
                                    .toString(playlistItem));
                        }
                        information.put("playlist_item_" + Integer.toString(
                                playlistItem++), itemname);
                    }
                    isItem = false;
                }
            } else if (tline.equalsIgnoreCase("<!-- Playlist -->")) {
                isPlaylist = true;
            } else if (tline.startsWith("State:")) {
                information.put("state", tline.substring(6, tline.indexOf('<'))
                        .trim());
            } else if (tline.startsWith("got_")) {
                final int equals = tline.indexOf('=');

                information.put(tline.substring(4, equals).trim(),
                        tline.substring(equals + 1, tline.length() - 1).trim());
            }
        }
    }
}
