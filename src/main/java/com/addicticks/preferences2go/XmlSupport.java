/*
 * Copyright 2016 Addicticks.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.addicticks.preferences2go;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * XML Support for TemporaryPreferencesFactory. Method to import preferences
 * from an XML file.
 *
 * <p>
 * As a result of how the import-from-XML-file functionality has been
 * implemented in the official JDK 
 * ({@link java.util.prefs.Preferences#importPreferences(java.io.InputStream) })
 * it has unfortunately been necessary to lift this particular piece of code
 * from the JDK. Most of the code here is from the JDK albeit changed slightly
 * to fit our particular use case. The corresponding class in the JDK is
 * {@code java.util.prefs.XmlSupport}.
 * 
 * 
 * @author Josh Bloch and Mark Reinhold (original authors)
 * @author Addicticks
 * @see Preferences
 */
class XmlSupport {

    // The required DTD URI for exported preferences
    private static final String PREFS_DTD_URI
            = "http://java.sun.com/dtd/preferences.dtd";

    // The actual DTD corresponding to the URI
    private static final String PREFS_DTD
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!-- DTD for preferences -->"
            + "<!ELEMENT preferences (root) >"
            + "<!ATTLIST preferences"
            + " EXTERNAL_XML_VERSION CDATA \"0.0\"  >"
            + "<!ELEMENT root (map, node*) >"
            + "<!ATTLIST root"
            + "          type (system|user) #REQUIRED >"
            + "<!ELEMENT node (map, node*) >"
            + "<!ATTLIST node"
            + "          name CDATA #REQUIRED >"
            + "<!ELEMENT map (entry*) >"
            + "<!ATTLIST map"
            + "  MAP_XML_VERSION CDATA \"0.0\"  >"
            + "<!ELEMENT entry EMPTY >"
            + "<!ATTLIST entry"
            + "          key CDATA #REQUIRED"
            + "          value CDATA #REQUIRED >";
    
    /**
     * Version number for the format exported preferences files.
     */
    private static final String EXTERNAL_XML_VERSION = "1.0";


    /**
     * Import preferences from the specified input stream, which is assumed to
     * contain an XML document in the format described in the Preferences spec.
     *
     * @throws IOException if reading from the specified output stream results
     * in an <tt>IOException</tt>.
     * @throws InvalidPreferencesFormatException Data on input stream does not
     * constitute a valid XML document with the mandated document type.
     */
    static void importPreferences(InputStream is, Preferences userRoot, Preferences systemRoot)
            throws IOException, InvalidPreferencesFormatException {
        try {
            Document doc = loadPrefsDoc(is);
            String xmlVersion
                    = doc.getDocumentElement().getAttribute("EXTERNAL_XML_VERSION");
            if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0) {
                throw new InvalidPreferencesFormatException(
                        "Exported preferences file format version " + xmlVersion
                        + " is not supported. This java installation can read"
                        + " versions " + EXTERNAL_XML_VERSION + " or older. You may need"
                        + " to install a newer version of JDK.");
            }

            Element xmlRoot = (Element) doc.getDocumentElement().
                    getChildNodes().item(0);
            Preferences prefsRoot
                    = (xmlRoot.getAttribute("type").equals("user")
                    ? userRoot : systemRoot);
            ImportSubtree(prefsRoot, xmlRoot);
        } catch (SAXException e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }

    /**
     * Load an XML document from specified input stream, which must have the
     * requisite DTD URI.
     */
    private static Document loadPrefsDoc(InputStream in)
            throws SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            return db.parse(new InputSource(in));
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Recursively traverse the specified preferences node and store the
     * described preferences into the system or current user preferences tree,
     * as appropriate.
     * 
     * IMPLEMENTATION NOTE:  Compared to the same method from the JDK this
     * method does not attempt to do any locking on pref nodes as isn't meant
     * to be used in a concurrent scenario.
     *
     */
    private static void ImportSubtree(Preferences prefsNode, Element xmlNode) {
        NodeList xmlKids = xmlNode.getChildNodes();
        int numXmlKids = xmlKids.getLength();

        Preferences[] prefsKids;

        // Import any preferences at this node
        Element firstXmlKid = (Element) xmlKids.item(0);
        ImportPrefs(prefsNode, firstXmlKid);
        prefsKids = new Preferences[numXmlKids - 1];

        // Get involved children
        for (int i = 1; i < numXmlKids; i++) {
            Element xmlKid = (Element) xmlKids.item(i);
            prefsKids[i - 1] = prefsNode.node(xmlKid.getAttribute("name"));
        }

        // import children
        for (int i = 1; i < numXmlKids; i++) {
            ImportSubtree(prefsKids[i - 1], (Element) xmlKids.item(i));
        }
    }

    /**
     * Import the preferences described by the specified XML element (a map from
     * a preferences document) into the specified preferences node.
     */
    private static void ImportPrefs(Preferences prefsNode, Element map) {
        NodeList entries = map.getChildNodes();
        for (int i = 0, numEntries = entries.getLength(); i < numEntries; i++) {
            Element entry = (Element) entries.item(i);
            prefsNode.put(entry.getAttribute("key"),
                    entry.getAttribute("value"));
        }
    }

    private static class Resolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String pid, String sid)
                throws SAXException {
            if (sid.equals(PREFS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PREFS_DTD));
                is.setSystemId(PREFS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    private static class EH implements ErrorHandler {

        @Override
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }

        @Override
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }

        @Override
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
}
