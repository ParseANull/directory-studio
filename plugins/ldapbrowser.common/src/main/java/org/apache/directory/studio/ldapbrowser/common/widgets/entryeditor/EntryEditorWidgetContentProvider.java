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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// -- CLASS: EntryEditorWidgetContentProvider -- C-3PO TRANSLATES R2's BINARY STREAM --
// In the hold of the Millennium Falcon, C-3PO listens to R2-D2's binary chirps and
// translates them one by one into plain-language entries the crew can actually read.
// This class does the same job: it takes raw LDAP model objects (IEntry / AttributeHierarchy)
// and hands JFace's TreeViewer a flat array of IValue (or IAttribute when folded) rows.
// ---------------------------------------------------------------------------------
/**
 * Supplies content to the entry editor's tree table widget.
 * JFace calls us whenever it needs to know what rows to show for a given LDAP entry
 * or attribute hierarchy -- we crack open the model objects and return the individual
 * {@link IValue} items (or a folded {@link IAttribute} group when there are too many values).
 * Think of this class as C-3PO in the Falcon's hold: R2 feeds in raw binary, and 3PO
 * hands the crew a clean, human-readable list they can actually act on.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetContentProvider implements ITreeContentProvider
{

    /** The preferences. */
    protected EntryEditorWidgetPreferences preferences;

    /** The main widget. */
    protected EntryEditorWidget mainWidget;


    // -- C-3PO REPORTS FOR DUTY IN THE FALCON'S HOLD ---------------------------
    // Before any translating can start, C-3PO needs two things: a reference manual
    // of how to format the data (preferences) and an understanding of where to
    // display the results (mainWidget -- the cockpit displays, so to speak).
    // We store both so every future call to getElements() can use them.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new instance and wires up the two collaborators we need.
     * {@code preferences} tells us things like whether to fold multi-value attributes,
     * and {@code mainWidget} is the surrounding UI shell we update with DN info and
     * enabled/disabled state when the input changes.
     *
     * <p>For example -- C-3PO steps into the hold:</p>
     * <pre>
     *   C-3PO: "R2, I have your manual right here. And I can see the display screen.
     *           Ready to begin translations on your command."
     * </pre>
     *
     * @param preferences  the display preferences (folding threshold, sort settings, etc.)
     * @param mainWidget   the parent widget shell we update with status info
     */
    public EntryEditorWidgetContentProvider( EntryEditorWidgetPreferences preferences, EntryEditorWidget mainWidget )
    {
        this.preferences = preferences;
        this.mainWidget = mainWidget;
    }


    // -- NEW MESSAGE COMING IN OVER THE COMLINK ---------------------------------
    // C-3PO sees that R2 has switched to a completely different data channel --
    // the old message is gone, a new one has arrived.
    // We update the info-text label at the top of the entry editor to show the
    // new entry's DN (or a "nothing selected" placeholder), and we enable or
    // disable the quick-filter and tree depending on whether real data arrived.
    // ---------------------------------------------------------------------------------
    /**
     * Called by JFace whenever the viewer's input object is swapped for a new one.
     * We use this hook to update the DN label at the top of the entry editor and to
     * grey out (or re-enable) the quick-filter bar and tree when there is no selection.
     *
     * <p>For example -- C-3PO notices the channel has changed:</p>
     * <pre>
     *   R2 switches feeds; C-3PO glances at the new header.
     *   If it carries a valid DN, he reads it aloud to the crew.
     *   If not, he says "No entry selected" and dims the controls.
     * </pre>
     *
     * @param viewer      the JFace viewer that owns us -- we don't use it directly here
     * @param oldInput    the previous input object (we ignore it)
     * @param newInput    the new input: an {@link IEntry}, an {@link AttributeHierarchy}, or null
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        if ( mainWidget != null )
        {
            String dn = ""; //$NON-NLS-1$
            boolean enabled = true;

            if ( newInput instanceof IEntry )
            {
                IEntry entry = ( IEntry ) newInput;
                dn = Messages.getString( "EntryEditorWidgetContentProvider.DNLabel" ) + entry.getDn().getName(); //$NON-NLS-1$
            }
            else if ( newInput instanceof AttributeHierarchy )
            {
                AttributeHierarchy ah = ( AttributeHierarchy ) newInput;
                dn = Messages.getString( "EntryEditorWidgetContentProvider.DNLabel" ) + ah.getAttribute().getEntry().getDn().getName(); //$NON-NLS-1$
            }
            else
            {
                dn = Messages.getString( "EntryEditorWidgetContentProvider.NoEntrySelected" ); //$NON-NLS-1$
                enabled = false;
            }

            if ( ( mainWidget.getInfoText() != null ) && !mainWidget.getInfoText().isDisposed() )
            {
                mainWidget.getInfoText().setText( dn );
            }

            if ( mainWidget.getQuickFilterWidget() != null )
            {
                mainWidget.getQuickFilterWidget().setEnabled( enabled );
            }

            if ( mainWidget.getViewer() != null && !mainWidget.getViewer().getTree().isDisposed() )
            {
                mainWidget.getViewer().getTree().setEnabled( enabled );
            }
        }
    }


    // -- C-3PO POWERS DOWN AT MISSION END ---------------------------------------
    // After the Battle of Yavin the debrief is over; C-3PO stows his translation
    // notes and hands back any resources he was holding.
    // We null out our references so the garbage collector can reclaim memory --
    // not doing this can cause stale UI references to hang around indefinitely.
    // ---------------------------------------------------------------------------------
    /**
     * Releases all references held by this content provider so they can be garbage-collected.
     * JFace calls this when the viewer is torn down -- after this point we must not
     * access the widget or preferences.
     *
     * <p>For example -- C-3PO signs off:</p>
     * <pre>
     *   C-3PO: "Mission complete. Powering down translation subroutines.
     *           All references cleared."
     * </pre>
     */
    public void dispose()
    {
        preferences = null;
        mainWidget = null;
    }


    // -- C-3PO READS OUT THE FULL MANIFEST FOR AN ENTRY -------------------------
    // Han has just asked: "3PO, what's in this entry?" C-3PO checks whether
    // the entry's attributes are loaded yet; if not, he kicks off the loading job
    // and returns an empty list ("stand by...").  Once loaded, he calls getValues()
    // to flatten the attribute/value tree into a table-friendly array.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the top-level rows to display in the entry editor for a given input object.
     * This is the primary workhorse of the content provider.  If the entry hasn't loaded
     * its attributes from the LDAP server yet, we kick off a background fetch and return
     * an empty array; JFace will call us again once the job completes and refreshes the viewer.
     *
     * <p>For example -- C-3PO reads the manifest:</p>
     * <pre>
     *   Han: "What's in dc=example,dc=com?"
     *   C-3PO (entry not loaded): "Stand by -- retrieving from server."
     *   C-3PO (entry loaded): "Certainly. cn=John, mail=john@co.com, objectClass=inetOrgPerson ..."
     * </pre>
     *
     * @param inputElement  an {@link IEntry} or {@link AttributeHierarchy}; anything else gets an empty array
     * @return              array of {@link IValue} objects (or {@link IAttribute} groups when folded),
     *                      or an empty array if nothing is loaded yet
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof IEntry )
        {
            IEntry entry = ( IEntry ) inputElement;

            if ( !entry.isAttributesInitialized() )
            {
                InitializeAttributesRunnable runnable = new InitializeAttributesRunnable( entry );
                StudioBrowserJob job = new StudioBrowserJob( runnable );
                job.execute();

                return new Object[0];
            }
            else
            {
                IAttribute[] attributes = entry.getAttributes();
                Object[] values = getValues( attributes );

                return values;
            }
        }
        else if ( inputElement instanceof AttributeHierarchy )
        {
            AttributeHierarchy ah = ( AttributeHierarchy ) inputElement;
            IAttribute[] attributes = ah.getAttributes();
            Object[] values = getValues( attributes );

            return values;
        }
        else
        {
            return new Object[0];
        }
    }


    // -- C-3PO BREAKS DOWN R2's RAW DATA STREAM ---------------------------------
    // R2 hands C-3PO a bundle of attribute packets. For most attributes, 3PO unpacks
    // each individual value into its own row. But when an attribute has so many values
    // that the table would become unreadable, he hands back just the attribute header
    // itself -- a folded summary row -- and lets the user expand it later.
    // ---------------------------------------------------------------------------------
    /**
     * Converts an array of {@link IAttribute} objects into a flat list of display rows.
     * Each attribute normally yields one row per value.  When folding is enabled and the
     * number of values exceeds the threshold set in preferences, the entire attribute is
     * returned as a single collapsed row instead -- the user can expand it in the tree.
     *
     * <p>For example -- C-3PO handles a verbose attribute:</p>
     * <pre>
     *   Attribute "memberOf" has 500 values. Threshold is 50.
     *   C-3PO: "Far too many to list individually. I'll show the header only.
     *            The crew can expand it themselves if they wish."
     * </pre>
     *
     * @param attributes  the attributes whose values we want to enumerate
     * @return            a mixed array of {@link IValue} and/or {@link IAttribute} objects
     *                    ready for the table to render
     */
    private Object[] getValues( IAttribute[] attributes )
    {
        List<Object> valueList = new ArrayList<Object>();

        if ( attributes != null )
        {
            for ( IAttribute attribute : attributes )
            {
                IValue[] values = attribute.getValues();

                if ((  preferences == null ) || !preferences.isUseFolding()
                    || ( values.length <= preferences.getFoldingThreshold() ) )
                {
                    for ( IValue value : values )
                    {
                        valueList.add( value );
                    }
                }
                else
                {
                    // if folding threshold is exceeded then return the attribute itself
                    valueList.add( attribute );
                }
            }
        }

        return valueList.toArray();
    }


    // -- C-3PO LISTS THE VALUES UNDER A FOLDED ATTRIBUTE ------------------------
    // When the user clicks the expand arrow on a folded attribute row, JFace asks:
    // "what are the children of this node?"  C-3PO looks inside the attribute packet
    // and hands back all the individual value rows tucked inside it.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the child rows of a folded {@link IAttribute} node in the tree.
     * JFace calls this when the user expands a collapsed attribute group.
     * We simply return all values the attribute contains so they appear as child rows.
     *
     * <p>For example -- C-3PO expands the folded packet:</p>
     * <pre>
     *   User clicks ▶ next to "memberOf (500 values)".
     *   C-3PO: "Certainly. Here are all 500 group DNs, in order."
     * </pre>
     *
     * @param parentElement  expected to be an {@link IAttribute}; anything else returns null
     * @return               array of {@link IValue} children, or null if the element has none
     */
    public Object[] getChildren( Object parentElement )
    {
        if ( parentElement instanceof IAttribute )
        {
            IAttribute attribute = ( IAttribute ) parentElement;
            IValue[] values = attribute.getValues();

            return values;
        }

        return null;
    }


    // -- C-3PO TRACES A VALUE BACK TO ITS ATTRIBUTE PARENT ---------------------
    // R2 points at a single value row in the table and asks "whose packet did this
    // come from?"  C-3PO simply looks at the value's back-reference and returns
    // the parent attribute -- so JFace can navigate the tree upward if it needs to.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the parent {@link IAttribute} of a given {@link IValue} element.
     * JFace uses this for tree navigation -- e.g., knowing which attribute to
     * collapse when the user closes a folded group.
     *
     * <p>For example -- C-3PO traces the chain of custody:</p>
     * <pre>
     *   R2 holds up value "john@example.com".
     *   C-3PO: "That belongs to the 'mail' attribute. Here is its parent record."
     * </pre>
     *
     * @param element  an {@link IValue} whose parent attribute we want
     * @return         the owning {@link IAttribute}, or null if element is not an IValue
     */
    public Object getParent( Object element )
    {
        if ( element instanceof IValue )
        {
            return ( ( IValue ) element ).getAttribute();
        }

        return null;
    }


    // -- C-3PO CHECKS WHETHER A NODE NEEDS AN EXPAND ARROW ---------------------
    // Before JFace draws the tree row it asks: "does this thing have children?"
    // C-3PO answers instantly -- only attribute nodes (the folded kind) can be
    // expanded; individual values are always leaf nodes with no children.
    // ---------------------------------------------------------------------------------
    /**
     * Tells JFace whether a tree element has children and therefore needs an expand arrow.
     * Only {@link IAttribute} instances (used for folded multi-value groups) are expandable;
     * {@link IValue} objects are always leaf nodes.
     *
     * <p>For example -- C-3PO scans the manifest entry:</p>
     * <pre>
     *   JFace: "Does 'memberOf (500 values)' have children?"
     *   C-3PO: "Yes -- it is an IAttribute group. Draw the expand arrow."
     *   JFace: "Does 'cn=John Doe' have children?"
     *   C-3PO: "No -- that is a plain value. No arrow needed."
     * </pre>
     *
     * @param element  the tree element to inspect
     * @return         {@code true} if element is an {@link IAttribute} (i.e., a folded group)
     */
    public boolean hasChildren( Object element )
    {
        return ( element instanceof IAttribute );
    }
}
