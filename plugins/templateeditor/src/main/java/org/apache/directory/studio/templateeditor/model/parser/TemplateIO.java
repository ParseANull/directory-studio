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
package org.apache.directory.studio.templateeditor.model.parser;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.directory.studio.templateeditor.model.AbstractTemplate;
import org.apache.directory.studio.templateeditor.model.ExtensionPointTemplate;
import org.apache.directory.studio.templateeditor.model.FileTemplate;
import org.apache.directory.studio.templateeditor.model.Template;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateCheckbox;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateComposite;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateDate;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateFileChooser;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateForm;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateImage;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateLabel;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateLink;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateListbox;
import org.apache.directory.studio.templateeditor.model.widgets.TemplatePassword;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateRadioButtons;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateSection;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateSpinner;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateTable;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateTextField;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateWidget;
import org.apache.directory.studio.templateeditor.model.widgets.ValueItem;
import org.apache.directory.studio.templateeditor.model.widgets.WidgetAlignment;
import org.apache.directory.studio.templateeditor.view.preferences.PreferencesFileTemplate;


// ── CLASS: TemplateIO — C-3PO READING AND WRITING JAWA DIALECT ───────────────────
// On Tatooine, C-3PO is the only protocol droid fluent in Jawa trade talk. When
// Luke needs a pair of droids, C-3PO translates the Jawas' cryptic squeaks into
// understandable descriptions of each unit's capabilities — and when Luke speaks,
// C-3PO encodes his reply back into Jawa. This class is C-3PO doing exactly that:
// it translates between XML (the Jawa dialect — terse, attribute-heavy, not quite
// human-readable) and our Java template model objects (Imperial Basic — structured,
// typed, easy for the rest of the app to reason about). Every read*() method
// decodes one XML element into a model object; every write*() method encodes a
// model object back into XML. The process is strict: a malformed dialect throws
// a {@link TemplateIOException} immediately rather than guessing at the meaning.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static XML serialization/deserialization for {@link Template} objects. Reads
 * template XML from an {@link InputStream} and hydrates a Java template model
 * tree; writes a template model tree out to an {@link OutputStream} as
 * pretty-printed UTF-8 XML. Uses dom4j for XML parsing and generation.
 * Think of this as C-3PO translating Jawa dialect.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateIO
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( TemplateIO.class );

    private static final String THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE = Messages
        .getString( "TemplateIO.FileIsNotAValidTemplateFile" ); //$NON-NLS-1$

    // XML Elements
    private static final String ATTRIBUTE_ATTRIBUTETYPE = "attributeType"; //$NON-NLS-1$
    private static final String ATTRIBUTE_CHARACTERSLIMIT = "charactersLimit"; //$NON-NLS-1$
    private static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$
    private static final String ATTRIBUTE_DIGITS = "digits"; //$NON-NLS-1$
    private static final String ATTRIBUTE_DOLLAR_SIGN_IS_NEW_LINE = "dollarSignIsNewLine"; //$NON-NLS-1$
    private static final String ATTRIBUTE_EXTENSIONS = "extensions"; //$NON-NLS-1$
    private static final String ATTRIBUTE_EQUALCOLUMNS = "equalColumns"; //$NON-NLS-1$
    private static final String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$
    private static final String ATTRIBUTE_EXPANDABLE = "expandable"; //$NON-NLS-1$
    private static final String ATTRIBUTE_EXPANDED = "expanded"; //$NON-NLS-1$
    private static final String ATTRIBUTE_FORMAT = "format"; //$NON-NLS-1$
    private static final String ATTRIBUTE_GRAB_EXCESS_HORIZONTAL_SPACE = "grabExcessHorizontalSpace"; //$NON-NLS-1$
    private static final String ATTRIBUTE_GRAB_EXCESS_VERTICAL_SPACE = "grabExcessVerticalSpace"; //$NON-NLS-1$
    private static final String ATTRIBUTE_HIDDEN = "hidden"; //$NON-NLS-1$
    private static final String ATTRIBUTE_HEIGHT = "height"; //$NON-NLS-1$
    private static final String ATTRIBUTE_HORIZONTAL_ALIGNMENT = "horizontalAlignment"; //$NON-NLS-1$
    private static final String ATTRIBUTE_HORIZONTAL_SPAN = "horizontalSpan"; //$NON-NLS-1$
    private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
    private static final String ATTRIBUTE_IMAGE_HEIGHT = "imageHeight"; //$NON-NLS-1$
    private static final String ATTRIBUTE_IMAGE_WIDTH = "imageWidth"; //$NON-NLS-1$
    private static final String ATTRIBUTE_INCREMENT = "increment"; //$NON-NLS-1$
    private static final String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$
    private static final String ATTRIBUTE_MAXIMUM = "maximum"; //$NON-NLS-1$
    private static final String ATTRIBUTE_MINIMUM = "minimum"; //$NON-NLS-1$
    private static final String ATTRIBUTE_MULTIPLESELECTION = "multipleSelection"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NUMBEROFCOLUMNS = "numberOfColumns"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NUMBEROFROWS = "numberOfRows"; //$NON-NLS-1$
    private static final String ATTRIBUTE_PAGEINCREMENT = "pageIncrement"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWADDBUTTON = "showAddButton"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWBROWSEBUTTON = "showBrowseButton"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWCLEARBUTTON = "showClearButton"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWDELETEBUTTON = "showDeleteButton"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWEDITBUTTON = "showEditButton"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWICON = "showIcon"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWSAVEASBUTTON = "showSaveAsButton"; //$NON-NLS-1$
    private static final String ATTRIBUTE_SHOWSHOWPASSWORDCHECKBOX = "showShowPasswordCheckbox"; //$NON-NLS-1$
    private static final String ATTRIBUTE_TITLE = "title"; //$NON-NLS-1$
    private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
    private static final String ATTRIBUTE_VERTICAL_ALIGNMENT = "verticalAlignment"; //$NON-NLS-1$
    private static final String ATTRIBUTE_VERTICAL_SPAN = "verticalSpan"; //$NON-NLS-1$
    private static final String ATTRIBUTE_WIDTH = "width"; //$NON-NLS-1$
    private static final String ELEMENT_AUXILIARIES = "auxiliaries"; //$NON-NLS-1$
    private static final String ELEMENT_AUXILIARY = "auxiliary"; //$NON-NLS-1$
    private static final String ELEMENT_BUTTON = "button"; //$NON-NLS-1$
    private static final String ELEMENT_BUTTONS = "buttons"; //$NON-NLS-1$
    private static final String ELEMENT_CHECKBOX = "checkbox"; //$NON-NLS-1$
    private static final String ELEMENT_CHECKEDVALUE = "checkedValue"; //$NON-NLS-1$
    private static final String ELEMENT_COMPOSITE = "composite"; //$NON-NLS-1$
    private static final String ELEMENT_DATA = "data"; //$NON-NLS-1$
    private static final String ELEMENT_DATE = "date"; //$NON-NLS-1$
    private static final String ELEMENT_FILECHOOSER = "fileChooser"; //$NON-NLS-1$
    private static final String ELEMENT_FORM = "form"; //$NON-NLS-1$
    private static final String ELEMENT_ICON = "icon"; //$NON-NLS-1$
    private static final String ELEMENT_IMAGE = "image"; //$NON-NLS-1$
    private static final String ELEMENT_ITEM = "item"; //$NON-NLS-1$
    private static final String ELEMENT_ITEMS = "items"; //$NON-NLS-1$
    private static final String ELEMENT_LABEL = "label"; //$NON-NLS-1$
    private static final String ELEMENT_LINK = "link"; //$NON-NLS-1$
    private static final String ELEMENT_LISTBOX = "listbox"; //$NON-NLS-1$
    private static final String ELEMENT_OBJECTCLASSES = "objectClasses"; //$NON-NLS-1$
    private static final String ELEMENT_PASSWORD = "password"; //$NON-NLS-1$
    private static final String ELEMENT_RADIOBUTTONS = "radiobuttons"; //$NON-NLS-1$
    private static final String ELEMENT_SECTION = "section"; //$NON-NLS-1$
    private static final String ELEMENT_SPINNER = "spinner"; //$NON-NLS-1$
    private static final String ELEMENT_STRUCTURAL = "structural"; //$NON-NLS-1$
    private static final String ELEMENT_TABLE = "table"; //$NON-NLS-1$
    private static final String ELEMENT_TEMPLATE = "template"; //$NON-NLS-1$
    private static final String ELEMENT_TEXTFIELD = "textfield"; //$NON-NLS-1$
    private static final String ELEMENT_VALUE = "value"; //$NON-NLS-1$
    private static final String ELEMENT_UNCHECKEDVALUE = "uncheckedValue"; //$NON-NLS-1$
    private static final String VALUE_BEGINNING = "beginning"; //$NON-NLS-1$
    private static final String VALUE_CENTER = "center"; //$NON-NLS-1$
    private static final String VALUE_END = "end"; //$NON-NLS-1$
    private static final String VALUE_FALSE = "false"; //$NON-NLS-1$
    private static final String VALUE_FILL = "fill"; //$NON-NLS-1$
    private static final String VALUE_NONE = "none"; //$NON-NLS-1$
    private static final String VALUE_TRUE = "true"; //$NON-NLS-1$


    // ── READ AS FILE TEMPLATE: DECODE JAWA DIALECT INTO A FILE TEMPLATE ───────────
    // C-3PO listens to the XML squeak and translates it into a FileTemplate —
    // the kind that lives on the filesystem, not in a plugin.xml.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given XML input stream as a {@link FileTemplate}. Creates a new
     * empty {@link FileTemplate}, fills it from the XML, and returns it.
     *
     * @param is  the XML input stream to parse
     * @return the hydrated {@link FileTemplate}
     * @throws TemplateIOException if the XML is malformed or missing required elements
     */
    public static FileTemplate readAsFileTemplate( InputStream is ) throws TemplateIOException
    {
        // Creating the FileTemplate
        FileTemplate template = new FileTemplate();

        // Reading the template
        readTemplate( is, template );

        // Returning the template
        return template;
    }


    // ── READ AS PREFERENCES FILE TEMPLATE: DECODE FOR THE PREFERENCES STORE ───────
    /**
     * Parses the given XML input stream as a {@link PreferencesFileTemplate}. Used
     * when loading templates managed by the Preferences page rather than the main
     * template manager.
     *
     * @param is  the XML input stream to parse
     * @return the hydrated {@link PreferencesFileTemplate}
     * @throws TemplateIOException if the XML is malformed or missing required elements
     */
    public static PreferencesFileTemplate readAsPreferencesFileTemplate( InputStream is ) throws TemplateIOException
    {
        // Creating the PreferencesFileTemplate
        PreferencesFileTemplate template = new PreferencesFileTemplate();

        // Reading the template
        readTemplate( is, template );

        // Returning the template
        return template;
    }


    // ── READ AS EXTENSION POINT TEMPLATE: DECODE A PLUGIN.XML CONTRIBUTION ────────
    /**
     * Parses the given XML input stream as an {@link ExtensionPointTemplate}. Used
     * when loading templates contributed via Eclipse extension points.
     *
     * @param is  the XML input stream to parse
     * @return the hydrated {@link ExtensionPointTemplate}
     * @throws TemplateIOException if the XML is malformed or missing required elements
     */
    public static ExtensionPointTemplate readAsExtensionPointTemplate( InputStream is ) throws TemplateIOException
    {
        // Creating the FileTemplate
        ExtensionPointTemplate template = new ExtensionPointTemplate();

        // Reading the template
        readTemplate( is, template );

        // Returning the template
        return template;
    }


    // ── READ TEMPLATE (stream): PARSE XML INTO ANY TEMPLATE TYPE ─────────────────
    // C-3PO opens the scroll, parses the top-level structure, and fills in the
    // template fields. This method is the common entry point for all three public
    // read* variants.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the XML input stream and fills the given {@link Template} in place.
     * Useful when the caller has already created the template instance and just
     * needs to populate it.
     *
     * @param is        the XML input stream
     * @param template  the template to fill
     * @throws TemplateIOException if the XML is malformed or missing required elements
     */
    public static void readTemplate( InputStream is, Template template ) throws TemplateIOException
    {
        // Getting the document
        Document document = getDocument( is );

        // Reading the template.
        readTemplate( document.getRootElement(), template );
    }


    // ── GET DOCUMENT: PARSE THE XML STREAM WITH SAX ──────────────────────────────
    /**
     * Parses the input stream into a dom4j {@link Document} using a {@link SAXReader}.
     *
     * @param is  the XML input stream
     * @return the parsed document
     * @throws TemplateIOException if the XML cannot be parsed
     */
    private static Document getDocument( InputStream is ) throws TemplateIOException
    {
        try
        {
            return ( new SAXReader() ).read( is );
        }
        catch ( DocumentException e )
        {
            throw new TemplateIOException( e.getMessage() );
        }
    }


    // ── READ TEMPLATE (element): FILL TEMPLATE FROM ROOT XML ELEMENT ──────────────
    // C-3PO reads the root <template> element and extracts the ID, title, object
    // classes, and form. If any mandatory piece is missing or invalid, he raises
    // his hands and throws a TemplateIOException.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fills the given {@link Template} from the root XML element. Validates the
     * element name, reads the {@code id} and {@code title} attributes, delegates
     * object class reading to {@link #readObjectClasses}, and form reading to
     * {@link #readForm}.
     *
     * @param rootElement  the root DOM element (must be named "template")
     * @param template     the template to fill
     * @throws TemplateIOException if required attributes or elements are missing
     */
    private static void readTemplate( Element rootElement, Template template ) throws TemplateIOException
    {
        LOG.debug( "Reading the template" ); //$NON-NLS-1$

        // Verifying the root 'template' element
        if ( ( rootElement == null ) || ( !rootElement.getName().equalsIgnoreCase( ELEMENT_TEMPLATE ) ) )
        {
            LOG.error( "Unable to find element: '" + ELEMENT_TEMPLATE + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindElement" ), ELEMENT_TEMPLATE ) ); //$NON-NLS-1$
        }

        // Reading the ID
        Attribute idAttribute = rootElement.attribute( ATTRIBUTE_ID );
        if ( ( idAttribute != null ) && ( idAttribute.getText() != null ) )
        {
            // Verifying if the ID is valid
            if ( AbstractTemplate.isValidId( idAttribute.getText() ) )
            {
                LOG.debug( "ID='" + idAttribute.getText() + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
                template.setId( idAttribute.getText() );
            }
            else
            {
                LOG.error( "Invalid ID attribute: '" + idAttribute.getText() + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
                throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                    + NLS.bind( Messages.getString( "TemplateIO.InvalidIdAttribute" ), idAttribute.getText() ) ); //$NON-NLS-1$
            }
        }
        else
        {
            LOG.error( "Unable to find attribute or attribute empty: '" + ATTRIBUTE_ID + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.AttributeNotFoundOrEmpty" ), ATTRIBUTE_ID ) ); //$NON-NLS-1$
        }

        // Reading the title
        Attribute titleAttribute = rootElement.attribute( ATTRIBUTE_TITLE );
        if ( ( titleAttribute != null ) && ( titleAttribute.getText() != null ) )
        {
            LOG.debug( "Title='" + titleAttribute.getText() + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            template.setTitle( titleAttribute.getText() );
        }
        else
        {

            LOG.error( "Unable to find attribute or attribute empty: '" + ATTRIBUTE_TITLE + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.AttributeNotFoundOrEmpty" ), ATTRIBUTE_TITLE ) ); //$NON-NLS-1$
        }

        // Reading the object classes
        readObjectClasses( rootElement, template );

        // Reading the form
        readForm( rootElement, template );
    }


    // ── READ OBJECT CLASSES: DECODE STRUCTURAL AND AUXILIARY LDAP TYPES ──────────
    /**
     * Reads the {@code <objectClasses>} element, extracting the structural object
     * class and any auxiliary object classes, and sets them on the template.
     *
     * @param element   the parent element containing {@code <objectClasses>}
     * @param template  the template to update
     * @throws TemplateIOException if the {@code <objectClasses>} or {@code <structural>} element is missing
     */
    private static void readObjectClasses( Element element, Template template ) throws TemplateIOException
    {
        LOG.debug( "Reading the template's object classes" ); //$NON-NLS-1$

        // Reading the 'objectClasses' element
        Element objectClassesElement = element.element( ELEMENT_OBJECTCLASSES );
        if ( objectClassesElement == null )
        {
            LOG.error( "Unable to find element: '" + ELEMENT_OBJECTCLASSES + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindElement" ), ELEMENT_OBJECTCLASSES ) ); //$NON-NLS-1$
        }

        // Reading the 'structural' element
        Element structuralElement = objectClassesElement.element( ELEMENT_STRUCTURAL );
        if ( structuralElement != null )
        {
            String structuralObjectClassText = structuralElement.getText();
            if ( ( structuralObjectClassText != null ) && ( !structuralObjectClassText.equals( "" ) ) ) //$NON-NLS-1$
            {
                template.setStructuralObjectClass( structuralObjectClassText );
            }
        }
        else
        {
            LOG.error( "Unable to find any: '" + ELEMENT_STRUCTURAL + "' element." ); //$NON-NLS-1$ //$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindAnyElement" ), ELEMENT_STRUCTURAL ) ); //$NON-NLS-1$
        }

        // Reading the 'auxiliaries' element
        Element auxliariesElement = objectClassesElement.element( ELEMENT_AUXILIARIES );
        if ( auxliariesElement != null )
        {
            // Reading the auxiliaries object classes
            for ( Iterator<?> i = auxliariesElement.elementIterator( ELEMENT_AUXILIARY ); i.hasNext(); )
            {
                Element auxliaryObjectClassElement = ( Element ) i.next();
                String auxliaryObjectClassText = auxliaryObjectClassElement.getText();
                if ( ( auxliaryObjectClassText != null ) && ( !auxliaryObjectClassText.equals( "" ) ) ) //$NON-NLS-1$
                {
                    template.addAuxiliaryObjectClass( auxliaryObjectClassText );
                }
            }
        }
    }


    // ── READ FORM: DECODE THE ROOT UI LAYOUT ─────────────────────────────────────
    // C-3PO reads the <form> element and builds the TemplateForm that sits at the
    // root of the UI widget tree. Only <section> and <composite> children are
    // allowed at the top level — anything else triggers an exception.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the {@code <form>} element and builds the root {@link TemplateForm}.
     * Only {@code <section>} and {@code <composite>} child elements are valid at
     * the top level. Throws if no children are found.
     *
     * @param element   the parent element containing {@code <form>}
     * @param template  the template to set the form on
     * @throws TemplateIOException if the form element is missing, contains invalid children, or is empty
     */
    private static void readForm( Element element, Template template ) throws TemplateIOException
    {
        LOG.debug( "Reading the template's form" ); //$NON-NLS-1$

        // Reading the 'form' element
        Element formElement = element.element( ELEMENT_FORM );
        if ( formElement == null )
        {
            LOG.error( "Unable to find element: '" + ELEMENT_FORM + "'." ); //$NON-NLS-1$//$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindElement" ), ELEMENT_FORM ) ); //$NON-NLS-1$
        }

        // Creating the form and setting it to the template
        TemplateForm form = new TemplateForm();
        template.setForm( form );

        // Reading the child elements
        for ( Iterator<?> i = formElement.elementIterator(); i.hasNext(); )
        {
            Element childElement = ( Element ) i.next();

            // Getting the name of the element
            String elementName = childElement.getName();
            if ( elementName.equalsIgnoreCase( ELEMENT_COMPOSITE ) )
            {
                readComposite( childElement, form );
            }
            else if ( elementName.equalsIgnoreCase( ELEMENT_SECTION ) )
            {
                readSection( childElement, form );
            }
            else
            {
                throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                    + NLS.bind( Messages.getString( "TemplateIO.ElementNotAllowedAtThisLevel" ), //$NON-NLS-1$
                        new String[]
                            { elementName, ELEMENT_SECTION, ELEMENT_COMPOSITE } ) );
            }

        }

        // Verifying if we've found at least one section
        if ( form.getChildren().size() == 0 )
        {
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindAnyXOrYElement" ), new String[] //$NON-NLS-1$
                    { ELEMENT_SECTION, ELEMENT_COMPOSITE } ) );
        }
    }


    // ── READ WIDGET: DISPATCH ELEMENT TO THE CORRECT WIDGET READER ───────────────
    // C-3PO recognizes the widget type by the element name and dispatches to the
    // appropriate specialist reader. Unknown element names throw an exception.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single widget XML element and creates the corresponding
     * {@link TemplateWidget}, attaching it to {@code parent}. Dispatches by
     * element name to the specific read* method for each widget type.
     *
     * @param element  the XML element describing the widget
     * @param parent   the parent widget to attach the new widget to
     * @throws TemplateIOException if the element name doesn't match any known widget type
     */
    private static void readWidget( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        // Getting the name of the element
        String elementName = element.getName();

        // Switching on the various widgets we support
        if ( elementName.equalsIgnoreCase( ELEMENT_CHECKBOX ) )
        {
            readCheckbox( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_COMPOSITE ) )
        {
            readComposite( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_DATE ) )
        {
            readDate( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_FILECHOOSER ) )
        {
            readFileChooser( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_IMAGE ) )
        {
            readImage( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_LABEL ) )
        {
            readLabel( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_LINK ) )
        {
            readLink( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_LISTBOX ) )
        {
            readListbox( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_PASSWORD ) )
        {
            readPassword( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_RADIOBUTTONS ) )
        {
            readRadioButtons( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_SECTION ) )
        {
            readSection( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_SPINNER ) )
        {
            readSpinner( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_TABLE ) )
        {
            readTable( element, parent );
        }
        else if ( elementName.equalsIgnoreCase( ELEMENT_TEXTFIELD ) )
        {
            readTextfield( element, parent );
        }
        // We could not find a widget associated with this name.
        else
        {
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnknownWidget" ), elementName ) ); //$NON-NLS-1$
        }
    }


    // ── READ WIDGET COMMON PROPERTIES: DECODE SHARED LAYOUT ATTRIBUTES ───────────
    // Every widget shares a set of layout attributes: attributeType, alignment,
    // spacing, and size. C-3PO reads them all here and sets them on the widget.
    // If attributeType is mandatory and missing, he throws immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the attributes common to every {@link TemplateWidget}: {@code attributeType},
     * {@code horizontalAlignment}, {@code verticalAlignment}, {@code grabExcessHorizontalSpace},
     * {@code grabExcessVerticalSpace}, {@code horizontalSpan}, {@code verticalSpan},
     * {@code width}, and {@code height}.
     *
     * @param element                              the XML element to read from
     * @param widget                               the widget to populate
     * @param throwExceptionIfMissingAttributeType {@code true} to throw if {@code attributeType} is absent
     * @param widgetElementName                    the element name (for error messages)
     * @throws TemplateIOException if {@code throwExceptionIfMissingAttributeType} is true and the attribute is missing
     */
    private static void readWidgetCommonProperties( Element element, TemplateWidget widget,
        boolean throwExceptionIfMissingAttributeType, String widgetElementName ) throws TemplateIOException
    {
        // Reading the 'attributeType' attribute
        boolean foundAttributeTypeAttribute = readAttributeTypeAttribute( element, widget );
        // If the 'attributeType' attribute does not exist, we throw an
        // exception
        if ( throwExceptionIfMissingAttributeType && !foundAttributeTypeAttribute )
        {
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindMandatoryAttribute" ), new String[] //$NON-NLS-1$
                    { ATTRIBUTE_ATTRIBUTETYPE, widgetElementName } ) );
        }

        // Reading the 'horizontalAlignment' attribute
        Attribute horizontalAlignmentAttribute = element.attribute( ATTRIBUTE_HORIZONTAL_ALIGNMENT );
        if ( ( horizontalAlignmentAttribute != null ) && ( horizontalAlignmentAttribute.getText() != null ) )
        {
            widget.setHorizontalAlignment( readWidgetAlignmentValue( horizontalAlignmentAttribute.getText() ) );
        }

        // Reading the 'verticalAlignment' attribute
        Attribute verticalAlignmentAttribute = element.attribute( ATTRIBUTE_VERTICAL_ALIGNMENT );
        if ( ( verticalAlignmentAttribute != null ) && ( verticalAlignmentAttribute.getText() != null ) )
        {
            widget.setVerticalAlignment( readWidgetAlignmentValue( verticalAlignmentAttribute.getText() ) );
        }

        // Reading the 'grabExcessHorizontalSpace' attribute
        Attribute grabExcessHorizontalSpaceAttribute = element.attribute( ATTRIBUTE_GRAB_EXCESS_HORIZONTAL_SPACE );
        if ( ( grabExcessHorizontalSpaceAttribute != null ) && ( grabExcessHorizontalSpaceAttribute.getText() != null ) )
        {
            widget.setGrabExcessHorizontalSpace( readBoolean( grabExcessHorizontalSpaceAttribute.getText() ) );
        }

        // Reading the 'grabExcessVerticalSpace' attribute
        Attribute grabExcessVerticalSpaceAttribute = element.attribute( ATTRIBUTE_GRAB_EXCESS_VERTICAL_SPACE );
        if ( ( grabExcessVerticalSpaceAttribute != null ) && ( grabExcessVerticalSpaceAttribute.getText() != null ) )
        {
            widget.setGrabExcessVerticalSpace( readBoolean( grabExcessVerticalSpaceAttribute.getText() ) );
        }

        // Reading the 'horizontalSpan' attribute
        Attribute horizontalSpanAttribute = element.attribute( ATTRIBUTE_HORIZONTAL_SPAN );
        if ( ( horizontalSpanAttribute != null ) && ( horizontalSpanAttribute.getText() != null ) )
        {
            widget.setHorizontalSpan( readInteger( horizontalSpanAttribute.getText() ) );
        }

        // Reading the 'verticalSpan' attribute
        Attribute verticalSpanAttribute = element.attribute( ATTRIBUTE_VERTICAL_SPAN );
        if ( ( verticalSpanAttribute != null ) && ( verticalSpanAttribute.getText() != null ) )
        {
            widget.setVerticalSpan( readInteger( verticalSpanAttribute.getText() ) );
        }

        // Reading the 'width' attribute
        Attribute widthAttribute = element.attribute( ATTRIBUTE_WIDTH );
        if ( ( widthAttribute != null ) && ( widthAttribute.getText() != null ) )
        {
            widget.setImageWidth( readInteger( widthAttribute.getText() ) );
        }

        // Reading the 'height' attribute
        Attribute heightAttribute = element.attribute( ATTRIBUTE_HEIGHT );
        if ( ( heightAttribute != null ) && ( heightAttribute.getText() != null ) )
        {
            widget.setImageHeight( readInteger( heightAttribute.getText() ) );
        }
    }


    // ── READ WIDGET ALIGNMENT VALUE: CONVERT TEXT TO ENUM ────────────────────────
    /**
     * Converts an alignment string ({@code "none"}, {@code "beginning"},
     * {@code "center"}, {@code "end"}, {@code "fill"}) into the corresponding
     * {@link WidgetAlignment} enum value.
     *
     * @param text  the alignment string from the XML
     * @return the corresponding {@link WidgetAlignment}
     * @throws TemplateIOException if the string doesn't match any known alignment value
     */
    private static WidgetAlignment readWidgetAlignmentValue( String text ) throws TemplateIOException
    {
        if ( text.equalsIgnoreCase( VALUE_NONE ) )
        {
            return WidgetAlignment.NONE;
        }
        else if ( text.equalsIgnoreCase( VALUE_BEGINNING ) )
        {
            return WidgetAlignment.BEGINNING;
        }
        else if ( text.equalsIgnoreCase( VALUE_CENTER ) )
        {
            return WidgetAlignment.CENTER;
        }
        else if ( text.equalsIgnoreCase( VALUE_END ) )
        {
            return WidgetAlignment.END;
        }
        else if ( text.equalsIgnoreCase( VALUE_FILL ) )
        {
            return WidgetAlignment.FILL;
        }
        else
        {
            String message = NLS.bind( Messages.getString( "TemplateIO.UnableToConvertStringToWidgetAlignmentValue" ), //$NON-NLS-1$
                new String[]
                    { VALUE_NONE, VALUE_BEGINNING, VALUE_CENTER, VALUE_END, VALUE_FILL, text } );
            LOG.error( message );
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" + message ); //$NON-NLS-1$
        }
    }


    // ── READ ATTRIBUTE TYPE ATTRIBUTE: DECODE THE LDAP ATTRIBUTE BINDING ─────────
    /**
     * Reads the {@code attributeType} XML attribute and sets it on the widget.
     *
     * @param element  the XML element to read from
     * @param widget   the widget to update
     * @return {@code true} if the attribute was found and set; {@code false} otherwise
     */
    private static boolean readAttributeTypeAttribute( Element element, TemplateWidget widget )
    {
        // Reading the 'attributeType' attribute
        Attribute attributeTypeAttribute = element.attribute( ATTRIBUTE_ATTRIBUTETYPE );
        if ( ( attributeTypeAttribute != null ) && ( attributeTypeAttribute.getText() != null ) )
        {
            widget.setAttributeType( attributeTypeAttribute.getText() );
            return true;
        }

        return false;
    }


    // ── READ CHECKBOX: DECODE A CHECKBOX WIDGET ───────────────────────────────────
    /**
     * Reads a {@code <checkbox>} element, creates a {@link TemplateCheckbox}, and
     * attaches it to {@code parent}.
     *
     * @param element  the {@code <checkbox>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readCheckbox( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template checkbox" ); //$NON-NLS-1$

        // Creating the checkbox
        TemplateCheckbox templateCheckbox = new TemplateCheckbox( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, templateCheckbox, true, ELEMENT_CHECKBOX );

        // Reading the 'label' attribute
        Attribute labelAttribute = element.attribute( ATTRIBUTE_LABEL );
        if ( ( labelAttribute != null ) && ( labelAttribute.getText() != null ) )
        {
            templateCheckbox.setLabel( labelAttribute.getText() );
        }

        // Reading the 'enabled' attribute
        Attribute enabledAttribute = element.attribute( ATTRIBUTE_ENABLED );
        if ( ( enabledAttribute != null ) && ( enabledAttribute.getText() != null ) )
        {
            templateCheckbox.setEnabled( readBoolean( enabledAttribute.getText() ) );
        }

        // Reading the 'checkedValue' element
        Element checkedValueElement = element.element( ELEMENT_CHECKEDVALUE );
        if ( checkedValueElement != null )
        {
            templateCheckbox.setCheckedValue( checkedValueElement.getText() );
        }

        // Reading the 'uncheckedValue' element
        Element uncheckedValueElement = element.element( ELEMENT_UNCHECKEDVALUE );
        if ( uncheckedValueElement != null )
        {
            templateCheckbox.setUncheckedValue( uncheckedValueElement.getText() );
        }
    }


    // ── READ COMPOSITE: DECODE A COMPOSITE WIDGET ─────────────────────────────────
    /**
     * Reads a {@code <composite>} element, creates a {@link TemplateComposite}, reads
     * its column/equal-column settings, and recursively reads all child widgets.
     *
     * @param element  the {@code <composite>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readComposite( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template composite" ); //$NON-NLS-1$

        // Creating the composite
        TemplateComposite templateComposite = new TemplateComposite( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, templateComposite, false, ELEMENT_COMPOSITE );

        // Reading the 'numberOfColumns' attribute
        Attribute numberOfColumnsAttribute = element.attribute( ATTRIBUTE_NUMBEROFCOLUMNS );
        if ( ( numberOfColumnsAttribute != null ) && ( numberOfColumnsAttribute.getText() != null ) )
        {
            templateComposite.setNumberOfColumns( readInteger( numberOfColumnsAttribute.getText() ) );
        }

        // Reading the 'equalColumns' attribute
        Attribute equalColumnsAttribute = element.attribute( ATTRIBUTE_EQUALCOLUMNS );
        if ( ( equalColumnsAttribute != null ) && ( equalColumnsAttribute.getText() != null ) )
        {
            templateComposite.setEqualColumns( readBoolean( equalColumnsAttribute.getText() ) );
        }

        // Reading the elements
        for ( Iterator<?> i = element.elementIterator(); i.hasNext(); )
        {
            Element childElement = ( Element ) i.next();
            readWidget( childElement, templateComposite );
        }
    }


    // ── READ DATE: DECODE A DATE PICKER WIDGET ────────────────────────────────────
    /**
     * Reads a {@code <date>} element, creates a {@link TemplateDate}, and attaches
     * it to {@code parent}.
     *
     * @param element  the {@code <date>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readDate( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template date" ); //$NON-NLS-1$

        // Creating the file chooser
        TemplateDate date = new TemplateDate( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, date, true, ELEMENT_DATE );

        // Reading the 'format' attribute
        Attribute formatAttribute = element.attribute( ATTRIBUTE_FORMAT );
        if ( ( formatAttribute != null ) && ( formatAttribute.getText() != null ) )
        {
            date.setFormat( formatAttribute.getText() );
        }

        // Reading the 'showEditButton' attribute
        Attribute showEditButtonAttribute = element.attribute( ATTRIBUTE_SHOWEDITBUTTON );
        if ( ( showEditButtonAttribute != null ) && ( showEditButtonAttribute.getText() != null ) )
        {
            date.setShowEditButton( readBoolean( showEditButtonAttribute.getText() ) );
        }
    }


    // ── READ FILE CHOOSER: DECODE A FILE CHOOSER WIDGET ──────────────────────────
    /**
     * Reads a {@code <fileChooser>} element, creates a {@link TemplateFileChooser},
     * reads its button visibility, extension list, and icon, and attaches it to
     * {@code parent}.
     *
     * @param element  the {@code <fileChooser>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readFileChooser( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template file chooser" ); //$NON-NLS-1$

        // Creating the file chooser
        TemplateFileChooser fileChooser = new TemplateFileChooser( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, fileChooser, true, ELEMENT_FILECHOOSER );

        // Reading the 'extensions' attribute
        Attribute extensionsAttribute = element.attribute( ATTRIBUTE_EXTENSIONS );
        if ( ( extensionsAttribute != null ) && ( extensionsAttribute.getText() != null ) )
        {
            readFileChooserExtensions( extensionsAttribute, fileChooser );
        }

        // Reading the 'showIcon' attribute
        Attribute showIconAttribute = element.attribute( ATTRIBUTE_SHOWICON );
        if ( ( showIconAttribute != null ) && ( showIconAttribute.getText() != null ) )
        {
            fileChooser.setShowIcon( readBoolean( showIconAttribute.getText() ) );
        }

        // Reading the 'showSaveAsButton' attribute
        Attribute showSaveAsButtonAttribute = element.attribute( ATTRIBUTE_SHOWSAVEASBUTTON );
        if ( ( showSaveAsButtonAttribute != null ) && ( showSaveAsButtonAttribute.getText() != null ) )
        {
            fileChooser.setShowSaveAsButton( readBoolean( showSaveAsButtonAttribute.getText() ) );
        }

        // Reading the 'showClearButton' attribute
        Attribute showClearButtonAttribute = element.attribute( ATTRIBUTE_SHOWCLEARBUTTON );
        if ( ( showClearButtonAttribute != null ) && ( showClearButtonAttribute.getText() != null ) )
        {
            fileChooser.setShowClearButton( readBoolean( showClearButtonAttribute.getText() ) );
        }

        // Reading the 'showBrowseButton' attribute
        Attribute showBrowseButtonAttribute = element.attribute( ATTRIBUTE_SHOWBROWSEBUTTON );
        if ( ( showBrowseButtonAttribute != null ) && ( showBrowseButtonAttribute.getText() != null ) )
        {
            fileChooser.setShowBrowseButton( readBoolean( showBrowseButtonAttribute.getText() ) );
        }

        // Reading the 'icon' element
        Element iconElement = element.element( ELEMENT_ICON );
        if ( iconElement != null )
        {
            fileChooser.setIcon( iconElement.getText() );
        }
    }


    // ── READ FILE CHOOSER EXTENSIONS: SPLIT COMMA-DELIMITED EXTENSION LIST ────────
    /**
     * Parses the comma-delimited {@code extensions} attribute value and adds each
     * extension to the {@link TemplateFileChooser}.
     *
     * @param extensionsAttribute  the {@code extensions} attribute
     * @param fileChooser          the file chooser to update
     */
    private static void readFileChooserExtensions( Attribute extensionsAttribute, TemplateFileChooser fileChooser )
    {
        // Getting the extensions
        String extensions = extensionsAttribute.getText();

        // Splitting and setting extensions to the file chooser
        for ( String extension : extensions.split( "," ) ) //$NON-NLS-1$
        {
            fileChooser.addExtension( extension );
        }
    }


    // ── READ IMAGE: DECODE AN IMAGE DISPLAY WIDGET ────────────────────────────────
    /**
     * Reads an {@code <image>} element, creates a {@link TemplateImage}, reads its
     * button visibility, size constraints, and optional embedded Base64 data, and
     * attaches it to {@code parent}.
     *
     * @param element  the {@code <image>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readImage( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template image" ); //$NON-NLS-1$

        // Creating the image
        TemplateImage image = new TemplateImage( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, image, false, ELEMENT_IMAGE );

        // Reading the 'showSaveAsButton' attribute
        Attribute showSaveAsButtonAttribute = element.attribute( ATTRIBUTE_SHOWSAVEASBUTTON );
        if ( ( showSaveAsButtonAttribute != null ) && ( showSaveAsButtonAttribute.getText() != null ) )
        {
            image.setShowSaveAsButton( readBoolean( showSaveAsButtonAttribute.getText() ) );
        }

        // Reading the 'showClearButton' attribute
        Attribute showClearButtonAttribute = element.attribute( ATTRIBUTE_SHOWCLEARBUTTON );
        if ( ( showClearButtonAttribute != null ) && ( showClearButtonAttribute.getText() != null ) )
        {
            image.setShowClearButton( readBoolean( showClearButtonAttribute.getText() ) );
        }

        // Reading the 'showBrowseButton' attribute
        Attribute showBrowseButtonAttribute = element.attribute( ATTRIBUTE_SHOWBROWSEBUTTON );
        if ( ( showBrowseButtonAttribute != null ) && ( showBrowseButtonAttribute.getText() != null ) )
        {
            image.setShowBrowseButton( readBoolean( showBrowseButtonAttribute.getText() ) );
        }

        // Reading the 'imageWidth' attribute
        Attribute imageWidthAttribute = element.attribute( ATTRIBUTE_IMAGE_WIDTH );
        if ( ( imageWidthAttribute != null ) && ( imageWidthAttribute.getText() != null ) )
        {
            image.setImageWidth( readInteger( imageWidthAttribute.getText() ) );
        }

        // Reading the 'imageHeight' attribute
        Attribute imageHeightAttribute = element.attribute( ATTRIBUTE_IMAGE_HEIGHT );
        if ( ( imageHeightAttribute != null ) && ( imageHeightAttribute.getText() != null ) )
        {
            image.setImageHeight( readInteger( imageHeightAttribute.getText() ) );
        }

        // Reading the 'data' element
        Element imageDataElement = element.element( ELEMENT_DATA );
        if ( imageDataElement != null )
        {
            image.setImageData( imageDataElement.getText() );
        }
    }


    // ── READ LABEL: DECODE A LABEL DISPLAY WIDGET ─────────────────────────────────
    /**
     * Reads a {@code <label>} element, creates a {@link TemplateLabel}, reads its
     * static value and row settings, and attaches it to {@code parent}.
     *
     * @param element  the {@code <label>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readLabel( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template label" ); //$NON-NLS-1$

        // Creating the label
        TemplateLabel label = new TemplateLabel( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, label, false, ELEMENT_LABEL );

        // Reading the 'value' attribute
        Attribute valueAttribute = element.attribute( ATTRIBUTE_VALUE );
        if ( ( valueAttribute != null ) && ( valueAttribute.getText() != null ) )
        {
            label.setValue( valueAttribute.getText() );
        }

        // Reading the 'numberOfRows' attribute
        Attribute numberOfRowsAttribute = element.attribute( ATTRIBUTE_NUMBEROFROWS );
        if ( ( numberOfRowsAttribute != null ) && ( numberOfRowsAttribute.getText() != null ) )
        {
            label.setNumberOfRows( readInteger( numberOfRowsAttribute.getText() ) );
        }

        // Reading the 'dollarSignIsNewLine' attribute
        Attribute dollarSignIsNewLineAttribute = element.attribute( ATTRIBUTE_DOLLAR_SIGN_IS_NEW_LINE );
        if ( ( dollarSignIsNewLineAttribute != null ) && ( dollarSignIsNewLineAttribute.getText() != null ) )
        {
            label.setDollarSignIsNewLine( readBoolean( dollarSignIsNewLineAttribute.getText() ) );
        }
    }


    // ── READ LINK: DECODE A HYPERLINK DISPLAY WIDGET ──────────────────────────────
    /**
     * Reads a {@code <link>} element, creates a {@link TemplateLink}, reads its
     * optional static value, and attaches it to {@code parent}.
     *
     * @param element  the {@code <link>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readLink( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template link" ); //$NON-NLS-1$

        // Creating the link
        TemplateLink link = new TemplateLink( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, link, false, ELEMENT_LINK );

        // Reading the 'value' attribute
        Attribute valueAttribute = element.attribute( ATTRIBUTE_VALUE );
        if ( ( valueAttribute != null ) && ( valueAttribute.getText() != null ) )
        {
            link.setValue( valueAttribute.getText() );
        }
    }


    // ── READ LISTBOX: DECODE A LIST BOX WIDGET ────────────────────────────────────
    /**
     * Reads a {@code <listbox>} element, creates a {@link TemplateListbox}, reads
     * its item list and selection mode, and attaches it to {@code parent}.
     *
     * @param element  the {@code <listbox>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if the item list is missing or empty
     */
    private static void readListbox( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template listbox" ); //$NON-NLS-1$

        // Creating the listbox
        TemplateListbox listbox = new TemplateListbox( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, listbox, true, ELEMENT_LISTBOX );

        // Reading the 'multipleSelection' attribute
        Attribute multipleSelectionAttribute = element.attribute( ATTRIBUTE_MULTIPLESELECTION );
        if ( ( multipleSelectionAttribute != null ) && ( multipleSelectionAttribute.getText() != null ) )
        {
            listbox.setMultipleSelection( readBoolean( multipleSelectionAttribute.getText() ) );
        }

        // Reading the 'enabled' attribute
        Attribute enabledAttribute = element.attribute( ATTRIBUTE_ENABLED );
        if ( ( enabledAttribute != null ) && ( enabledAttribute.getText() != null ) )
        {
            listbox.setEnabled( readBoolean( enabledAttribute.getText() ) );
        }

        // Reading the 'items' element
        Element itemsElement = element.element( ELEMENT_ITEMS );
        if ( itemsElement != null )
        {
            // Reading the 'item' elements
            for ( Iterator<?> i = itemsElement.elementIterator( ELEMENT_ITEM ); i.hasNext(); )
            {
                Element itemElement = ( Element ) i.next();
                listbox.addValue( readValueItem( itemElement ) );
            }

            // Verifying if at least one button has been read
            if ( listbox.getItems().size() == 0 )
            {
                throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                    + NLS.bind( Messages.getString( "TemplateIO.UnableToFindAnyElement" ), ELEMENT_ITEM ) ); //$NON-NLS-1$
            }
        }
        else
        {
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindElement" ), ELEMENT_ITEMS ) ); //$NON-NLS-1$
        }
    }


    // ── READ VALUE ITEM: DECODE A LABEL/VALUE PAIR ────────────────────────────────
    /**
     * Reads an {@code <item>} or {@code <button>} element and returns a
     * {@link ValueItem} containing the display label and stored value.
     *
     * @param element  the XML element containing {@code <label>} and {@code <value>} children
     * @return the decoded {@link ValueItem}
     */
    private static ValueItem readValueItem( Element element )
    {
        ValueItem valueItem = new ValueItem();

        // Reading the 'label' element
        Element labelElement = element.element( ELEMENT_LABEL );
        if ( ( labelElement != null ) && ( labelElement.getText() != null ) )
        {
            valueItem.setLabel( labelElement.getText() );
        }

        // Reading the 'value' element
        Element valueElement = element.element( ELEMENT_VALUE );
        if ( ( valueElement != null ) && ( valueElement.getText() != null ) )
        {
            valueItem.setValue( valueElement.getText() );
        }

        return valueItem;
    }


    // ── READ SECTION: DECODE A COLLAPSIBLE SECTION WIDGET ────────────────────────
    /**
     * Reads a {@code <section>} element, creates a {@link TemplateSection}, reads
     * its title, description, column layout, expand settings, and recursively reads
     * all child widgets.
     *
     * @param element  the {@code <section>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readSection( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template section" ); //$NON-NLS-1$

        // Creating the section
        TemplateSection templateSection = new TemplateSection( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, templateSection, false, ELEMENT_SECTION );

        // Reading the 'title' attribute
        Attribute titleAttribute = element.attribute( ATTRIBUTE_TITLE );
        if ( ( titleAttribute != null ) && ( titleAttribute.getText() != null ) )
        {
            templateSection.setTitle( titleAttribute.getText() );
        }

        // Reading the 'description' attribute
        Attribute descriptionAttribute = element.attribute( ATTRIBUTE_DESCRIPTION );
        if ( ( descriptionAttribute != null ) && ( descriptionAttribute.getText() != null ) )
        {
            templateSection.setDescription( descriptionAttribute.getText() );
        }

        // Reading the 'numberOfColumns' attribute
        Attribute numberOfColumnsAttribute = element.attribute( ATTRIBUTE_NUMBEROFCOLUMNS );
        if ( ( numberOfColumnsAttribute != null ) && ( numberOfColumnsAttribute.getText() != null ) )
        {
            templateSection.setNumberOfColumns( readInteger( numberOfColumnsAttribute.getText() ) );
        }

        // Reading the 'equalColumns' attribute
        Attribute equalColumnsAttribute = element.attribute( ATTRIBUTE_EQUALCOLUMNS );
        if ( ( equalColumnsAttribute != null ) && ( equalColumnsAttribute.getText() != null ) )
        {
            templateSection.setEqualColumns( readBoolean( equalColumnsAttribute.getText() ) );
        }

        // Reading the 'expandable' attribute
        Attribute expandableAttribute = element.attribute( ATTRIBUTE_EXPANDABLE );
        if ( ( expandableAttribute != null ) && ( expandableAttribute.getText() != null ) )
        {
            templateSection.setExpandable( readBoolean( expandableAttribute.getText() ) );
        }

        // Reading the 'expanded' attribute
        Attribute expandedAttribute = element.attribute( ATTRIBUTE_EXPANDED );
        if ( ( expandedAttribute != null ) && ( expandedAttribute.getText() != null ) )
        {
            templateSection.setExpanded( readBoolean( expandedAttribute.getText() ) );
        }

        // Reading the elements
        for ( Iterator<?> i = element.elementIterator(); i.hasNext(); )
        {
            Element childElement = ( Element ) i.next();
            readWidget( childElement, templateSection );
        }
    }


    // ── READ PASSWORD: DECODE A PASSWORD WIDGET ───────────────────────────────────
    /**
     * Reads a {@code <password>} element, creates a {@link TemplatePassword}, and
     * attaches it to {@code parent}.
     *
     * @param element  the {@code <password>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readPassword( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template password" ); //$NON-NLS-1$

        // Creating the password
        TemplatePassword password = new TemplatePassword( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, password, true, ELEMENT_PASSWORD );

        // Reading the 'hidden' attribute
        Attribute hiddenAttribute = element.attribute( ATTRIBUTE_HIDDEN );
        if ( ( hiddenAttribute != null ) && ( hiddenAttribute.getText() != null ) )
        {
            password.setHidden( readBoolean( hiddenAttribute.getText() ) );
        }

        // Reading the 'showEditButton' attribute
        Attribute showEditButtonAttribute = element.attribute( ATTRIBUTE_SHOWEDITBUTTON );
        if ( ( showEditButtonAttribute != null ) && ( showEditButtonAttribute.getText() != null ) )
        {
            password.setShowEditButton( readBoolean( showEditButtonAttribute.getText() ) );
        }

        // Reading the 'showShowPasswordCheckbox' attribute
        Attribute showShowPasswordCheckboxAttribute = element.attribute( ATTRIBUTE_SHOWSHOWPASSWORDCHECKBOX );
        if ( ( showShowPasswordCheckboxAttribute != null ) && ( showShowPasswordCheckboxAttribute.getText() != null ) )
        {
            password.setShowShowPasswordCheckbox( readBoolean( showShowPasswordCheckboxAttribute.getText() ) );
        }
    }


    // ── READ RADIO BUTTONS: DECODE A RADIO BUTTON GROUP WIDGET ───────────────────
    /**
     * Reads a {@code <radiobuttons>} element, creates a {@link TemplateRadioButtons},
     * reads the button list, and attaches it to {@code parent}.
     *
     * @param element  the {@code <radiobuttons>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if the button list is missing or empty
     */
    private static void readRadioButtons( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template radio buttons" ); //$NON-NLS-1$

        // Creating the radioButtons
        TemplateRadioButtons radioButtons = new TemplateRadioButtons( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, radioButtons, true, ELEMENT_RADIOBUTTONS );

        // Reading the 'enabled' attribute
        Attribute enabledAttribute = element.attribute( ATTRIBUTE_ENABLED );
        if ( ( enabledAttribute != null ) && ( enabledAttribute.getText() != null ) )
        {
            radioButtons.setEnabled( readBoolean( enabledAttribute.getText() ) );
        }

        // Reading the 'buttons' element
        Element buttonsElement = element.element( ELEMENT_BUTTONS );
        if ( buttonsElement != null )
        {
            // Reading the 'button' elements
            for ( Iterator<?> i = buttonsElement.elementIterator( ELEMENT_BUTTON ); i.hasNext(); )
            {
                Element buttonElement = ( Element ) i.next();
                radioButtons.addButton( readValueItem( buttonElement ) );
            }

            // Verifying if at least one button has been read
            if ( radioButtons.getButtons().size() == 0 )
            {
                throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                    + NLS.bind( Messages.getString( "TemplateIO.UnableToFindAnyElement" ), ELEMENT_BUTTON ) ); //$NON-NLS-1$
            }
        }
        else
        {
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToFindElement" ), ELEMENT_BUTTONS ) ); //$NON-NLS-1$
        }
    }


    // ── READ SPINNER: DECODE A NUMERIC SPINNER WIDGET ────────────────────────────
    /**
     * Reads a {@code <spinner>} element, creates a {@link TemplateSpinner}, reads
     * its numeric range and step settings, and attaches it to {@code parent}.
     *
     * @param element  the {@code <spinner>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readSpinner( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template spinner" ); //$NON-NLS-1$

        // Creating the spinner
        TemplateSpinner spinner = new TemplateSpinner( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, spinner, true, ELEMENT_SPINNER );

        // Reading the 'minimum' attribute
        Attribute minimumAttribute = element.attribute( ATTRIBUTE_MINIMUM );
        if ( ( minimumAttribute != null ) && ( minimumAttribute.getText() != null ) )
        {
            spinner.setMinimum( readInteger( minimumAttribute.getText() ) );
        }

        // Reading the 'maximum' attribute
        Attribute maximumAttribute = element.attribute( ATTRIBUTE_MAXIMUM );
        if ( ( maximumAttribute != null ) && ( maximumAttribute.getText() != null ) )
        {
            spinner.setMaximum( readInteger( maximumAttribute.getText() ) );
        }

        // Reading the 'increment' attribute
        Attribute incrementAttribute = element.attribute( ATTRIBUTE_INCREMENT );
        if ( ( incrementAttribute != null ) && ( incrementAttribute.getText() != null ) )
        {
            spinner.setIncrement( readInteger( incrementAttribute.getText() ) );
        }

        // Reading the 'pageIncrement' attribute
        Attribute pageIncrementAttribute = element.attribute( ATTRIBUTE_PAGEINCREMENT );
        if ( ( pageIncrementAttribute != null ) && ( pageIncrementAttribute.getText() != null ) )
        {
            spinner.setPageIncrement( readInteger( pageIncrementAttribute.getText() ) );
        }

        // Reading the 'digits' attribute
        Attribute digitsAttribute = element.attribute( ATTRIBUTE_DIGITS );
        if ( ( digitsAttribute != null ) && ( digitsAttribute.getText() != null ) )
        {
            spinner.setDigits( readInteger( digitsAttribute.getText() ) );
        }
    }


    // ── READ TABLE: DECODE A MULTI-VALUE TABLE WIDGET ─────────────────────────────
    /**
     * Reads a {@code <table>} element, creates a {@link TemplateTable}, reads its
     * Add/Edit/Delete button visibility, and attaches it to {@code parent}.
     *
     * @param element  the {@code <table>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readTable( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template table" ); //$NON-NLS-1$

        // Creating the table
        TemplateTable table = new TemplateTable( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, table, true, ELEMENT_TABLE );

        // Reading the 'showAddButton' attribute
        Attribute showAddButtonAttribute = element.attribute( ATTRIBUTE_SHOWADDBUTTON );
        if ( ( showAddButtonAttribute != null ) && ( showAddButtonAttribute.getText() != null ) )
        {
            table.setShowAddButton( readBoolean( showAddButtonAttribute.getText() ) );
        }

        // Reading the 'showEditButton' attribute
        Attribute showEditButtonAttribute = element.attribute( ATTRIBUTE_SHOWEDITBUTTON );
        if ( ( showEditButtonAttribute != null ) && ( showEditButtonAttribute.getText() != null ) )
        {
            table.setShowEditButton( readBoolean( showEditButtonAttribute.getText() ) );
        }

        // Reading the 'showDeleteButton' attribute
        Attribute showDeleteButtonAttribute = element.attribute( ATTRIBUTE_SHOWDELETEBUTTON );
        if ( ( showDeleteButtonAttribute != null ) && ( showDeleteButtonAttribute.getText() != null ) )
        {
            table.setShowDeleteButton( readBoolean( showDeleteButtonAttribute.getText() ) );
        }
    }


    // ── READ TEXTFIELD: DECODE A TEXT INPUT WIDGET ────────────────────────────────
    /**
     * Reads a {@code <textfield>} element, creates a {@link TemplateTextField}, reads
     * its row count, character limit, and newline settings, and attaches it to
     * {@code parent}.
     *
     * @param element  the {@code <textfield>} XML element
     * @param parent   the parent widget
     * @throws TemplateIOException if required attributes are missing
     */
    private static void readTextfield( Element element, TemplateWidget parent ) throws TemplateIOException
    {
        LOG.debug( "Reading a template textfield" ); //$NON-NLS-1$

        // Creating the text field
        TemplateTextField textField = new TemplateTextField( parent );

        // Reading the widget's common properties
        readWidgetCommonProperties( element, textField, true, ELEMENT_TEXTFIELD );

        // Reading the 'numberOfRows' attribute
        Attribute numberOfRowsAttribute = element.attribute( ATTRIBUTE_NUMBEROFROWS );
        if ( ( numberOfRowsAttribute != null ) && ( numberOfRowsAttribute.getText() != null ) )
        {
            textField.setNumberOfRows( readInteger( numberOfRowsAttribute.getText() ) );
        }

        // Reading the 'charactersLimit' attribute
        Attribute charactersLimitAttribute = element.attribute( ATTRIBUTE_CHARACTERSLIMIT );
        if ( ( charactersLimitAttribute != null ) && ( charactersLimitAttribute.getText() != null ) )
        {
            textField.setCharactersLimit( readInteger( charactersLimitAttribute.getText() ) );
        }

        // Reading the 'dollarSignIsNewLine' attribute
        Attribute dollarSignIsNewLineAttribute = element.attribute( ATTRIBUTE_DOLLAR_SIGN_IS_NEW_LINE );
        if ( ( dollarSignIsNewLineAttribute != null ) && ( dollarSignIsNewLineAttribute.getText() != null ) )
        {
            textField.setDollarSignIsNewLine( readBoolean( dollarSignIsNewLineAttribute.getText() ) );
        }
    }


    // ── READ BOOLEAN: CONVERT "true"/"false" TO boolean ──────────────────────────
    /**
     * Converts the string {@code "true"} or {@code "false"} (case-insensitive) to
     * a Java {@code boolean}.
     *
     * @param text  the XML attribute value to convert
     * @return {@code true} or {@code false}
     * @throws TemplateIOException if the string is neither "true" nor "false"
     */
    private static boolean readBoolean( String text ) throws TemplateIOException
    {
        if ( text.equalsIgnoreCase( VALUE_TRUE ) )
        {
            return true;
        }
        else if ( text.equalsIgnoreCase( VALUE_FALSE ) )
        {
            return false;
        }
        else
        {
            LOG.error( "Unable to convert this string to a boolean ('" + VALUE_TRUE + "' or '" + VALUE_FALSE + "'): '" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + text + "'." ); //$NON-NLS-1$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToConvertStringToBoolean" ), new String[] //$NON-NLS-1$
                    { VALUE_TRUE, VALUE_FALSE, text } ) );
        }
    }


    // ── READ INTEGER: PARSE A STRING AS AN INT ────────────────────────────────────
    /**
     * Parses the given string as a Java {@code int}.
     *
     * @param text  the XML attribute value to parse
     * @return the parsed integer
     * @throws TemplateIOException if the string is not a valid integer
     */
    private static int readInteger( String text ) throws TemplateIOException
    {
        try
        {
            return Integer.parseInt( text );
        }
        catch ( NumberFormatException e )
        {
            LOG.error( "Unable to convert this string to an integer: '" + text + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
            throw new TemplateIOException( THE_FILE_DOES_NOT_SEEM_TO_BE_A_VALID_TEMPLATE_FILE + "\n" //$NON-NLS-1$
                + NLS.bind( Messages.getString( "TemplateIO.UnableToConvertStringToInteger" ), text ) ); //$NON-NLS-1$
        }
    }


    // ── SAVE: ENCODE THE TEMPLATE MODEL AS XML AND WRITE TO STREAM ───────────────
    // C-3PO translates the Java model back into Jawa dialect — encoding the entire
    // template tree as pretty-printed UTF-8 XML and writing it to the output stream.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given {@link Template} to pretty-printed UTF-8 XML and writes
     * it to {@code stream}. Creates a dom4j {@link Document}, builds the XML tree
     * from the model, then flushes via a {@link XMLWriter}.
     *
     * @param template  the template to serialize
     * @param stream    the output stream to write to
     * @throws IOException if an I/O error occurs during writing
     */
    public static void save( Template template, OutputStream stream ) throws IOException
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        writeTemplate( document, template );

        // Writing the file to disk
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
        XMLWriter writer = new XMLWriter( stream, outformat );
        writer.write( document );
        writer.flush();
        writer.close();
    }


    // ── WRITE TEMPLATE: ENCODE THE ROOT TEMPLATE ELEMENT ──────────────────────────
    /**
     * Creates the root {@code <template>} element in the document and writes the
     * {@code id}, {@code title}, object classes, and form.
     *
     * @param document  the dom4j document to write into
     * @param template  the template to encode
     */
    private static void writeTemplate( Document document, Template template )
    {
        // Creating the root element
        Element rootElement = document.addElement( ELEMENT_TEMPLATE );

        // Writing the ID
        rootElement.addAttribute( ATTRIBUTE_ID, template.getId() );

        // Writing the title
        rootElement.addAttribute( ATTRIBUTE_TITLE, template.getTitle() );

        // Writing the object classes
        writeObjectClasses( rootElement, template );

        // Writing the form
        writeForm( rootElement, template );
    }


    // ── WRITE OBJECT CLASSES: ENCODE STRUCTURAL AND AUXILIARY CLASSES ─────────────
    /**
     * Creates the {@code <objectClasses>} element with a {@code <structural>} child
     * and optional {@code <auxiliaries>/<auxiliary>} children.
     *
     * @param element   the parent element
     * @param template  the template to read object classes from
     */
    private static void writeObjectClasses( Element element, Template template )
    {
        // Creating the 'objectClasses' element
        Element objectClassesElement = element.addElement( ELEMENT_OBJECTCLASSES );

        // Creating the 'structural' element
        objectClassesElement.addElement( ELEMENT_STRUCTURAL ).setText( template.getStructuralObjectClass() );

        List<String> auxiliaryObjectClasses = template.getAuxiliaryObjectClasses();
        if ( ( auxiliaryObjectClasses != null ) & ( auxiliaryObjectClasses.size() > 0 ) )
        {
            // Creating the 'auxiliaries' element
            Element auxiliariesElement = objectClassesElement.addElement( ELEMENT_AUXILIARIES );

            // Creating each 'auxiliary' element
            for ( String auxliaryObjectClass : template.getAuxiliaryObjectClasses() )
            {
                auxiliariesElement.addElement( ELEMENT_AUXILIARY ).setText( auxliaryObjectClass );
            }
        }
    }


    // ── WRITE FORM: ENCODE THE ROOT UI LAYOUT ────────────────────────────────────
    /**
     * Creates the {@code <form>} element and recursively encodes all top-level
     * child widgets.
     *
     * @param element   the parent element
     * @param template  the template whose form to encode
     */
    private static void writeForm( Element element, Template template )
    {
        // Creating the 'form' element
        Element formElement = element.addElement( ELEMENT_FORM );

        // Getting the form
        TemplateForm form = template.getForm();

        // Creating each child element
        for ( TemplateWidget widget : form.getChildren() )
        {
            writeWidget( formElement, widget );
        }
    }


    // ── WRITE WIDGET: DISPATCH TO THE CORRECT WIDGET ENCODER ─────────────────────
    /**
     * Dispatches the given {@link TemplateWidget} to the correct write* method
     * based on its runtime type. Unknown types are silently skipped.
     *
     * @param element  the parent XML element
     * @param widget   the widget to encode
     */
    private static void writeWidget( Element element, TemplateWidget widget )
    {
        // Switching on the various widgets we support
        if ( widget instanceof TemplateCheckbox )
        {
            writeCheckbox( element, ( TemplateCheckbox ) widget );
        }
        else if ( widget instanceof TemplateComposite )
        {
            writeComposite( element, ( TemplateComposite ) widget );
        }
        else if ( widget instanceof TemplateDate )
        {
            writeDate( element, ( TemplateDate ) widget );
        }
        else if ( widget instanceof TemplateFileChooser )
        {
            writeFileChooser( element, ( TemplateFileChooser ) widget );
        }
        else if ( widget instanceof TemplateImage )
        {
            writeImage( element, ( TemplateImage ) widget );
        }
        else if ( widget instanceof TemplateLabel )
        {
            writeLabel( element, ( TemplateLabel ) widget );
        }
        else if ( widget instanceof TemplateLink )
        {
            writeLink( element, ( TemplateLink ) widget );
        }
        else if ( widget instanceof TemplateListbox )
        {
            writeListbox( element, ( TemplateListbox ) widget );
        }
        else if ( widget instanceof TemplateSection )
        {
            writeSection( element, ( TemplateSection ) widget );
        }
        else if ( widget instanceof TemplatePassword )
        {
            writePassword( element, ( TemplatePassword ) widget );
        }
        else if ( widget instanceof TemplateRadioButtons )
        {
            writeRadioButtons( element, ( TemplateRadioButtons ) widget );
        }
        else if ( widget instanceof TemplateSpinner )
        {
            writeSpinner( element, ( TemplateSpinner ) widget );
        }
        else if ( widget instanceof TemplateTable )
        {
            writeTable( element, ( TemplateTable ) widget );
        }
        else if ( widget instanceof TemplateTextField )
        {
            writeTextfield( element, ( TemplateTextField ) widget );
        }
        // We could not find a correct widget type.
        else
        {
            // Unsupported widget type
        }
    }


    // ── WRITE WIDGET COMMON PROPERTIES: ENCODE SHARED LAYOUT ATTRIBUTES ───────────
    /**
     * Writes the attributes common to every {@link TemplateWidget} — only emitting
     * non-default values to keep the XML compact.
     *
     * @param element  the XML element to write attributes into
     * @param widget   the widget to read values from
     */
    private static void writeWidgetCommonProperties( Element element, TemplateWidget widget )
    {
        // Creating the 'attributeType' attribute
        writeAttributeTypeAttribute( element, widget );

        // Creating the 'horizontalAlignment' attribute
        if ( widget.getHorizontalAlignment() != TemplateWidget.DEFAULT_HORIZONTAL_ALIGNMENT )
        {
            element.addAttribute( ATTRIBUTE_HORIZONTAL_ALIGNMENT, getWidgetAlignmentValue( widget
                .getHorizontalAlignment() ) );
        }

        // Creating the 'verticalAlignment' attribute
        if ( widget.getVerticalAlignment() != TemplateWidget.DEFAULT_VERTICAL_ALIGNMENT )
        {
            element
                .addAttribute( ATTRIBUTE_VERTICAL_ALIGNMENT, getWidgetAlignmentValue( widget.getVerticalAlignment() ) );
        }

        // Creating the 'grabExcessHorizontalSpace' attribute
        if ( widget.isGrabExcessHorizontalSpace() != TemplateWidget.DEFAULT_GRAB_EXCESS_HORIZONTAL_SPACE )
        {
            element.addAttribute( ATTRIBUTE_GRAB_EXCESS_HORIZONTAL_SPACE, "" + widget.isGrabExcessHorizontalSpace() ); //$NON-NLS-1$
        }

        // Creating the 'grabExcessVerticalSpace' attribute
        if ( widget.isGrabExcessVerticalSpace() != TemplateWidget.DEFAULT_GRAB_EXCESS_VERTICAL_SPACE )
        {
            element.addAttribute( ATTRIBUTE_GRAB_EXCESS_VERTICAL_SPACE, "" + widget.isGrabExcessVerticalSpace() ); //$NON-NLS-1$
        }

        // Creating the 'horizontalSpan' attribute
        if ( widget.getHorizontalSpan() != TemplateWidget.DEFAULT_HORIZONTAL_SPAN )
        {
            element.addAttribute( ATTRIBUTE_HORIZONTAL_SPAN, "" + widget.getHorizontalSpan() ); //$NON-NLS-1$
        }

        // Creating the 'verticalSpan' attribute
        if ( widget.getVerticalSpan() != TemplateWidget.DEFAULT_VERTICAL_SPAN )
        {
            element.addAttribute( ATTRIBUTE_VERTICAL_SPAN, "" + widget.getVerticalSpan() ); //$NON-NLS-1$
        }

        // Creating the 'width' attribute
        if ( widget.getImageWidth() != TemplateWidget.DEFAULT_SIZE )
        {
            element.addAttribute( ATTRIBUTE_WIDTH, "" + widget.getImageWidth() ); //$NON-NLS-1$
        }

        // Creating the 'height' attribute
        if ( widget.getImageHeight() != TemplateWidget.DEFAULT_SIZE )
        {
            element.addAttribute( ATTRIBUTE_HEIGHT, "" + widget.getImageHeight() ); //$NON-NLS-1$
        }
    }


    // ── GET WIDGET ALIGNMENT VALUE: CONVERT ENUM TO STRING ───────────────────────
    /**
     * Converts a {@link WidgetAlignment} enum value to the corresponding XML string
     * ({@code "none"}, {@code "beginning"}, {@code "center"}, {@code "end"}, or
     * {@code "fill"}).
     *
     * @param alignment  the alignment to convert
     * @return the XML string representation
     */
    private static String getWidgetAlignmentValue( WidgetAlignment alignment )
    {
        switch ( alignment )
        {
            case NONE:
                return VALUE_NONE;
            case BEGINNING:
                return VALUE_BEGINNING;
            case CENTER:
                return VALUE_CENTER;
            case END:
                return VALUE_END;
            case FILL:
                return VALUE_FILL;
            default:
                return VALUE_NONE;
        }
    }


    // ── WRITE ATTRIBUTE TYPE ATTRIBUTE: ENCODE THE LDAP ATTRIBUTE BINDING ─────────
    /**
     * Writes the {@code attributeType} XML attribute for the widget, if one is set.
     *
     * @param element  the XML element to add the attribute to
     * @param widget   the widget to read the attribute type from
     */
    private static void writeAttributeTypeAttribute( Element element, TemplateWidget widget )
    {
        String attributeType = widget.getAttributeType();
        if ( ( attributeType != null ) && ( !attributeType.equals( "" ) ) ) //$NON-NLS-1$
        {
            element.addAttribute( ATTRIBUTE_ATTRIBUTETYPE, attributeType );
        }
    }


    // ── WRITE CHECKBOX: ENCODE A CHECKBOX WIDGET ──────────────────────────────────
    /**
     * Creates a {@code <checkbox>} element with label, enabled, checkedValue, and
     * uncheckedValue (non-default values only).
     *
     * @param element   the parent element
     * @param checkbox  the checkbox widget to encode
     */
    private static void writeCheckbox( Element element, TemplateCheckbox checkbox )
    {
        // Creating the 'checkbox' element
        Element checkboxElement = element.addElement( ELEMENT_CHECKBOX );

        // Creating the widget's common properties
        writeWidgetCommonProperties( checkboxElement, checkbox );

        // Creating the 'label' attribute
        String label = ( String ) checkbox.getLabel();
        if ( ( label != null ) && ( !( label.equals( TemplateCheckbox.DEFAULT_LABEL ) ) ) )
        {
            checkboxElement.addAttribute( ATTRIBUTE_LABEL, checkbox.getLabel() );
        }

        // Creating the 'enabled' attribute
        if ( checkbox.isEnabled() != TemplateCheckbox.DEFAULT_ENABLED )
        {
            checkboxElement.addAttribute( ATTRIBUTE_ENABLED, "" + checkbox.isEnabled() ); //$NON-NLS-1$
        }

        // Creating the 'checkedValue' element (if necessary)
        String checkedValue = ( String ) checkbox.getCheckedValue();
        if ( ( checkedValue != null ) && ( !( checkedValue.equals( TemplateCheckbox.DEFAULT_CHECKED_VALUE ) ) ) )
        {
            Element checkedValueElement = checkboxElement.addElement( ELEMENT_CHECKEDVALUE );
            checkedValueElement.setText( checkedValue );
        }

        // Creating the 'uncheckedValue' element
        String uncheckedValue = ( String ) checkbox.getUncheckedValue();
        if ( ( uncheckedValue != null ) && ( !( uncheckedValue.equals( TemplateCheckbox.DEFAULT_UNCHECKED_VALUE ) ) ) )
        {
            Element uncheckedValueElement = checkboxElement.addElement( ELEMENT_UNCHECKEDVALUE );
            uncheckedValueElement.setText( uncheckedValue );
        }
    }


    // ── WRITE COMPOSITE: ENCODE A COMPOSITE WIDGET ────────────────────────────────
    /**
     * Creates a {@code <composite>} element with column layout settings and
     * recursively encodes all child widgets.
     *
     * @param element    the parent element
     * @param composite  the composite widget to encode
     */
    private static void writeComposite( Element element, TemplateComposite composite )
    {
        // Creating the 'composite' element
        Element compositeElement = element.addElement( ELEMENT_COMPOSITE );

        // Creating the widget's common properties
        writeWidgetCommonProperties( compositeElement, composite );

        // Creating the 'numberOfColumns' attribute
        if ( composite.getNumberOfColumns() != TemplateComposite.DEFAULT_NUMBER_OF_COLUMNS )
        {
            compositeElement.addAttribute( ATTRIBUTE_NUMBEROFCOLUMNS, "" + composite.getNumberOfColumns() ); //$NON-NLS-1$
        }

        // Creating the 'equalColumns' attribute
        if ( composite.isEqualColumns() != TemplateComposite.DEFAULT_EQUAL_COLUMNS )
        {
            compositeElement.addAttribute( ATTRIBUTE_EQUALCOLUMNS, "" + composite.isEqualColumns() ); //$NON-NLS-1$
        }

        // Creating the children
        List<TemplateWidget> children = composite.getChildren();
        if ( ( children != null ) && ( children.size() > 0 ) )
        {
            for ( TemplateWidget child : children )
            {
                writeWidget( compositeElement, child );
            }
        }
    }


    // ── WRITE DATE: ENCODE A DATE PICKER WIDGET ───────────────────────────────────
    /**
     * Creates a {@code <date>} element with format and edit-button settings.
     *
     * @param element  the parent element
     * @param date     the date widget to encode
     */
    private static void writeDate( Element element, TemplateDate date )
    {
        // Creating the 'date' element
        Element dateElement = element.addElement( ELEMENT_DATE );

        // Creating the widget's common properties
        writeWidgetCommonProperties( dateElement, date );

        // Creating the 'showEditButton' attribute
        if ( date.isShowEditButton() != TemplateDate.DEFAULT_SHOW_EDIT_BUTTON )
        {
            dateElement.addAttribute( ATTRIBUTE_SHOWEDITBUTTON, convert( date.isShowEditButton() ) );
        }

        // Creating the 'format' element
        String format = date.getFormat();
        if ( ( format != null ) && ( !( format.equals( TemplateDate.DEFAULT_FORMAT ) ) ) )
        {
            dateElement.addAttribute( ATTRIBUTE_FORMAT, format );
        }
    }


    // ── WRITE FILE CHOOSER: ENCODE A FILE CHOOSER WIDGET ─────────────────────────
    /**
     * Creates a {@code <fileChooser>} element with extension list, button visibility,
     * and optional icon element.
     *
     * @param element      the parent element
     * @param fileChooser  the file chooser widget to encode
     */
    private static void writeFileChooser( Element element, TemplateFileChooser fileChooser )
    {
        // Creating the 'fileChooser' element
        Element fileChooserElement = element.addElement( ELEMENT_FILECHOOSER );

        // Creating the widget's common properties
        writeWidgetCommonProperties( fileChooserElement, fileChooser );

        // Creating the 'extensions' attribute (if necessary)
        Set<String> extensions = fileChooser.getExtensions();
        if ( ( extensions != null ) && ( extensions.size() > 0 ) )
        {
            // Creating the string containing the extensions value (all the
            // extensions are concatenated and split with a ',' character).
            StringBuilder sb = new StringBuilder();
            for ( String extension : extensions )
            {
                sb.append( extension );
                sb.append( "," ); //$NON-NLS-1$
            }
            // Removing the last ',' character
            if ( sb.length() > 0 )
            {
                sb.deleteCharAt( sb.length() - 1 );
            }

            // Creating the 'extensions' attribute
            fileChooserElement.addAttribute( ATTRIBUTE_EXTENSIONS, sb.toString() );
        }

        // Creating the 'showIcon' attribute
        if ( fileChooser.isShowIcon() != TemplateFileChooser.DEFAULT_SHOW_ICON )
        {
            fileChooserElement.addAttribute( ATTRIBUTE_SHOWICON, convert( fileChooser.isShowIcon() ) );
        }

        // Creating the 'showSaveAsButton' attribute
        if ( fileChooser.isShowClearButton() != TemplateFileChooser.DEFAULT_SHOW_SAVE_AS_BUTTON )
        {
            fileChooserElement.addAttribute( ATTRIBUTE_SHOWSAVEASBUTTON, convert( fileChooser.isShowSaveAsButton() ) );
        }

        // Creating the 'showClearButton' attribute
        if ( fileChooser.isShowClearButton() != TemplateFileChooser.DEFAULT_SHOW_CLEAR_BUTTON )
        {
            fileChooserElement.addAttribute( ATTRIBUTE_SHOWCLEARBUTTON, convert( fileChooser.isShowClearButton() ) );
        }

        // Creating the 'showBrowseButton' attribute
        if ( fileChooser.isShowBrowseButton() != TemplateFileChooser.DEFAULT_SHOW_BROWSE_BUTTON )
        {
            fileChooserElement.addAttribute( ATTRIBUTE_SHOWBROWSEBUTTON, convert( fileChooser.isShowBrowseButton() ) );
        }

        // Creating the 'icon' element
        String icon = ( String ) fileChooser.getIcon();
        if ( ( icon != null ) && ( !( icon.equals( TemplateFileChooser.DEFAULT_ICON ) ) ) )
        {
            fileChooserElement.addElement( ELEMENT_ICON ).setText( icon );
        }
    }


    // ── WRITE IMAGE: ENCODE AN IMAGE DISPLAY WIDGET ───────────────────────────────
    /**
     * Creates an {@code <image>} element with button visibility, size constraints,
     * and optional embedded Base64 image data.
     *
     * @param element  the parent element
     * @param image    the image widget to encode
     */
    private static void writeImage( Element element, TemplateImage image )
    {
        // Creating the 'image' element
        Element imageElement = element.addElement( ELEMENT_IMAGE );

        // Creating the widget's common properties
        writeWidgetCommonProperties( imageElement, image );

        // Creating the 'showSaveAsButton' attribute
        if ( image.isShowSaveAsButton() != TemplateImage.DEFAULT_SHOW_SAVE_AS_BUTTON )
        {
            imageElement.addAttribute( ATTRIBUTE_SHOWSAVEASBUTTON, convert( image.isShowSaveAsButton() ) );
        }

        // Creating the 'showClearButton' attribute
        if ( image.isShowClearButton() != TemplateImage.DEFAULT_SHOW_CLEAR_BUTTON )
        {
            imageElement.addAttribute( ATTRIBUTE_SHOWCLEARBUTTON, convert( image.isShowClearButton() ) );
        }

        // Creating the 'showChooseButton' attribute
        if ( image.isShowBrowseButton() != TemplateImage.DEFAULT_SHOW_BROWSE_BUTTON )
        {
            imageElement.addAttribute( ATTRIBUTE_SHOWBROWSEBUTTON, convert( image.isShowBrowseButton() ) );
        }

        // Creating the 'imageWidth' attribute
        if ( image.getImageWidth() != TemplateWidget.DEFAULT_SIZE )
        {
            imageElement.addAttribute( ATTRIBUTE_IMAGE_WIDTH, "" + image.getImageWidth() ); //$NON-NLS-1$
        }

        // Creating the 'imageHeight' attribute
        if ( image.getImageHeight() != TemplateWidget.DEFAULT_SIZE )
        {
            imageElement.addAttribute( ATTRIBUTE_IMAGE_HEIGHT, "" + image.getImageHeight() ); //$NON-NLS-1$
        }

        // Creating the 'data' element
        String imageData = ( String ) image.getImageData();
        if ( ( imageData != null ) && ( !( imageData.equals( TemplateImage.DEFAULT_IMAGE_DATA ) ) ) )
        {
            imageElement.addElement( ELEMENT_DATA ).setText( imageData );
        }
    }


    // ── WRITE LABEL: ENCODE A LABEL DISPLAY WIDGET ────────────────────────────────
    /**
     * Creates a {@code <label>} element with optional static value, row count, and
     * dollar-sign newline settings.
     *
     * @param element  the parent element
     * @param label    the label widget to encode
     */
    private static void writeLabel( Element element, TemplateLabel label )
    {
        // Creating the 'label' element
        Element labelElement = element.addElement( ELEMENT_LABEL );

        // Creating the widget's common properties
        writeWidgetCommonProperties( labelElement, label );

        // Creating the 'value' attribute
        String value = label.getValue();
        if ( ( value != null ) && ( !( value.equals( TemplateLabel.DEFAULT_VALUE ) ) ) )
        {
            labelElement.addAttribute( ATTRIBUTE_VALUE, value );
        }

        // Creating the 'numberOfRows' attribute
        if ( label.getNumberOfRows() != TemplateLabel.DEFAULT_NUMBER_OF_ROWS )
        {
            labelElement.addAttribute( ATTRIBUTE_NUMBEROFROWS, "" + label.getNumberOfRows() ); //$NON-NLS-1$
        }

        // Creating the 'dollarSignIsNewLine' attribute
        if ( label.isDollarSignIsNewLine() != TemplateLabel.DEFAULT_DOLLAR_SIGN_IS_NEW_LINE )
        {
            labelElement.addAttribute( ATTRIBUTE_DOLLAR_SIGN_IS_NEW_LINE, "" + label.isDollarSignIsNewLine() ); //$NON-NLS-1$
        }
    }


    // ── WRITE LINK: ENCODE A HYPERLINK WIDGET ─────────────────────────────────────
    /**
     * Creates a {@code <link>} element with optional static value.
     *
     * @param element  the parent element
     * @param link     the link widget to encode
     */
    private static void writeLink( Element element, TemplateLink link )
    {
        // Creating the 'link' element
        Element linkElement = element.addElement( ELEMENT_LINK );

        // Creating the widget's common properties
        writeWidgetCommonProperties( linkElement, link );

        // Creating the 'value' attribute
        String value = link.getValue();
        if ( ( value != null ) && ( !( value.equals( TemplateLink.DEFAULT_VALUE ) ) ) )
        {
            linkElement.addAttribute( ATTRIBUTE_VALUE, value );
        }
    }


    // ── WRITE LISTBOX: ENCODE A LIST BOX WIDGET ───────────────────────────────────
    /**
     * Creates a {@code <listbox>} element with enabled/multiple-selection flags and
     * an {@code <items>} child containing all {@link ValueItem} entries.
     *
     * @param element  the parent element
     * @param listbox  the listbox widget to encode
     */
    private static void writeListbox( Element element, TemplateListbox listbox )
    {
        // Creating the 'listbox' element
        Element listboxElement = element.addElement( ELEMENT_LISTBOX );

        // Creating the widget's common properties
        writeWidgetCommonProperties( listboxElement, listbox );

        // Creating the 'enabled' attribute
        if ( listbox.isEnabled() != TemplateListbox.DEFAULT_ENABLED )
        {
            listboxElement.addAttribute( ATTRIBUTE_ENABLED, "" + listbox.isEnabled() ); //$NON-NLS-1$
        }

        // Creating the 'multipleSelection' attribute
        if ( listbox.isMultipleSelection() != TemplateListbox.DEFAULT_MULTIPLE_SELECTION )
        {
            listboxElement.addAttribute( ATTRIBUTE_MULTIPLESELECTION, convert( listbox.isMultipleSelection() ) );
        }

        // Creating the 'items' element
        List<ValueItem> items = listbox.getItems();
        if ( ( items != null ) && ( items.size() > 0 ) )
        {
            Element itemsElement = listboxElement.addElement( ELEMENT_ITEMS );

            // Creating the 'item' elements
            for ( ValueItem item : items )
            {
                Element itemElement = itemsElement.addElement( ELEMENT_ITEM );
                writeValueItem( itemElement, item );
            }
        }
    }


    // ── WRITE VALUE ITEM: ENCODE A LABEL/VALUE PAIR ───────────────────────────────
    /**
     * Writes a {@link ValueItem} as {@code <label>} and {@code <value>} child
     * elements inside the given parent element.
     *
     * @param element  the parent XML element (e.g. {@code <item>} or {@code <button>})
     * @param item     the value item to encode
     */
    private static void writeValueItem( Element element, ValueItem item )
    {
        // Creating the 'label' element
        String itemLabel = item.getLabel();
        if ( ( itemLabel != null ) && ( !itemLabel.equals( "" ) ) ) //$NON-NLS-1$
        {
            element.addElement( ELEMENT_LABEL ).setText( itemLabel );
        }

        // Creating the 'value' element
        String itemValue = ( String ) item.getValue();
        if ( ( itemValue != null ) && ( !itemValue.equals( "" ) ) ) //$NON-NLS-1$
        {
            element.addElement( ELEMENT_VALUE ).setText( itemValue );
        }
    }


    // ── WRITE PASSWORD: ENCODE A PASSWORD WIDGET ──────────────────────────────────
    /**
     * Creates a {@code <password>} element with hidden, edit-button, and
     * show-password-checkbox flags.
     *
     * @param element   the parent element
     * @param password  the password widget to encode
     */
    private static void writePassword( Element element, TemplatePassword password )
    {
        // Creating the 'password' element
        Element passwordElement = element.addElement( ELEMENT_PASSWORD );

        // Creating the widget's common properties
        writeWidgetCommonProperties( passwordElement, password );

        // Creating the 'hidden' attribute
        if ( password.isHidden() != TemplatePassword.DEFAULT_HIDDEN )
        {
            passwordElement.addAttribute( ATTRIBUTE_HIDDEN, convert( password.isHidden() ) );
        }

        // Creating the 'showEditButton' attribute
        if ( password.isShowEditButton() != TemplatePassword.DEFAULT_SHOW_EDIT_BUTTON )
        {
            passwordElement.addAttribute( ATTRIBUTE_SHOWEDITBUTTON, convert( password.isShowEditButton() ) );
        }

        // Creating the 'showShowPasswordCheckbox' attribute
        if ( password.isShowShowPasswordCheckbox() != TemplatePassword.DEFAULT_SHOW_PASSWORD_CHECKBOX )
        {
            passwordElement.addAttribute( ATTRIBUTE_SHOWSHOWPASSWORDCHECKBOX, convert( password
                .isShowShowPasswordCheckbox() ) );
        }
    }


    // ── WRITE RADIO BUTTONS: ENCODE A RADIO BUTTON GROUP WIDGET ──────────────────
    /**
     * Creates a {@code <radiobuttons>} element with enabled flag and a
     * {@code <buttons>} child containing all {@link ValueItem} buttons.
     *
     * @param element       the parent element
     * @param radioButtons  the radio buttons widget to encode
     */
    private static void writeRadioButtons( Element element, TemplateRadioButtons radioButtons )
    {
        // Creating the 'radioButtons' element
        Element radioButtonsElement = element.addElement( ELEMENT_RADIOBUTTONS );

        // Creating the widget's common properties
        writeWidgetCommonProperties( element, radioButtons );

        // Creating the 'enabled' attribute
        if ( radioButtons.isEnabled() != TemplateRadioButtons.DEFAULT_ENABLED )
        {
            radioButtonsElement.addAttribute( ATTRIBUTE_ENABLED, "" + radioButtons.isEnabled() ); //$NON-NLS-1$
        }

        // Creating the 'buttons' element
        List<ValueItem> buttons = radioButtons.getButtons();
        if ( ( buttons != null ) && ( buttons.size() > 0 ) )
        {
            Element buttonsElement = radioButtonsElement.addElement( ELEMENT_BUTTONS );

            for ( ValueItem button : buttons )
            {
                Element buttonElement = buttonsElement.addElement( ELEMENT_BUTTON );
                writeValueItem( buttonElement, button );
            }
        }
    }


    // ── WRITE SECTION: ENCODE A COLLAPSIBLE SECTION WIDGET ───────────────────────
    /**
     * Creates a {@code <section>} element with title, description, column layout,
     * expand settings, and recursively encoded child widgets.
     *
     * @param element  the parent element
     * @param section  the section widget to encode
     */
    private static void writeSection( Element element, TemplateSection section )
    {
        // Creating the 'section' element
        Element sectionElement = element.addElement( ELEMENT_SECTION );

        // Creating the widget's common properties
        writeWidgetCommonProperties( sectionElement, section );

        // Creating the 'title' attribute
        String title = section.getTitle();
        if ( ( title != null ) && ( !( title.equals( TemplateSection.DEFAULT_TITLE ) ) ) )
        {
            sectionElement.addAttribute( ATTRIBUTE_TITLE, title );
        }

        // Creating the 'description' attribute
        String description = section.getDescription();
        if ( ( description != null ) && ( !( description.equals( TemplateSection.DEFAULT_DESCRIPTION ) ) ) )
        {
            sectionElement.addAttribute( ATTRIBUTE_DESCRIPTION, description );
        }

        // Creating the 'numberOfColumns' attribute
        if ( section.getNumberOfColumns() != TemplateSection.DEFAULT_NUMBER_OF_COLUMNS )
        {
            sectionElement.addAttribute( ATTRIBUTE_NUMBEROFCOLUMNS, "" + section.getNumberOfColumns() ); //$NON-NLS-1$
        }

        // Creating the 'equalColumns' attribute
        if ( section.isEqualColumns() != TemplateSection.DEFAULT_EQUAL_COLUMNS )
        {
            sectionElement.addAttribute( ATTRIBUTE_EQUALCOLUMNS, "" + section.isEqualColumns() ); //$NON-NLS-1$
        }

        // Creating the 'expandable' attribute
        if ( section.isExpandable() != TemplateSection.DEFAULT_EXPANDABLE )
        {
            sectionElement.addAttribute( ATTRIBUTE_EXPANDABLE, "" + section.isExpandable() ); //$NON-NLS-1$
        }

        // Creating the 'expanded' attribute
        if ( section.isExpanded() != TemplateSection.DEFAULT_EXPANDED )
        {
            sectionElement.addAttribute( ATTRIBUTE_EXPANDED, "" + section.isExpanded() ); //$NON-NLS-1$
        }

        // Creating the children
        List<TemplateWidget> children = section.getChildren();
        if ( ( children != null ) && ( children.size() > 0 ) )
        {
            for ( TemplateWidget child : children )
            {
                writeWidget( sectionElement, child );
            }
        }
    }


    // ── WRITE SPINNER: ENCODE A NUMERIC SPINNER WIDGET ───────────────────────────
    /**
     * Creates a {@code <spinner>} element with minimum, maximum, increment, page
     * increment, and digits settings.
     *
     * @param element  the parent element
     * @param spinner  the spinner widget to encode
     */
    private static void writeSpinner( Element element, TemplateSpinner spinner )
    {
        // Creating the 'spinner' element
        Element spinnerElement = element.addElement( ELEMENT_SPINNER );

        // Creating the widget's common properties
        writeWidgetCommonProperties( spinnerElement, spinner );

        // Reading the 'minimum' attribute
        if ( spinner.getMinimum() != TemplateSpinner.DEFAULT_MINIMUM )
        {
            spinnerElement.addAttribute( ATTRIBUTE_MINIMUM, "" + spinner.getMinimum() ); //$NON-NLS-1$
        }

        // Reading the 'maximum' attribute
        if ( spinner.getMaximum() != TemplateSpinner.DEFAULT_MAXIMUM )
        {
            spinnerElement.addAttribute( ATTRIBUTE_MAXIMUM, "" + spinner.getMaximum() ); //$NON-NLS-1$
        }

        // Reading the 'increment' attribute
        if ( spinner.getIncrement() != TemplateSpinner.DEFAULT_INCREMENT )
        {
            spinnerElement.addAttribute( ATTRIBUTE_INCREMENT, "" + spinner.getIncrement() ); //$NON-NLS-1$
        }

        // Reading the 'pageIncrement' attribute
        if ( spinner.getPageIncrement() != TemplateSpinner.DEFAULT_PAGE_INCREMENT )
        {
            spinnerElement.addAttribute( ATTRIBUTE_PAGEINCREMENT, "" + spinner.getPageIncrement() ); //$NON-NLS-1$
        }

        // Reading the 'digits' attribute
        if ( spinner.getDigits() != TemplateSpinner.DEFAULT_DIGITS )
        {
            spinnerElement.addAttribute( ATTRIBUTE_DIGITS, "" + spinner.getDigits() ); //$NON-NLS-1$
        }
    }


    // ── WRITE TABLE: ENCODE A MULTI-VALUE TABLE WIDGET ────────────────────────────
    /**
     * Creates a {@code <table>} element with Add/Edit/Delete button visibility flags.
     *
     * @param element  the parent element
     * @param table    the table widget to encode
     */
    private static void writeTable( Element element, TemplateTable table )
    {
        // Creating the 'table' element
        Element tableElement = element.addElement( ELEMENT_TABLE );

        // Creating the widget's common properties
        writeWidgetCommonProperties( tableElement, table );

        // Creating the 'showAddButton' attribute
        if ( table.isShowAddButton() != TemplateTable.DEFAULT_SHOW_ADD_BUTTON )
        {
            tableElement.addAttribute( ATTRIBUTE_SHOWADDBUTTON, convert( table.isShowAddButton() ) );
        }

        // Creating the 'showEditButton' attribute
        if ( table.isShowEditButton() != TemplateTable.DEFAULT_SHOW_EDIT_BUTTON )
        {
            tableElement.addAttribute( ATTRIBUTE_SHOWEDITBUTTON, convert( table.isShowEditButton() ) );
        }

        // Creating the 'showDeleteButton' attribute
        if ( table.isShowDeleteButton() != TemplateTable.DEFAULT_SHOW_DELETE_BUTTON )
        {
            tableElement.addAttribute( ATTRIBUTE_SHOWDELETEBUTTON, convert( table.isShowDeleteButton() ) );
        }
    }


    // ── WRITE TEXTFIELD: ENCODE A TEXT INPUT WIDGET ───────────────────────────────
    /**
     * Creates a {@code <textfield>} element with row count, character limit, and
     * dollar-sign newline settings.
     *
     * @param element    the parent element
     * @param textField  the text field widget to encode
     */
    private static void writeTextfield( Element element, TemplateTextField textField )
    {
        // Creating the 'textField' element
        Element textFieldElement = element.addElement( ELEMENT_TEXTFIELD );

        // Creating the widget's common properties
        writeWidgetCommonProperties( textFieldElement, textField );

        // Creating the 'numberOfRows' attribute
        if ( textField.getNumberOfRows() != TemplateTextField.DEFAULT_NUMBER_OF_ROWS )
        {
            textFieldElement.addAttribute( ATTRIBUTE_NUMBEROFROWS, "" + textField.getNumberOfRows() ); //$NON-NLS-1$
        }

        // Creating the 'charactersLimit' attribute
        if ( textField.getCharactersLimit() != TemplateTextField.DEFAULT_CHARACTERS_LIMIT )
        {
            textFieldElement.addAttribute( ATTRIBUTE_CHARACTERSLIMIT, "" + textField.getCharactersLimit() ); //$NON-NLS-1$
        }

        // Creating the 'dollarSignIsNewLine' attribute
        if ( textField.isDollarSignIsNewLine() != TemplateTextField.DEFAULT_DOLLAR_SIGN_IS_NEW_LINE )
        {
            textFieldElement.addAttribute( ATTRIBUTE_DOLLAR_SIGN_IS_NEW_LINE, "" + textField.isDollarSignIsNewLine() ); //$NON-NLS-1$
        }
    }


    // ── CONVERT: ENCODE A BOOLEAN AS "true" OR "false" ───────────────────────────
    /**
     * Converts a Java {@code boolean} to its XML string representation
     * ({@code "true"} or {@code "false"}).
     *
     * @param bool  the boolean to convert
     * @return {@code "true"} or {@code "false"}
     */
    private static String convert( boolean bool )
    {
        if ( bool )
        {
            return VALUE_TRUE;
        }
        else
        {
            return VALUE_FALSE;
        }
    }
}
