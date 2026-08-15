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

package org.apache.directory.studio.common.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;


// ── CLASS: HistoryUtils — R2-D2 RECORDING MISSION LOGS ──────────────────────
// R2-D2 faithfully records every mission into his memory banks, keeping the
// most recent entry at the front and trimming the log to twenty entries so his
// storage does not overflow.  This class does the same for input field history:
// we save and load the most-recently-used values so the user can quickly
// revisit past inputs without retyping them each time.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We manage the save-and-load of input field history using Eclipse's
 * {@link IDialogSettings} storage.  The most recent entry always appears at
 * the front of the list, duplicates are moved to the front rather than
 * added again, and we cap the history at twenty entries per key.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HistoryUtils
{
    // ── METHOD save — LOGGING A NEW MISSION ENTRY ────────────────────────────
    // R2-D2 opens his mission log and records the latest entry.  If the entry
    // already exists in the log, he moves it to the top rather than duplicating
    // it.  He trims the log to the twenty most recent entries to save space.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We save the given value under the given key in the dialog settings,
     * moving it to the front of the history list if it already exists there.
     * The list is capped at twenty entries; older entries are dropped when the
     * limit is exceeded.  Does nothing if {@code dialogSettings} is null.
     *
     * @param dialogSettings the settings store to write into
     * @param key            the history key (typically the field name)
     * @param value          the value to add to the front of the history
     */
    public static void save( IDialogSettings dialogSettings, String key, String value )
    {
        if ( dialogSettings != null )
        {

            // get current history
            String[] history = load( dialogSettings, key );
            List<String> list = new ArrayList<String>( Arrays.asList( history ) );

            // add new value or move to first position
            if ( list.contains( value ) )
            {
                list.remove( value );
            }
            list.add( 0, value );

            // check history size
            while ( list.size() > 20 )
            {
                list.remove( list.size() - 1 );
            }

            // save
            history = list.toArray( new String[list.size()] );
            dialogSettings.put( key, history );
        }
    }


    // ── METHOD load — READING BACK THE MISSION LOG ───────────────────────────
    // R2-D2 retrieves the mission log stored under the given key.  If no log
    // exists yet for that key, he hands back an empty array rather than making
    // a fuss about it — the caller can treat it as a fresh start.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We load and return the history array stored under the given key.  If no
     * history has been saved yet, or if {@code dialogSettings} is null, we
     * return an empty array so callers always get a non-null result.
     *
     * @param dialogSettings the settings store to read from
     * @param key            the history key to look up
     * @return the stored history entries, or an empty array if none exist
     */
    public static String[] load( IDialogSettings dialogSettings, String key )
    {
        if ( dialogSettings != null )
        {
            String[] history = dialogSettings.getArray( key );

            if ( history == null )
            {
                history = new String[0];
            }

            return history;
        }

        return new String[0];
    }
}
