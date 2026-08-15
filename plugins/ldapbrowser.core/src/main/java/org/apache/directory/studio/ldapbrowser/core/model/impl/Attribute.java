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

package org.apache.directory.studio.ldapbrowser.core.model.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.internal.search.LdapSearchPageScoreComputer;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeDescription;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.search.ui.ISearchPageScoreComputer;


// ── CLASS: Attribute — ONE BLUEPRINT PANEL ON THE DEATH STAR ─────────────────
// Each room on the Death Star blueprint has panels: power coupling, ventilation
// shaft, turret status.  Each panel (attribute) has a description (like
// "cn;lang-en"), an entry it belongs to, and a list of value slots.  You can
// add and remove values, and each change fires an event back to the display.
// Han shoots first: if a value is null or belongs to the wrong attribute, the
// IllegalArgumentException fires before anything gets written.
// Attribute is the concrete IAttribute: an AttributeDescription + an IEntry +
// a List<IValue> that fires model events on every modification.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Default implementation of {@link IAttribute}.
 * Holds an {@link AttributeDescription}, a reference to the owning
 * {@link IEntry}, and a list of {@link IValue} objects.  Every mutating method
 * fires a {@link EntryModificationEvent} through {@link EventRegistry}.
 *
 * <p>Think of this as one blueprint panel on the Death Star entry — named,
 * entry-scoped, and firing events every time a value is added, changed, or
 * removed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Attribute implements IAttribute
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = -5679384884002589786L;

    /** The attribute description */
    private AttributeDescription attributeDescription;

    /** The entry this attribute belongs to */
    private IEntry entry;

    /** The values */
    private List<IValue> valueList;


    // ── Blueprint Panel Constructor: Entry + Description Required ────────────────
    /**
     * Creates a new instance of Attribute with the given description and no
     * value.
     *
     * @param entry the entry this attribute belongs to; must not be {@code null}
     * @param description the attribute description string; must not be {@code null}
     */
    public Attribute( IEntry entry, String description )
    {
        assert entry != null;
        assert description != null;

        this.entry = entry;
        this.attributeDescription = new AttributeDescription( description );
        this.valueList = new ArrayList<IValue>();

    }


    /**
     * {@inheritDoc}
     */
    public IEntry getEntry()
    {
        return entry;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isConsistent()
    {
        if ( valueList.isEmpty() )
        {
            return false;
        }

        for ( IValue value : valueList )
        {
            if ( value.isEmpty() )
            {
                return false;
            }
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isMustAttribute()
    {
        if ( isObjectClassAttribute() )
        {
            return true;
        }
        else
        {
            Collection<AttributeType> mustAtds = SchemaUtils.getMustAttributeTypeDescriptions( entry );
            return mustAtds.contains( getAttributeTypeDescription() );
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isMayAttribute()
    {
        return !isObjectClassAttribute() && !isMustAttribute() && !isOperationalAttribute();
    }


    /**
     * {@inheritDoc}
     */
    public boolean isOperationalAttribute()
    {
        return getAttributeTypeDescription() == null || SchemaUtils.isOperational( getAttributeTypeDescription() );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isObjectClassAttribute()
    {
        return SchemaConstants.OBJECT_CLASS_AT.equalsIgnoreCase( getDescription() );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isString()
    {
        return !isBinary();
    }


    /**
     * {@inheritDoc}
     */
    public boolean isBinary()
    {
        return SchemaUtils.isBinary( getAttributeTypeDescription(), getEntry().getBrowserConnection().getSchema() );
    }


    /**
     * {@inheritDoc}
     */
    public void addEmptyValue()
    {
        IValue emptyValue = new Value( this );
        valueList.add( emptyValue );
        attributeModified( new EmptyValueAddedEvent( getEntry().getBrowserConnection(), getEntry(), this, emptyValue ) );
    }


    /**
     * {@inheritDoc}
     */
    public void deleteEmptyValue()
    {
        for ( Iterator<IValue> it = this.valueList.iterator(); it.hasNext(); )
        {
            IValue value = it.next();
            if ( value.isEmpty() )
            {
                it.remove();
                attributeModified( new EmptyValueDeletedEvent( getEntry().getBrowserConnection(), getEntry(), this,
                    value ) );
                return;
            }
        }
    }


    // ── Obi-Wan Senses An Attribute Change And Notifies The Force ────────────────
    /**
     * Fires an EntryModificationEvent.
     *
     * @param event the EntryModificationEvent to broadcast
     */
    private void attributeModified( EntryModificationEvent event )
    {
        EventRegistry.fireEntryUpdated( event, getEntry() );
    }


    // ── Han Shoots First: Validate The Value Before Accepting It ─────────────────
    /**
     * Checks if the given value is valid.
     *
     * @param value the value to check; must not be {@code null} and must
     *              belong to this attribute
     * @throws IllegalArgumentException if the value is null or belongs to a
     *                                  different attribute
     */
    private void checkValue( IValue value ) throws IllegalArgumentException
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__empty_value );
        }
        if ( !value.getAttribute().equals( this ) )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__values_attribute_is_not_myself );
        }
    }


    // ── R2-D2 Removes The Value From The Internal List Without Firing Events ──────
    /**
     * Deletes the given value from value list.
     *
     * @param valueToDelete the value to delete
     * @return {@code true} if a matching value was found and removed
     */
    private boolean internalDeleteValue( IValue valueToDelete )
    {
        for ( Iterator<IValue> it = valueList.iterator(); it.hasNext(); )
        {
            IValue value = it.next();
            if ( value.equals( valueToDelete ) )
            {
                it.remove();
                return true;
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    public void addValue( IValue valueToAdd ) throws IllegalArgumentException
    {
        checkValue( valueToAdd );
        valueList.add( valueToAdd );
        attributeModified( new ValueAddedEvent( getEntry().getBrowserConnection(), getEntry(), this, valueToAdd ) );
    }


    /**
     * {@inheritDoc}
     */
    public void deleteValue( IValue valueToDelete ) throws IllegalArgumentException
    {
        checkValue( valueToDelete );

        if ( internalDeleteValue( valueToDelete ) )
        {
            attributeModified( new ValueDeletedEvent( getEntry().getBrowserConnection(), getEntry(), this,
                valueToDelete ) );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void modifyValue( IValue oldValue, IValue newValue ) throws IllegalArgumentException
    {
        checkValue( oldValue );
        checkValue( newValue );

        internalDeleteValue( oldValue );
        valueList.add( newValue );
        attributeModified( new ValueModifiedEvent( getEntry().getBrowserConnection(), getEntry(), this, oldValue,
            newValue ) );
    }


    /**
     * {@inheritDoc}
     */
    public IValue[] getValues()
    {
        return ( IValue[] ) valueList.toArray( new IValue[0] );
    }


    /**
     * {@inheritDoc}
     */
    public int getValueSize()
    {
        return valueList.size();
    }


    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return getAttributeDescription().getDescription();
    }


    /**
     * {@inheritDoc}
     */
    public String getType()
    {
        return getAttributeDescription().getParsedAttributeType();
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getDescription();
    }


    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        // check argument
        if ( !( o instanceof IAttribute ) )
        {
            return false;
        }
        IAttribute a = ( IAttribute ) o;

        // compare entries
        if ( !getEntry().equals( a.getEntry() ) )
        {
            return false;
        }

        // compare attribute description
        return getDescription().equals( a.getDescription() );
    }


    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return getDescription().hashCode();
    }


    /**
     * {@inheritDoc}
     */
    public byte[][] getBinaryValues()
    {
        List<byte[]> binaryValueList = new ArrayList<byte[]>();

        IValue[] values = getValues();
        for ( IValue value : values )
        {
            binaryValueList.add( value.getBinaryValue() );
        }

        return binaryValueList.toArray( new byte[0][] );
    }


    /**
     * {@inheritDoc}
     */
    public String getStringValue()
    {
        if ( getValueSize() > 0 )
        {
            return ( ( IValue ) valueList.get( 0 ) ).getStringValue();
        }
        else
        {
            return null;
        }
    }


    /**
     * {@inheritDoc}
     */
    public String[] getStringValues()
    {
        List<String> stringValueList = new ArrayList<String>();

        IValue[] values = getValues();
        for ( IValue value : values )
        {
            stringValueList.add( value.getStringValue() );
        }

        return stringValueList.toArray( new String[stringValueList.size()] );
    }


    /**
     * {@inheritDoc}
     */
    public AttributeType getAttributeTypeDescription()
    {
        return getEntry().getBrowserConnection().getSchema().getAttributeTypeDescription( getType() );
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter )
    {
        Class<?> clazz = ( Class<?> ) adapter;
        if ( clazz.isAssignableFrom( ISearchPageScoreComputer.class ) )
        {
            return new LdapSearchPageScoreComputer();
        }
        if ( clazz.isAssignableFrom( Connection.class ) )
        {
            return getEntry().getBrowserConnection().getConnection();
        }
        if ( clazz.isAssignableFrom( IBrowserConnection.class ) )
        {
            return getEntry().getBrowserConnection();
        }
        if ( clazz.isAssignableFrom( IEntry.class ) )
        {
            return getEntry();
        }
        if ( clazz.isAssignableFrom( IAttribute.class ) )
        {
            return this;
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    public AttributeDescription getAttributeDescription()
    {
        return attributeDescription;
    }

}
