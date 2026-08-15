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
package org.apache.directory.studio.ldapservers;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;


// ── CLASS: ConsolesManager — CASSIAN'S INTERCEPT LOG STATION ─────────────────────────────
// In Rogue One, Cassian Andor maintains a dedicated intelligence dossier for every covert
// operation — one channel per mission, carefully labeled, never mixed up.
// We do the same here: every LDAP server gets its own Eclipse console window so its log
// output never bleeds into another server's output.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Manages the Eclipse console windows associated with each LDAP server instance.
 * When a server starts producing output (logs, errors, info messages), we need somewhere
 * to display it — this class hands out a dedicated {@link MessageConsole} per server.
 * Think of this class as Cassian Andor's comm-channel assignment desk: one channel per
 * mission, created on demand, never duplicated.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConsolesManager
{
    /** The default instance */
    private static ConsolesManager instance;

    /** The map of consoles identified by server ID */
    private Map<LdapServer, MessageConsole> consolesMap;


    // ── Cassian Opens His Intelligence Dossier ──────────────────────────────────────────────
    // Cassian Andor sits down at the Rebel intelligence desk on Yavin IV before a new op.
    // He opens a fresh binder — empty for now, but ready to hold each mission's intercept log.
    // We initialize the map that will eventually hold one console per server.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the single ConsolesManager instance.
     * Private because this is a singleton — we only ever want one manager controlling all consoles.
     * The map starts empty; consoles are created lazily the first time a server needs output.
     *
     * <p>For example — Cassian sets up his workstation:</p>
     * <pre>
     *   Cassian opens an empty folder labelled "Active Mission Channels".
     *   No intercepts yet — but the moment a server calls in, it gets its own tab.
     * </pre>
     */
    private ConsolesManager()
    {
        // Initialization of the map
        consolesMap = new HashMap<LdapServer, MessageConsole>();
    }


    // ── Retrieving Cassian's Desk (Singleton) ────────────────────────────────────────────────
    // The Rebellion has one central intelligence desk — every handler who needs to file a report
    // goes to the same desk, not a different one each time.
    // We use the classic singleton pattern so the whole application shares one ConsolesManager.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton ConsolesManager instance, creating it on the first call.
     * Singletons are useful here because we want one central place that tracks all console
     * assignments — multiple callers asking for the manager should get the exact same object.
     *
     * <p>For example — Cassian's desk is shared across the whole base:</p>
     * <pre>
     *   Agent 1 walks up: "I need the intercept manager." → gets Cassian's desk.
     *   Agent 2 walks up: same request → same desk, not a new one.
     * </pre>
     *
     * @return the single shared ConsolesManager
     */
    public static ConsolesManager getDefault()
    {
        if ( instance == null )
        {
            instance = new ConsolesManager();
        }

        return instance;
    }


    // ── Assigning A Comm Channel To A Mission ───────────────────────────────────────────────
    // Cassian receives a new mission brief and checks his assignment board.
    // If that mission already has a channel open, he hands back the existing one; otherwise,
    // he opens a fresh channel and pins it to the board.
    // We do the same: look up an existing console for this server, or create a new one.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse {@link MessageConsole} dedicated to the given server, creating it
     * if one doesn't exist yet.
     * Each server gets its own named console tab in the Eclipse Console view so we can read
     * that server's log output without it mixing with other servers' messages.
     *
     * <p>For example — Cassian hands out a comm channel:</p>
     * <pre>
     *   Mission "ApacheDS-Local" calls in.
     *   Cassian checks: channel already open? No — he opens "ApacheDS-Local [LDAP Server]".
     *   He registers it with Rebel HQ (ConsolePlugin) and hands back the channel handle.
     *   Next time "ApacheDS-Local" calls, Cassian finds the existing channel and returns it.
     * </pre>
     *
     * @param server  the LDAP server instance that needs a console — we use it as the map key
     *                and its name becomes part of the console's title
     * @return the existing or newly created {@link MessageConsole} for this server
     */
    public MessageConsole getMessageConsole( LdapServer server )
    {
        if ( consolesMap.containsKey( server ) )
        {
            return consolesMap.get( server );
        }
        else
        {
            MessageConsole messageConsole = new MessageConsole( server.getName()
                + " " + Messages.getString( "ConsolesManager.LdapServer" ), null ); //$NON-NLS-1$ //$NON-NLS-2$
            // DIRSTUDIO-1148: limit the amount of characters shown in the console
            messageConsole.setWaterMarks( 70000, 80000 );
            consolesMap.put( server, messageConsole );

            ConsolePlugin.getDefault().getConsoleManager().addConsoles( new IConsole[]
                { messageConsole } );

            return messageConsole;
        }
    }
}
