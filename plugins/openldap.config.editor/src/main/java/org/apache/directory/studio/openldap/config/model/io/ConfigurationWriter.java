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
package org.apache.directory.studio.openldap.config.model.io;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditorUtils;
import org.apache.directory.studio.openldap.config.model.AuxiliaryObjectClass;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcConfig;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.database.OlcDatabaseConfig;


// ── CLASS: ConfigurationWriter — R2-D2 Uploading Updated Plans ───────────────
// R2-D2 has the updated Death Star plans in memory — now he has to encode them
// back into the format the Imperial network expects and upload them. This class
// does the same: given an in-memory OpenLdapConfiguration bean tree, it walks
// each config bean, uses reflection to read every @ConfigurationElement field,
// and serialises the values into LDIF entries. Those entries can then be written
// to disk or sent to a live LDAP server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Converts an in-memory {@link OpenLdapConfiguration} bean tree into a list of
 * LDIF entries, suitable for writing to a file or pushing to a live LDAP server.
 * <p>
 * This class is the counterpart to {@link ConfigurationReader}. Where the reader
 * takes LDAP entries and produces Java beans, the writer takes Java beans and
 * produces LDAP entries. We use Java reflection to scan {@link ConfigurationElement}
 * annotations and convert field values to the correct LDAP attribute format.
 * Think of this as R2-D2 encoding the updated Death Star plans back into the
 * Imperial data format and uploading them to the server.
 * </p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConfigurationWriter
{
    /** The browserConnection */
    private IBrowserConnection browserConnection;

    /** The configuration */
    private OpenLdapConfiguration configuration;

    /** The list of entries */
    private List<LdifEntry> entries;


    // ── ConfigurationWriter(IBrowserConnection, OpenLdapConfiguration) — R2 Preps for Upload
    // R2-D2 loads both the target connection (where to upload) and the updated plans
    // (what to upload). With both in hand, he's ready to serialise and transmit.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ConfigurationWriter connected to a live LDAP server.
     * Use this constructor when you want to write the configuration back to
     * an active server (via {@link #getConvertedLdifEntries()}).
     *
     * <p>For example — R2 preps for a live upload:</p>
     * <pre>
     *   ConfigurationWriter writer = new ConfigurationWriter( browserConn, cfg );
     *   List&lt;LdifEntry&gt; entries = writer.getConvertedLdifEntries();
     * </pre>
     *
     * @param browserConnection  the browser connection to the target LDAP server
     * @param configuration      the in-memory configuration to serialise
     */
    public ConfigurationWriter( IBrowserConnection browserConnection, OpenLdapConfiguration configuration )
    {
        this.browserConnection = browserConnection;
        this.configuration = configuration;
    }


    // ── ConfigurationWriter(OpenLdapConfiguration) — R2 Preps for File Output ─────
    // R2-D2 loads just the plans with no live connection — he'll be writing to a local
    // file rather than uploading to the server. Use this for LDIF file export.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ConfigurationWriter for writing to a local file.
     * Use this constructor when you want to write the configuration to a file
     * (via {@link #writeToFile(File)} or {@link #writeToString()}).
     * No browser connection is needed.
     *
     * <p>For example — R2 preps for local file output:</p>
     * <pre>
     *   ConfigurationWriter writer = new ConfigurationWriter( cfg );
     *   writer.writeToFile( new File( "/tmp/slapd.ldif" ) );
     * </pre>
     *
     * @param configuration  the in-memory configuration to serialise
     */
    public ConfigurationWriter( OpenLdapConfiguration configuration )
    {
        this.configuration = configuration;
    }


    // ── convertConfigurationBeanToLdifEntries — R2 Encodes All Beans to LDIF ──────
    // R2-D2 walks the entire plan: global settings first, then each database (with its
    // overlays), then any remaining config elements. He encodes each one into an LDIF
    // entry and appends it to the upload queue. This method is lazy — it only runs once;
    // subsequent calls do nothing since the entries list is already populated.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts the entire {@link OpenLdapConfiguration} bean tree into a list of
     * {@link LdifEntry} objects.
     * This method is called lazily — if {@code entries} is already set, it does
     * nothing. Databases are processed with their overlays immediately after;
     * other config elements (module lists, etc.) come last.
     *
     * @param configurationDn  the base DN for the cn=config tree (used as parent for databases)
     * @throws ConfigurationException  if any bean cannot be converted to an LDIF entry
     */
    private void convertConfigurationBeanToLdifEntries( Dn configurationDn ) throws ConfigurationException
    {
        try
        {
            if ( entries == null )
            {
                entries = new ArrayList<>();

                // Adding the global configuration
                addConfigurationBean( configuration.getGlobal(), Dn.EMPTY_DN );

                // Adding databases
                for ( OlcDatabaseConfig database : configuration.getDatabases() )
                {
                    LdifEntry entry = addConfigurationBean( database, configurationDn );

                    if ( entry != null )
                    {
                        for ( OlcOverlayConfig overlay : database.getOverlays() )
                        {
                            addConfigurationBean( overlay, entry.getDn() );
                        }
                    }
                }

                // Adding other elements
                for ( OlcConfig configurationBean : configuration.getConfigurationElements() )
                {
                    addConfigurationBean( configurationBean, configurationDn );
                }
            }
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( "Unable to convert the configuration beans to LDIF entries", e );
        }
    }


    // ── addConfigurationBean — R2 Encodes One Bean into One LDIF Entry ────────────
    // R2-D2 takes one config bean, computes its DN, sets up the objectClass attribute,
    // handles any auxiliary object classes, then walks the full class hierarchy calling
    // addFieldsToBean for each level. The finished LDIF entry goes into the upload queue.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single {@link OlcConfig} bean to an {@link LdifEntry} and appends
     * it to the internal entries list.
     * We compute the entry's DN from the bean's RDN field, add the objectClass
     * attribute (including superiors), handle any auxiliary object classes, then
     * walk up the class hierarchy to collect all {@link ConfigurationElement} field
     * values.
     *
     * @param configurationBean  the bean to serialise
     * @param parentDn           the parent DN under which this entry lives
     * @return  the created {@link LdifEntry}, or {@code null} if the bean is null
     * @throws Exception  if the entry cannot be built (DN computation, schema lookup, field access)
     */
    private LdifEntry addConfigurationBean( OlcConfig configurationBean, Dn parentDn ) throws Exception
    {
        if ( configurationBean != null )
        {
            // Getting the class of the bean
            Class<?> beanClass = configurationBean.getClass();

            // Creating the entry to hold the bean and adding it to the list
            LdifEntry entry = new LdifEntry();
            entry.setDn( getDn( configurationBean, parentDn ) );
            addObjectClassAttribute( entry, getObjectClassNameForBean( beanClass ) );
            entries.add( entry );

            // Checking auxiliary object classes
            List<AuxiliaryObjectClass> auxiliaryObjectClassesList = configurationBean.getAuxiliaryObjectClasses();

            if ( ( auxiliaryObjectClassesList != null ) && !auxiliaryObjectClassesList.isEmpty() )
            {
                for ( AuxiliaryObjectClass auxiliaryObjectClass : auxiliaryObjectClassesList )
                {
                    // Getting the bean class for the auxiliary object class
                    Class<?> auxiliaryObjectClassBeanClass = auxiliaryObjectClass.getClass();

                    // Updating the objectClass attribute value
                    addAttributeTypeValue( SchemaConstants.OBJECT_CLASS_AT,
                        getObjectClassNameForBean( auxiliaryObjectClassBeanClass ), entry );

                    // Adding fields of the auxiliary object class to the entry
                    addFieldsToBean( auxiliaryObjectClass, auxiliaryObjectClassBeanClass, entry );
                }
            }

            // A flag to know when we reached the 'OlcConfig' class when
            // looping on the class hierarchy of the bean
            boolean olcConfigBeanClassFound = false;

            // Looping until the 'OlcConfig' class has been found
            while ( !olcConfigBeanClassFound )
            {
                // Checking if we reached the 'OlcConfig' class
                if ( beanClass == OlcConfig.class )
                {
                    olcConfigBeanClassFound = true;
                }

                // Adding fields of the bean to the entry
                addFieldsToBean( configurationBean, beanClass, entry );

                // Moving to the upper class in the class hierarchy
                beanClass = beanClass.getSuperclass();
            }

            return entry;
        }

        return null;
    }


    // ── addFieldsToBean — R2 Scans Fields and Writes Their Values to an Entry ──────
    // R2-D2 scans each field in the current class level of the bean, checks for the
    // @ConfigurationElement annotation, and if present, encodes the field's value
    // into the LDIF entry. Non-null values with a valid attributeType get written;
    // nested OlcConfig sub-beans get recursed into as separate entries.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Scans all declared fields of the given class level of a bean and adds their
     * values to the given LDIF entry.
     * Only fields annotated with {@link ConfigurationElement} and having a non-null
     * value are written. For fields referencing nested {@link OlcConfig} beans,
     * we recurse by calling {@link #addConfigurationBean(OlcConfig, Dn)}.
     *
     * <p>For example — R2 scans one class level of a bean:</p>
     * <pre>
     *   addFieldsToBean( olcMdbConfig, OlcMdbConfig.class, entry );
     *   // entry now has olcDbDirectory, olcDbMaxSize, etc. added
     * </pre>
     *
     * @param configurationBean  the bean instance to read field values from
     * @param beanClass          the specific class level to scan (not its superclass)
     * @param entry              the LDIF entry to add attribute values to
     * @throws Exception  if a field cannot be accessed or its value cannot be encoded
     */
    private void addFieldsToBean( Object configurationBean, Class<?> beanClass, LdifEntry entry ) throws Exception
    {
        if ( ( configurationBean != null ) && ( beanClass != null ) && ( entry != null ) )
        {
            // Looping on all fields of the bean
            for ( Field field : beanClass.getDeclaredFields() )
            {
                // Making the field accessible (we get an exception if we don't do that)
                field.setAccessible( true );

                // Getting the class of the field
                Class<?> fieldClass = field.getType();
                Object fieldValue = field.get( configurationBean );

                if ( fieldValue != null )
                {
                    // Looking for the @ConfigurationElement annotation
                    ConfigurationElement configurationElement = field.getAnnotation( ConfigurationElement.class );

                    if ( configurationElement != null )
                    {
                        // Checking if we have a value for the attribute type
                        String attributeType = configurationElement.attributeType();

                        if ( !Strings.isEmpty( attributeType ) )
                        {
                            // Adding values to the entry, and if it's empty, add the default value
                            addAttributeTypeValues( configurationElement, fieldValue, entry );
                        }

                        else if ( OlcConfig.class.isAssignableFrom( fieldClass ) )
                        {
                            // Checking if we're dealing with a AdsBaseBean subclass type
                            addConfigurationBean( ( OlcConfig ) fieldValue, entry.getDn() );
                        }
                    }
                }
            }
        }
    }


    // ── getDn — R2 Computes the DN for a Config Bean ──────────────────────────────
    // R2-D2 scans the bean's field hierarchy for the field marked as the RDN
    // (@ConfigurationElement with isRdn=true). He uses that field's value to
    // construct the entry's full DN relative to the parent DN.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Computes the LDAP distinguished name (DN) for a configuration bean.
     * We scan up the class hierarchy looking for a field annotated with
     * {@code @ConfigurationElement(isRdn = true)} and use its value as the RDN.
     * If the bean has a pre-set parent DN, that is used; otherwise the given
     * {@code parentDn} is used.
     * Returns {@link Dn#EMPTY_DN} if no RDN field is found.
     *
     * <p>For example — R2 computes the DN for a database config bean:</p>
     * <pre>
     *   Dn dn = getDn( olcMdbConfig, configurationDn );
     *   // dn == "olcDatabase={1}mdb,cn=config"
     * </pre>
     *
     * @param bean      the configuration bean to compute the DN for
     * @param parentDn  the parent DN to append the RDN to
     * @return  the full DN for this bean, or {@link Dn#EMPTY_DN} if no RDN field was found
     * @throws LdapInvalidDnException             if the DN cannot be constructed
     * @throws LdapInvalidAttributeValueException if the RDN attribute value is invalid
     * @throws IllegalAccessException             if a field cannot be read
     */
    private Dn getDn( OlcConfig bean, Dn parentDn ) throws LdapInvalidDnException, LdapInvalidAttributeValueException,
        IllegalAccessException
    {
        // Getting the class of the bean
        Class<?> beanClass = bean.getClass();

        // A flag to know when we reached the 'AdsBaseBean' class when
        // looping on the class hierarchy of the bean
        boolean olcConfigBeanClassFound = false;

        // Looping until the 'OlcConfig' class has been found
        while ( !olcConfigBeanClassFound )
        {
            // Checking if we reached the 'OlcConfig' class
            if ( beanClass == OlcConfig.class )
            {
                olcConfigBeanClassFound = true;
            }

            // Looping on all fields of the bean
            for ( Field field : beanClass.getDeclaredFields() )
            {
                // Making the field accessible (we get an exception if we don't do that)
                field.setAccessible( true );

                // Looking for the @ConfigurationElement annotation and
                // if the field is the Rdn
                ConfigurationElement configurationElement = field.getAnnotation( ConfigurationElement.class );

                if ( ( configurationElement != null ) && ( configurationElement.isRdn() ) )
                {
                    Object value = field.get( bean );

                    if ( value == null )
                    {
                        continue;
                    }

                    // Is the value multiple?
                    if ( isMultiple( value.getClass() ) )
                    {
                        Collection<?> values = ( Collection<?> ) value;

                        if ( values.isEmpty() )
                        {
                            String defaultValue = configurationElement.defaultValue();

                            if ( defaultValue != null )
                            {
                                value = defaultValue;
                            }
                            else
                            {
                                continue;
                            }
                        }
                        else
                        {
                            value = values.toArray()[0];
                        }
                    }

                    if ( ( bean.getParentDn() != null ) )
                    {
                        return bean.getParentDn()
                            .add( new Rdn( configurationElement.attributeType(), value.toString() ) );
                    }
                    else
                    {
                        return parentDn.add( new Rdn( configurationElement.attributeType(), value.toString() ) );
                    }
                }
            }

            // Moving to the upper class in the class hierarchy
            beanClass = beanClass.getSuperclass();
        }

        return Dn.EMPTY_DN;
    }


    // ── getObjectClassNameForBean — R2 Maps a Java Class Name to an LDAP Object Class
    // R2-D2 converts the Java class name to the LDAP object class name by lower-casing
    // the first letter. So "OlcMdbConfig" becomes "olcMdbConfig" — the object class
    // name in the schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP object class name corresponding to the given Java bean class.
     * The convention is simply to lower-case the first letter of the class name
     * (stripped of its package): {@code OlcMdbConfig} -> {@code olcMdbConfig}.
     *
     * @param clazz  the Java bean class
     * @return  the corresponding LDAP object class name
     */
    private String getObjectClassNameForBean( Class<?> clazz )
    {
        String classNameWithPackage = getClassNameWithoutPackageName( clazz );

        return Character.toLowerCase( classNameWithPackage.charAt( 0 ) ) + classNameWithPackage.substring( 1 );
    }


    // ── getClassNameWithoutPackageName — R2 Strips the Package Prefix ────────────
    // R2-D2 takes the fully-qualified class name and strips everything up to and
    // including the last dot, leaving just the simple class name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the simple class name of the given class, without the package prefix.
     * For example, {@code org.apache.directory.OlcMdbConfig} returns {@code OlcMdbConfig}.
     *
     * @param clazz  the class to get the simple name for
     * @return  the simple class name
     */
    private String getClassNameWithoutPackageName( Class<?> clazz )
    {
        String className = clazz.getName();

        int firstChar = className.lastIndexOf( '.' ) + 1;

        if ( firstChar > 0 )
        {
            return className.substring( firstChar );
        }

        return className;
    }


    // ── writeToPath — R2 Writes the LDIF to a File Path ──────────────────────────
    // R2-D2 writes the encoded plans to the path given. He delegates to writeToFile.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the configuration as LDIF to the file at the given path.
     * Delegates to {@link #writeToFile(File)}.
     *
     * <p>For example — R2 writes plans to a file path:</p>
     * <pre>
     *   writer.writeToPath( "/etc/openldap/slapd.d/config.ldif" );
     * </pre>
     *
     * @param path  the output file path
     * @throws ConfigurationException  if the configuration cannot be serialised
     * @throws IOException             if the file cannot be written
     */
    public void writeToPath( String path ) throws ConfigurationException, IOException
    {
        writeToFile( new File( path ) );
    }


    // ── writeToFile — R2 Writes the LDIF to a File Object ────────────────────────
    // R2-D2 opens the target file and streams the LDIF string into it. He uses
    // writeToString to do the serialisation and then writes the result to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the configuration as LDIF to the given file.
     * Calls {@link #writeToString()} to serialise the configuration, then writes
     * the result to the file via a {@link FileWriter}.
     *
     * <p>For example — R2 writes plans to a File object:</p>
     * <pre>
     *   writer.writeToFile( new File( "/tmp/slapd.ldif" ) );
     * </pre>
     *
     * @param file  the output file
     * @throws ConfigurationException  if the configuration cannot be serialised
     * @throws IOException             if the file cannot be written
     */
    public void writeToFile( File file ) throws ConfigurationException, IOException
    {
        // Writing the file to disk
        try ( FileWriter writer = new FileWriter( file ) )
        {
            writer.append( writeToString() );
        }
    }


    // ── writeToString — R2 Encodes All Plans to an LDIF String ───────────────────
    // R2-D2 serialises the entire configuration to a single LDIF string — version
    // header followed by all the entries. This is the complete data payload ready
    // to be written to a file or sent to a server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts the entire configuration to a single LDIF string.
     * Triggers {@link #convertConfigurationBeanToLdifEntries(Dn)} to build the
     * entry list, then concatenates them with the standard "version: 1" header.
     *
     * <p>For example — R2 produces the full LDIF string:</p>
     * <pre>
     *   String ldif = writer.writeToString();
     *   // "version: 1\ndn: cn=config\n..."
     * </pre>
     *
     * @return  the LDIF string representing the full configuration
     * @throws ConfigurationException  if the configuration cannot be serialised
     */
    public String writeToString() throws ConfigurationException
    {
        // Converting the configuration bean to a list of LDIF entries
        convertConfigurationBeanToLdifEntries( ConfigurationUtils.getConfigurationDn( browserConnection ) );

        // Building the StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append( "version: 1\n" );

        for ( LdifEntry entry : entries )
        {
            sb.append( entry.toString() );
        }

        return sb.toString();
    }


    // ── getConvertedLdifEntries() — R2 Returns the Encoded Entries (Live Server Mode)
    // R2-D2 triggers encoding against the live server's configuration DN and returns
    // the list of LDIF entries ready for uploading.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of {@link LdifEntry} objects representing the serialised
     * configuration, using the browser connection's configuration DN as the base.
     * Triggers encoding if not already done.
     *
     * <p>For example — R2 returns encoded entries for live server upload:</p>
     * <pre>
     *   List&lt;LdifEntry&gt; entries = writer.getConvertedLdifEntries();
     * </pre>
     *
     * @return  the list of serialised LDIF entries
     * @throws ConfigurationException  if the configuration cannot be serialised
     */
    public List<LdifEntry> getConvertedLdifEntries() throws ConfigurationException
    {
        // Converting the configuration bean to a list of LDIF entries
        convertConfigurationBeanToLdifEntries( ConfigurationUtils.getConfigurationDn( browserConnection ) );

        // Returning the list of entries
        return entries;
    }


    // ── getConvertedLdifEntries(Dn) — R2 Returns Entries Using a Specific Base DN ─
    // R2-D2 uses the explicitly provided configuration DN instead of looking it up
    // from the browser connection — useful for file-based output with a known base DN.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of {@link LdifEntry} objects using the given configuration DN
     * as the base. Use this when you want to specify the base DN explicitly rather
     * than derive it from the browser connection.
     *
     * <p>For example — R2 returns entries with an explicit base DN:</p>
     * <pre>
     *   Dn configDn = new Dn( "cn=config" );
     *   List&lt;LdifEntry&gt; entries = writer.getConvertedLdifEntries( configDn );
     * </pre>
     *
     * @param configurationDn  the base DN for the cn=config tree
     * @return  the list of serialised LDIF entries
     * @throws ConfigurationException  if the configuration cannot be serialised
     */
    public List<LdifEntry> getConvertedLdifEntries( Dn configurationDn ) throws ConfigurationException
    {
        // Converting the configuration bean to a list of LDIF entries
        convertConfigurationBeanToLdifEntries( configurationDn );

        // Returning the list of entries
        return entries;
    }


    // ── addObjectClassAttribute — R2 Adds the objectClass Attribute Hierarchy ─────
    // R2-D2 looks up the object class in the schema, walks up its superior chain,
    // and adds each object class name to the entry's objectClass attribute. This
    // ensures the LDIF entry has the full objectClass hierarchy (top, olcConfig,
    // olcDatabaseConfig, olcMdbConfig, etc.).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code objectClass} attribute to an LDIF entry, including all
     * superiors of the given object class (walking up the schema hierarchy to top).
     * We use the plugin's schema manager to look up the object class and its chain.
     *
     * @param entry        the LDIF entry to add objectClass values to
     * @param objectClass  the name of the structural object class for this entry
     * @throws LdapException  if the object class is not found in the schema
     */
    private void addObjectClassAttribute( LdifEntry entry, String objectClass )
        throws LdapException
    {
        try
        {
            ObjectClass objectClassObject = OpenLdapServerConfigurationEditorUtils.getObjectClass( OpenLdapConfigurationPlugin
                .getDefault().getSchemaManager(), objectClass );

            if ( objectClassObject != null )
            {
                // Building the list of 'objectClass' attribute values
                Set<String> objectClassAttributeValues = new HashSet<>();
                computeObjectClassAttributeValues( objectClassAttributeValues, objectClassObject );

                // Adding values to the entry
                addAttributeTypeValues( SchemaConstants.OBJECT_CLASS_AT, objectClassAttributeValues, entry );
            }
            else
            {
                // TODO: throw an exception
            }
        }
        catch ( Exception e )
        {
            throw new LdapException( e );
        }
    }


    // ── computeObjectClassAttributeValues — R2 Walks the Object Class Hierarchy ───
    // R2-D2 recursively walks up the schema hierarchy from the given object class to
    // the top, collecting each class name along the way. This builds the complete
    // set of objectClass values needed for the entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recursively collects all object class names from the given class up to
     * {@code top}, building the complete set of {@code objectClass} attribute values.
     * We stop recursing when we hit {@code top} or when there are no more superiors.
     *
     * @param objectClassAttributeValues  the accumulator set of object class names
     * @param objectClass                 the current object class to process
     * @throws LdapException  if the schema manager cannot be accessed
     */
    private void computeObjectClassAttributeValues( Set<String> objectClassAttributeValues, ObjectClass objectClass )
        throws LdapException
    {
        try
        {
            SchemaManager schemaManager = OpenLdapConfigurationPlugin.getDefault().getSchemaManager();

            ObjectClass topObjectClass = OpenLdapServerConfigurationEditorUtils.getObjectClass( schemaManager,
                SchemaConstants.TOP_OC );

            if ( topObjectClass != null )
            {
                // TODO throw new exception (there should be a top object class
            }

            if ( topObjectClass.equals( objectClass ) )
            {
                objectClassAttributeValues.add( objectClass.getName() );
            }
            else
            {
                objectClassAttributeValues.add( objectClass.getName() );

                List<String> superiors = objectClass.getSuperiorOids();

                if ( ( superiors != null ) && !superiors.isEmpty() )
                {
                    for ( String superior : superiors )
                    {
                        ObjectClass superiorObjectClass = OpenLdapServerConfigurationEditorUtils.getObjectClass( schemaManager,
                            superior );
                        computeObjectClassAttributeValues( objectClassAttributeValues, superiorObjectClass );
                    }
                }
                else
                {
                    objectClassAttributeValues.add( topObjectClass.getName() );
                }
            }
        }
        catch ( Exception e )
        {
            throw new LdapException( e );
        }
    }


    // ── addAttributeTypeValues(ConfigurationElement, Object, LdifEntry) — R2 Encodes a Field
    // R2-D2 reads a field value (which may be a collection or a single value) and
    // adds it to the LDIF entry. For empty optional collections, nothing is added.
    // For empty required collections, the default value from the annotation is used.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the value(s) from a {@link ConfigurationElement}-annotated field to an
     * LDIF entry. Handles both single values and collections. For empty optional
     * collections, nothing is written. For empty required collections, the annotation's
     * {@code defaultValue} is used instead.
     *
     * @param configurationElement  the annotation holding the attribute type and default value
     * @param o                     the field value to encode
     * @param entry                 the LDIF entry to add the attribute to
     * @throws LdapException  if the attribute value cannot be added
     */
    private void addAttributeTypeValues( ConfigurationElement configurationElement, Object o, LdifEntry entry )
        throws LdapException
    {
        String attributeType = configurationElement.attributeType();

        // We don't store a 'null' value
        if ( o != null )
        {
            // Is the value multiple?
            if ( isMultiple( o.getClass() ) )
            {
                // Adding each single value separately
                Collection<?> values = ( Collection<?> ) o;

                if ( values.isEmpty() )
                {
                    if ( !configurationElement.isOptional() )
                    {
                        // Add the default value
                        addAttributeTypeValue( attributeType, configurationElement.defaultValue(), entry );
                    }
                }
                else
                {
                    for ( Object value : values )
                    {
                        addAttributeTypeValue( attributeType, value, entry );
                    }
                }
            }
            else
            {
                // Adding the single value
                addAttributeTypeValue( attributeType, o, entry );
            }
        }
    }


    // ── addAttributeTypeValues(String, Object, LdifEntry) — R2 Encodes a Raw Value Set
    // R2-D2 handles the case where we already have the attribute type name and a raw
    // value (or collection). This is used for objectClass values which aren't backed
    // by a @ConfigurationElement annotation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds values for a given attribute type to an LDIF entry, where the value may
     * be a single object or a collection. Used internally for objectClass attribute
     * population (which doesn't have a {@link ConfigurationElement} annotation).
     *
     * @param attributeType  the LDAP attribute type name
     * @param object         the value(s) to add — may be a {@link Collection} or a single value
     * @param entry          the LDIF entry to add the attribute to
     * @throws LdapException  if the attribute value cannot be added
     */
    private void addAttributeTypeValues( String attributeType, Object object, LdifEntry entry )
        throws LdapException
    {
        // We don't store a 'null' value
        if ( object != null )
        {
            // Is the value multiple?
            if ( isMultiple( object.getClass() ) )
            {
                // Adding each single value separately
                Collection<?> values = ( Collection<?> ) object;

                for ( Object value : values )
                {
                    addAttributeTypeValue( attributeType, value, entry );
                }
            }
            else
            {
                // Adding the single value
                addAttributeTypeValue( attributeType, object, entry );
            }
        }
    }


    // ── addAttributeTypeValue — R2 Adds One Attribute Value to an Entry ──────────
    // R2-D2 adds a single value to the right attribute in the entry. If the attribute
    // doesn't exist yet, he creates it. byte[] values go in raw; Booleans go in
    // uppercase (TRUE/FALSE as LDAP requires); everything else is toString().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single attribute value to an LDIF entry.
     * Creates the attribute if it doesn't exist yet. Handles three value formats:
     * <ul>
     *   <li>{@code byte[]} — stored as binary</li>
     *   <li>{@code Boolean} — stored as uppercase "TRUE" or "FALSE" (LDAP requirement)</li>
     *   <li>anything else — stored as {@link Object#toString()}</li>
     * </ul>
     *
     * @param attributeType  the LDAP attribute type name
     * @param value          the value to add
     * @param entry          the LDIF entry to add the attribute value to
     * @throws LdapException  if the value cannot be added to the attribute
     */
    private void addAttributeTypeValue( String attributeType, Object value, LdifEntry entry ) throws LdapException
    {
        // We don't store a 'null' value
        if ( value != null )
        {
            // Getting the attribute from the entry
            Attribute attribute = entry.get( attributeType );

            // If no attribute has been found, we need to create it and add it to the entry
            if ( attribute == null )
            {
                attribute = new DefaultAttribute( attributeType );
                entry.addAttribute( attribute );
            }

            // Storing the value to the attribute
            if ( value instanceof byte[] )
            {
                // Value is a byte[]
                attribute.add( ( byte[] ) value );
            }
            // Storing the boolean value in UPPERCASE (TRUE or FALSE) to the attribute
            else if ( value instanceof Boolean )
            {
                // Value is a byte[]
                attribute.add( value.toString().toUpperCase() );
            }
            else
            {
                // Value is another type of object that we store as a String
                // (There will be an automatic translation for primary types like int, long, etc.)
                attribute.add( value.toString() );
            }
        }
    }


    // ── isMultiple — R2 Checks If a Field Holds Multiple Values ──────────────────
    // R2-D2 checks whether a field is a collection type (List, Set, etc.) so he
    // knows whether to iterate over it or treat it as a single value.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given class is a {@link Collection} subtype (e.g., List or Set).
     * Used to decide whether to iterate over a field's value or treat it as a single item.
     *
     * @param clazz  the class to check
     * @return  {@code true} if the class is a collection; {@code false} otherwise
     */
    private boolean isMultiple( Class<?> clazz )
    {
        return Collection.class.isAssignableFrom( clazz );
    }
}
