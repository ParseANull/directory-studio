/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.studio.openldap.config.editor;


import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ConnectionServerConfigurationInput — Han's Jump To Hyperspace ──────
// In A New Hope, Han Solo slams the Millennium Falcon's hyperdrive lever forward
// and the ship lurches into lightspeed — suddenly the galaxy collapses to a
// single streaking point and the Falcon is somewhere entirely new.
// This class does the same thing for our editor: it wraps a live LDAP
// {@link Connection} and uses it as the launchpad to pull the server's
// cn=config tree directly over the wire.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Editor input that sources its configuration from a live LDAP
 * {@link Connection} — as opposed to a local directory or a brand-new file.
 * When the user right-clicks a connection and chooses "Open Configuration,"
 * we wrap that connection here and hand it to the editor, which reads
 * cn=config straight from the server.
 * Think of it as Han's hyperspace jump: the connection object is the
 * Falcon's nav computer, and this class is the moment we engage the drive.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionServerConfigurationInput extends AbstractServerConfigurationInput
{
    /** The live LDAP connection this input is backed by. */
    private Connection connection;


    // ── Han Locks In The Destination Coordinates ──────────────────────────────
    // Han punches the target star system into the nav computer before hitting
    // hyperspace — no connection, no jump.
    // We store the connection object here so every subsequent operation
    // (load, save, diff) knows which server to talk to.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new input backed by the given live connection.
     * The connection must be non-null — without it, we have no way to
     * reach the server's cn=config tree.
     *
     * @param connection  the LDAP connection to use — must not be null
     */
    public ConnectionServerConfigurationInput( Connection connection )
    {
        this.connection = connection;
    }


    // ── Chewie Checks The Nav Computer ────────────────────────────────────────
    // Chewbacca verifies the coordinates Han loaded before they engage the
    // drive — the nav computer is the source of truth for where they're headed.
    // We expose the connection so the editor and save utilities can interact
    // with the actual server.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection this input is backed by.
     * The editor uses this to open a browser connection and read/write
     * cn=config entries on the live server.
     *
     * @return the connection — may be null if not properly initialized, but
     *         that shouldn't happen in normal use
     */
    public Connection getConnection()
    {
        return connection;
    }


    // ── Falcon's Transponder Broadcasts Her Name ───────────────────────────────
    // The Millennium Falcon doesn't just show up as "Unknown Freighter" on the
    // scope — she broadcasts her registry name so everyone knows it's the Falcon.
    // We format the tooltip as "{connectionName} - Configuration" so the user
    // can tell which server this editor tab belongs to.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text for the editor tab, formatted as
     * "{connectionName} - Configuration".
     * This makes it easy to tell which server a given editor tab is pointed at,
     * especially when you have multiple connections open.
     *
     * @return the tooltip string with the connection name embedded
     */
    @Override
    public String getToolTipText()
    {
        return NLS.bind( "{0} - Configuration", connection.getName() );
    }


    // ── Falcon's Name Appears On The Display ─────────────────────────────────
    // Every ship in the fleet has a short name on the battle board — "Falcon,"
    // not "YT-1300 Light Freighter, registry MF-1977."
    // We return the same formatted name for the editor tab label.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label for the editor tab — same format as
     * {@link #getToolTipText()}: "{connectionName} - Configuration".
     *
     * @return the tab label with the connection name embedded
     */
    @Override
    public String getName()
    {
        return NLS.bind( "{0} - Configuration", connection.getName() );
    }


    // ── Verify The Falcon Is Actually In The Docking Bay ─────────────────────
    // Before engaging hyperspace, Han confirms the Falcon is actually docked
    // and ready — not just listed in some manifest.
    // We check that the connection field is non-null so the editor doesn't
    // try to open against a ghost connection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the connection is non-null, {@code false}
     * otherwise.
     * The editor checks this before rendering itself — if the connection
     * object disappeared, we'd rather show nothing than crash.
     *
     * @return {@code true} when a connection is set, {@code false} if null
     */
    @Override
    public boolean exists()
    {
        return connection != null;
    }


    // ── Two Ships Confirm They're Headed For The Same Star ───────────────────
    // In a convoy, two Falcons (if that were possible) are "the same" if they're
    // both registered to the same coordinates — same ship, same destination.
    // We consider two inputs equal when they wrap the same connection object
    // (using the connection's own equals).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a
     * {@link ConnectionServerConfigurationInput} wrapping the same connection.
     * We delegate to the connection's own {@code equals()} method so the
     * comparison respects whatever identity the connection layer uses.
     *
     * @param obj  the object to compare to
     * @return {@code true} if both inputs reference the same connection
     */
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }

        if ( obj instanceof ConnectionServerConfigurationInput )
        {
            ConnectionServerConfigurationInput input = ( ConnectionServerConfigurationInput ) obj;

            if ( input.exists() && exists() )
            {
                Connection inputConnection = input.getConnection();

                if ( inputConnection != null )
                {
                    return inputConnection.equals( connection );
                }
            }
        }

        return false;
    }


    // ── Falcon's Transponder Code Is Unique ───────────────────────────────────
    // Every ship has a unique transponder hash — the Falcon's is different from
    // every other freighter in the Outer Rim.
    // We delegate to the connection's hashCode so this input can be safely
    // used in HashMaps and HashSets.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code based on the wrapped connection.
     * Consistent with {@link #equals(Object)} — two inputs that are equal
     * will return the same hash.
     *
     * @return the connection's hash code
     */
    public int hashCode()
    {
        return connection.hashCode();
    }
}
