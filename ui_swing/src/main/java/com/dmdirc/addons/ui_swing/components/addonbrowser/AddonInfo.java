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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.URLBuilder;

import java.awt.Image;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Describes an addon.
 */
public class AddonInfo {

    /** Addon site ID. */
    private final int id;
    /** Stable download name. */
    private final String stableDownload;
    /** Unstable download name. */
    private final String unstableDownload;
    /** Nightly download name. */
    private final String nightlyDownload;
    /** Addon title. */
    private final String title;
    /** Addon author and email. */
    private final String author;
    /** Addon rating from 0-10. */
    private final int rating;
    /** Full text description. */
    private final String description;
    /** Addon type. */
    private final AddonType type;
    /** Has this addon been verified by the developers? */
    private final boolean verified;
    /** Date this addon was updated. */
    private final int date;
    /** Screenshot image. */
    private final ImageIcon screenshot;
    /** Current client update channel. */
    private final UpdateChannel channel;
    /** The manager to use to check for update information. */
    private final UpdateManager updateManager;

    /**
     * Creates a new addon info class with the specified entries.
     *
     * @param configManager The config provider to use to find settings.
     * @param updateManager The manager to use to check for update information.
     * @param urlBuilder    The URL builder to use to retrieve image URLs.
     * @param entry         List of entries
     */
    public AddonInfo(
            final AggregateConfigProvider configManager,
            final UpdateManager updateManager,
            final URLBuilder urlBuilder,
            final Map<String, String> entry) {
        id = Integer.parseInt(entry.get("id"));
        title = entry.get("title");
        author = entry.get("user");
        rating = Integer.parseInt(entry.get("rating"));
        type = entry.get("type").equals("plugin") ? AddonType.TYPE_PLUGIN : AddonType.TYPE_THEME;
        stableDownload = entry.containsKey("stable") ? entry.get("stable") : "";
        unstableDownload = entry.containsKey("unstable") ? entry
                .get("unstable") : "";
        nightlyDownload = entry.containsKey("nightly") ? entry.get("nightly")
                : "";
        description = entry.get("description");
        verified = entry.get("verified").equals("yes");
        date = Integer.parseInt(entry.get("date"));
        if (entry.get("screenshot").equals("yes")) {
            screenshot = new ImageIcon(
                    urlBuilder.getUrl("https://addons.dmdirc.com/addonimg/" + id));
            screenshot.setImage(screenshot.getImage().
                    getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        } else {
            screenshot = new ImageIcon(urlBuilder.getUrl("dmdirc://com/dmdirc/res/logo.png"));
        }

        UpdateChannel tempChannel;
        try {
            tempChannel = UpdateChannel.valueOf(configManager.getOption(
                    "updater", "channel"));
        } catch (final IllegalArgumentException ex) {
            tempChannel = UpdateChannel.NONE;
        }
        channel = tempChannel;

        this.updateManager = updateManager;
    }

    public int getId() {
        return id;
    }

    public String getStableDownload() {
        return stableDownload;
    }

    public String getUnstableDownload() {
        return unstableDownload;
    }

    public String getNightlyDownload() {
        return nightlyDownload;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public AddonType getType() {
        return type;
    }

    public boolean isVerified() {
        return verified;
    }

    public int getDate() {
        return date;
    }

    public ImageIcon getScreenshot() {
        return screenshot;
    }

    public UpdateChannel getChannel() {
        return channel;
    }

    /**
     * Is the plugin installed?
     *
     * @return true iff installed
     */
    public boolean isInstalled() {
        for (UpdateComponent comp : updateManager.getComponents()) {
            if (comp.getName().equals("addon-" + getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is the plugin downloadable?
     *
     * @return true iff the plugin is downloadable
     */
    public boolean isDownloadable() {
        return !getDownload().isEmpty();
    }

    /**
     * Returns the download location for this addoninfo, or an empty string.
     *
     * @return Download location or empty string
     */
    @SuppressWarnings("fallthrough")
    public String getDownload() {
        switch (channel) { // NOPMD
            case NONE:
            // fallthrough
            case NIGHTLY:
                if (!nightlyDownload.isEmpty()) {
                    return nightlyDownload;
                }
            // fallthrough
            case UNSTABLE:
                if (!unstableDownload.isEmpty()) {
                    return unstableDownload;
                }
            // fallthrough
            case STABLE:
                if (!stableDownload.isEmpty()) {
                    return stableDownload;
                }
                return "";
            default:
                return "";
        }
    }

    /**
     * Checks if the text matches this plugin
     *
     * @param text Comparison addon text.
     *
     * @return true iff the plugin matches
     */
    public boolean matches(final String text) {
        return title.toLowerCase().contains(text.toLowerCase())
                || description.toLowerCase().contains(text.toLowerCase());
    }

}
