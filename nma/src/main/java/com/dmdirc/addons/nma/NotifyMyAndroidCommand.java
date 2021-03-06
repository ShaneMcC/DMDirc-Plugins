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

package com.dmdirc.addons.nma;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.plugins.PluginDomain;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Command to raise notifications with NotifyMyAndroid.
 */
public class NotifyMyAndroidCommand extends BaseCommand {

    private static final org.slf4j.Logger LOG = LoggerFactory.
            getLogger(NotifyMyAndroidCommand.class);
    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo(
            "notifymyandroid",
            "notifymyandroid <title> -- <body>"
            + " - Sends notification to NotifyMyAndroid",
            CommandType.TYPE_GLOBAL);
    /** The configuration domain to retrieve settings from. */
    private final String configDomain;

    /**
     * Creates a new instance of this command.
     *
     * @param controller   The controller to use for command information.
     * @param configDomain This plugin's settings domain
     */
    @Inject
    public NotifyMyAndroidCommand(final CommandController controller,
            @PluginDomain(NotifyMyAndroidPlugin.class) final String configDomain) {
        super(controller);
        this.configDomain = configDomain;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final String[] parts = args.getArgumentsAsString().split("\\s+--\\s+", 2);
        LOG.trace("Split input: {}", (Object[]) parts);

        if (parts.length != 2) {
            showUsage(origin, args.isSilent(), INFO.getName(), INFO.getHelp());
        }

        LOG.trace("Retrieving settings from domain '{}'", configDomain);
        final NotifyMyAndroidClient client = new NotifyMyAndroidClient(
                origin.getConfigManager().getOption(configDomain, "apikey"),
                origin.getConfigManager().getOption(configDomain, "application"));

        new Thread(() -> {
            try {
                client.notify(parts[0], parts[1]);
                showOutput(origin, args.isSilent(), "Notification sent");
            } catch (IOException ex) {
                LOG.info("Exception when trying to notify NMA", ex);
                showError(origin, args.isSilent(), "Unable to send: " + ex.getMessage());
            }
        }, "NMA Thread").start();
    }

}
