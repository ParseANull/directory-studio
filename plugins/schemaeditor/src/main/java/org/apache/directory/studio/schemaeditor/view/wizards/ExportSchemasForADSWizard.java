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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObjectSorter;
import org.apache.directory.api.ldap.schema.converter.AttributeTypeHolder;
import org.apache.directory.api.ldap.schema.converter.ObjectClassHolder;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ExportSchemasForADSWizard — Jyn Transmits Plans From Scarif ────────
// In Rogue One, Jyn Erso and Cassian Andor fight their way to the Scarif data
// vault, pull the Death Star plans, and transmit them up the orbital satellite
// dish to the rebel fleet above — converting raw data into a structured signal
// that the Alliance can actually use.
// We do the same: take LDAP schema objects and convert them to ApacheDS LDIF
// format, then write that signal to disk so ApacheDS can ingest it directly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that exports LDAP schemas in ApacheDS LDIF format.
 * LDIF (LDAP Data Interchange Format) is how ApacheDS stores its schema
 * definitions — so this wizard lets you take a schema you've built or edited
 * in the Schema Editor and push it out in a format ApacheDS can load directly.
 * Think of Jyn Erso at the Scarif satellite dish: she grabs the Death Star
 * blueprints from the vault and transmits them in a format the rebel fleet
 * can actually read and act on.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasForADSWizard extends Wizard implements IExportWizard
{
    /** The selected schemas */
    private Schema[] selectedSchemas = new Schema[0];

    // The pages of the wizard
    private ExportSchemasForADSWizardPage page;


    // ── Jyn Preps The Transmission Terminal ───────────────────────────────────
    // Jyn reaches the top of the Scarif communications tower and powers up the
    // terminal — the interface she'll use to configure which data goes out and
    // to where. Before she can transmit, she has to set up the controls.
    // Here we create and register the wizard's single configuration page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers the wizard's configuration page.
     * Eclipse calls this at wizard startup so we can contribute our UI page
     * before the dialog opens.
     * We forward any pre-selected schemas to the page so the user starts
     * with sensible defaults in the checkbox table.
     *
     * <p>For example — Jyn activates the transmission terminal:</p>
     * <pre>
     *   Jyn powers up the Scarif communications console.
     *   The page appears: "Select schemas to transmit. Choose destination."
     *   Pre-selected schemas are already highlighted on the manifest.
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new ExportSchemasForADSWizardPage();
        page.setSelectedSchemas( selectedSchemas );

        // Adding pages
        addPage( page );
    }


    // ── Jyn Throws The Transmission Switch ────────────────────────────────────
    // Jyn grabs the dish controls and heaves — the signal goes out, carrying
    // the Death Star plans to the rebel fleet above Scarif.
    // When the user hits Finish, we convert each selected schema to LDIF and
    // write it to disk — one file per schema, or everything in one combined file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the export when the user clicks the Finish button.
     * We read the user's choices from the page, convert each schema to ApacheDS
     * LDIF format via {@link #toLdif(Schema, StringBuffer)}, and write the result
     * to disk — either as individual {@code .ldif} files or as one combined file.
     * Errors during conversion or writing are logged and shown in a dialog, but
     * we return {@code true} regardless so the wizard closes cleanly.
     *
     * <p>For example — Jyn transmits the Death Star plans:</p>
     * <pre>
     *   She throws the switch. The satellite dish rotates toward the rebel fleet.
     *   Each schema is a blueprint module; each LDIF file is a transmission burst.
     *   Even if one burst fails, the transmission attempt is recorded and we close.
     * </pre>
     *
     * @return {@code true} always — errors are handled inline; the wizard always closes
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the schemas to be exported and where to export them
        final Schema[] selectedSchemas = page.getSelectedSchemas();
        int exportType = page.getExportType();
        if ( exportType == ExportSchemasAsXmlWizardPage.EXPORT_MULTIPLE_FILES )
        {
            final String exportDirectory = page.getExportDirectory();
            try
            {
                getContainer().run( false, true, new IRunnableWithProgress()
                {
                    public void run( IProgressMonitor monitor )
                    {
                        monitor.beginTask(
                            Messages.getString( "ExportSchemasForADSWizard.ExportingSchemas" ), selectedSchemas.length ); //$NON-NLS-1$
                        for ( Schema schema : selectedSchemas )
                        {
                            monitor.subTask( schema.getSchemaName() );

                            StringBuffer sb = new StringBuffer();
                            DateFormat format = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.MEDIUM );
                            Date date = new Date();
                            sb
                                .append( NLS
                                    .bind(
                                        Messages.getString( "ExportSchemasForADSWizard.GeneratedByApacheComment" ), new String[] { format.format( date ) } ) ); //$NON-NLS-1$

                            try
                            {
                                toLdif( schema, sb );

                                BufferedWriter buffWriter = new BufferedWriter( new FileWriter( exportDirectory + "/" //$NON-NLS-1$
                                    + schema.getSchemaName() + ".ldif" ) ); //$NON-NLS-1$
                                buffWriter.write( sb.toString() );
                                buffWriter.close();
                            }
                            catch ( Exception e )
                            {
                                PluginUtils
                                    .logError(
                                        NLS
                                            .bind(
                                                Messages.getString( "ExportSchemasForADSWizard.ErrorSavingSchema" ), new String[] { schema.getSchemaName() } ), e ); //$NON-NLS-1$
                                ViewUtils
                                    .displayErrorMessageDialog(
                                        Messages.getString( "ExportSchemasForADSWizard.Error" ), NLS.bind( Messages.getString( "ExportSchemasForADSWizard.ErrorSavingSchema" ), new String[] { schema.getSchemaName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            monitor.worked( 1 );
                        }
                        monitor.done();
                    }
                } );
            }
            catch ( InvocationTargetException e )
            {
                // Nothing to do (it will never occur)
            }
            catch ( InterruptedException e )
            {
                // Nothing to do.
            }
        }
        else if ( exportType == ExportSchemasAsXmlWizardPage.EXPORT_SINGLE_FILE )
        {
            final String exportFile = page.getExportFile();
            try
            {
                getContainer().run( false, true, new IRunnableWithProgress()
                {
                    public void run( IProgressMonitor monitor )
                    {
                        monitor.beginTask( Messages.getString( "ExportSchemasForADSWizard.ExportingSchemas" ), 1 ); //$NON-NLS-1$

                        StringBuffer sb = new StringBuffer();
                        DateFormat format = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.MEDIUM );
                        Date date = new Date();
                        sb
                            .append( NLS
                                .bind(
                                    Messages.getString( "ExportSchemasForADSWizard.GeneratedByApacheComment" ), new String[] { format.format( date ) } ) ); //$NON-NLS-1$

                        for ( Schema schema : selectedSchemas )
                        {
                            try
                            {
                                toLdif( schema, sb );
                            }
                            catch ( Exception e )
                            {
                                PluginUtils
                                    .logError(
                                        NLS
                                            .bind(
                                                Messages.getString( "ExportSchemasForADSWizard.ErrorSavingSchema" ), new String[] { schema.getSchemaName() } ), e ); //$NON-NLS-1$
                                ViewUtils
                                    .displayErrorMessageDialog(
                                        Messages.getString( "ExportSchemasForADSWizard.Error" ), NLS.bind( Messages.getString( "ExportSchemasForADSWizard.ErrorSavingSchema" ), new String[] { schema.getSchemaName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }

                        try
                        {
                            BufferedWriter buffWriter = new BufferedWriter( new FileWriter( exportFile ) );
                            buffWriter.write( sb.toString() );
                            buffWriter.close();
                        }
                        catch ( IOException e )
                        {
                            PluginUtils.logError(
                                Messages.getString( "ExportSchemasForADSWizard.ErrorSavingSchemas" ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ExportSchemasForADSWizard.Error" ), Messages.getString( "ExportSchemasForADSWizard.ErrorSavingSchemas" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        monitor.worked( 1 );
                        monitor.done();
                    }
                } );
            }
            catch ( InvocationTargetException e )
            {
                // Nothing to do (it will never occur)
            }
            catch ( InterruptedException e )
            {
                // Nothing to do.
            }
        }

        return true;
    }


    // ── Jyn Encodes The Plans Into The Transmission Signal ────────────────────
    // Inside the Scarif data vault, Jyn feeds each blueprint module through the
    // encoding console — converting raw schematic data into the specific signal
    // format the rebel fleet's receivers can decode.
    // We do the same: walk each schema's attribute types and object classes,
    // building LDIF entries in the structure ApacheDS expects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single schema into ApacheDS LDIF and appends it to the given buffer.
     * LDIF is a text format where each LDAP entry is a block of {@code key: value}
     * lines — ApacheDS uses it to store schema definitions in its data directory.
     * We write the schema's metaSchema entry, then sub-containers for attribute types,
     * object classes, and all the other schema node types ApacheDS requires.
     *
     * <p>For example — Jyn encodes the schematics into a transmission burst:</p>
     * <pre>
     *   "dn: cn=inetOrgPerson, ou=schema"
     *   "objectclass: metaSchema"
     *   Each attribute type and object class becomes an LDIF entry block.
     *   The burst goes into the StringBuffer, ready to transmit.
     * </pre>
     *
     * @param schema  the schema to convert — we iterate its attribute types and object classes
     * @param sb      the buffer we append LDIF text to; caller writes this to disk when done
     * @throws LdapException  if something in the schema model is malformed (e.g., invalid OID)
     */
    private void toLdif( Schema schema, StringBuffer sb ) throws LdapException
    {
        sb
            .append( NLS
                .bind(
                    Messages.getString( "ExportSchemasForADSWizard.SchemaComment" ), new String[] { schema.getSchemaName().toUpperCase() } ) ); //$NON-NLS-1$

        sb.append( "dn: cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: metaSchema\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "cn: " + schema.getSchemaName() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        String[] schemaDependencies = getSchemaDependencies( schema );
        for ( String schemaName : schemaDependencies )
        {
            sb.append( "m-dependencies: " + schemaName + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Attribute Types Node
        sb.append( "dn: " + SchemaConstants.ATTRIBUTE_TYPES_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: attributetypes\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generating LDIF for Attributes Types
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            AttributeTypeHolder holder = new AttributeTypeHolder( at.getOid() );
            holder.setCollective( at.isCollective() );
            holder.setDescription( at.getDescription() );
            holder.setEquality( at.getEqualityOid() );

            List<String> names = new ArrayList<String>();

            for ( String name : at.getNames() )
            {
                names.add( name );
            }

            holder.setNames( names );

            holder.setNoUserModification( !at.isUserModifiable() );
            holder.setObsolete( at.isObsolete() );
            holder.setOrdering( at.getOrderingOid() );
            holder.setSingleValue( at.isSingleValued() );
            holder.setSubstr( at.getSubstringOid() );
            holder.setSuperior( at.getSuperiorOid() );
            holder.setSyntax( at.getSyntaxOid() );

            if ( at.getSyntaxLength() > 0 )
            {
                holder.setOidLen( at.getSyntaxLength() );
            }

            holder.setUsage( at.getUsage() );

            sb.append( holder.toLdif( schema.getSchemaName() ) + "\n" ); //$NON-NLS-1$
        }

        // Generation the Comparators Node
        sb.append( "dn: " + SchemaConstants.COMPARATORS_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: comparators\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the DIT Content Rules Node
        sb.append( "dn: " + SchemaConstants.DIT_CONTENT_RULES_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: ditcontentrules\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the DIT Structure RulesNode
        sb.append( "dn: " + SchemaConstants.DIT_STRUCTURE_RULES_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: ditstructurerules\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Matching Rules Node
        sb.append( "dn: " + SchemaConstants.MATCHING_RULES_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: matchingrules\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Matching Rule Use Node
        sb.append( "dn: " + SchemaConstants.MATCHING_RULE_USE_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: matchingruleuse\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Name Forms Node
        sb.append( "dn: " + SchemaConstants.NAME_FORMS_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: nameforms\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Normalizers Node
        sb.append( "dn: " + SchemaConstants.NORMALIZERS_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: normalizers\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Object Classes Node
        sb.append( "dn: " + SchemaConstants.OBJECT_CLASSES_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: objectClasses\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generating LDIF for Object Classes
        Iterable<ObjectClass> sortedObjectClasses = SchemaObjectSorter.sortObjectClasses( schema.getObjectClasses() );
        for ( ObjectClass oc : sortedObjectClasses )
        {
            ObjectClassHolder holder = new ObjectClassHolder( oc.getOid() );
            holder.setClassType( oc.getType() );
            holder.setDescription( oc.getDescription() );
            List<String> mayList = new ArrayList<String>();
            for ( String may : oc.getMayAttributeTypeOids() )
            {
                mayList.add( may );
            }
            holder.setMay( mayList );
            List<String> mustList = new ArrayList<String>();
            for ( String must : oc.getMustAttributeTypeOids() )
            {
                mustList.add( must );
            }
            holder.setMust( mustList );
            List<String> names = new ArrayList<String>();
            for ( String name : oc.getNames() )
            {
                names.add( name );
            }
            holder.setNames( names );
            List<String> superiorList = new ArrayList<String>();
            for ( String superior : oc.getSuperiorOids() )
            {
                superiorList.add( superior );
            }
            holder.setSuperiors( superiorList );
            holder.setObsolete( oc.isObsolete() );

            sb.append( holder.toLdif( schema.getSchemaName() ) + "\n" ); //$NON-NLS-1$
        }

        // Generation the Syntax Checkers Node
        sb.append( "dn: " + SchemaConstants.SYNTAX_CHECKERS_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: syntaxcheckers\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$

        // Generation the Syntaxes Node
        sb.append( "dn: " + SchemaConstants.SYNTAXES_PATH + ", cn=" + Rdn.escapeValue( schema.getSchemaName() ) + ", ou=schema\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( "objectclass: organizationalUnit\n" ); //$NON-NLS-1$
        sb.append( "objectclass: top\n" ); //$NON-NLS-1$
        sb.append( "ou: syntaxes\n" ); //$NON-NLS-1$
        sb.append( "\n" ); //$NON-NLS-1$
    }


    // ── Jyn Checks What Other Vaults The Plans Reference ──────────────────────
    // Before transmitting a blueprint module, Jyn verifies which other vault
    // sections it cross-references — you can't understand the reactor schematic
    // without also having the power-coupling specs from a different vault section.
    // We scan the schema's attribute types and object classes to find which other
    // schemas they inherit from, so callers can include m-dependencies in the LDIF.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds all other schema names that the given schema depends on.
     * A schema "depends on" another when its attribute types or object classes
     * inherit from types defined in that other schema.
     * We inspect attribute type superiors and object class superiors, mays, and
     * musts — if any of those resolve to a type in a different schema, that schema
     * is a dependency and gets listed as an {@code m-dependencies} entry in the LDIF.
     *
     * <p>For example — Jyn cross-references the vault sections:</p>
     * <pre>
     *   "inetOrgPerson inherits from organizationalPerson — that's the 'core' vault section."
     *   "We need m-dependencies: core in the LDIF so ApacheDS loads 'core' first."
     *   Jyn lists every cross-reference before finalizing the transmission.
     * </pre>
     *
     * @param schema  the schema whose cross-schema references we want to discover
     * @return        array of schema names that {@code schema} depends on (may be empty)
     */
    private String[] getSchemaDependencies( Schema schema )
    {
        Set<String> schemaNames = new HashSet<String>();
        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();

        // Looping on Attribute Types
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            // Superior
            String supName = at.getSuperiorOid();
            if ( supName != null )
            {
                AttributeType sup = schemaHandler.getAttributeType( supName );
                if ( sup != null )
                {
                    if ( !Strings.toLowerCase( schema.getSchemaName() ).equals(
                        Strings.toLowerCase( sup.getSchemaName() ) ) )
                    {
                        schemaNames.add( sup.getSchemaName() );
                    }
                }
            }
        }

        // Looping on Object Classes
        for ( ObjectClass oc : schema.getObjectClasses() )
        {
            // Superiors
            List<String> supNames = oc.getSuperiorOids();
            if ( supNames != null )
            {
                for ( String supName : oc.getSuperiorOids() )
                {
                    ObjectClass sup = schemaHandler.getObjectClass( supName );
                    if ( sup != null )
                    {
                        if ( !Strings.toLowerCase( schema.getSchemaName() ).equals(
                            Strings.toLowerCase( sup.getSchemaName() ) ) )
                        {
                            schemaNames.add( sup.getSchemaName() );
                        }
                    }
                }
            }

            // Mays
            List<String> mayNames = oc.getMayAttributeTypeOids();
            if ( mayNames != null )
            {
                for ( String mayName : mayNames )
                {
                    AttributeType may = schemaHandler.getAttributeType( mayName );
                    if ( may != null )
                    {
                        if ( !Strings.toLowerCase( schema.getSchemaName() ).equals(
                            Strings.toLowerCase( may.getSchemaName() ) ) )
                        {
                            schemaNames.add( may.getSchemaName() );
                        }
                    }
                }

            }

            // Musts
            List<String> mustNames = oc.getMustAttributeTypeOids();
            if ( mustNames != null )
            {
                for ( String mustName : oc.getMustAttributeTypeOids() )
                {
                    AttributeType must = schemaHandler.getAttributeType( mustName );
                    if ( must != null )
                    {
                        if ( !Strings.toLowerCase( schema.getSchemaName() ).equals(
                            Strings.toLowerCase( must.getSchemaName() ) ) )
                        {
                            schemaNames.add( must.getSchemaName() );
                        }
                    }
                }
            }
        }

        return schemaNames.toArray( new String[0] );
    }


    // ── Jyn Receives Her Mission Briefing ─────────────────────────────────────
    // Before the Rogue One team boards the stolen Imperial shuttle, Jyn gets
    // her briefing from Mon Mothma: here's the goal, here's the equipment.
    // Eclipse calls init() right after creating the wizard — we enable the
    // progress monitor so the UI stays responsive during a large export.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initializes the wizard with the Eclipse workbench context.
     * Eclipse calls this once right after creating the wizard, before any pages appear.
     * We enable the progress monitor so that long-running exports don't freeze the UI.
     *
     * <p>For example — Mon Mothma briefs the Rogue One team:</p>
     * <pre>
     *   "Your mission: reach Scarif, pull the plans, transmit to the fleet."
     *   Jyn confirms: systems enabled, progress monitor armed.
     *   setNeedsProgressMonitor(true) is the "all systems go" signal.
     * </pre>
     *
     * @param workbench  the Eclipse workbench — required by the interface but not used directly
     * @param selection  the current selection in the UI — not used by this wizard
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );
    }


    // ── Jyn Decides Which Plans To Transmit ───────────────────────────────────
    // In the data vault, Jyn narrows down the file: not every blueprint module,
    // just the Death Star plans. She makes that selection before the mission starts.
    // Callers use this to pre-check schemas in the wizard's table viewer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects the schemas that should appear checked when the wizard page opens.
     * If the user launched this export from a context where specific schemas were
     * already highlighted, the caller passes them here so the wizard starts with
     * the right items pre-ticked.
     *
     * <p>For example — Jyn pulls the specific data cards from the vault shelf:</p>
     * <pre>
     *   "Not all of them — just these three schemas," Jyn decides.
     *   She hands the stack to Cassian. The wizard opens with those checked.
     * </pre>
     *
     * @param schemas  the schemas to pre-select in the wizard page's checkbox table
     */
    public void setSelectedSchemas( Schema[] schemas )
    {
        selectedSchemas = schemas;
    }
}
