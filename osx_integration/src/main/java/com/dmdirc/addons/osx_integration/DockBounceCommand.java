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

package com.dmdirc.addons.osx_integration;

import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A command to bounce the icon in the OSX dock.
 */
public class DockBounceCommand extends BaseCommand {

    /**
     * Information about this command.
     */
    public static final BaseCommandInfo INFO = new BaseCommandInfo(
            "dockbounce",
            "dockbounce - bounce the dock icon on OS X",
            CommandType.TYPE_GLOBAL);
    /**
     * The current UI's Apple-specific library.
     */
    private final Apple apple;

    /**
     * Creates an instance of {@link DockBounceCommand}.
     *
     * @param controller The controller to use for command information.
     * @param apple      The UI wrapper for Apple-specific things
     */
    @Inject
    public DockBounceCommand(final CommandController controller, final Apple apple) {
        super(controller);
        this.apple = apple;
    }

    @Override
    public void execute(
            @Nonnull final WindowModel origin,
            final CommandArguments args,
            final CommandContext context) {
        apple.requestUserAttention(false);
    }

}
