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
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: SchemaBrowserInput — Leia's Hologram Message ──────────────────────
// Leia records a hologram inside R2-D2: "Help me, Obi-Wan Kenobi, you're my
// only hope."  The message is a small, self-contained packet — a connection
// identity plus a specific request — that gets handed off and replayed wherever
// R2 ends up.  SchemaBrowserInput is exactly that: a tiny immutable carrier
// holding a connection and a schema element, passed into the schema browser
// editor so it knows what to display next.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Carries the two pieces of state that the schema browser editor needs: which
 * LDAP connection to use and which schema element to show.
 * Eclipse's editor framework uses {@link IEditorInput} as the "what to open"
 * signal, and we implement a one-instance hack via {@code equals()} so only one
 * schema browser tab is ever open at a time (unless navigation history needs to
 * distinguish states, in which case we disable the hack temporarily).
 * Think of this class as Leia's hologram: a compact, self-contained message
 * that gets handed off to the right recipient and replayed on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaBrowserInput implements IEditorInput
{

    /** The connection */
    private IBrowserConnection connection;

    /** The schema element */
    private AbstractSchemaObject schemaElement;

    /** One instance hack flag */
    private static boolean oneInstanceHackEnabled = true;


    // ── Leia Records Her Message Into R2 ─────────────────────────────────────────
    // Leia stands before R2-D2 and speaks her plea — connection ID (Alderaan) and
    // the specific request (plans for the Death Star).  R2 stores both so he can
    // replay the message exactly as given.
    // We store the connection and schema element together so the browser can
    // restore exactly this state when navigating history.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new input carrying the given connection and schema element.
     * Either parameter may be null: a null connection means "no server selected"
     * and a null schema element means "show the list but select nothing."
     *
     * <p>For example — Leia records her hologram:</p>
     * <pre>
     *   SchemaBrowserInput input =
     *       new SchemaBrowserInput(connection, attributeType);
     *   // browser will open on that connection, scroll to that attribute type
     * </pre>
     *
     * @param connection    the LDAP connection whose schema to display; may be null
     * @param schemaElement the specific schema element to highlight; may be null
     */
    public SchemaBrowserInput( IBrowserConnection connection, AbstractSchemaObject schemaElement )
    {
        this.connection = connection;
        this.schemaElement = schemaElement;
    }


    //    /**
    //     * Creates a new instance of SchemaBrowserInput.
    //     *
    //     *@param connection the connection
    //     * @param schemaElement the schema element input
    //     */
    //    public SchemaBrowserInput( Connection connection, SchemaPart schemaElement )
    //    {
    //        this.connection = BrowserCorePlugin.getDefault().getConnectionManager().getConnection( connection );
    //        this.schemaElement = schemaElement;
    //    }

    // ── Leia's Message Is Not In The File Menu ────────────────────────────────────
    // Leia's hologram is urgent and personal — it does not belong in a file cabinet
    // or a "most recently used" list for everyone to browse.
    // We return false here so Eclipse never adds schema browser sessions to the
    // "Open Recent" list, which would be confusing and meaningless.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns false because schema browser sessions are transient; they
     * should not appear in Eclipse's "File ▸ Open Recent" list.
     *
     * <p>For example — Leia's message is not filed:</p>
     * <pre>
     *   input.exists(); // false — never shows in MRU
     * </pre>
     *
     * @return false always
     */
    public boolean exists()
    {
        return false;
    }


    // ── R2 Projects The Hologram ──────────────────────────────────────────────────
    // R2's projector head spins up and a blue glow appears above him — the visual
    // icon that marks this as a schema browser session.
    // We return the schema browser editor icon descriptor so Eclipse can render
    // the correct tab image.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the schema browser editor tab icon.
     * Eclipse uses this to render the tab image in the editor area.
     *
     * <p>For example — R2 projects a blue holographic glow:</p>
     * <pre>
     *   ImageDescriptor icon = input.getImageDescriptor();
     *   // Eclipse uses it to paint the "Schema Browser" editor tab
     * </pre>
     *
     * @return the schema browser image descriptor; never null
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_BROWSER_SCHEMABROWSEREDITOR );
    }


    // ── Leia Announces Her Identity ───────────────────────────────────────────────
    // "I am Princess Leia Organa of Alderaan" — the hologram introduces itself
    // before delivering its content.
    // We return the localised display name for the schema browser editor tab.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name shown in the editor tab and window title.
     * We delegate to the message bundle so the name is translatable.
     *
     * <p>For example — Leia states her name:</p>
     * <pre>
     *   input.getName(); // "Schema Browser"
     * </pre>
     *
     * @return the localised name for this editor input
     */
    @Override
    public String getName()
    {
        return Messages.getString( "SchemaBrowserInput.SchemaBrowser" ); //$NON-NLS-1$
    }


    // ── R2 Cannot Be Saved To A File ─────────────────────────────────────────────
    // Leia's hologram lives inside R2 — you cannot export it to a disk file or
    // restore it from one; it just replays on demand.
    // We return null here because schema browser state is not persistable across
    // Eclipse restarts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns null because schema browser sessions are not persistable;
     * there is no meaningful way to restore a schema navigation state across
     * Eclipse restarts.
     *
     * <p>For example — R2 has no save slot:</p>
     * <pre>
     *   input.getPersistable(); // null
     * </pre>
     *
     * @return null always
     */
    @Override
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── R2 Offers No Verbal Summary ───────────────────────────────────────────────
    // R2's hologram speaks for itself — there is no narrated summary above the
    // projection.  We return an empty string so Eclipse shows no tooltip text
    // over the tab, keeping the UI clean.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty string for the editor's tooltip text.
     * A schema browser session has no useful one-line summary that would fit in a
     * tooltip, so we return blank.
     *
     * @return an empty string
     */
    @Override
    public String getToolTipText()
    {
        return ""; //$NON-NLS-1$
    }


    // ── R2 Cannot Be Adapted To Another Droid ─────────────────────────────────────
    // Ask R2 to be a protocol droid and he bleeps in refusal — he is what he is.
    // We return null for all adapter requests because this input has no extended
    // capabilities to expose.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns null for all adapter requests; this input does not expose any
     * additional interfaces beyond {@link IEditorInput}.
     *
     * @param adapter  the requested adapter class
     * @return         null always
     */
    @Override
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── R2 Replays Which Ship Leia Was On ─────────────────────────────────────────
    // When asked "where did this message come from?" R2 reports the connection —
    // which server the schema was loaded from.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection this input was created for.
     * The schema browser uses this to know which server's schema to display
     * and which connection the toolbar dropdown should highlight.
     *
     * @return the browser connection, or null if no connection is set
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── R2 Reveals Which Part Of The Plans Leia Sent ──────────────────────────────
    // Deep in his memory R2 holds not just the full plans but which section Leia
    // specifically highlighted as urgent.
    // We return the schema element that was selected when this input was created.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema element this input points at, or null if the input
     * was created without a specific selection (e.g. just to open the browser).
     * The schema browser uses this to scroll the list and update the detail pane.
     *
     * @return the schema element, or null
     */
    public AbstractSchemaObject getSchemaElement()
    {
        return schemaElement;
    }


    // ── Every Hologram Looks The Same To The Docking Bay ──────────────────────────
    // The Death Star's docking bay computer sees all incoming holograms and hashes
    // them identically — it only cares that they are holograms, not their content.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code consistent with the one-instance-hack equals logic.
     * We hash the tooltip text (always empty) so all SchemaBrowserInput instances
     * hash to the same bucket, which pairs with the always-true equals when the
     * hack is enabled.
     *
     * @return a stable hash code
     */
    @Override
    public int hashCode()
    {
        return getToolTipText().hashCode();
    }


    // ── One Hologram Or Many? Depends Who Is Asking ───────────────────────────────
    // In the docking bay all holograms are "the same" — only one gets through.
    // But in the navigation history each one is unique so you can go back to a
    // specific moment.  The oneInstanceHackEnabled flag controls which regime
    // we are in.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Compares this input with another, with behaviour controlled by
     * {@code oneInstanceHackEnabled}.
     * When the hack is on (the default), any two SchemaBrowserInput instances are
     * considered equal so Eclipse reuses the single open schema browser tab.
     * When the hack is off (during navigation history operations), we compare
     * connection and schema element for true equality.
     *
     * <p>For example — the docking bay admits only one hologram at a time:</p>
     * <pre>
     *   enableOneInstanceHack(true);
     *   input1.equals(input2); // true — reuses existing tab
     *
     *   enableOneInstanceHack(false);
     *   input1.equals(input2); // true only if same connection and element
     * </pre>
     *
     * @param obj  the object to compare with
     * @return     true if the inputs are considered equal under the current hack setting
     */
    @Override
    public boolean equals( Object obj )
    {

        boolean equal;

        if ( oneInstanceHackEnabled )
        {
            equal = ( obj instanceof SchemaBrowserInput );
        }
        else
        {
            if ( obj instanceof SchemaBrowserInput )
            {
                SchemaBrowserInput other = ( SchemaBrowserInput ) obj;
                if ( this.connection == null && other.connection == null )
                {
                    return true;
                }
                else if ( this.connection == null || other.connection == null )
                {
                    return false;
                }
                else if ( !this.connection.equals( other.connection ) )
                {
                    return false;
                }
                else if ( this.schemaElement == null && other.schemaElement == null )
                {
                    return true;
                }
                else if ( this.schemaElement == null || other.schemaElement == null )
                {
                    return false;
                }
                else
                {
                    equal = other.schemaElement.equals( this.schemaElement );
                }
            }
            else
            {
                equal = false;
            }
        }

        return equal;
    }


    // ── The Docking Bay Opens Or Closes Its Gates ─────────────────────────────────
    // When the Empire wants to let all ships through it opens the gate; when it
    // needs to distinguish each vessel it checks IDs carefully.
    // We toggle the one-instance hack here so navigation history can temporarily
    // compare inputs for true equality.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the one-instance-hack that makes all SchemaBrowserInput
     * instances compare equal.
     * We need this hack on so Eclipse only ever opens one schema browser tab.
     * We turn it off temporarily when the navigation history is recording a new
     * entry so it can tell the old input from the new one.
     *
     * <p>For example — the gate opens then closes:</p>
     * <pre>
     *   enableOneInstanceHack(false); // let navigation history distinguish inputs
     *   firePropertyChange(PROP_INPUT);
     *   getSite().getPage().getNavigationHistory().markLocation(this);
     *   enableOneInstanceHack(true);  // back to single-instance mode
     * </pre>
     *
     * @param b  true to enable the hack (all inputs equal), false to disable it
     */
    public static void enableOneInstanceHack( boolean b )
    {
        oneInstanceHackEnabled = b;
    }

}
