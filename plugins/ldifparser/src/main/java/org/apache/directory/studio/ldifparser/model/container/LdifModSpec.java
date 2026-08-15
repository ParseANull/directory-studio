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

package org.apache.directory.studio.ldifparser.model.container;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecTypeLine;


// ── CLASS: LdifModSpec — REBEL FIELD MODIFICATION ORDER ──────────────────────
// A Rebel field modification order specifies exactly one attribute operation:
// "add: mail", "delete: cn", or "replace: sn".  The order opens with the
// operation type, lists zero or more new attribute-value lines, then closes
// with a dash separator.
// LdifModSpec is that sub-container: a nested LdifContainer inside a modify
// record that groups one mod-spec-type line, its attribute-value lines, and
// its closing mod-spec-sep line.  It validates that add specs have at least
// one value and that all attribute descriptions match.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for a single modification specification inside a
 * {@code changetype: modify} record.
 * A mod-spec consists of a {@link LdifModSpecTypeLine} ({@code add:},
 * {@code delete:}, or {@code replace:} followed by the attribute name),
 * zero or more {@link LdifAttrValLine}s, and a {@link LdifModSpecSepLine}
 * (the {@code -} separator).
 * Factory methods {@link #createAdd}, {@link #createReplace}, and
 * {@link #createDelete} simplify programmatic construction.
 * Think of this as the Rebel field modification order — one specific change
 * operation on one specific attribute.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifModSpec extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a mod-spec starting with {@code modSpecTypeLine}.
     *
     * @param modSpecTypeLine  the type line declaring the operation and
     *                         attribute name (must not be {@code null})
     */
    public LdifModSpec( LdifModSpecTypeLine modSpecTypeLine )
    {
        super( modSpecTypeLine );
    }


    // ── ADD AN ATTRIBUTE-VALUE LINE ───────────────────────────────────────────
    /**
     * Appends an {@link LdifAttrValLine} to this mod-spec.
     *
     * @param attrVal  the attribute-value line to add (must not be {@code null})
     * @throws IllegalArgumentException if {@code attrVal} is {@code null}
     */
    public void addAttrVal( LdifAttrValLine attrVal )
    {
        if ( attrVal == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( attrVal );
    }


    // ── CLOSE WITH THE MOD-SPEC SEPARATOR ─────────────────────────────────────
    /**
     * Terminates this mod-spec with a {@link LdifModSpecSepLine} ({@code -}).
     *
     * @param modSpecSepLine  the separator line (must not be {@code null})
     * @throws IllegalArgumentException if {@code modSpecSepLine} is {@code null}
     */
    public void finish( LdifModSpecSepLine modSpecSepLine )
    {
        if ( modSpecSepLine == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( modSpecSepLine );
    }


    // ── GET THE MOD-SPEC TYPE LINE ────────────────────────────────────────────
    /**
     * Returns the first part cast to {@link LdifModSpecTypeLine}.
     *
     * @return the mod-spec type line
     */
    public LdifModSpecTypeLine getModSpecType()
    {
        return ( LdifModSpecTypeLine ) ldifParts.get( 0 );
    }


    // ── GET ATTRIBUTE-VALUE LINES ─────────────────────────────────────────────
    /**
     * Returns all {@link LdifAttrValLine} parts in this mod-spec.
     *
     * @return array of attribute-value lines (may be empty)
     */
    public LdifAttrValLine[] getAttrVals()
    {
        List<LdifAttrValLine> ldifAttrValLines = new ArrayList<LdifAttrValLine>();

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifAttrValLine )
            {
                ldifAttrValLines.add( ( LdifAttrValLine ) ldifPart );
            }
        }

        return ldifAttrValLines.toArray( new LdifAttrValLine[ldifAttrValLines.size()] );
    }


    // ── GET THE MOD-SPEC SEPARATOR ────────────────────────────────────────────
    /**
     * Returns the {@link LdifModSpecSepLine} if it is the last part, or
     * {@code null} if the mod-spec has not been closed yet.
     *
     * @return the separator line, or {@code null}
     */
    public LdifModSpecSepLine getModSpecSep()
    {
        LdifPart lastPart = getLastPart();

        if ( lastPart instanceof LdifModSpecSepLine )
        {
            return ( LdifModSpecSepLine ) lastPart;
        }
        else
        {
            return null;
        }
    }


    // ── OPERATION TYPE QUERIES ────────────────────────────────────────────────
    /**
     * Returns {@code true} if this mod-spec is an {@code add:} operation.
     *
     * @return {@code true} for add
     */
    public boolean isAdd()
    {
        return getModSpecType().isAdd();
    }


    /**
     * Returns {@code true} if this mod-spec is a {@code replace:} operation.
     *
     * @return {@code true} for replace
     */
    public boolean isReplace()
    {
        return getModSpecType().isReplace();
    }


    /**
     * Returns {@code true} if this mod-spec is a {@code delete:} operation.
     *
     * @return {@code true} for delete
     */
    public boolean isDelete()
    {
        return getModSpecType().isDelete();
    }


    // ── FACTORY METHODS ───────────────────────────────────────────────────────
    /**
     * Creates an {@code add:} mod-spec for {@code attributeName}.
     *
     * @param attributeName  the attribute to add
     * @return a new {@link LdifModSpec}
     */
    public static LdifModSpec createAdd( String attributeName )
    {
        return new LdifModSpec( LdifModSpecTypeLine.createAdd( attributeName ) );
    }


    /**
     * Creates a {@code replace:} mod-spec for {@code attributeName}.
     *
     * @param attributeName  the attribute to replace
     * @return a new {@link LdifModSpec}
     */
    public static LdifModSpec createReplace( String attributeName )
    {
        return new LdifModSpec( LdifModSpecTypeLine.createReplace( attributeName ) );
    }


    /**
     * Creates a {@code delete:} mod-spec for {@code attributeName}.
     *
     * @param attributeName  the attribute to delete
     * @return a new {@link LdifModSpec}
     */
    public static LdifModSpec createDelete( String attributeName )
    {
        return new LdifModSpec( LdifModSpecTypeLine.createDelete( attributeName ) );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>A mod-spec is valid when:</p>
     * <ul>
     *   <li>the superclass abstract check passes</li>
     *   <li>a mod-spec type line is present</li>
     *   <li>all attribute-value lines have the same attribute description as
     *       the type line (case-insensitive)</li>
     *   <li>an {@code add:} spec has at least one value;
     *       {@code delete:} and {@code replace:} may have zero</li>
     * </ul>
     */
    public boolean isValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        if ( getModSpecType() == null )
        {
            return false;
        }

        String att = getModSpecType().getUnfoldedAttributeDescription();
        int sizeAttrVals = 0;

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifAttrValLine )
            {
                if ( !att.equalsIgnoreCase( ( ( LdifAttrValLine ) ldifPart ).getUnfoldedAttributeDescription() ) )
                {
                    return false;
                }
                else
                {
                    sizeAttrVals++;
                }
            }
        }

        if ( isAdd() )
        {
            return sizeAttrVals > 0;
        }
        else
        {
            return isDelete() || isReplace();
        }
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns a descriptive message for the first validity violation.</p>
     */
    public String getInvalidString()
    {
        if ( getModSpecType() == null )
        {
            return "Missing mod spec line ";
        }

        int sizeAttrVals = 0;
        String att = getModSpecType().getUnfoldedAttributeDescription();

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifAttrValLine )
            {
                if ( !att.equalsIgnoreCase( ( ( LdifAttrValLine ) ldifPart ).getUnfoldedAttributeDescription() ) )
                {
                    return "Attribute descriptions don't match";
                }

                sizeAttrVals++;
            }
        }

        if ( isAdd() && sizeAttrVals == 0 )
        {
            return "Modification must contain attribute value lines ";
        }

        return null;
    }
}
