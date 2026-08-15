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
package org.apache.directory.studio.templateeditor.model;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateForm;


// ── CLASS: AbstractTemplate — THE DEATH STAR BLUEPRINTS BASE FRAME ───────────────
// When Galen Erso designed the Death Star, he built a common structural skeleton
// that every variant shared: an ID (the station designation), a title (its informal
// name), a structural classification (superlaser battle station), and a list of
// optional subsystems (tractor beams, hangar bays). This abstract class is that
// skeleton — every concrete template (FileTemplate, ExtensionPointTemplate) extends
// it and inherits all the standard metadata fields and list management methods.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Base implementation of {@link Template} that stores the common metadata shared by
 * all template types: ID, title, structural object class, auxiliary object class list,
 * and the root {@link TemplateForm}. Concrete subclasses (e.g. {@link FileTemplate},
 * {@link ExtensionPointTemplate}) add only what's specific to their origin.
 * Think of this as the Death Star blueprints base frame.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractTemplate implements Template
{
    // ── IS VALID ID: VALIDATE THE TEMPLATE IDENTIFIER FORMAT ─────────────────────
    // The Empire's naming conventions are strict: a template ID must start with a
    // letter and contain only letters, digits, hyphens, or dots. No spaces, no
    // underscores — this enforces the OSGi-style naming convention.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given string is a valid template ID. Valid IDs
     * start with a letter and contain only letters, digits, hyphens, or dots —
     * matching the pattern {@code [a-zA-Z][a-zA-Z0-9-.]*}.
     *
     * @param id  the candidate ID string to validate
     * @return {@code true} if the ID is valid
     */
    public static boolean isValidId( String id )
    {
        return Pattern.matches( "[a-zA-Z][a-zA-Z0-9-.]*", id ); //$NON-NLS-1$
    }

    /** The ID */
    private String id;

    /** The title */
    private String title;

    /** The structural object class */
    private String structuralObjectClass;

    /** The list of auxiliary object classes */
    private List<String> auxiliaryObjectClasses;

    /** The form */
    private TemplateForm form;


    // ── DEFAULT CONSTRUCTOR: INITIALISE THE BASE FRAME ───────────────────────────
    /**
     * Creates an empty template with no ID, no title, and an empty auxiliary object
     * class list. Subclasses must call {@code setId()} and {@code setTitle()} before
     * registering the template.
     */
    public AbstractTemplate()
    {
        init();
    }


    // ── ID CONSTRUCTOR: INITIALISE WITH A KNOWN ID ────────────────────────────────
    /**
     * Creates a template with the given ID and an empty auxiliary object class list.
     *
     * @param id  the template's unique identifier (e.g. "com.example.person")
     */
    public AbstractTemplate( String id )
    {
        this.id = id;
        init();
    }


    // ── ADD AUXILIARY OBJECT CLASS: ATTACH AN OPTIONAL LDAP SUBSYSTEM ─────────────
    /**
     * Adds an auxiliary LDAP object class to the required list. The target LDAP entry
     * must carry both the structural object class and all auxiliary ones for this
     * template to match.
     *
     * @param objectClass  the auxiliary object class name to add (e.g. "extensibleObject")
     * @return {@code true} if the list didn't already contain the class
     */
    public boolean addAuxiliaryObjectClass( String objectClass )
    {
        return auxiliaryObjectClasses.add( objectClass );
    }


    // ── GET AUXILIARY OBJECT CLASSES: LIST OPTIONAL SUBSYSTEMS ───────────────────
    /**
     * Returns the list of auxiliary LDAP object classes this template requires in
     * addition to the structural one.
     *
     * @return the auxiliary object class list; may be empty, never {@code null}
     */
    public List<String> getAuxiliaryObjectClasses()
    {
        return auxiliaryObjectClasses;
    }


    // ── GET FORM: RETRIEVE THE ROOT UI LAYOUT ────────────────────────────────────
    /**
     * Returns the root {@link TemplateForm} that describes the editor layout for
     * this template.
     *
     * @return the root form; {@code null} if not yet set
     */
    public TemplateForm getForm()
    {
        return form;
    }


    // ── GET ID: RETRIEVE THE UNIQUE IDENTIFIER ────────────────────────────────────
    /**
     * Returns the unique ID of this template (e.g. "com.example.person").
     *
     * @return the template ID
     */
    public String getId()
    {
        return id;
    }


    // ── GET STRUCTURAL OBJECT CLASS: RETRIEVE THE CORE LDAP TYPE ─────────────────
    /**
     * Returns the structural LDAP object class that an entry must have for this
     * template to apply (e.g. "inetOrgPerson").
     *
     * @return the structural object class name
     */
    public String getStructuralObjectClass()
    {
        return structuralObjectClass;
    }


    // ── GET TITLE: RETRIEVE THE DISPLAY NAME ─────────────────────────────────────
    /**
     * Returns the human-readable title of this template (e.g. "Person Entry").
     *
     * @return the display title
     */
    public String getTitle()
    {
        return title;
    }


    // ── INIT: COMMON FIELD INITIALISATION ────────────────────────────────────────
    /**
     * Initializes shared fields — currently just the auxiliary object class list.
     * Called by both constructors.
     */
    private void init()
    {
        auxiliaryObjectClasses = new ArrayList<String>();
    }


    // ── REMOVE AUXILIARY OBJECT CLASS: DETACH AN OPTIONAL SUBSYSTEM ───────────────
    /**
     * Removes an auxiliary LDAP object class from the required list.
     *
     * @param objectClass  the auxiliary object class name to remove
     * @return {@code true} if the list contained the class
     */
    public boolean removeAuxiliaryObjectClass( String objectClass )
    {
        return auxiliaryObjectClasses.remove( objectClass );
    }


    // ── SET AUXILIARY OBJECT CLASSES: REPLACE THE ENTIRE LIST ────────────────────
    /**
     * Replaces the entire auxiliary object class list with the given list.
     *
     * @param objectClasses  the new list of auxiliary object class names
     */
    public void setAuxiliaryObjectClasses( List<String> objectClasses )
    {
        this.auxiliaryObjectClasses = objectClasses;
    }


    // ── SET FORM: INSTALL THE ROOT UI LAYOUT ─────────────────────────────────────
    /**
     * Sets the root {@link TemplateForm} describing this template's editor layout.
     *
     * @param form  the root form
     */
    public void setForm( TemplateForm form )
    {
        this.form = form;
    }


    // ── SET ID: ASSIGN THE UNIQUE IDENTIFIER ─────────────────────────────────────
    /**
     * Sets the unique ID of this template.
     *
     * @param id  the template ID (must satisfy {@link #isValidId(String)})
     */
    public void setId( String id )
    {
        this.id = id;
    }


    // ── SET STRUCTURAL OBJECT CLASS: DEFINE THE CORE LDAP TYPE ───────────────────
    /**
     * Sets the structural LDAP object class an entry must have for this template
     * to apply.
     *
     * @param objectClass  the structural object class name (e.g. "inetOrgPerson")
     */
    public void setStructuralObjectClass( String objectClass )
    {
        structuralObjectClass = objectClass;
    }


    // ── SET TITLE: ASSIGN THE DISPLAY NAME ───────────────────────────────────────
    /**
     * Sets the human-readable title shown in the template picker.
     *
     * @param title  the display title
     */
    public void setTitle( String title )
    {
        this.title = title;
    }


    // ── TO STRING: RETURN THE TEMPLATE'S DISPLAY LABEL ───────────────────────────
    /**
     * Returns the template's title, or a localized "Untitled Template" fallback if
     * no title has been set. Used in list views and combo boxes to identify the
     * template to the user.
     *
     * @return the title or the fallback label
     */
    public String toString()
    {
        return ( title == null ) ? Messages.getString( "AbstractTemplate.UntitledTemplate" ) : title; //$NON-NLS-1$
    }
}
