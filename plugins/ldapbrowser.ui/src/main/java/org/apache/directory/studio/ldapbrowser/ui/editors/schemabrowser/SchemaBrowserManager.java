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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: SchemaBrowserManager — Lando Running Cloud City ────────────────────
// Lando Calrissian runs Cloud City from a single administrator's console — he
// controls every platform, every corridor, ensuring only one instance of each
// critical system is active at a time.  When someone arrives (Han's Falcon docks)
// he decides whether to open a new bay or redirect them to an existing one.
// This class does the same thing: it manages the single schema browser instance
// in the Eclipse workbench, opening one if none exists or redirecting to the
// already-open one, then steering it to the requested content.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility class that acts as the single point of control for the schema
 * browser editor instance in the workbench.
 * Any code that wants to navigate the schema browser calls this class; it
 * finds the existing editor or opens a new one, then sets the desired input.
 * Think of this class as Lando at his administrator's console: it ensures
 * Cloud City (the schema browser) has exactly one open instance and routes
 * every request to the right bay without creating chaos.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaBrowserManager
{

    /** The dummy input, to find the single schema browser instance */
    private static SchemaBrowserInput DUMMY_INPUT = new SchemaBrowserInput( null, null );


    // ── Lando Routes A Visitor To The Right Platform ──────────────────────────────
    // A new ship arrives at Cloud City; Lando checks whether a landing pad is
    // already assigned and sends it there, or opens a fresh pad if none exists.
    // We package the connection and schema element into a SchemaBrowserInput and
    // hand it off to the private setInput overload that finds or opens the editor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Navigates the schema browser to the given connection and schema element,
     * opening the browser editor if it is not already open.
     * This is the primary entry point for all external code that wants to show
     * a specific schema element (e.g. when the user double-clicks an attribute in
     * the entry editor).
     *
     * <p>For example — Lando assigns a docking bay:</p>
     * <pre>
     *   SchemaBrowserManager.setInput(connection, myAttributeType);
     *   // schema browser opens (or comes to front) showing myAttributeType
     * </pre>
     *
     * @param connection    the LDAP connection whose schema to display
     * @param schemaElement the schema element to highlight in the browser
     */
    public static void setInput( IBrowserConnection connection, AbstractSchemaObject schemaElement )
    {
        SchemaBrowserInput input = new SchemaBrowserInput( connection, schemaElement );
        setInput( input );
    }


    // ── Lando Finds The Right Bay And Guides The Ship In ──────────────────────────
    // Lando searches his display for the assigned bay, opens a new one if the ship
    // has no record, then guides it in with the correct coordinates.
    // We search for the existing schema browser using the dummy input (which equals
    // any SchemaBrowserInput thanks to the one-instance hack), open a new one if
    // absent, then push the real input in either case.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds or opens the single schema browser editor and sets its input.
     * We use {@code DUMMY_INPUT} to locate an existing instance via
     * {@code findEditor()}, which works because all SchemaBrowserInput objects
     * compare equal when the one-instance hack is enabled.
     * If no editor is found and the desired input is non-null, we open a fresh
     * one, then bring it to the front if it was hidden behind another editor.
     *
     * <p>For example — Lando's console finds or opens a bay:</p>
     * <pre>
     *   setInput(new SchemaBrowserInput(conn, objectClass));
     *   // existing editor found  → editor.setInput(input)
     *   // no editor found        → openEditor(...) then setInput
     *   // editor not visible     → bringToTop(editor)
     * </pre>
     *
     * @param input  the input to set on the schema browser; may be null to clear
     */
    private static void setInput( SchemaBrowserInput input )
    {
        SchemaBrowser editor = ( SchemaBrowser ) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .findEditor( DUMMY_INPUT );
        if ( editor == null && input != null )
        {
            // open new schema browser
            try
            {
                editor = ( SchemaBrowser ) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor( input, SchemaBrowser.getId(), false );
                editor.setInput( input );
            }
            catch ( PartInitException e )
            {
                e.printStackTrace();
            }
        }
        else if ( editor != null )
        {
            // set the input to already opened schema browser
            editor.setInput( input );

            // bring schema browser to top
            if ( !PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().isPartVisible( editor ) )
            {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop( editor );
            }
        }
    }

}
