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
import java.util.stream.Collectors;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.StudioControl;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.apache.directory.studio.ldifparser.model.LdifEOFPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;


// ── CLASS: ModelConverter — R2-D2 TRANSLATING BETWEEN LDIF AND ENTRY MODELS ──
// R2-D2 can translate between any two alien protocols — and between the LDIF
// parser world and the entry model world is exactly what ModelConverter does.
// It converts LdifContentRecord ↔ IEntry, LdifChangeAddRecord ↔ IEntry,
// IValue ↔ LdifAttrValLine, Dn ↔ LdifDnLine, and IEntry ↔ LDAP API Entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static converters between the ldifparser model and the ldapbrowser entry
 * model, and between the entry model and the Apache Directory API entry model.
 *
 * <p>Think of this as R2-D2 translating between the Rebel Alliance's LDIF
 * protocol and the Empire's native entry format — both directions, no data
 * lost.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModelConverter
{

    // ── R2-D2 Converts An LDIF Content Record Into A DummyEntry ─────────────────
    // R2-D2 reads the content record and populates a DummyEntry with its DN and
    // attribute values, suspending events so no notifications fire during loading.
    // This is the entry point for reading static LDIF files into the entry model.
    /**
     * Converts the given {@link LdifContentRecord} to a {@link DummyEntry}.
     *
     * @param ldifContentRecord the LDIF content record to convert
     * @param connection the browser connection
     * @return the resulting dummy entry
     * @throws LdapInvalidDnException if the DN in the record is invalid
     */
    public static DummyEntry ldifContentRecordToEntry( LdifContentRecord ldifContentRecord,
        IBrowserConnection connection ) throws LdapInvalidDnException
    {
        return createIntern( ldifContentRecord, connection );
    }


    // ── R2-D2 Converts An LDIF Change-Add Record Into A DummyEntry ───────────────
    // Like ldifContentRecordToEntry but for LDIF change records with changeType: add.
    // Delegates to the shared createIntern helper which handles both record types.
    // Used when importing LDIF files that use the changetype: add format.
    /**
     * Converts the given {@link LdifChangeAddRecord} to a {@link DummyEntry}.
     *
     * @param ldifChangeAddRecord the LDIF change-add record to convert
     * @param connection the browser connection
     * @return the resulting dummy entry
     * @throws LdapInvalidDnException if the DN in the record is invalid
     */
    public static DummyEntry ldifChangeAddRecordToEntry( LdifChangeAddRecord ldifChangeAddRecord,
        IBrowserConnection connection ) throws LdapInvalidDnException
    {
        return createIntern( ldifChangeAddRecord, connection );
    }


    // ── R2-D2 Builds A DummyEntry From Any LdifRecord Type ───────────────────────
    // Shared private helper for content and change-add record conversions.
    // R2-D2 suspends events, creates the DummyEntry with the record's DN,
    // then iterates all LdifParts: LdifAttrValLines become attribute values,
    // and other structural parts (comments, controls) are stored verbatim.
    private static DummyEntry createIntern( LdifRecord ldifRecord, IBrowserConnection connection )
        throws LdapInvalidDnException
    {
        LdifPart[] parts = ldifRecord.getParts();

        EventRegistry.suspendEventFiringInCurrentThread();

        DummyEntry entry = new DummyEntry( new Dn( ldifRecord.getDnLine().getValueAsString() ), connection );

        for ( int i = 0; i < parts.length; i++ )
        {
            if ( parts[i] instanceof LdifAttrValLine )
            {
                LdifAttrValLine line = ( LdifAttrValLine ) parts[i];
                String attributeName = line.getUnfoldedAttributeDescription();
                Object value = line.getValueAsObject();
                IAttribute attribute = entry.getAttribute( attributeName );
                if ( attribute == null )
                {
                    attribute = new Attribute( entry, attributeName );
                    entry.addAttribute( attribute );
                }
                attribute.addValue( new Value( attribute, value ) );
            }
            else if ( !( parts[i] instanceof LdifDnLine ) && !( parts[i] instanceof LdifSepLine )
                && !( parts[i] instanceof LdifEOFPart ) && !( parts[i] instanceof LdifChangeTypeLine ) )
            {
                String name = parts[i].toRawString();
                name = name.replaceAll( "\n", "" ); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replaceAll( "\r", "" ); //$NON-NLS-1$ //$NON-NLS-2$
                IAttribute attribute = new Attribute( entry, name );
                attribute.addValue( new Value( attribute, parts[i] ) );
                entry.addAttribute( attribute );
            }
        }

        EventRegistry.resumeEventFiringInCurrentThread();

        return entry;
    }


    // ── R2-D2 Serialises A DummyEntry Back Into An LDIF Change-Add Record ────────
    // R2-D2 walks every attribute and value in the entry and emits LdifAttrValLines.
    // If any value's raw object is already a LdifPart (comment, control, changeType),
    // those structural parts are re-inserted verbatim rather than re-encoded.
    // The resulting LdifChangeAddRecord can be written directly to an LDIF stream.
    /**
     * Converts the given {@link IEntry} to a {@link LdifChangeAddRecord}.
     *
     * @param entry the entry to convert
     * @return the resulting LDIF change-add record
     */
    public static LdifChangeAddRecord entryToLdifChangeAddRecord( IEntry entry )
    {
        boolean mustCreateChangeTypeLine = true;
        for ( IAttribute attribute : entry.getAttributes() )
        {
            for ( IValue value : attribute.getValues() )
            {
                if ( value.getRawValue() instanceof LdifPart )
                {
                    mustCreateChangeTypeLine = false;
                }
            }
        }

        LdifChangeAddRecord record = new LdifChangeAddRecord( LdifDnLine.create( entry.getDn().getName() ) );
        if ( mustCreateChangeTypeLine )
        {
            addControls( record, entry );
            record.setChangeType( LdifChangeTypeLine.createAdd() );
        }

        for ( IAttribute attribute : entry.getAttributes() )
        {
            String name = attribute.getDescription();
            for ( IValue value : attribute.getValues() )
            {
                if ( !value.isEmpty() )
                {
                    if ( value.getRawValue() instanceof LdifPart )
                    {
                        LdifPart part = ( LdifPart ) value.getRawValue();
                        if ( part instanceof LdifChangeTypeLine )
                        {
                            record.setChangeType( ( LdifChangeTypeLine ) part );
                        }
                        else if ( part instanceof LdifCommentLine )
                        {
                            record.addComment( ( LdifCommentLine ) part );
                        }
                        else if ( part instanceof LdifControlLine )
                        {
                            record.addControl( ( LdifControlLine ) part );
                        }
                    }
                    else if ( value.isString() )
                    {
                        record.addAttrVal( LdifAttrValLine.create( name, value.getStringValue() ) );
                    }
                    else
                    {
                        record.addAttrVal( LdifAttrValLine.create( name, value.getBinaryValue() ) );
                    }
                }
            }
        }

        record.finish( LdifSepLine.create() );

        return record;
    }


    // ── R2-D2 Serialises A DummyEntry Into A Flat LDIF Content Record ────────────
    // Like entryToLdifChangeAddRecord but produces a static content record with
    // no changeType line — suitable for exporting an LDAP entry as plain LDIF.
    // R2-D2 emits LdifCommentLines verbatim and encodes string/binary values.
    // The finished record is terminated with a LdifSepLine blank-line separator.
    /**
     * Converts the given {@link IEntry} to a {@link LdifContentRecord}.
     *
     * @param entry the entry to convert
     * @return the resulting LDIF content record
     */
    public static LdifContentRecord entryToLdifContentRecord( IEntry entry )
    {
        LdifContentRecord record = LdifContentRecord.create( entry.getDn().getName() );

        if ( entry.getAttributes() != null )
        {
            for ( IAttribute attribute : entry.getAttributes() )
            {
                String name = attribute.getDescription();
                for ( IValue value : attribute.getValues() )
                {
                    if ( !value.isEmpty() )
                    {
                        if ( value.getRawValue() instanceof LdifPart )
                        {
                            LdifPart part = ( LdifPart ) value.getRawValue();
                            if ( part instanceof LdifCommentLine )
                            {
                                record.addComment( ( LdifCommentLine ) part );
                            }
                        }
                        else if ( value.isString() )
                        {
                            record.addAttrVal( LdifAttrValLine.create( name, value.getStringValue() ) );
                        }
                        else
                        {
                            record.addAttrVal( LdifAttrValLine.create( name, value.getBinaryValue() ) );
                        }
                    }
                }
            }
        }

        record.finish( LdifSepLine.create() );

        return record;
    }


    // ── R2-D2 Converts A Single IValue Into One LdifAttrValLine ──────────────────
    // R2-D2 checks whether the value carries string or binary data and calls the
    // matching LdifAttrValLine.create() factory — strings are stored as-is,
    // binary values are base-64 encoded by the LDIF library automatically.
    // Used when building LDIF output one value at a time.
    /**
     * Converts the given {@link IValue} to a {@link LdifAttrValLine}.
     *
     * @param value the value to convert
     * @return the resulting LDIF attribute-value line
     */
    public static LdifAttrValLine valueToLdifAttrValLine( IValue value )
    {

        LdifAttrValLine line;
        if ( value.isString() )
        {
            line = LdifAttrValLine.create( value.getAttribute().getDescription(), value.getStringValue() );
        }
        else
        {
            line = LdifAttrValLine.create( value.getAttribute().getDescription(), value.getBinaryValue() );
        }
        return line;
    }


    // ── R2-D2 Unpacks One LdifAttrValLine Into A Transient IValue ────────────────
    // R2-D2 creates a throw-away Attribute shell just to wrap the IValue,
    // since IValue must always have a parent IAttribute reference.
    // If anything goes wrong during construction, R2-D2 silently returns null.
    // Used when the caller needs a single detached IValue from an LDIF line.
    /**
     * Converts the given {@link LdifAttrValLine} to an {@link IValue}.
     *
     * @param line  the LDIF attribute-value line to convert
     * @param entry the entry that will serve as the value's owning context
     * @return the resulting value, or {@code null} on error
     */
    public static IValue ldifAttrValLineToValue( LdifAttrValLine line, IEntry entry )
    {
        try
        {
            IAttribute attribute = new Attribute( entry, line.getUnfoldedAttributeDescription() );
            IValue value = new Value( attribute, line.getValueAsObject() );
            return value;
        }
        catch ( Exception e )
        {
            return null;
        }
    }


    // ── R2-D2 Wraps A Dn Object In A LdifDnLine Ready For Output ─────────────────
    // The Dn's string representation (getName()) is passed straight to the
    // LdifDnLine.create() factory — no escaping needed, the LDIF library handles it.
    // This small wrapper keeps the rest of the code free of LdifDnLine construction.
    // Returns a ready-to-use LdifDnLine for inclusion in any LDIF record.
    /**
     * Converts the given {@link Dn} to a {@link LdifDnLine}.
     *
     * @param dn the distinguished name to wrap
     * @return the resulting LDIF DN line
     */
    public static LdifDnLine dnToLdifDnLine( Dn dn )
    {
        LdifDnLine line = LdifDnLine.create( dn.getName() );
        return line;
    }


    // ── R2-D2 Attaches Referral Controls To An LDIF Change Record ────────────────
    // When the target entry is flagged as a referral, the ManageDsaIT control
    // must be included so the server mutates the referral object itself rather
    // than chasing the reference.  R2-D2 adds that single control line here.
    // No-op for non-referral entries — no controls are added in that case.
    /**
     * Attaches any required LDAP controls to the given {@link LdifChangeRecord}.
     * Currently adds the ManageDsaIT control when the entry is a referral.
     *
     * @param cr    the change record to decorate with controls
     * @param entry the source entry whose referral flag is inspected
     */
    public static void addControls( LdifChangeRecord cr, IEntry entry )
    {
        if ( entry.isReferral() )
        {
            cr.addControl( LdifControlLine.create( StudioControl.MANAGEDSAIT_CONTROL.getOid(),
                StudioControl.MANAGEDSAIT_CONTROL.isCritical(), StudioControl.MANAGEDSAIT_CONTROL.getControlValue() ) );
        }
    }


    // ── R2-D2 Extracts A Display String From Any IValue Regardless Of Type ───────
    // For pure string values R2-D2 returns the string directly.
    // For binary values that cannot be represented as UTF-8, he applies the
    // requested encoding: BASE64, HEX, or the generic "(BINARY)" placeholder.
    // The encoding constant comes from BrowserCoreConstants — Han shoots first.
    /**
     * Gets the string value from the given {@link IValue}. If the given
     * {@link IValue} is binary is is encoded according to the regquested
     * encoding type.
     *
     * @param value the value
     * @param binaryEncoding the binary encoding type
     *
     * @return the string value
     */
    public static String getStringValue( IValue value, int binaryEncoding )
    {
        String s = value.getStringValue();
        if ( value.isBinary() && LdifUtils.mustEncode( s ) )
        {
            byte[] binary = value.getBinaryValue();
            if ( binaryEncoding == BrowserCoreConstants.BINARYENCODING_BASE64 )
            {
                s = LdifUtils.base64encode( binary );
            }
            else if ( binaryEncoding == BrowserCoreConstants.BINARYENCODING_HEX )
            {
                s = LdifUtils.hexEncode( binary );
            }
            else
            {
                s = BrowserCoreConstants.BINARY;
            }
        }
        return s;
    }


    // ── R2-D2 Translates The Studio Entry Model Into An Apache Directory API Entry ─
    // R2-D2 walks every attribute and value in the IEntry and populates a fresh
    // DefaultEntry with the same DN, attribute descriptions, and values.
    // String and binary values are added via their respective overloads.
    // The resulting Entry can be passed directly to the Apache Directory API.
    /**
     * Converts the given {@link IEntry} to an Apache Directory API {@link Entry}.
     *
     * @param entry the Studio entry to convert
     * @return the equivalent Apache Directory API entry
     * @throws LdapException if an attribute or value cannot be added
     */
    public static Entry toLdapApiEntry( IEntry entry ) throws LdapException
    {
        Entry ldapApiEntry = new DefaultEntry( entry.getDn() );
        for ( IAttribute iAttribute : entry.getAttributes() )
        {
            for ( IValue value : iAttribute.getValues() )
            {
                String attributeDescription = value.getAttribute().getDescription();
                if ( value.isString() )
                {
                    ldapApiEntry.add( attributeDescription, value.getStringValue() );
                }
                else
                {
                    ldapApiEntry.add( attributeDescription, value.getBinaryValue() );
                }
            }
        }
        return ldapApiEntry;
    }


    // ── R2-D2 Builds A REPLACE Modification List From An Apache Directory API Entry ─
    // R2-D2 maps every attribute in the entry to a DefaultModification with
    // REPLACE_ATTRIBUTE operation, collecting them into a list via Java streams.
    // This is used when the full entry state must be sent as a bulk MODIFY-REPLACE.
    // Lando runs Cloud City: R2-D2 manages every slot in the collection at once.
    /**
     * Builds a list of {@link Modification} objects — one REPLACE per attribute —
     * from the given Apache Directory API {@link Entry}.
     *
     * @param entry the source entry whose attributes are to be wrapped
     * @return a collection of REPLACE modifications, one per attribute
     */
    public static Collection<Modification> toReplaceModifications( Entry entry )
    {
        Collection<Modification> modifications = entry.getAttributes()
            .stream()
            .map( attribute -> new DefaultModification(
                ModificationOperation.REPLACE_ATTRIBUTE, attribute ) )
            .collect( Collectors.toList() );
        return modifications;
    }
}
