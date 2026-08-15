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
package org.apache.directory.studio.schemaeditor.view.views;


import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.schemachecker.NoAliasWarning;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaWarning;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaErrorWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWarningWrapper;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ProblemsViewLabelProvider — C-3PO Translating for the Rebel Alliance ─
// When the Millennium Falcon lands at the Rebel base on Yavin IV, C-3PO is the
// only one who can translate the raw R2-D2 beeps, the Wookiee growls, and the
// alien briefings into plain language the Rebel commanders can act on. He takes
// a raw, cryptic signal — an error code, a species-specific grunt — looks up its
// meaning in his internal dictionary of six million languages, and outputs a
// clear human-readable sentence.
// This label provider is C-3PO. It takes raw schema error codes (from
// LdapSchemaException) and raw warning types (from SchemaWarning) and translates
// each one into a clear English message that the Problems View table can display.
// Twenty-plus private helper methods each handle one specific error code or warning
// type — one language each — and assemble the right NLS-formatted string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides text and icon labels for every row in the Problems View table.
 * Rows can be {@link SchemaErrorWrapper} (wrapping an {@link LdapSchemaException}),
 * {@link SchemaWarningWrapper} (wrapping a {@link SchemaWarning}), or a {@link Folder}
 * group header. For errors and warnings, we dispatch to a private helper method for
 * each exception/warning code that knows how to look up and format the right NLS
 * message string. Think of it as C-3PO translating: each helper method is one
 * language in his six-million-forms vocabulary.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProblemsViewLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // ── C-3PO Identifies the Type of Signal ──────────────────────────────────
    // Before translating, C-3PO needs to know what kind of signal he's dealing
    // with: Wookiee? Binary? R2's proprietary beep dialect? getColumnImage does
    // the same: it identifies whether the row is an error, a warning, or a folder
    // header, and returns the appropriate icon.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for a given table cell in the Problems View.
     * Column 0 carries an icon that indicates whether the row is an error (red X),
     * a warning (yellow triangle), or a folder group header (folder icon). All other
     * columns return {@code null} (no icon). The caller is JFace's table viewer, which
     * calls this for every cell.
     *
     * <p>For example — C-3PO identifies the signal type:</p>
     * <pre>
     *   element = SchemaErrorWrapper   → IMG_PROBLEMS_ERROR (red X)
     *   element = SchemaWarningWrapper → IMG_PROBLEMS_WARNING (yellow triangle)
     *   element = Folder               → IMG_PROBLEMS_GROUP (folder)
     * </pre>
     *
     * @param element      the row's model object (error, warning, or folder)
     * @param columnIndex  which column is being rendered
     * @return             the icon image, or {@code null} for no icon
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        if ( columnIndex == 0 )
        {
            if ( element instanceof SchemaErrorWrapper )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_PROBLEMS_ERROR );
            }
            else if ( element instanceof SchemaWarningWrapper )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_PROBLEMS_WARNING );
            }
            else if ( element instanceof Folder )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_PROBLEMS_GROUP );
            }
        }

        // Default
        return null;
    }


    // ── C-3PO Delivers the Translation ───────────────────────────────────────
    // Once C-3PO knows what kind of message it is, he produces the translation.
    // getColumnText delivers the human-readable text for a table cell: column 0
    // is the problem description ("Name already registered in attribute type X"),
    // column 1 is the schema object's display name. Folder rows show their count
    // in column 0 and nothing in column 1.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for a given table cell in the Problems View.
     * Column 0 gets the formatted human-readable problem message. Column 1 gets
     * the display name (or OID) of the schema object involved. Folder rows show
     * their name and child count in column 0.
     *
     * @param element      the row's model object (error, warning, or folder)
     * @param columnIndex  which column is being rendered (0 = description, 1 = object name)
     * @return             the text to display, never {@code null}
     */
    public String getColumnText( Object element, int columnIndex )
    {
        if ( element instanceof SchemaErrorWrapper )
        {
            SchemaErrorWrapper errorWrapper = ( SchemaErrorWrapper ) element;

            if ( columnIndex == 0 )
            {
                return getMessage( errorWrapper.getLdapSchemaException() );
            }
            else if ( columnIndex == 1 )
            {
                return getDisplayName( errorWrapper.getLdapSchemaException().getSourceObject() );
            }
        }
        else if ( element instanceof SchemaWarningWrapper )
        {
            SchemaWarningWrapper warningWrapper = ( SchemaWarningWrapper ) element;

            if ( columnIndex == 0 )
            {
                return getMessage( warningWrapper.getSchemaWarning() );
            }
            else if ( columnIndex == 1 )
            {
                String name = warningWrapper.getSchemaWarning().getSource().getName();

                if ( ( name != null ) && ( !name.equals( "" ) ) ) //$NON-NLS-1$
                {
                    return name;
                }
                else
                {
                    return warningWrapper.getSchemaWarning().getSource().getOid();
                }
            }
        }
        else if ( element instanceof Folder )
        {
            Folder folder = ( Folder ) element;
            if ( columnIndex == 0 )
            {
                return folder.getName() + " (" + folder.getChildren().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return ""; //$NON-NLS-1$
            }
        }

        // Default
        return element.toString();
    }


    // ── C-3PO Looks Up the Error Code ────────────────────────────────────────
    // "That's a Binary language, Master Luke — let me look it up." C-3PO consults
    // his internal error-code dictionary (the switch statement below) and delegates
    // to the specific per-code helper method that knows how to format the message.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches to the right per-code message formatter for a schema exception.
     * We switch on the exception's error code and call the private helper that knows
     * how to format that specific message (which may include NLS placeholders for
     * related OIDs or names). Returns an empty string if the exception is null or the
     * code is unrecognized.
     *
     * @param exception  the schema exception from the SchemaChecker
     * @return           the formatted human-readable error message
     */
    private String getMessage( LdapSchemaException exception )
    {
        if ( exception != null )
        {
            switch ( exception.getCode() )
            {
            // Codes for all Schema Objects
                case NAME_ALREADY_REGISTERED:
                    return getMessageNameAlreadyRegistered( exception );
                case OID_ALREADY_REGISTERED:
                    return getMessageOidAlreadyRegistered( exception );
                case NONEXISTENT_SCHEMA:
                    return getMessageNonExistentSchema( exception );

                    // Codes for Attribute Type
                case AT_NONEXISTENT_SUPERIOR:
                    return getMessageATNonExistentSuperior( exception );
                case AT_CANNOT_SUBTYPE_COLLECTIVE_AT:
                    return getMessageATCannotSubtypeCollectiveAT( exception );
                case AT_CYCLE_TYPE_HIERARCHY:
                    return getMessageATCycleTypeHierarchy( exception );
                case AT_NONEXISTENT_SYNTAX:
                    return getMessageATNonExistentSyntax( exception );
                case AT_SYNTAX_OR_SUPERIOR_REQUIRED:
                    return getMessageATSyntaxOrSuperiorRequired( exception );
                case AT_NONEXISTENT_EQUALITY_MATCHING_RULE:
                    return getMessageATNonExistentEqualityMatchingRule( exception );
                case AT_NONEXISTENT_ORDERING_MATCHING_RULE:
                    return getMessageATNonExistentOrderingMatchingRule( exception );
                case AT_NONEXISTENT_SUBSTRING_MATCHING_RULE:
                    return getMessageATNonExistentSubstringMatchingRule( exception );
                case AT_MUST_HAVE_SAME_USAGE_THAN_SUPERIOR:
                    return getMessageATMustHaveSameUsageThanSuperior( exception );
                case AT_USER_APPLICATIONS_USAGE_MUST_BE_USER_MODIFIABLE:
                    return getMessageATUserApplicationsUsageMustBeUserModifiable( exception );
                case AT_COLLECTIVE_MUST_HAVE_USER_APPLICATIONS_USAGE:
                    return getMessageATCollectiveMustHaveUserApplicationsUsage( exception );
                case AT_COLLECTIVE_CANNOT_BE_SINGLE_VALUED:
                    return getMessageATCollectiveCannotBeSingleValued( exception );

                    // Codes for Object Class
                case OC_ABSTRACT_MUST_INHERIT_FROM_ABSTRACT_OC:
                    return getMessageOCAbstractMustInheritFromAbstractOC( exception );
                case OC_AUXILIARY_CANNOT_INHERIT_FROM_STRUCTURAL_OC:
                    return getMessageOCAuxiliaryCannotInheritFromStructuralOC( exception );
                case OC_STRUCTURAL_CANNOT_INHERIT_FROM_AUXILIARY_OC:
                    return getMessageOCStructuralCannotInheritFromAuxiliaryOC( exception );
                case OC_NONEXISTENT_SUPERIOR:
                    return getMessageOCNonExistentSuperior( exception );
                case OC_CYCLE_CLASS_HIERARCHY:
                    return getMessageOCCycleClassHierarchy( exception );
                case OC_COLLECTIVE_NOT_ALLOWED_IN_MUST:
                    return getMessageOCCollectiveNotAllowedInMust( exception );
                case OC_COLLECTIVE_NOT_ALLOWED_IN_MAY:
                    return getMessageOCCollectiveNotAllowedInMay( exception );
                case OC_DUPLICATE_AT_IN_MUST:
                    return getMessageOCDuplicateATInMust( exception );
                case OC_DUPLICATE_AT_IN_MAY:
                    return getMessageOCDuplicateATInMay( exception );
                case OC_NONEXISTENT_MUST_AT:
                    return getMessageOCNonExistentMustAT( exception );
                case OC_NONEXISTENT_MAY_AT:
                    return getMessageOCNonExistentMayAT( exception );
                case OC_DUPLICATE_AT_IN_MAY_AND_MUST:
                    return getMessageOCDuplicateATInMayAndMust( exception );

                    // Codes for Matching Rule
                case MR_NONEXISTENT_SYNTAX:
                    return getMessageMRNonExistentSyntax( exception );
            }
        }

        return ""; //$NON-NLS-1$
    }


    // ── C-3PO Reads the Warning Signal ───────────────────────────────────────
    // Not all messages are errors — some are polite cautions. "Pardon me, sir,
    // but you might want to know that R2 hasn't filed his name registry."
    // This override handles SchemaWarning types (currently just NoAliasWarning).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Produces a human-readable message for a schema warning.
     * Currently only {@link NoAliasWarning} is handled: we produce a message that
     * names the OID of the attribute type or object class that has no alias. Other
     * warning types return an empty string.
     *
     * @param warning  the schema warning from the SchemaChecker
     * @return         the formatted human-readable warning message
     */
    private String getMessage( SchemaWarning warning )
    {

        if ( warning instanceof NoAliasWarning )
        {
            NoAliasWarning noAliasWarning = ( NoAliasWarning ) warning;
            SchemaObject source = noAliasWarning.getSource();
            if ( source instanceof AttributeType )
            {
                return NLS
                    .bind(
                        Messages.getString( "ProblemsViewLabelProvider.NoAliasWarningAttributeType" ), new String[] { source.getOid() } ); //$NON-NLS-1$
            }
            else if ( source instanceof ObjectClass )
            {
                return NLS
                    .bind(
                        Messages.getString( "ProblemsViewLabelProvider.NoAliasWarningObjectClass" ), new String[] { source.getOid() } ); //$NON-NLS-1$
            }
        }

        return ""; //$NON-NLS-1$
    }


    // ── The Name Is Already in the Registry ──────────────────────────────────
    // "Master Luke, that alias is already registered to another attribute type —
    // number 2.5.4.3, to be precise." C-3PO knows his Jedi records cold.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "name already registered" error message.
     * The exception's related ID holds the duplicate name; the other object is the
     * existing AT or OC that already owns that name. We pick the right NLS key based
     * on whether the duplicate is an AT or OC, then bind in the name and OID.
     *
     * @param exception  the exception carrying the duplicate name and conflicting object
     * @return           formatted error message naming the conflicting object
     */
    private String getMessageNameAlreadyRegistered( LdapSchemaException exception )
    {
        SchemaObject duplicate = exception.getOtherObject();
        String message = null;

        if ( duplicate instanceof AttributeType )
        {
            message = Messages.getString( "ProblemsViewLabelProvider.NameAlreadyRegisteredAT" ); //$NON-NLS-1$
        }
        else if ( duplicate instanceof ObjectClass )
        {
            message = Messages.getString( "ProblemsViewLabelProvider.NameAlreadyRegisteredOC" ); //$NON-NLS-1$
        }

        return NLS.bind( message, new String[]
            { exception.getRelatedId(), duplicate.getOid() } );
    }


    // ── The OID Is Already Taken ─────────────────────────────────────────────
    // "That OID has already been issued to another citizen of the galaxy, sir —
    // 2.5.4.3 belongs to AttributeType 'cn'." OIDs must be unique; this message
    // tells the user which object already owns the conflicting OID.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "OID already registered" error message.
     * The OID in question (from {@code exception.getRelatedId()}) is already owned by
     * the object in {@code exception.getOtherObject()}. We pick an AT-specific or
     * OC-specific message key and bind in both the conflicting OID and the existing
     * object's name.
     *
     * @param exception  the exception carrying the duplicate OID and conflicting object
     * @return           formatted error message naming the object that already owns the OID
     */
    private String getMessageOidAlreadyRegistered( LdapSchemaException exception )
    {
        SchemaObject duplicate = exception.getOtherObject();
        String message = null;

        if ( duplicate instanceof AttributeType )
        {
            message = Messages.getString( "ProblemsViewLabelProvider.OidAlreadyRegisteredAT" ); //$NON-NLS-1$
        }
        else if ( duplicate instanceof ObjectClass )
        {
            message = Messages.getString( "ProblemsViewLabelProvider.OidAlreadyRegisteredOC" ); //$NON-NLS-1$
        }

        return NLS.bind( message, new String[]
            { exception.getRelatedId(), duplicate.getName() } );
    }


    // ── That Schema Doesn't Exist ─────────────────────────────────────────────
    // "I'm afraid I cannot locate that schema in any of my records, sir."
    // A schema object references a schema name that isn't loaded — like pointing
    // to a planet that's not on the star charts.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "nonexistent schema" error message.
     * The schema name in {@code exception.getRelatedId()} doesn't correspond to any
     * loaded schema. We bind that name into the NLS message for display.
     *
     * @param exception  the exception carrying the missing schema name
     * @return           formatted error message naming the missing schema
     */
    private String getMessageNonExistentSchema( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.NonExistentSchema" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── This AT's Superior Doesn't Exist ─────────────────────────────────────
    // "The superior attribute type it claims to inherit from is not in our
    // records — it references a phantom entry." An AT declares a SUP that
    // no loaded schema knows about.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT has a nonexistent superior" error message.
     * The attribute type declares a SUP OID that doesn't match any loaded attribute type.
     * We bind that OID into the NLS message.
     *
     * @param exception  the exception carrying the missing superior OID
     * @return           formatted error message naming the missing superior
     */
    private String getMessageATNonExistentSuperior( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.ATNonExistentSuperior" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── Can't Inherit from a Collective AT ───────────────────────────────────
    // "You cannot inherit from a collective attribute, Master Luke — that's
    // against the rules of the LDAP Council." RFC 4512: no AT may subtype
    // a collective attribute type.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT cannot subtype a collective AT" error message.
     * This is a fixed message (no parameters) — the rule is absolute, no NLS
     * placeholders needed.
     *
     * @param exception  the exception (not examined; the rule is unconditional)
     * @return           the fixed human-readable error message
     */
    private String getMessageATCannotSubtypeCollectiveAT( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.ATCannotSubtypeCollectiveAT" ); //$NON-NLS-1$;
    }


    // ── The AT Hierarchy Is a Circle ─────────────────────────────────────────
    // "The inheritance chain loops back on itself — it goes A inherits B, B
    // inherits A. That's not an inheritance tree, that's a Tauntaun eating its
    // own tail." RFC 4512 requires an acyclic hierarchy.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT has a cycle in its type hierarchy" error message.
     * The attribute type's SUP chain forms a cycle (A SUP B, B SUP A). Fixed message,
     * no parameters.
     *
     * @param exception  the exception (not examined; the cycle speaks for itself)
     * @return           the fixed human-readable error message
     */
    private String getMessageATCycleTypeHierarchy( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.ATCycleTypeHierarchy" ); //$NON-NLS-1$;
    }


    // ── That Syntax Doesn't Exist ─────────────────────────────────────────────
    // "The SYNTAX OID it references is not in our records — I cannot translate
    // what I've never seen." An AT points to a syntax OID that no loaded syntax
    // definition covers.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT references a nonexistent syntax" error message.
     * Also reused for MatchingRules ({@link #getMessageMRNonExistentSyntax}). Binds
     * the missing syntax OID into the NLS message.
     *
     * @param exception  the exception carrying the unknown syntax OID
     * @return           formatted error message naming the missing syntax
     */
    private String getMessageATNonExistentSyntax( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.NonExistentSyntax" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── Either Syntax or Superior Is Required ────────────────────────────────
    // "Every attribute type must have either a SYNTAX or a SUP — it can't float
    // in hyperspace with neither. Even droids need a power source." RFC 4512
    // demands one or the other.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT has neither syntax nor superior" error message.
     * An attribute type must declare either a SYNTAX or a SUP (from which it inherits
     * syntax). Binds the AT's related ID into the NLS message.
     *
     * @param exception  the exception carrying the AT's identifier
     * @return           formatted error message
     */
    private String getMessageATSyntaxOrSuperiorRequired( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.ATSyntaxOrSuperiorRequired" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── The Equality Matching Rule Doesn't Exist ─────────────────────────────
    // "That equality matching rule OID is not in any of my language databases —
    // it points to something that doesn't exist in this schema." An AT's EQUALITY
    // field references an unknown matching rule.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT references a nonexistent equality matching rule" error message.
     * Binds the unknown equality matching rule OID or name from the exception.
     *
     * @param exception  the exception carrying the missing rule's identifier
     * @return           formatted error message naming the missing rule
     */
    private String getMessageATNonExistentEqualityMatchingRule( LdapSchemaException exception )
    {
        return NLS.bind(
            Messages.getString( "ProblemsViewLabelProvider.ATNonExistentEqualityMatchingRule" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── The Ordering Matching Rule Doesn't Exist ─────────────────────────────
    // "That ordering matching rule isn't in the records either — another phantom."
    // An AT's ORDERING field references an unknown matching rule.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT references a nonexistent ordering matching rule" error message.
     * Binds the unknown ordering matching rule OID or name from the exception.
     *
     * @param exception  the exception carrying the missing rule's identifier
     * @return           formatted error message naming the missing rule
     */
    private String getMessageATNonExistentOrderingMatchingRule( LdapSchemaException exception )
    {
        return NLS.bind(
            Messages.getString( "ProblemsViewLabelProvider.ATNonExistentOrderingMatchingRule" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── The Substring Matching Rule Doesn't Exist ────────────────────────────
    // "And that substring matching rule — also not in my records." Three strikes,
    // all three matching rule types can reference nonexistent rules.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT references a nonexistent substring matching rule" error message.
     * Binds the unknown substring matching rule OID or name from the exception.
     *
     * @param exception  the exception carrying the missing rule's identifier
     * @return           formatted error message naming the missing rule
     */
    private String getMessageATNonExistentSubstringMatchingRule( LdapSchemaException exception )
    {
        return NLS.bind(
            Messages.getString( "ProblemsViewLabelProvider.ATNonExistentSubstringMatchingRule" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── Usage Must Match the Superior's Usage ────────────────────────────────
    // "Protocol demands that a sub-type must have the same usage as its parent —
    // you cannot be a directoryOperation while your parent is userApplications."
    // RFC 4512 section 4.1.2.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT must have the same usage as its superior" error message.
     * Fixed message — the rule is a binary constraint with no variable part.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageATMustHaveSameUsageThanSuperior( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.ATMustHaveSameUsageThanSuperior" ); //$NON-NLS-1$
    }


    // ── userApplications Must Be User-Modifiable ─────────────────────────────
    // "A userApplications attribute must be writable by regular users — the
    // Emperor cannot lock down a field intended for the people." RFC 4512 rule.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "userApplications AT must be user-modifiable" error message.
     * Fixed message — any attribute type with usage userApplications must also have
     * NO-USER-MODIFICATION set to false.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageATUserApplicationsUsageMustBeUserModifiable( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.ATUserApplicationsUsageMustBeUserModifiable" ); //$NON-NLS-1$
    }


    // ── Collective ATs Must Have userApplications Usage ──────────────────────
    // "A collective attribute is shared across entries — it must belong to
    // userApplications. The Emperor can't make a collective attribute an
    // operational secret." RFC 4512 collective constraint.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "collective AT must have userApplications usage" error message.
     * Fixed message — collective attribute types are only allowed to have
     * userApplications usage, never operational usage.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageATCollectiveMustHaveUserApplicationsUsage( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.ATCollectiveMustHaveUserApplicationsUsage" ); //$NON-NLS-1$
    }


    // ── Collective ATs Can't Be Single-Valued ────────────────────────────────
    // "A collective attribute can appear on multiple entries — it cannot be
    // single-valued. Even Jedi can't be the only one in the galaxy." RFC 4512.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "collective AT cannot be single-valued" error message.
     * Fixed message — collective attribute types by definition carry multiple values
     * across entries and thus cannot be declared SINGLE-VALUE.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageATCollectiveCannotBeSingleValued( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.ATCollectiveCannotBeSingleValued" ); //$NON-NLS-1$
    }


    // ── Abstract OC Must Inherit From Abstract OC ────────────────────────────
    // "An abstract object class can only inherit from another abstract class —
    // you cannot have a concrete emperor and call him abstract." RFC 4512 rule
    // for abstract class hierarchy.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "abstract OC must inherit from an abstract OC" error message.
     * Fixed message — abstract object classes may only SUP another abstract OC,
     * never a structural or auxiliary one.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageOCAbstractMustInheritFromAbstractOC( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.OCAbstractMustInheritFromAbstractOC" ); //$NON-NLS-1$
    }


    // ── Auxiliary OC Can't Inherit From Structural OC ────────────────────────
    // "An auxiliary object class cannot inherit from a structural class — that
    // crosses the great divide between structural and auxiliary in the LDAP
    // Council's laws." RFC 4512 structural vs. auxiliary separation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "auxiliary OC cannot inherit from a structural OC" error message.
     * Fixed message — auxiliary OCs may only SUP abstract or other auxiliary OCs.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageOCAuxiliaryCannotInheritFromStructuralOC( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.OCAuxiliaryCannotInheritFromStructuralOC" ); //$NON-NLS-1$
    }


    // ── Structural OC Can't Inherit From Auxiliary OC ────────────────────────
    // "The reverse is equally true: a structural class cannot claim an auxiliary
    // as its parent. What's structural must stay structural." RFC 4512.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "structural OC cannot inherit from an auxiliary OC" error message.
     * Fixed message — structural OCs may only SUP abstract or other structural OCs.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageOCStructuralCannotInheritFromAuxiliaryOC( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.OCStructuralCannotInheritFromAuxiliaryOC" ); //$NON-NLS-1$
    }


    // ── That OC's Superior Doesn't Exist ─────────────────────────────────────
    // "This object class claims to inherit from a superior that isn't in our
    // records — like claiming Darth Vader trained you when Vader doesn't exist."
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "OC has a nonexistent superior" error message.
     * Binds the missing superior's OID or name from the exception into the NLS message.
     *
     * @param exception  the exception carrying the unknown superior's identifier
     * @return           formatted error message naming the missing superior
     */
    private String getMessageOCNonExistentSuperior( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCNonExistentSuperior" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── The OC Hierarchy Is a Circle ─────────────────────────────────────────
    // "The class hierarchy loops: A inherits B, B inherits A. That's not
    // inheritance — that's a broken hyperdrive spinning in circles." RFC 4512
    // requires an acyclic class hierarchy.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "OC has a cycle in its class hierarchy" error message.
     * Fixed message — the OC's SUP chain loops back on itself.
     *
     * @param exception  the exception (not examined)
     * @return           the fixed human-readable error message
     */
    private String getMessageOCCycleClassHierarchy( LdapSchemaException exception )
    {
        return Messages.getString( "ProblemsViewLabelProvider.OCCycleClassHierarchy" ); //$NON-NLS-1$
    }


    // ── Collective AT Not Allowed in MUST List ────────────────────────────────
    // "You cannot mandate a collective attribute — you can't force every citizen
    // to carry something that's shared across entries. It's unconstitutional."
    // RFC 4512: collective ATs cannot appear in a MUST list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "collective AT not allowed in MUST list" error message.
     * Binds the collective AT's identifier from the exception into the NLS message.
     *
     * @param exception  the exception carrying the collective AT's identifier
     * @return           formatted error message naming the offending AT
     */
    private String getMessageOCCollectiveNotAllowedInMust( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCCollectiveNotAllowedInMust" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── Collective AT Not Allowed in MAY List ────────────────────────────────
    // "And you can't put it in the MAY list either — collective attributes stand
    // alone, outside both lists." RFC 4512 applies the same rule to MAY.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "collective AT not allowed in MAY list" error message.
     * Binds the collective AT's identifier from the exception into the NLS message.
     *
     * @param exception  the exception carrying the collective AT's identifier
     * @return           formatted error message naming the offending AT
     */
    private String getMessageOCCollectiveNotAllowedInMay( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCCollectiveNotAllowedInMay" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── Duplicate AT in MUST List ─────────────────────────────────────────────
    // "The same attribute type appears twice in the MUST list — C-3PO sees the
    // same entry in two seats at the Rebel briefing." MUST lists must be unique.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "duplicate AT in MUST list" error message.
     * Binds the duplicated AT's identifier into the NLS message.
     *
     * @param exception  the exception carrying the duplicate AT's identifier
     * @return           formatted error message naming the duplicate
     */
    private String getMessageOCDuplicateATInMust( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCDuplicateATInMust" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── Duplicate AT in MAY List ──────────────────────────────────────────────
    // "Same problem in the MAY list — that AT appears twice. The roster has a
    // duplicate entry." MAY lists must also be unique.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "duplicate AT in MAY list" error message.
     * Binds the duplicated AT's identifier into the NLS message.
     *
     * @param exception  the exception carrying the duplicate AT's identifier
     * @return           formatted error message naming the duplicate
     */
    private String getMessageOCDuplicateATInMay( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCDuplicateATInMay" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── A MUST AT Doesn't Exist ───────────────────────────────────────────────
    // "This object class requires an attribute type that doesn't exist — it's
    // pointing to a phantom. Like requiring all entries have a 'midi-chlorianCount'
    // attribute that no schema defines." MUST ATs must be loaded.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "OC references a nonexistent must AT" error message.
     * Binds the unknown must AT's OID or name into the NLS message.
     *
     * @param exception  the exception carrying the missing AT's identifier
     * @return           formatted error message naming the missing AT
     */
    private String getMessageOCNonExistentMustAT( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCNonExistentMustAT" ), new String[]//$NON-NLS-1$
            { exception.getRelatedId() } );
    }


    // ── A MAY AT Doesn't Exist ────────────────────────────────────────────────
    // "Same issue in the MAY list — that attribute type isn't in our records."
    // MAY ATs must also be loadable from the schema.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "OC references a nonexistent may AT" error message.
     * Binds the unknown may AT's OID or name into the NLS message.
     *
     * @param exception  the exception carrying the missing AT's identifier
     * @return           formatted error message naming the missing AT
     */
    private String getMessageOCNonExistentMayAT( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCNonExistentMayAT" ), new String[] //$NON-NLS-1$
            { exception.getRelatedId() } );

    }


    // ── The Same AT Is in Both MUST and MAY ──────────────────────────────────
    // "That attribute is listed as both mandatory and optional — that's a
    // contradiction. Even C-3PO can't translate both 'required' and 'optional'
    // for the same item at the same time." RFC 4512 forbids this.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "AT appears in both MAY and MUST lists" error message.
     * An attribute type cannot be both mandatory and optional in the same OC.
     * Binds the conflicting AT's identifier into the NLS message.
     *
     * @param exception  the exception carrying the conflicting AT's identifier
     * @return           formatted error message naming the conflicting AT
     */
    private String getMessageOCDuplicateATInMayAndMust( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.OCDuplicateATInMayAndMust" ), new String[] //$NON-NLS-1$;
            { exception.getRelatedId() } );
    }


    // ── The Matching Rule's Syntax Doesn't Exist ─────────────────────────────
    // "This matching rule references a syntax that isn't loaded. Even I can't
    // translate a syntax that doesn't exist — and I know six million forms."
    // A MatchingRule's SYNTAX must resolve to a known LdapSyntax.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Formats the "matching rule references a nonexistent syntax" error message.
     * Reuses the same NLS key as {@link #getMessageATNonExistentSyntax}. Binds the
     * unknown syntax OID from the exception.
     *
     * @param exception  the exception carrying the missing syntax OID
     * @return           formatted error message naming the missing syntax
     */
    private String getMessageMRNonExistentSyntax( LdapSchemaException exception )
    {
        return NLS.bind( Messages.getString( "ProblemsViewLabelProvider.NonExistentSyntax" ), new String[] //$NON-NLS-1$;
            { exception.getRelatedId() } );
    }


    // ── C-3PO Looks Up the Name ──────────────────────────────────────────────
    // "Allow me to look that up in my personnel records." When the table needs
    // the display name for a schema object involved in an error, we look it up
    // in the SchemaHandler to get the human-readable name (or fall back to OID).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns a displayable name for the schema object involved in an error.
     * We look the object up in the {@link SchemaHandler} (by OID) to get the
     * best available representation. If it has a name, we return that; if not,
     * we return the OID. If the object can't be found in the handler, we use the
     * source object's own OID as a fallback.
     *
     * @param so  the schema object from the exception's source
     * @return    the display name or OID string, never {@code null}
     */
    private String getDisplayName( SchemaObject so )
    {
        if ( so != null )
        {
            SchemaObject schemaObject = getSchemaObject( so );
            if ( schemaObject != null )
            {
                String name = schemaObject.getName();
                if ( ( name != null ) && ( !name.equals( "" ) ) ) // $NON-NLS-1$ //$NON-NLS-1$
                {
                    return name;
                }
                else
                {
                    return so.getOid();
                }
            }
            else
            {
                return so.getOid();
            }
        }

        return ""; // $NON-NLS-1$ //$NON-NLS-1$
    }


    // ── C-3PO Checks the Master Registry ─────────────────────────────────────
    // "Let me cross-reference that against the official schema registry."
    // The schema objects in exceptions are sometimes stubs — we look up the
    // live version from the SchemaHandler, which has the full name list and OID.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the live {@link SchemaObject} from the {@link SchemaHandler} by OID.
     * The object carried in an exception may be a partial stub. We use the handler
     * as the authoritative source, resolving by OID for AttributeType, LdapSyntax,
     * MatchingRule, or ObjectClass.
     *
     * @param so  the schema object (possibly a stub) from the exception
     * @return    the fully resolved schema object from the handler, or {@code null} if not found
     */
    private SchemaObject getSchemaObject( SchemaObject so )
    {
        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
        SchemaObject schemaObject = null;

        if ( so instanceof AttributeType )
        {
            schemaObject = schemaHandler.getAttributeType( so.getOid() );
        }
        else if ( so instanceof LdapSyntax )
        {
            schemaObject = schemaHandler.getSyntax( so.getOid() );
        }
        else if ( so instanceof MatchingRule )
        {
            schemaObject = schemaHandler.getMatchingRule( so.getOid() );
        }
        else if ( so instanceof ObjectClass )
        {
            schemaObject = schemaHandler.getObjectClass( so.getOid() );
        }

        return schemaObject;
    }
}
