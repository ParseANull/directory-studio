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


import java.util.List;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateForm;


// ── INTERFACE: Template — THE DEATH STAR BLUEPRINTS CONTRACT ─────────────────────
// When Princess Leia encoded the Death Star blueprints in R2-D2, those blueprints
// had a well-defined structure: an ID, a title, a structural type (superlaser
// turbo-laser), and a list of auxiliary systems (tractor beams, hangar bays).
// This interface is that contract: every template must have an ID, a title, a
// structural LDAP object class (the entry's core type), optional auxiliary object
// classes (the extras), and a root form that describes its UI layout.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Contract that every LDAP entry template must satisfy. A template describes which
 * LDAP object classes an entry must have for the template to apply, and carries
 * the root {@link TemplateForm} that lays out the editor UI. Think of it as the
 * Death Star blueprints: a precise spec every implementation must follow.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface Template
{
    // ── ADD AUXILIARY OBJECT CLASS: ATTACH AN OPTIONAL SYSTEM ────────────────────
    /**
     * Adds an optional LDAP auxiliary object class. An entry must have this object
     * class (on top of the structural one) for the template to match.
     *
     * @param objectClass  the auxiliary object class name to add (e.g. "extensibleObject")
     * @return {@code true} if the template didn't already contain the specified class
     */
    boolean addAuxiliaryObjectClass( String objectClass );


    // ── GET AUXILIARY OBJECT CLASSES: LIST OPTIONAL SYSTEMS ──────────────────────
    /**
     * Returns the list of auxiliary LDAP object classes this template requires.
     *
     * @return the list of auxiliary object class names; may be empty, never {@code null}
     */
    List<String> getAuxiliaryObjectClasses();


    // ── GET FORM: RETRIEVE THE ROOT UI BLUEPRINT ──────────────────────────────────
    /**
     * Returns the root {@link TemplateForm} that describes the editor's top-level
     * layout for this template.
     *
     * @return the root form; may be {@code null} if not yet set
     */
    TemplateForm getForm();


    // ── GET ID: RETRIEVE THE UNIQUE TEMPLATE IDENTIFIER ──────────────────────────
    /**
     * Returns the unique identifier for this template (e.g. "com.example.person").
     * IDs must start with a letter and contain only letters, digits, hyphens, or dots.
     *
     * @return the template ID
     */
    String getId();


    // ── GET STRUCTURAL OBJECT CLASS: RETRIEVE THE CORE TYPE ──────────────────────
    /**
     * Returns the structural LDAP object class that the target entry must carry
     * for this template to apply (e.g. "inetOrgPerson").
     *
     * @return the structural object class name
     */
    String getStructuralObjectClass();


    // ── GET TITLE: RETRIEVE THE DISPLAY TITLE ────────────────────────────────────
    /**
     * Returns the human-readable title shown in the UI when this template is
     * selected (e.g. "Person Entry").
     *
     * @return the template title
     */
    String getTitle();


    // ── REMOVE AUXILIARY OBJECT CLASS: DETACH AN OPTIONAL SYSTEM ─────────────────
    /**
     * Removes an auxiliary LDAP object class from the required list.
     *
     * @param objectClass  the auxiliary object class name to remove
     * @return {@code true} if the template contained the specified class
     */
    boolean removeAuxiliaryObjectClass( String objectClass );


    // ── SET AUXILIARY OBJECT CLASSES: REPLACE THE OPTIONAL SYSTEMS LIST ───────────
    /**
     * Replaces the entire auxiliary object class list.
     *
     * @param objectClasses  the new list of auxiliary object class names
     */
    void setAuxiliaryObjectClasses( List<String> objectClasses );


    // ── SET FORM: INSTALL THE ROOT UI BLUEPRINT ──────────────────────────────────
    /**
     * Sets the root {@link TemplateForm} that describes the editor layout.
     *
     * @param form  the root form
     */
    void setForm( TemplateForm form );


    // ── SET ID: ASSIGN THE UNIQUE TEMPLATE IDENTIFIER ────────────────────────────
    /**
     * Sets the unique identifier for this template.
     *
     * @param id  the template ID (must match {@code [a-zA-Z][a-zA-Z0-9-.]*})
     */
    void setId( String id );


    // ── SET STRUCTURAL OBJECT CLASS: DEFINE THE CORE TYPE ────────────────────────
    /**
     * Sets the structural LDAP object class the entry must carry for this template
     * to apply.
     *
     * @param objectClass  the structural object class name (e.g. "inetOrgPerson")
     */
    void setStructuralObjectClass( String objectClass );


    // ── SET TITLE: ASSIGN THE DISPLAY TITLE ──────────────────────────────────────
    /**
     * Sets the human-readable title shown in template-picker UI.
     *
     * @param title  the display title (e.g. "Person Entry")
     */
    void setTitle( String title );
}
