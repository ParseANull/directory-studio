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
package org.apache.directory.studio.openldap.config.acl;


import java.text.ParseException;

import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.openldap.config.acl.dialogs.OpenLdapAclDialog;
import org.apache.directory.studio.openldap.config.acl.model.AclItem;
import org.apache.directory.studio.openldap.config.acl.model.OpenLdapAclParser;


// ── CLASS: OpenLdapAclValueWithContext — LEIA'S HOLOGRAM MESSAGE IN A CAPSULE ──
// Princess Leia doesn't just shout her message into empty space — she records
// it into R2-D2 with everything the recipient needs: who is sending it, which
// ship it came from, the priority level, and the actual text. This class is
// that capsule: we bundle the LDAP connection, the directory entry being
// edited, an optional precedence number, the raw ACL string, and a parsed
// AclItem into one tidy object that the dialog can open and work with.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A context bag passed to {@link OpenLdapAclDialog} when we open the ACL
 * editor. It carries everything the dialog needs to operate: the LDAP
 * connection (for browsing DNs), the LDAP entry being edited, an optional
 * precedence value, the raw ACL string, and the pre-parsed {@link AclItem}
 * model. We parse the raw ACL string in the constructor so the dialog gets
 * a ready-to-use model right away.
 * Think of this class as Princess Leia's hologram message capsule — it has
 * the sender, the recipient context, the priority rank, and the actual content,
 * all sealed up together for transport.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclValueWithContext
{
    /** The connection used to browse the directory */
    private IBrowserConnection connection;

    /** The entry */
    private IEntry entry;

    /** The precedence value, -1 means no precedence */
    private int precedence = -1;

    /** The ACL value */
    private String aclValue;

    /** The ACL instance */
    private AclItem aclItem;

    /** A reference to the ACL dialog */
    private OpenLdapAclDialog aclDialog;

    /** The ACL parser */
    private static final OpenLdapAclParser parser = new OpenLdapAclParser();


    // ── Recording the Message Into the Capsule ───────────────────────────────
    // Leia walks up to R2 and speaks: "You are my only hope." R2 captures the
    // connection context, the target entry, her priority level, and her words,
    // then immediately converts the words to structured data so anyone who
    // opens the capsule gets the fully decoded version.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a new context capsule from all the pieces the dialog will need.
     * We immediately attempt to parse {@code aclValue} into an {@link AclItem}
     * so the dialog can start with a live model. If parsing fails we print the
     * stack trace but do not throw — the dialog can still open in raw-text mode.
     *
     * <p>For example — Leia recording her holographic briefing for Obi-Wan:</p>
     * <pre>
     *   OpenLdapAclValueWithContext ctx = new OpenLdapAclValueWithContext(
     *       connection, entry, 2, "access to dn=\"ou=Rebels\" by users read");
     *   // ctx.getAclItem() is a parsed AclItem ready for the dialog
     * </pre>
     *
     * @param connection  The LDAP browser connection — used to resolve and
     *                    browse DN values in the editor dialogs.
     * @param entry       The LDAP directory entry whose ACL we are editing.
     * @param precedence  The {@code {N}} ordering prefix, or {@code -1} if none.
     * @param aclValue    The raw ACL text string, e.g. {@code "access to * by users read"}.
     */
    public OpenLdapAclValueWithContext( IBrowserConnection connection, IEntry entry, int precedence, String aclValue )
    {
        this.connection = connection;
        this.entry = entry;
        this.precedence = precedence;
        this.aclValue = aclValue;

        try
        {
            aclItem = parser.parse( aclValue );
        }
        catch ( ParseException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // ── Reading the Message Text ──────────────────────────────────────────────
    // Obi-Wan plays back the hologram and first reads the raw message: the
    // exact words Leia spoke, before any interpretation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw ACL text string exactly as it was passed into the
     * constructor. This is the unprocessed value before any parsing — useful
     * when we need to persist or display the canonical string form.
     *
     * <p>For example — reading Leia's unedited message text:</p>
     * <pre>
     *   String raw = ctx.getAclValue();
     *   // "access to dn.exact=\"ou=Rebels,dc=galaxy,dc=far\" by users read"
     * </pre>
     *
     * @return  The raw ACL string; may be {@code null} if none was provided.
     */
    public String getAclValue()
    {
        return aclValue;
    }


    // ── Identifying the Sending Ship ──────────────────────────────────────────
    // Before Obi-Wan acts on the message he checks which ship it came from —
    // the LDAP connection is our "sending ship", the live link to the directory.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP browser connection attached to this context. The
     * connection lets UI widgets browse the directory — for example, to pick
     * a DN value using an entry browser.
     *
     * <p>For example — checking which ship the hologram was transmitted from:</p>
     * <pre>
     *   IBrowserConnection conn = ctx.getConnection();
     *   // conn.getRootDSE() gives us the top of the directory tree
     * </pre>
     *
     * @return  The {@link IBrowserConnection}; may be {@code null} in unit tests.
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── Identifying the Target Entry ──────────────────────────────────────────
    // The message is intended for a specific person at a specific location.
    // The IEntry is that location — the directory entry whose ACL we are editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP directory entry this ACL belongs to. We need this so the
     * editor can display or default to the entry's DN and schema information.
     *
     * <p>For example — identifying the intended recipient of Leia's message:</p>
     * <pre>
     *   IEntry entry = ctx.getEntry();
     *   // entry.getDn() is the base DN for the entry being edited
     * </pre>
     *
     * @return  The {@link IEntry}; may be {@code null} in some call paths.
     */
    public IEntry getEntry()
    {
        return entry;
    }


    // ── Checking the Priority Level ───────────────────────────────────────────
    // Leia's messages have a priority stamp: messages marked {0} are processed
    // before those marked {1}. We store that number as the precedence value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACL precedence value — the number in the {@code {N}} prefix
     * that determines which ACL rule takes effect when multiple rules match.
     * Lower numbers win. Returns {@code -1} if no precedence is set.
     *
     * <p>For example — Leia stamping a message as Priority 0:</p>
     * <pre>
     *   int p = ctx.getPrecedence();
     *   // p == 2  →  {2} is prepended to the ACL string when saving
     *   // p == -1 →  no precedence prefix
     * </pre>
     *
     * @return  The precedence integer, or {@code -1} if unset.
     */
    public int getPrecedence()
    {
        return precedence;
    }


    // ── Checking Whether a Priority Stamp Exists ─────────────────────────────
    // Before acting on the priority number Obi-Wan first checks whether
    // the message actually has one, or if it was sent without a rank.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells us whether a precedence value was specified. We use this to decide
     * whether to prepend the {@code {N}} prefix when serialising the ACL back
     * to a string. If this returns {@code false} we skip the prefix entirely.
     *
     * <p>For example — checking whether the message carries a priority stamp:</p>
     * <pre>
     *   if (ctx.hasPrecedence()) {
     *       output = "{" + ctx.getPrecedence() + "}" + ctx.getAclValue();
     *   }
     * </pre>
     *
     * @return  {@code true} if precedence is set (i.e. not {@code -1}).
     */
    public boolean hasPrecedence()
    {
        return precedence != -1;
    }


    // ── Retrieving the Decoded Message Model ─────────────────────────────────
    // After C-3PO has translated the raw words into a structured model —
    // what it applies to, who gets access, and at what level — Obi-Wan can
    // work with the decoded AclItem directly without re-reading the raw text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AclItem} model that was parsed from the raw ACL string
     * in the constructor. This is the structured representation the visual editor
     * operates on — it knows the what-clause, the who-clauses, and the access
     * levels without needing to parse text again.
     *
     * <p>For example — Obi-Wan reading the decoded structured briefing:</p>
     * <pre>
     *   AclItem item = ctx.getAclItem();
     *   item.getWhatClause(); // the resource being protected
     *   item.getWhoClauses(); // who gets access and at what level
     * </pre>
     *
     * @return  The parsed {@link AclItem}; may be {@code null} if parsing failed.
     */
    public AclItem getAclItem()
    {
        return aclItem;
    }


    // ── Finding the Dialog That Opened This Context ───────────────────────────
    // R2 keeps a back-reference to the holodeck that is currently playing
    // Leia's message, so other systems can call back into the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link OpenLdapAclDialog} that is currently displaying this
     * context, or {@code null} if no dialog is open. Widgets inside the dialog
     * use this back-reference to trigger a re-render when the model changes.
     *
     * <p>For example — a sub-widget refreshing the parent dialog's OK button state:</p>
     * <pre>
     *   OpenLdapAclDialog dlg = ctx.getAclDialog();
     *   if (dlg != null) dlg.refresh();
     * </pre>
     *
     * @return  The active {@link OpenLdapAclDialog}, or {@code null}.
     */
    public OpenLdapAclDialog getAclDialog()
    {
        return aclDialog;
    }


    // ── Registering Which Dialog Is Displaying This Context ───────────────────
    // When the holodeck powers up to play Leia's message it tells R2 where it
    // is so R2 can relay callbacks. The dialog calls this setter when it opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the {@link OpenLdapAclDialog} that opened this context. Called
     * by the dialog itself right after construction. The back-reference lets
     * sub-widgets trigger a full dialog refresh when something changes deep in
     * the composite tree.
     *
     * <p>For example — the holodeck registering itself with R2:</p>
     * <pre>
     *   ctx.setAclDialog(this); // called inside OpenLdapAclDialog constructor
     * </pre>
     *
     * @param aclDialog  The dialog now displaying this context.
     */
    public void setAclDialog( OpenLdapAclDialog aclDialog )
    {
        this.aclDialog = aclDialog;
    }


    // ── Rendering the Full Message as a String ────────────────────────────────
    // When R2 plays back the hologram he combines the priority stamp with
    // the message content into a single transmittable string that OpenLDAP
    // can write directly into the directory.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this context back to the ACL string form that OpenLDAP
     * understands. If a precedence value was set we prepend it as {@code {N}}.
     * Returns an empty string if the ACL value itself is {@code null}.
     *
     * <p>For example — R2 transmitting Leia's full prioritised message:</p>
     * <pre>
     *   ctx.toString()
     *   // → "{2}access to * by users read"   (with precedence)
     *   // → "access to * by users read"       (without precedence)
     *   // → ""                                (no ACL value at all)
     * </pre>
     *
     * @return  The fully serialised ACL string, possibly with a {@code {N}} prefix.
     */
    public String toString()
    {
        if ( aclValue != null )
        {
            if ( precedence != -1 )
            {
                return "{" + precedence + "}" + aclValue;
            }
            else
            {
                return aclValue;
            }
        }

        return "";
    }
}
