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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.AttributeAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.AttributeDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.internal.search.LdapSearchPageScoreComputer;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeDescription;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ICompareableEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.search.ui.ISearchPageScoreComputer;


// ── CLASS: AbstractEntry — PARTIAL DEATH STAR BLUEPRINT (ABSTRACT BASE) ──────
// The Death Star's blueprints don't describe just one Death Star — they define
// the template every Death Star is built from: structural sections, power
// couplings, trench layout.  Each concrete Death Star fills in the unique bits
// (which sector it occupies, what its serial number is).  AbstractEntry is that
// master blueprint: it handles everything common to all directory entries —
// managing children, attributes, and flag bits — while leaving the entry's DN
// and parent pointer for the concrete subclass (Entry, DummyEntry, etc.) to
// supply.  Children and attributes are stored off-entry in BrowserConnection
// maps so we don't bloat each entry object in memory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base implementation of the {@link IEntry} interface.
 *
 * <p>Optimised to save memory — children and attributes are NOT stored on the
 * entry itself.  Instead, {@link ChildrenInfo} and {@link AttributeInfo} live
 * in maps inside {@link BrowserConnection} and are looked up on demand.
 * Behavioural flags (alias, referral, subentry, etc.) are packed into a single
 * {@code int} field.</p>
 *
 * <p>Think of this as the master Death Star blueprint: every concrete entry
 * subclass inherits the structural logic from here and only has to fill in its
 * own DN, parent pointer, and RDN.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractEntry implements IEntry, ICompareableEntry
{

    private static final long serialVersionUID = -2431637532526418774L;

    private static final int HAS_CHILDREN_HINT_FLAG = 1 << 0;

    private static final int IS_DIRECTORY_ENTRY_FLAG = 1 << 1;

    private static final int IS_ALIAS_FLAG = 1 << 2;

    private static final int IS_REFERRAL_FLAG = 1 << 3;

    private static final int IS_SUBENTRY_FLAG = 1 << 4;

    private static final int IS_INIT_OPERATIONAL_ATTRIBUTES_FLAG = 1 << 5;

    private static final int IS_FETCH_ALIASES_FLAG = 1 << 6;

    private static final int IS_FETCH_REFERRALS_FLAG = 1 << 7;

    private static final int IS_FETCH_SUBENTRIES_FLAG = 1 << 8;

    private volatile int flags;

    protected IAttribute objectClassAttribute;


    // ── Blueprint Constructor — Sets The "Has Children" Hint By Default ──────────
    /**
     * Creates a new instance of AbstractEntry.
     * Initialises the flags field with {@code HAS_CHILDREN_HINT_FLAG} set so
     * the tree view shows an expand arrow before we have loaded the children.
     */
    protected AbstractEntry()
    {
        this.flags = HAS_CHILDREN_HINT_FLAG;
    }


    // ── Concrete Subclass Fills In The Parent Pointer ────────────────────────────
    /**
     * Sets the parent entry.
     *
     * @param newParent the new parent entry
     */
    protected abstract void setParent( IEntry newParent );


    // ── Concrete Subclass Fills In The RDN ───────────────────────────────────────
    /**
     * Sets the Rdn.
     *
     * @param newRdn the new Rdn
     */
    protected abstract void setRdn( Rdn newRdn );


    // ── Lando Assigns A New Platform Resident To This Sector ─────────────────────
    // "Cloud City has a new resident.  Log them in the children registry and
    // fire an EntryAdded event so the rest of the galaxy knows."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void addChild( IEntry childToAdd )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }

        if ( ci.childrenSet == null )
        {
            ci.childrenSet = new LinkedHashSet<IEntry>();
        }
        ci.childrenSet.add( childToAdd );
        entryModified( new EntryAddedEvent( childToAdd.getBrowserConnection(), childToAdd ) );
    }


    // ── Lando Removes A Resident From The Platform Registry ──────────────────────
    /**
     * {@inheritDoc}
     */
    public void deleteChild( IEntry childToDelete )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );

        if ( ci != null )
        {
            if ( ci.childrenSet != null )
            {
                ci.childrenSet.remove( childToDelete );
            }
            if ( ci.childrenSet == null || ci.childrenSet.isEmpty() )
            {
                getBrowserConnectionImpl().setChildrenInfo( this, null );
            }
            entryModified( new EntryDeletedEvent( getBrowserConnectionImpl(), childToDelete ) );
        }
    }


    // ── Han Shoots First If The Attribute Belongs To Someone Else ────────────────
    // Before accepting a new attribute onto this entry's blueprint, we check that
    // it actually belongs here.  If it doesn't, we throw immediately — no waiting
    // around for Greedo to pull his blaster.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void addAttribute( IAttribute attributeToAdd ) throws IllegalArgumentException
    {
        if ( !equals( attributeToAdd.getEntry() ) )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__attributes_entry_is_not_myself );
        }

        if ( attributeToAdd.isObjectClassAttribute() )
        {
            if ( objectClassAttribute != null )
            {
                throw new IllegalArgumentException( BrowserCoreMessages.model__attribute_already_exists );
            }

            objectClassAttribute = attributeToAdd;
        }
        else
        {
            String oidString = attributeToAdd.getAttributeDescription()
                .toOidString( getBrowserConnection().getSchema() );
            AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
            if ( ai == null )
            {
                ai = new AttributeInfo();
                getBrowserConnectionImpl().setAttributeInfo( this, ai );
            }

            if ( ai.attributeMap.containsKey( Strings.toLowerCase( oidString ) ) )
            {
                throw new IllegalArgumentException( BrowserCoreMessages.model__attribute_already_exists );
            }

            ai.attributeMap.put( Strings.toLowerCase( oidString ), attributeToAdd );
        }

        entryModified( new AttributeAddedEvent( getBrowserConnectionImpl(), this, attributeToAdd ) );
    }


    // ── Han Shoots First If The Attribute Is Already Gone ────────────────────────
    /**
     * {@inheritDoc}
     */
    public void deleteAttribute( IAttribute attributeToDelete ) throws IllegalArgumentException
    {
        if ( attributeToDelete.isObjectClassAttribute() )
        {
            if ( objectClassAttribute == null )
            {
                throw new IllegalArgumentException( BrowserCoreMessages.model__attribute_does_not_exist + ": " //$NON-NLS-1$
                    + attributeToDelete );
            }

            objectClassAttribute = null;
        }
        else
        {
            String oidString = attributeToDelete.getAttributeDescription().toOidString(
                getBrowserConnection().getSchema() );
            AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
            if ( ai != null && ai.attributeMap != null
                && ai.attributeMap.containsKey( Strings.toLowerCase( oidString ) ) )
            {
                attributeToDelete = ( IAttribute ) ai.attributeMap.get( Strings.toLowerCase( oidString ) );
                ai.attributeMap.remove( Strings.toLowerCase( oidString ) );
                if ( ai.attributeMap.isEmpty() )
                {
                    getBrowserConnectionImpl().setAttributeInfo( this, null );
                }
            }
            else
            {
                throw new IllegalArgumentException( BrowserCoreMessages.model__attribute_does_not_exist + ": " //$NON-NLS-1$
                    + attributeToDelete );
            }
        }

        entryModified( new AttributeDeletedEvent( getBrowserConnectionImpl(), this, attributeToDelete ) );
    }


    // ── Blueprint Flag: Mark This Entry As A Real Directory Node ─────────────────
    /**
     * {@inheritDoc}
     */
    public void setDirectoryEntry( boolean isDirectoryEntry )
    {
        if ( isDirectoryEntry )
        {
            flags = flags | IS_DIRECTORY_ENTRY_FLAG;
        }
        else
        {
            flags = flags & ~IS_DIRECTORY_ENTRY_FLAG;
        }
    }


    // ── Mace Windu Checks: Is This Entry Really An Alias? ────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isAlias()
    {
        if ( ( flags & IS_ALIAS_FLAG ) != 0 )
        {
            return true;
        }

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null )
        {
            return getObjectClassDescriptions().contains(
                getBrowserConnection().getSchema().getObjectClassDescription( SchemaConstants.ALIAS_OC ) );
        }

        return false;
    }


    // ── Blueprint Flag: Mark Or Clear The Alias Bit ───────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setAlias( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_ALIAS_FLAG;
        }
        else
        {
            flags = flags & ~IS_ALIAS_FLAG;
        }
    }


    // ── Mace Windu Checks: Is This Entry A Referral? ─────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isReferral()
    {
        if ( ( flags & IS_REFERRAL_FLAG ) != 0 )
        {
            return true;
        }

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null )
        {
            return getObjectClassDescriptions().contains(
                getBrowserConnection().getSchema().getObjectClassDescription( SchemaConstants.REFERRAL_OC ) );
        }

        return false;
    }


    // ── Blueprint Flag: Mark Or Clear The Referral Bit ───────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setReferral( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_REFERRAL_FLAG;
        }
        else
        {
            flags = flags & ~IS_REFERRAL_FLAG;
        }
    }


    // ── Mace Windu Checks: Is This A Subentry? ───────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isSubentry()
    {
        if ( ( flags & IS_SUBENTRY_FLAG ) != 0 )
        {
            return true;
        }

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null )
        {
            return getObjectClassDescriptions().contains(
                getBrowserConnection().getSchema().getObjectClassDescription( SchemaConstants.SUBENTRY_OC ) );
        }

        return false;
    }


    // ── Blueprint Flag: Mark Or Clear The Subentry Bit ───────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setSubentry( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_SUBENTRY_FLAG;
        }
        else
        {
            flags = flags & ~IS_SUBENTRY_FLAG;
        }
    }


    // ── Obi-Wan Senses A Disturbance And Notifies The Force ──────────────────────
    // Any time we add a child, delete an attribute, or change a flag, we fire
    // an event through EventRegistry — like Obi-Wan sensing a disturbance and
    // broadcasting it to every listener tuned to the Force.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Triggers firing of the modification event.
     *
     * @param event the modification event to broadcast to all registered listeners
     */
    private void entryModified( EntryModificationEvent event )
    {
        EventRegistry.fireEntryUpdated( event, this );
    }


    // ── Blueprint: Read The Entry's Relative Distinguished Name ──────────────────
    /**
     * {@inheritDoc}
     */
    public Rdn getRdn()
    {
        Rdn rdn = getDn().getRdn();
        return rdn == null ? new Rdn() : rdn;
    }


    // ── Mace Windu Checks: Have The Entry's Attributes Been Loaded? ──────────────
    /**
     * {@inheritDoc}
     */
    public boolean isAttributesInitialized()
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        return ai != null && ai.attributesInitialized;
    }


    // ── R2-D2 Flags Whether The Attribute Map Is Fully Loaded ─────────────────────
    /**
     * {@inheritDoc}
     */
    public void setAttributesInitialized( boolean b )
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null && b )
        {
            ai = new AttributeInfo();
            getBrowserConnectionImpl().setAttributeInfo( this, ai );
        }

        if ( ai != null )
        {
            ai.attributesInitialized = b;
        }

        if ( ai != null && !b )
        {
            ai.attributeMap.clear();
            getBrowserConnectionImpl().setAttributeInfo( this, null );
        }

        entryModified( new AttributesInitializedEvent( this ) );
    }


    // ── Blueprint Flag: Should Operational Attributes Be Loaded? ─────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isInitOperationalAttributes()
    {
        return ( flags & IS_INIT_OPERATIONAL_ATTRIBUTES_FLAG ) != 0;
    }


    // ── Blueprint Flag: Set Whether Operational Attributes Should Be Loaded ──────
    /**
     * {@inheritDoc}
     */
    public void setInitOperationalAttributes( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_INIT_OPERATIONAL_ATTRIBUTES_FLAG;
        }
        else
        {
            flags = flags & ~IS_INIT_OPERATIONAL_ATTRIBUTES_FLAG;
        }
    }


    // ── Blueprint Flag: Should Alias Entries Be Fetched? ─────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isFetchAliases()
    {
        return ( flags & IS_FETCH_ALIASES_FLAG ) != 0;
    }


    // ── Blueprint Flag: Set The Fetch-Aliases Preference ─────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setFetchAliases( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_FETCH_ALIASES_FLAG;
        }
        else
        {
            flags = flags & ~IS_FETCH_ALIASES_FLAG;
        }
    }


    // ── Blueprint Flag: Should Referral Entries Be Followed? ─────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isFetchReferrals()
    {
        return ( flags & IS_FETCH_REFERRALS_FLAG ) != 0;
    }


    // ── Blueprint Flag: Set The Fetch-Referrals Preference ───────────────────────
    /**
     * {@inheritDoc}
     */
    public void setFetchReferrals( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_FETCH_REFERRALS_FLAG;
        }
        else
        {
            flags = flags & ~IS_FETCH_REFERRALS_FLAG;
        }
    }


    // ── Blueprint Flag: Should Subentries Be Fetched? ────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isFetchSubentries()
    {
        return ( flags & IS_FETCH_SUBENTRIES_FLAG ) != 0;
    }


    // ── Blueprint Flag: Set The Fetch-Subentries Preference ──────────────────────
    /**
     * {@inheritDoc}
     */
    public void setFetchSubentries( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_FETCH_SUBENTRIES_FLAG;
        }
        else
        {
            flags = flags & ~IS_FETCH_SUBENTRIES_FLAG;
        }
    }


    // ── Lando Lists All Residents Of This Cloud City Section ─────────────────────
    /**
     * {@inheritDoc}
     */
    public IAttribute[] getAttributes()
    {
        Collection<IAttribute> attributes = new HashSet<IAttribute>();

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null && ai.attributeMap != null )
        {
            attributes.addAll( ai.attributeMap.values() );
        }
        if ( objectClassAttribute != null )
        {
            attributes.add( objectClassAttribute );
        }

        return attributes.toArray( new IAttribute[0] );
    }


    // ── R2-D2 Looks Up A Single Attribute By Description ─────────────────────────
    /**
     * {@inheritDoc}
     */
    public IAttribute getAttribute( String attributeDescription )
    {
        AttributeDescription ad = new AttributeDescription( attributeDescription );
        String oidString = ad.toOidString( getBrowserConnection().getSchema() );
        if ( oidString.equals( SchemaConstants.OBJECT_CLASS_AT_OID )
            || ( SchemaConstants.OBJECT_CLASS_AT.equalsIgnoreCase( attributeDescription ) ) )
        {
            return objectClassAttribute;
        }
        else
        {
            AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
            if ( ai == null || ai.attributeMap == null )
            {
                return null;
            }
            else
            {
                return ( IAttribute ) ai.attributeMap.get( Strings.toLowerCase( oidString ) );
            }
        }
    }


    // ── Lando Gathers An Attribute And All Its Subtypes Into One Package ─────────
    /**
     * {@inheritDoc}
     */
    public AttributeHierarchy getAttributeWithSubtypes( String attributeDescription )
    {
        List<IAttribute> attributeList = new ArrayList<IAttribute>();

        IAttribute myAttribute = getAttribute( attributeDescription );
        if ( myAttribute != null )
        {
            attributeList.add( myAttribute );
        }

        AttributeDescription ad = new AttributeDescription( attributeDescription );
        IAttribute[] allAttributes = getAttributes();
        for ( IAttribute attribute : allAttributes )
        {
            AttributeDescription other = attribute.getAttributeDescription();
            if ( other.isSubtypeOf( ad, getBrowserConnection().getSchema() ) )
            {
                attributeList.add( attribute );
            }
        }

        if ( attributeList.isEmpty() )
        {
            return null;
        }
        else
        {
            IAttribute[] attributes = attributeList.toArray( new IAttribute[attributeList.size()] );
            AttributeHierarchy ah = new AttributeHierarchy( this, attributeDescription, attributes );
            return ah;
        }
    }


    // ── R2-D2 Flags Whether The Children Map Has Been Loaded ─────────────────────
    /**
     * {@inheritDoc}
     */
    public void setChildrenInitialized( boolean b )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null && b )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }

        if ( ci != null )
        {
            ci.childrenInitialized = b;
        }

        if ( ci != null && !b )
        {
            if ( ci.childrenSet != null )
            {
                ci.childrenSet.clear();
            }
            getBrowserConnectionImpl().setChildrenInfo( this, null );
        }

        entryModified( new ChildrenInitializedEvent( this ) );
    }


    // ── Mace Windu Checks: Has The Children List Been Loaded? ────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isChildrenInitialized()
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        return ci != null && ci.childrenInitialized;
    }


    // ── Lando Lists All Child Residents Of This Platform Section ─────────────────
    /**
     * {@inheritDoc}
     */
    public IEntry[] getChildren()
    {
        int count = getChildrenCount();
        if ( count < 0 )
        {
            return null;
        }
        else if ( count == 0 )
        {
            return new IEntry[0];
        }
        else
        {
            IEntry[] children = new IEntry[count];
            ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
            int i = 0;
            if ( ci.childrenSet != null )
            {
                for ( IEntry child : ci.childrenSet )
                {
                    children[i] = child;
                    i++;
                }
            }
            return children;
        }
    }


    // ── Lando Counts The Residents In This Platform Section ──────────────────────
    /**
     * {@inheritDoc}
     */
    public int getChildrenCount()
    {
        if ( isSubentry() )
        {
            return 0;
        }
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null )
        {
            return -1;
        }
        else
        {
            return ci.childrenSet == null ? 0 : ci.childrenSet.size();
        }
    }


    // ── R2-D2 Flags: More Children Exist Beyond The Current Page ─────────────────
    /**
     * {@inheritDoc}
     */
    public void setHasMoreChildren( boolean b )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }
        ci.hasMoreChildren = b;

        entryModified( new ChildrenInitializedEvent( this ) );
    }


    // ── Mace Windu Checks: Are There More Children Beyond The Loaded Page? ───────
    /**
     * {@inheritDoc}
     */
    public boolean hasMoreChildren()
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        return ci != null && ci.hasMoreChildren;
    }


    // ── Clone Trooper: Store The "Load First Page" Job For Later Execution ───────
    /**
     * {@inheritDoc}
     */
    public void setTopPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress topPageChildrenRunnable )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null && topPageChildrenRunnable != null )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }

        if ( ci != null )
        {
            ci.topPageChildrenRunnable = topPageChildrenRunnable;
        }
    }


    // ── Clone Trooper: Retrieve The "Load First Page" Job ────────────────────────
    /**
     * {@inheritDoc}
     */
    public StudioConnectionBulkRunnableWithProgress getTopPageChildrenRunnable()
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        return ci != null ? ci.topPageChildrenRunnable : null;
    }


    // ── Clone Trooper: Store The "Load Next Page" Job For Later Execution ────────
    /**
     * {@inheritDoc}
     */
    public void setNextPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress nextPageChildrenRunnable )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null && nextPageChildrenRunnable != null )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }

        if ( ci != null )
        {
            ci.nextPageChildrenRunnable = nextPageChildrenRunnable;
        }
    }


    // ── Clone Trooper: Retrieve The "Load Next Page" Job ─────────────────────────
    /**
     * {@inheritDoc}
     */
    public StudioConnectionBulkRunnableWithProgress getNextPageChildrenRunnable()
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        return ci != null ? ci.nextPageChildrenRunnable : null;
    }


    // ── Blueprint Flag: Set The "Has Children" Hint For Tree Display ─────────────
    /**
     * {@inheritDoc}
     */
    public void setHasChildrenHint( boolean b )
    {
        if ( b )
        {
            flags = flags | HAS_CHILDREN_HINT_FLAG;
        }
        else
        {
            flags = flags & ~HAS_CHILDREN_HINT_FLAG;
        }
    }


    // ── Mace Windu Checks: Does This Entry Have Any Children? ────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean hasChildren()
    {
        return ( flags & HAS_CHILDREN_HINT_FLAG ) != 0 || getChildrenCount() > 0;
    }


    // ── Blueprint: Get The Optional Filter Applied To This Entry's Children ──────
    /**
     * {@inheritDoc}
     */
    public String getChildrenFilter()
    {
        return getBrowserConnectionImpl().getChildrenFilter( this );
    }


    // ── Blueprint: Set The Filter That Restricts Which Children Are Shown ────────
    /**
     * {@inheritDoc}
     */
    public void setChildrenFilter( String childrenFilter )
    {
        getBrowserConnectionImpl().setChildrenFilter( this, childrenFilter );
    }


    // ── Mace Windu Checks: Does This Entry Have A Parent? ────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean hasParententry()
    {
        return getParententry() != null;
    }


    // ── R2-D2 Gets The Connection Handle To Look Things Up ────────────────────────
    /**
     * Gets the browser connection implementation.
     *
     * @return the browser connection implementation cast to {@link BrowserConnection}
     */
    private BrowserConnection getBrowserConnectionImpl()
    {
        return ( BrowserConnection ) getBrowserConnection();
    }


    // ── Blueprint Prints Its DN As A String ───────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getDn().getName();
    }


    // ── Blueprint Hash Code Is Derived From Its DN ───────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return getDn().hashCode();
    }


    // ── Mace Windu Checks: Is This The Same Entry (DN + Connection)? ─────────────
    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        // check argument
        if ( !( o instanceof ICompareableEntry ) )
        {
            return false;
        }
        
        ICompareableEntry e = ( ICompareableEntry ) o;

        // compare dn and connection
        if ( getDn() == null )
        {
            return e.getDn() == null;
        }
        else
        {
            return getDn().equals( e.getDn() ) && 
                   getBrowserConnection().equals( e.getBrowserConnection() );
        }
    }


    // ── R2-D2 Plugs In And Adapts This Entry To Any Requested Interface ──────────
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
            return getBrowserConnection().getConnection();
        }
        if ( clazz.isAssignableFrom( IBrowserConnection.class ) )
        {
            return getBrowserConnection();
        }
        if ( clazz.isAssignableFrom( IEntry.class ) )
        {
            return this;
        }
        return null;
    }


    // ── Blueprint Returns Its Full LDAP URL ───────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public LdapUrl getUrl()
    {
        return Utils.getLdapURL( this );
    }


    // ── Jedi Archives: List All ObjectClass Descriptions For This Entry ──────────
    /**
     * {@inheritDoc}
     */
    public Collection<ObjectClass> getObjectClassDescriptions()
    {
        Collection<ObjectClass> ocds = new ArrayList<ObjectClass>();
        IAttribute ocAttribute = getAttribute( SchemaConstants.OBJECT_CLASS_AT );
        if ( ocAttribute != null )
        {
            String[] ocNames = ocAttribute.getStringValues();
            Schema schema = getBrowserConnection().getSchema();
            for ( String ocName : ocNames )
            {
                ObjectClass ocd = schema.getObjectClassDescription( ocName );
                ocds.add( ocd );
            }
        }
        return ocds;
    }

}
