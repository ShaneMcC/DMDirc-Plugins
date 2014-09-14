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

package com.dmdirc.addons.serverlists;

import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.config.IdentityController;

import java.net.URI;

/**
 * Describes an entry for a server within a {@link ServerGroup}.
 *
 * @since 0.6.4
 */
public class ServerEntry extends ServerGroupItemBase {

    /** The address of the server in question. */
    private URI address;
    /** The group that owns this entry. */
    private final ServerGroup group;
    /** The manager to use to create new servers. */
    private final ConnectionManager connectionManager;

    /**
     * Creates a new server entry.
     *
     * @param identityController The controller to read/write settings with.
     * @param connectionManager      The server manager to connect to servers with.
     * @param group              The group that owns this entry
     * @param name               The name of this server
     * @param address            The address of this server
     * @param profile            The name of the profile to be used by this server
     */
    public ServerEntry(
            final IdentityController identityController,
            final ConnectionManager connectionManager,
            final ServerGroup group, final String name,
            final URI address, final String profile) {
        super(identityController);

        this.connectionManager = connectionManager;
        setName(name);
        setProfile(profile);
        this.address = address;
        this.group = group;
    }

    @Override
    public ServerGroup getGroup() {
        return group;
    }

    @Override
    protected ServerGroup getParent() {
        return getGroup();
    }

    /**
     * Retrieves the address used by this server.
     *
     * @return This server's address
     */
    @Override
    public URI getAddress() {
        return address;
    }

    /**
     * Sets the address for this server entry.
     *
     * @param address The new address for this entry
     */
    public void setAddress(final URI address) {
        setModified(true);
        this.address = address;
    }

    @Override
    public void connect() {
        connectionManager.connectToAddress(address, getProfileIdentity());
    }

    @Override
    public String toString() {
        return "[" + getName() + ": address: " + getAddress() + "]";
    }

}