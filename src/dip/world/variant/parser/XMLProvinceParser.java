//
//  @(#)XMLProvinceParser.java			7/2002
//
//  Copyright 2002 Zachary DelProposto. All rights reserved.
//  Use is subject to license terms.
//
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//  Or from http://www.gnu.org/
//
package dip.world.variant.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dip.misc.Log;
import dip.world.variant.data.BorderData;
import dip.world.variant.data.ProvinceData;

/**
 *	Parses an XML ProvinceData description.
 *
 *
 *
 *
 */
public class XMLProvinceParser implements ProvinceParser {
    // Element constants

    public static final String EL_PROVINCES = "PROVINCES";
    public static final String EL_PROVINCE = "PROVINCE";
    public static final String EL_UNIQUENAME = "UNIQUENAME";
    public static final String EL_ADJACENCY = "ADJACENCY";
    public static final String EL_BORDER_DEFINITIONS = "BORDER_DEFINITIONS";
    public static final String EL_BORDER = "BORDER";
    // Attribute constants
    public static final String ATT_SHORTNAME = "shortname";
    public static final String ATT_FULLNAME = "fullname";
    public static final String ATT_NAME = "name";
    public static final String ATT_TYPE = "type";
    public static final String ATT_REFS = "refs";
    public static final String ATT_CONVOYABLE_COAST = "isConvoyableCoast";
    public static final String ATT_ID = "id";
    public static final String ATT_DESCRIPTION = "description";
    public static final String ATT_UNIT_TYPES = "unitTypes";
    public static final String ATT_FROM = "from";
    public static final String ATT_ORDER_TYPES = "orderTypes";
    public static final String ATT_BASE_MOVE_MODIFIER = "baseMoveModifier";
    public static final String ATT_BORDERS = "borders";
    public static final String ATT_YEAR = "year";
    public static final String ATT_SEASON = "season";
    public static final String ATT_PHASE = "phase";
    // instance variables
    private Document doc = null;
    private DocumentBuilder docBuilder = null;
    private final List<ProvinceData> provinceList = new ArrayList<ProvinceData>(100);
    private final List<BorderData> borderList = new ArrayList<BorderData>(10);

    /** Create an XMLProvinceParser */
    public XMLProvinceParser(final DocumentBuilderFactory dbf)
            throws ParserConfigurationException {
        docBuilder = dbf.newDocumentBuilder();
        docBuilder.setErrorHandler(new XMLErrorHandler());
        FastEntityResolver.attach(docBuilder);
    }// XMLProvinceParser()

    /** Parse the given input stream; parsed data available via <code>getProvinceData()</code> */
    public void parse(InputStream is)
            throws IOException, SAXException {
        long time = System.currentTimeMillis();
        provinceList.clear();
        borderList.clear();

        doc = docBuilder.parse(is);
        procProvinceData();
        Log.printTimed(time, "   province parse time: ");
    }// parse()

    /** Cleanup, clearing any references/resources */
    public void close() {
        provinceList.clear();
        borderList.clear();
    }// close()

    /** Returns the ProvinceData objects, or an empty list. */
    @Override
	public ProvinceData[] getProvinceData() {
        return provinceList.toArray(new ProvinceData[provinceList.size()]);
    }// getProvinceData()

    /** Returns the BorderData objects, or an empty list. */
    @Override
	public BorderData[] getBorderData() {
        return borderList.toArray(new BorderData[borderList.size()]);
    }// getBorderData()

    /** Parse the XML */
    private void procProvinceData()
            throws IOException, SAXException {
        // find root element
        Element root = doc.getDocumentElement();

        // find all BORDER elements. We will use these later, via the borderMap
        NodeList borderNodes = root.getElementsByTagName(EL_BORDER);
        for (int i = 0; i < borderNodes.getLength(); i++) {
            final Node borderNode = borderNodes.item(i);
            if (borderNode instanceof Element) {
                Element border = (Element) borderNodes.item(i);
                BorderData bd = new BorderData();
                bd.setID(border.getAttribute(ATT_ID));
                bd.setDescription(border.getAttribute(ATT_DESCRIPTION));
                bd.setUnitTypes(border.getAttribute(ATT_UNIT_TYPES));
                bd.setFrom(border.getAttribute(ATT_FROM));
                bd.setOrderTypes(border.getAttribute(ATT_ORDER_TYPES));
                bd.setBaseMoveModifier(border.getAttribute(ATT_BASE_MOVE_MODIFIER));
                bd.setYear(border.getAttribute(ATT_YEAR));
                bd.setSeason(border.getAttribute(ATT_SEASON));
                bd.setPhase(border.getAttribute(ATT_PHASE));

                borderList.add(bd);
            }
        }

        // find all PROVINCE elements
        NodeList provinceNodes = root.getElementsByTagName(EL_PROVINCE);
        for (int i = 0; i < provinceNodes.getLength(); i++) {
            Element elProvince = (Element) provinceNodes.item(i);
            ProvinceData provinceData = new ProvinceData();

            // create short/unique name list
            final List<String> nameList = new LinkedList<String>();

            // region attributes
            provinceData.setFullName(elProvince.getAttribute(ATT_FULLNAME));
            nameList.add(elProvince.getAttribute(ATT_SHORTNAME));

            // convoyable coast
            provinceData.setConvoyableCoast(Boolean.valueOf(elProvince.getAttribute(ATT_CONVOYABLE_COAST)).booleanValue());

            // borders data (optional); a list of references, seperated by commas/spaces
            String borders = elProvince.getAttribute(ATT_BORDERS).trim();
            final List<String> borderList = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(borders, ", ");

            while (st.hasMoreTokens()) {
                borderList.add(st.nextToken());
            }

            provinceData.setBorders(borderList);


            // adjacency data
            NodeList adjNodes = elProvince.getElementsByTagName(EL_ADJACENCY);
            String[] adjTypeNames = new String[adjNodes.getLength()];
            String[] adjProvinceNames = new String[adjNodes.getLength()];
            for (int j = 0; j < adjNodes.getLength(); j++) {
                Element element = (Element) adjNodes.item(j);
                adjTypeNames[j] = element.getAttribute(ATT_TYPE);
                adjProvinceNames[j] = element.getAttribute(ATT_REFS);
            }
            provinceData.setAdjacentProvinceTypes(adjTypeNames);
            provinceData.setAdjacentProvinceNames(adjProvinceNames);

            // unique name(s) (if any)
            adjNodes = elProvince.getElementsByTagName(EL_UNIQUENAME);
            for (int j = 0; j < adjNodes.getLength(); j++) {
                final Node node = adjNodes.item(j);
                if (node instanceof Element) {
                    final Element element = (Element) node;
                    nameList.add(element.getAttribute(ATT_NAME));
                }
            }

            // set all short & unique names
            provinceData.setShortNames(nameList);

            // add to list
            provinceList.add(provinceData);
        }
    }// procProvinceData()
}// class XMLProvinceParser

