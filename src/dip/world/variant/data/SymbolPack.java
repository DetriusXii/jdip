//
//  @(#)SymbolPack.java		10/2003
//
//  Copyright 2003 Zachary DelProposto. All rights reserved.
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
package dip.world.variant.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *	A SymbolPack
 *
 *
 */
public class SymbolPack implements Comparable<SymbolPack> {

    private String name = null;
    private float version = 0.0f;
    private String description = "";
    private URI thumbURI;
    private URI svgURI;
    private List<Symbol> symbols;
    private List<CSSStyle> cssStyles = new ArrayList<CSSStyle>();

    /** The name of the SymbolPack. */
    public String getName() {
        return name;
    }

    /** Version of this SymbolPack */
    public float getVersion() {
        return version;
    }

    /** The description of the SymbolPack. */
    public String getDescription() {
        return description;
    }

    /** Set the SymbolPack name. */
    public void setName(String value) {
        name = value;
    }

    /** Set the SymbolPack of this variant */
    public void setVersion(float value) {
        version = value;
    }

    /** Set the SymbolPack description */
    public void setDescription(String value) {
        description = value;
    }

    /** Get the URI for the thumbnail image */
    public URI getThumbnailURI() {
        return thumbURI;
    }

    /** Get the URI for the Symbol SVG data */
    public URI getSVGURI() {
        return svgURI;
    }

    /** Set the URI for the thumbnail image */
    public void setThumbnailURI(String value) {
        thumbURI = makeURI(value);
    }

    /** Set the URI for the Symbol SVG data */
    public void setSVGURI(String value) {
        svgURI = makeURI(value);
    }

    /** Get the Symbols */
    public List<Symbol> getSymbols() {
        return symbols;
    }

    /** Get the CSS Style data (if any) */
    public List<CSSStyle> getCSSStyles() {
        return cssStyles;
    }

    /** Do we have any CSS data? */
    public boolean hasCSSStyles() {
        return (cssStyles.size() > 0);
    }

    /** Set the CSS Style data */
    public void setCSSStyles(final List<CSSStyle> styles) {
        if (styles == null) {
            throw new IllegalArgumentException();
        }

        this.cssStyles = styles;
    }// setCSSStyles()

    /** Set the Symbols */
    public void setSymbols(final List<Symbol> symbols) {
        this.symbols = symbols;
    }

    /** Set the Symbols */
    /** Find the Symbol with the given Name (case sensitive); returns null if name not found. */
    public Symbol getSymbol(String name) {
        for (final Symbol symbol : symbols) {
            if (symbol.getName().equals(name)) {
                return symbol;
            }
        }

        return null;
    }// getSymbol()

    /** Comparison, based on Name. Only compares to other SymbolPack objects. */
    @Override
	public int compareTo(final SymbolPack sp) {
        return this.getName().compareTo(sp.getName());
    }// compareTo()

    /** Make a URI from a String */
    private URI makeURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }// makeURI()

    /** SymbolPack CSS data styles. */
    public static class CSSStyle {

        private final String name;
        private final String style;

        /** 
         *	Create a CSS style
         */
        public CSSStyle(String name, String style) {
            if (name == null || style == null) {
                throw new IllegalArgumentException();
            }

            this.name = name;
            this.style = style;
        }// CSSStyle()

        /** Get the CSS Style name. This will have the '.' in front of it. */
        public String getName() {
            return name;
        }

        /** Get the CSS Style value. */
        public String getStyle() {
            return style;
        }
    }// nested class CSSStyle
}// class SymbolPack
