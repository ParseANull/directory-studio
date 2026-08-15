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

package org.apache.directory.studio.ldapbrowser.core.utils;


import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueMultiModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueRenamedEvent;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;


// ── CLASS: CompoundModification — HAN EXECUTING MULTI-STEP MILLENNIUM FALCON OPS
// Han never just flips one switch: when he modifies the Falcon he suspends
// all alerts, makes all the changes in sequence, then re-enables alerts and
// fires exactly one event.  CompoundModification does the same: it wraps
// multi-step attribute/value mutations in a single suspended-event window
// so the UI sees exactly one notification per logical operation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Performs compound (multi-step, single-event) operations on the LDAP entry
 * model.  All mutating methods suspend event firing during the inner loop and
 * fire exactly one summary event at the end.
 *
 * <p>Think of this as Han executing a complex Millennium Falcon manoeuvre —
 * all controls adjusted in sequence, one final status report sent.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CompoundModification
{

    // ── Han Moves Cargo From One Hold To Another With A Single Status Call ───────
    // Han suspends the ship's alert system, deletes each old value from its
    // attribute, creates the target attribute if it does not exist, and adds
    // a new Value with the same raw content under the new description.
    // One ValueRenamedEvent is fired after all values are moved — Han shoots once.
    /**
     * Renames values by moving them from the old attribute to a new attribute.
     * Event firing is suppressed during the inner loop; one {@link ValueRenamedEvent}
     * is fired at the end.
     *
     * @param oldValues the old values to rename; must not be null or empty
     * @param newAttributeDescription the new attribute description; must not be blank
     * @throws IllegalArgumentException if oldValues is empty or newAttributeDescription is blank
     */
    public void renameValues( IValue[] oldValues, String newAttributeDescription )
    {
        if ( ArrayUtils.isEmpty( oldValues ) )
        {
            throw new IllegalArgumentException( "Expected non-null and non-empty values array." ); //$NON-NLS-1$
        }
        if ( StringUtils.isEmpty( newAttributeDescription ) )
        {
            throw new IllegalArgumentException( "Expected non-null and non-empty attribute description." ); //$NON-NLS-1$
        }

        if ( newAttributeDescription != null && !"".equals( newAttributeDescription ) //$NON-NLS-1$
            && !newAttributeDescription.equals( oldValues[0].getAttribute().getDescription() ) )
        {
            ValueRenamedEvent event = null;
            try
            {
                EventRegistry.suspendEventFiringInCurrentThread();
                for ( IValue oldValue : oldValues )
                {
                    if ( !newAttributeDescription.equals( oldValue.getAttribute().getDescription() ) )
                    {
                        IAttribute oldAttribute = oldValue.getAttribute();
                        IEntry entry = oldAttribute.getEntry();
                        IValue newValue = null;

                        // delete old value
                        oldAttribute.deleteValue( oldValue );
                        if ( oldAttribute.getValueSize() == 0 )
                        {
                            entry.deleteAttribute( oldAttribute );
                        }

                        // add new value
                        IAttribute attribute = entry.getAttribute( newAttributeDescription );
                        if ( attribute == null )
                        {
                            attribute = new Attribute( entry, newAttributeDescription );
                            entry.addAttribute( attribute );
                        }
                        newValue = new Value( attribute, oldValue.getRawValue() );
                        attribute.addValue( newValue );

                        // prepare event
                        if ( event == null )
                        {
                            event = new ValueRenamedEvent( entry.getBrowserConnection(), entry, oldValue, newValue );
                        }
                    }
                }
            }
            finally
            {
                EventRegistry.resumeEventFiringInCurrentThread();
            }

            // fire events
            EventRegistry.fireEntryUpdated( event, this );
        }
    }


    // ── Han Jettisons Multiple Cargo Items In A Single Airlock Cycle ─────────────
    // Han suspends the ship's alert system, deletes each value from its attribute,
    // and removes the attribute from the entry if it becomes empty.
    // One ValueDeletedEvent is fired after all deletions — one announcement.
    // Han shoots first: null or empty values collection triggers IllegalArgument.
    /**
     * Deletes the given values, removing empty attributes from the entry.
     * Event firing is suppressed during the inner loop; one {@link ValueDeletedEvent}
     * is fired at the end.
     *
     * @param values the values to delete; must not be null or empty
     * @throws IllegalArgumentException if values is null or empty
     */
    public void deleteValues( Collection<IValue> values )
    {
        if ( CollectionUtils.isEmpty( values ) )
        {
            throw new IllegalArgumentException( "Expected non-null and non-empty values collection." ); //$NON-NLS-1$
        }

        ValueDeletedEvent event = null;
        try
        {
            EventRegistry.suspendEventFiringInCurrentThread();
            for ( IValue value : values )
            {
                IAttribute attribute = value.getAttribute();
                IEntry entry = attribute.getEntry();

                attribute.deleteValue( value );
                if ( event == null )
                {
                    event = new ValueDeletedEvent( entry.getBrowserConnection(), entry, attribute, value );
                }

                if ( attribute.getValueSize() == 0 )
                {
                    attribute.getEntry().deleteAttribute( attribute );
                }
            }
        }
        finally
        {
            EventRegistry.resumeEventFiringInCurrentThread();
        }

        // fire event
        EventRegistry.fireEntryUpdated( event, this );
    }


    // ── Han Swaps A Cargo Item For A Different One In Place ──────────────────────
    // If the new raw value differs from the old, Han makes the swap.
    // For an empty placeholder slot, he deletes the empty value silently and adds
    // the new one (ValueAddedEvent); for a real existing value, he modifies it
    // (ValueModifiedEvent).  If the values are identical, nothing happens.
    /**
     * Modifies a value by replacing its raw content with a new raw value.
     * Fires a {@link ValueAddedEvent} if the old value was empty, or a
     * {@link ValueModifiedEvent} otherwise.  Does nothing if the new value
     * is identical to the old.
     *
     * @param oldValue the value to replace; must not be null
     * @param newRawValue the new raw value (String or byte[]); must not be null
     * @throws IllegalArgumentException if either argument is null
     */
    public void modifyValue( IValue oldValue, Object newRawValue )
    {
        if ( oldValue == null || newRawValue == null )
        {
            throw new IllegalArgumentException( "Expected non-null value." ); //$NON-NLS-1$
        }

        IAttribute attribute = oldValue.getAttribute();

        boolean modify = false;
        if ( oldValue != null && newRawValue instanceof byte[] )
        {
            byte[] newValue = ( byte[] ) newRawValue;
            if ( !Utils.equals( oldValue.getBinaryValue(), newValue ) )
            {
                modify = true;
            }
        }
        else if ( oldValue != null && newRawValue instanceof String )
        {

            String newValue = ( String ) newRawValue;
            if ( !oldValue.getStringValue().equals( newValue ) )
            {
                modify = true;
            }
        }

        if ( modify )
        {
            if ( oldValue.isEmpty() )
            {
                EventRegistry.suspendEventFiringInCurrentThread();
                attribute.deleteEmptyValue();
                EventRegistry.resumeEventFiringInCurrentThread();

                Value value = new Value( attribute, newRawValue );
                attribute.addValue( value );
            }
            else
            {
                IValue newValue = new Value( attribute, newRawValue );
                attribute.modifyValue( oldValue, newValue );
            }
        }
    }


    // ── Han Loads A Single New Cargo Item Into The Falcon ────────────────────────
    // If the named attribute does not yet exist, Han creates it silently
    // (suppressing the add-attribute event) before adding the new value.
    // One ValueAddedEvent is fired when the value is appended to the attribute.
    // Han shoots first: null entry, blank description, or null raw value all throw.
    /**
     * Creates a value for the given attribute description in the given entry.
     * Creates the attribute if it does not yet exist (suppressing that event).
     * Fires a {@link ValueAddedEvent} when the value is added.
     *
     * @param entry the target entry; must not be null
     * @param attributeDescription the attribute description; must not be blank
     * @param newRawValue the new raw value; must not be null
     * @throws IllegalArgumentException if any argument is null or blank
     */
    public void createValue( IEntry entry, String attributeDescription, Object newRawValue )
    {
        if ( entry == null )
        {
            throw new IllegalArgumentException( "Expected non-null entry." ); //$NON-NLS-1$
        }
        if ( StringUtils.isEmpty( attributeDescription ) )
        {
            throw new IllegalArgumentException( "Expected non-null and non-empty attribute description." ); //$NON-NLS-1$
        }
        if ( newRawValue == null )
        {
            throw new IllegalArgumentException( "Expected non-null value." ); //$NON-NLS-1$
        }

        IAttribute attribute = entry.getAttribute( attributeDescription );
        if ( attribute == null )
        {
            EventRegistry.suspendEventFiringInCurrentThread();
            attribute = new Attribute( entry, attributeDescription );
            entry.addAttribute( attribute );
            EventRegistry.resumeEventFiringInCurrentThread();
        }

        Value value = new Value( attribute, newRawValue );
        attribute.addValue( value );
    }


    // ── Han Loads Multiple Cargo Items Into The Falcon In One Run ────────────────
    // Han suspends the alert system, creates any missing attributes, and appends
    // all new values in a single batch.  One ValueAddedEvent is fired at the end.
    // The first value's attribute and entry are used for the event reference.
    // Han shoots first: null entry or empty values array both throw immediately.
    /**
     * Creates multiple values in the given entry.
     * Event firing is suppressed during the inner loop; one {@link ValueAddedEvent}
     * is fired at the end.
     *
     * @param entry the target entry; must not be null
     * @param values the values to add; must not be null or empty
     * @throws IllegalArgumentException if entry is null or values is empty
     */
    public void createValues( IEntry entry, IValue... values )
    {
        if ( entry == null )
        {
            throw new IllegalArgumentException( "Expected non-null entry." ); //$NON-NLS-1$
        }
        if ( ArrayUtils.isEmpty( values ) )
        {
            throw new IllegalArgumentException( "Expected non-null and non-empty values array." ); //$NON-NLS-1$
        }

        ValueAddedEvent event = null;
        EventRegistry.suspendEventFiringInCurrentThread();
        for ( IValue value : values )
        {
            String attributeDescription = value.getAttribute().getDescription();
            IAttribute attribute = entry.getAttribute( attributeDescription );
            if ( attribute == null )
            {
                attribute = new Attribute( entry, attributeDescription );
                entry.addAttribute( attribute );
            }
            Value newValue = new Value( attribute, value.getRawValue() );
            attribute.addValue( newValue );
            if ( event == null )
            {
                event = new ValueAddedEvent( entry.getBrowserConnection(), entry, attribute, newValue );
            }
        }
        EventRegistry.resumeEventFiringInCurrentThread();

        // fire event
        EventRegistry.fireEntryUpdated( event, this );
    }


    // ── Han Replaces All Cargo In One Hold With Cargo From Another ───────────────
    // Han suspends alerts, clears every attribute from the target entry, then
    // copies all attributes and values from the source entry in one pass.
    // One ValueMultiModificationEvent is fired after the replacement is complete.
    // The source parameter identifies the originator in the event system.
    /**
     * Replaces all attributes in {@code toEntry} with those from {@code fromEntry}.
     * Clears the target entry first; fires one {@link ValueMultiModificationEvent}.
     *
     * @param fromEntry the source entry
     * @param toEntry the target entry (cleared and repopulated)
     * @param source the originating object for the fired event
     */
    public void replaceAttributes( IEntry fromEntry, IEntry toEntry, Object source )
    {
        EventRegistry.suspendEventFiringInCurrentThread();
        for ( IAttribute attribute : toEntry.getAttributes() )
        {
            toEntry.deleteAttribute( attribute );
        }

        // create new attributes
        for ( IAttribute attribute : fromEntry.getAttributes() )
        {
            IAttribute newAttribute = new Attribute( toEntry, attribute.getDescription() );
            for ( IValue value : attribute.getValues() )
            {
                IValue newValue = new Value( newAttribute, value.getRawValue() );
                newAttribute.addValue( newValue );
            }
            toEntry.addAttribute( newAttribute );
        }
        EventRegistry.resumeEventFiringInCurrentThread();

        ValueMultiModificationEvent event = new ValueMultiModificationEvent( toEntry.getBrowserConnection(), toEntry );
        EventRegistry.fireEntryUpdated( event, source );
    }


    // ── R2-D2 Makes A Perfect Copy Of A Death Star Room Blueprint ────────────────
    // R2-D2 suspends events, converts the entry to an LDIF record via ModelConverter,
    // then converts it back into a fresh IEntry with the same BrowserConnection.
    // No event is fired — the clone is a silent operation.
    // A RuntimeException wraps any LdapInvalidDnException thrown during conversion.
    /**
     * Creates a deep clone of the given entry.  No event is fired.
     *
     * @param entry the entry to clone
     * @return the cloned entry
     * @throws RuntimeException if the DN of the entry is invalid
     */
    public IEntry cloneEntry( IEntry entry )
    {
        try
        {
            EventRegistry.suspendEventFiringInCurrentThread();
            IBrowserConnection browserConnection = entry.getBrowserConnection();
            LdifContentRecord record = ModelConverter.entryToLdifContentRecord( entry );
            IEntry clonedEntry = ModelConverter.ldifContentRecordToEntry( record, browserConnection );
            return clonedEntry;
        }
        catch ( LdapInvalidDnException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            EventRegistry.resumeEventFiringInCurrentThread();
        }
    }

}
