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
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;


// ── CLASS: SchemaBrowserNavigationLocation — R2 At The Death Star Terminal ────
// R2-D2 plugs into the Death Star's computer, records the exact coordinates of
// every system he has accessed, and can replay any of them on command.  When
// the crew needs to go back to "that power coupling on level 5," R2 has the
// exact address stored.  This class does the same thing: it records exactly
// which schema element was showing in the schema browser at a given moment,
// so Eclipse's back/forward navigation can return the editor to that precise state.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single entry in the Eclipse workbench navigation history for the
 * schema browser editor.
 * Each time the user clicks on a different schema element the browser creates one
 * of these and hands it to Eclipse, which adds it to the back/forward stack.
 * We serialize connection ID and schema element OID into an {@link IMemento} so
 * the location survives an Eclipse restart.
 * Think of this class as R2 recording a terminal address: he notes the connection,
 * the element type, and the OID so he can plug back in and restore exactly that view.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaBrowserNavigationLocation extends NavigationLocation
{

    // ── R2 Plugs Into The Terminal ─────────────────────────────────────────────────
    // R2 finds an open port in the Death Star corridor and inserts his probe — the
    // connection is made, the current terminal address is noted.
    // We call super() with the schema browser reference so Eclipse knows which
    // editor this location belongs to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation location entry for the given schema browser instance.
     * Eclipse associates this location with that specific editor part so that
     * pressing Back navigates within the correct window.
     *
     * <p>For example — R2 connects to the terminal:</p>
     * <pre>
     *   SchemaBrowserNavigationLocation loc =
     *       new SchemaBrowserNavigationLocation(schemaBrowser);
     *   // Eclipse now tracks this location in the history stack
     * </pre>
     *
     * @param schemaBrowser  the schema browser editor this location belongs to
     */
    SchemaBrowserNavigationLocation( SchemaBrowser schemaBrowser )
    {
        super( schemaBrowser );
    }


    // ── R2 Reads The Terminal Label ───────────────────────────────────────────────
    // R2's readout shows "Detention Block AA-23" — a human-readable name for the
    // coordinate so the crew knows where they are in the history.
    // We build a human-readable label like "Object Class: person" from the current
    // schema element so the navigation dropdown is useful.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable label that Eclipse shows for this entry in the
     * navigation history dropdown (e.g. "Object Class: person").
     * We prefix the element type name so the user can quickly identify which kind
     * of schema object this history entry represents.
     *
     * <p>For example — R2 reports the location label:</p>
     * <pre>
     *   getText(); // "Object Class: inetOrgPerson"
     *   getText(); // "Attribute Type: cn"
     *   getText(); // "Syntax: Integer"
     * </pre>
     *
     * @return a descriptive label for this navigation location
     */
    @Override
    public String getText()
    {
        AbstractSchemaObject schemaElement = getSchemaElement();
        if ( schemaElement != null )
        {
            if ( schemaElement instanceof ObjectClass )
            {

                return Messages.getString( "SchemaBrowserNavigationLocation.ObjectClass" ) + SchemaUtils.toString( schemaElement ); //$NON-NLS-1$
            }
            else if ( schemaElement instanceof AttributeType )
            {
                return Messages.getString( "SchemaBrowserNavigationLocation.AttributeType" ) + SchemaUtils.toString( schemaElement ); //$NON-NLS-1$
            }
            else if ( schemaElement instanceof LdapSyntax )
            {
                return Messages.getString( "SchemaBrowserNavigationLocation.Syntax" ) + SchemaUtils.toString( schemaElement ); //$NON-NLS-1$
            }
            else if ( schemaElement instanceof MatchingRule )
            {
                return Messages.getString( "SchemaBrowserNavigationLocation.MatchingRule" ) + SchemaUtils.toString( schemaElement ); //$NON-NLS-1$
            }
            else if ( schemaElement instanceof MatchingRuleUse )
            {
                return Messages.getString( "SchemaBrowserNavigationLocation.MatchingRuleUse" ) + SchemaUtils.toString( schemaElement ); //$NON-NLS-1$
            }
            else
            {
                return SchemaUtils.toString( schemaElement );
            }
        }
        else
        {
            return super.getText();
        }
    }


    // ── R2 Saves The Terminal Address To His Memory Bank ─────────────────────────
    // R2 captures the exact coordinates of this terminal — connection ID, corridor
    // type, and room number — so he can find it again even if the Death Star's
    // layout changes between visits.
    // We write the connection ID, schema element class name, and OID into the
    // memento so Eclipse can restore this location after a restart.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists this navigation location to the given memento so Eclipse can
     * restore the schema browser to this exact state after a workspace restart.
     * We save the connection ID, the Java class name of the schema element, and
     * the element's OID — enough to reconstruct the full state in
     * {@link #restoreState(IMemento)}.
     *
     * <p>For example — R2 writes the address to memory:</p>
     * <pre>
     *   memento.getString("CONNECTION")       // "connection-42"
     *   memento.getString("SCHEMAELEMENTYPE") // "org...ObjectClass"
     *   memento.getString("SCHEMAELEMENTOID") // "2.5.6.0"
     * </pre>
     *
     * @param memento  the memento to write into; must not be null
     */
    @Override
    public void saveState( IMemento memento )
    {
        IBrowserConnection connection = getConnection();
        AbstractSchemaObject schemaElement = getSchemaElement();
        memento.putString( "CONNECTION", connection.getConnection().getId() ); //$NON-NLS-1$
        memento.putString( "SCHEMAELEMENTYPE", schemaElement.getClass().getName() ); //$NON-NLS-1$
        memento.putString( "SCHEMAELEMENTOID", schemaElement.getOid() ); //$NON-NLS-1$
    }


    // ── R2 Navigates Back To The Saved Address ────────────────────────────────────
    // R2 reads the stored coordinates and plugs back into the terminal at exactly
    // that location, retrieving the data that was there before.
    // We reconstruct the connection and schema element from the memento and wrap
    // them in a new SchemaBrowserInput, ready to be replayed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the schema browser input from the memento saved by
     * {@link #saveState(IMemento)}.
     * We look up the connection by stored ID, determine the schema element type
     * from the stored class name, and fetch the element by OID from the schema.
     *
     * <p>For example — R2 replays the saved address:</p>
     * <pre>
     *   restoreState(memento);
     *   // connection and schemaElement reconstructed
     *   // super.setInput(new SchemaBrowserInput(connection, schemaElement))
     * </pre>
     *
     * @param memento  the memento produced by a prior {@link #saveState(IMemento)} call
     */
    @Override
    public void restoreState( IMemento memento )
    {
        IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager().getBrowserConnectionById(
            memento.getString( "CONNECTION" ) ); //$NON-NLS-1$
        String schemaElementType = memento.getString( "SCHEMAELEMENTYPE" ); //$NON-NLS-1$
        String schemaElementOid = memento.getString( "SCHEMAELEMENTOID" ); //$NON-NLS-1$
        AbstractSchemaObject schemaElement = null;
        if ( ObjectClass.class.getName().equals( schemaElementType ) )
        {
            schemaElement = connection.getSchema().getObjectClassDescription( schemaElementOid );
        }
        else if ( AttributeType.class.getName().equals( schemaElementType ) )
        {
            schemaElement = connection.getSchema().getAttributeTypeDescription( schemaElementOid );
        }
        else if ( LdapSyntax.class.getName().equals( schemaElementType ) )
        {
            schemaElement = connection.getSchema().getLdapSyntaxDescription( schemaElementOid );
        }
        else if ( MatchingRule.class.getName().equals( schemaElementType ) )
        {
            schemaElement = connection.getSchema().getMatchingRuleDescription( schemaElementOid );
        }
        else if ( MatchingRuleUse.class.getName().equals( schemaElementType ) )
        {
            schemaElement = connection.getSchema().getMatchingRuleUseDescription( schemaElementOid );
        }

        super.setInput( new SchemaBrowserInput( connection, schemaElement ) );
    }


    // ── R2 Replays The Corridor Sequence ─────────────────────────────────────────
    // R2 signals the schema browser to navigate back to the exact room he noted —
    // the schema browser editor receives the stored input and updates its display.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Applies this navigation location to the schema browser editor, restoring
     * the view to the connection and schema element that were recorded.
     * We only act if the editor part is actually a SchemaBrowser and the stored
     * input has both a connection and a schema element.
     *
     * <p>For example — R2 guides the crew back to the right corridor:</p>
     * <pre>
     *   restoreLocation();
     *   // schemaBrowser.setInput(sbi) called if everything is non-null
     * </pre>
     */
    @Override
    public void restoreLocation()
    {
        IEditorPart editorPart = getEditorPart();

        if ( editorPart instanceof SchemaBrowser )
        {
            SchemaBrowser schemaBrowser = ( SchemaBrowser ) editorPart;
            Object input = getInput();

            if ( input instanceof SchemaBrowserInput )
            {
                SchemaBrowserInput sbi = ( SchemaBrowserInput ) input;

                if ( sbi.getConnection() != null && sbi.getSchemaElement() != null )
                {
                    schemaBrowser.setInput( sbi );
                }
            }
        }
    }


    // ── R2 Checks Whether Two Addresses Are The Same Room ─────────────────────────
    // R2 compares two terminal coordinates — if they point to the same room there
    // is no need to record a duplicate entry in the history.
    // We merge if the schema elements are identical so the back/forward list stays
    // clean and doesn't fill up with repeated visits to the same element.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if this location represents the same schema element as
     * {@code currentLocation}, so Eclipse can avoid adding a duplicate to the
     * navigation stack.
     * We consider two locations equal when both are SchemaBrowserNavigationLocation
     * instances pointing at the same schema element (or both have null elements).
     *
     * <p>For example — R2 detects a duplicate address:</p>
     * <pre>
     *   loc1.mergeInto(loc2); // true if both point at "inetOrgPerson"
     * </pre>
     *
     * @param currentLocation  the location already in the stack to compare against
     * @return                 true if this location can be merged (i.e. is a duplicate)
     */
    @Override
    public boolean mergeInto( INavigationLocation currentLocation )
    {
        if ( currentLocation == null )
        {
            return false;
        }

        if ( getClass() != currentLocation.getClass() )
        {
            return false;
        }

        SchemaBrowserNavigationLocation location = ( SchemaBrowserNavigationLocation ) currentLocation;
        AbstractSchemaObject other = location.getSchemaElement();
        AbstractSchemaObject element = getSchemaElement();

        if ( other == null && element == null )
        {
            return true;
        }
        else if ( other == null || element == null )
        {
            return false;
        }
        else
        {
            return element.equals( other );
        }
    }


    // ── R2 Acknowledges An Update Signal ─────────────────────────────────────────
    // The Death Star computer pings R2 to refresh his cache; R2 acknowledges but
    // has nothing to do because this location is already fully captured.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the navigation location should update itself.
     * We have nothing to do here because the location is immutable once created.
     */
    @Override
    public void update()
    {
    }


    // ── R2 Retrieves The Schema Element From His Memory ───────────────────────────
    // R2 scans his memory for the schema element he noted when this location was
    // created, returning it so other methods can inspect or display it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the schema element from the stored input.
     * We unwrap the SchemaBrowserInput to get at the specific schema object
     * that was selected when this location was created.
     *
     * @return the schema element, or null if no element was recorded
     */
    private AbstractSchemaObject getSchemaElement()
    {
        Object editorInput = getInput();

        if ( editorInput instanceof SchemaBrowserInput )
        {
            SchemaBrowserInput schemaBrowserInput = ( SchemaBrowserInput ) editorInput;
            AbstractSchemaObject schemaElement = schemaBrowserInput.getSchemaElement();

            if ( schemaElement != null )
            {
                return schemaElement;
            }
        }

        return null;
    }


    // ── R2 Retrieves The Connection From His Memory ───────────────────────────────
    // R2 notes which port he was connected to when he recorded this address, so he
    // can re-establish the same connection when navigating back.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the browser connection from the stored input.
     * We need this when restoring or saving state so we know which LDAP server
     * the recorded schema element belongs to.
     *
     * @return the browser connection, or null if none was recorded
     */
    private IBrowserConnection getConnection()
    {
        Object editorInput = getInput();

        if ( editorInput instanceof SchemaBrowserInput )
        {
            SchemaBrowserInput schemaBrowserInput = ( SchemaBrowserInput ) editorInput;

            return schemaBrowserInput.getConnection();
        }

        return null;
    }


    // ── R2 Reports His Current Terminal Address ───────────────────────────────────
    // Ask R2 where he is and he bleeps back the coordinates as a string — useful
    // for debugging and logging.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a brief string representation of this location, mainly useful for
     * debugging.
     *
     * @return the schema element as a string, or "" if none is set
     */
    @Override
    public String toString()
    {
        return "" + getSchemaElement(); //$NON-NLS-1$
    }

}
