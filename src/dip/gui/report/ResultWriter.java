//
//  @(#)ResultWriter.java		4/2002
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
package dip.gui.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ScrollPaneConstants;

import dip.gui.ClientFrame;
import dip.gui.dialog.TextViewer;
import dip.misc.Help;
import dip.misc.Utils;
import dip.order.OrderFormatOptions;
import dip.order.Orderable;
import dip.order.result.OrderResult;
import dip.order.result.OrderResult.ResultType;
import dip.order.result.Result;
import dip.world.Position;
import dip.world.Power;
import dip.world.TurnState;
import dip.world.World;

/**
*	Writes a summary of adjudication results in HTML format.
*	<p>
*	This now supports CSS styles and is internationalized. 
*	Note: nested &lt;div&gt; indents are cumulative.
*
*/
public class ResultWriter
{
	// i18n constants
	private static final String HTML_TEMPLATE 			= "ResultWriter.template";
	private static final String HTML_NO_RESULTS			= "ResultWriter.noresults.message";
	private static final String NO_GENERAL_RESULTS		= "ResultWriter.nogeneralresults.message";
	// i18n dialog constants
	private static final String DIALOG_TITLE		= "ResultWriter.dialog.title";
	
	
	// instance variables
	private final Position 		position;
	private final TurnState 	turnState;
	private final World 		world;
	private final Power[] 		allPowers;
	private final OrderFormatOptions ofo;
	
	/**
	*	Displays a summary of the current results as HTML.
	*	If the TurnState has not yet been resolved, an
	*	appropriate message is displayed.
	*/
	public static String resultsToHTML(TurnState ts, OrderFormatOptions orderFormatOptions)
	{
		if(!ts.isResolved())
		{
			return Utils.getText(Utils.getLocalString(HTML_NO_RESULTS));
		}
		
		ResultWriter rw = new ResultWriter(ts, orderFormatOptions);
		return rw.getResultsAsHTML();
	}// resultsToHTML()
	
	
	/**
	*	Returns the HTML-encoded adjudication results inside a dialog.
	*/
	public static void displayDialog(final ClientFrame clientFrame, 
		final TurnState ts, final OrderFormatOptions orderFormatOptions)
	{
		final StringBuffer title = new StringBuffer(64);
		title.append(Utils.getLocalString(DIALOG_TITLE));
		title.append(": ");
		title.append(ts.getPhase());
		
		TextViewer tv = new TextViewer(clientFrame);
		tv.setEditable(false);
		tv.addSingleButton( tv.makeOKButton() );
		tv.setTitle(title.toString());
		tv.setHelpID(Help.HelpID.Dialog_ResultReport);
		tv.setHeaderVisible(false);
		tv.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		tv.lazyLoadDisplayDialog(new TextViewer.TVRunnable()
		{
			@Override
			public void run()
			{
				setText(resultsToHTML(ts, orderFormatOptions));
			}
		});
	}// displayDialog()

	
	/** ResultWriter constructor. */
	private ResultWriter(TurnState ts, OrderFormatOptions ofo)
	{
		turnState = ts;
		world = ts.getWorld();
		allPowers = world.getMap().getPowers();
		position = ts.getPosition();
		this.ofo = ofo;
	}// ResultWriter()
	
	
	/** Write results as HTML */
	private String getResultsAsHTML()
	{
		// get template
		String templateText = Utils.getText(Utils.getLocalString(HTML_TEMPLATE));
		
		// get template objects
		Object[] templateData = new Object[]
		{
			turnState.getPhase(), 	// {0} : Phase
			getGeneralResults(), 	// {1} : General Results
			getPerPowerResults(),	// {2} : Per-Power results (combined)
		};
		
		// format into template
		return Utils.format(templateText, templateData);
	}// getResultsAsHTML()
	
	
	/** 
	*	Get the General results (results that are not addressed to 
	*	a specific power).
	*/
	private String getGeneralResults()
	{
            final List<Result> resultList = turnState.getResultList();
		
		// we want only results with a 'null' power.
		// these results are addressed to all.
		final List<Result> generalResults = new ArrayList<Result>(32);
		
                for(final Result r: resultList) {
			if(r.getPower() == null)
			{
				generalResults.add(r);
			}
		}
		
		// sort
		Collections.sort(generalResults);
		
		
		// print; if no general results, indicate.
		if(generalResults.isEmpty())
		{
			return Utils.getLocalString(NO_GENERAL_RESULTS);
		}
		
		
		StringBuffer sb = new StringBuffer(2048);
		for(final Result r: generalResults) {
                    sb.append( r.getMessage(ofo) );
                    sb.append("<br>\n");
		}
		
		return sb.toString();
	}// getGeneralResults()
	
	
	/** 
	*	Get the per-power results. This will display the power name,
	*	in bold, followed by an indented list of results. Non-order
	*	results will come before order results.
	*/
	private String getPerPowerResults()
	{
		// Seperate results into OrderResults and 'regular' Results
		final List<OrderResult> orderResults = new ArrayList<OrderResult>(128);
		final List<Result> otherResults = new ArrayList<Result>(64);
		
		final List<Result> resultList = turnState.getResultList();
		for(final Result r: resultList) {
			if(r.getPower() != null) {
				if(r instanceof OrderResult) {
					orderResults.add((OrderResult) r);
				} else {
					otherResults.add(r);
				}
			}
		}
		
		// Sort the results
		Collections.sort( orderResults );
		Collections.sort( otherResults );
                
		// Print results, by power.
		StringBuffer sb = new StringBuffer(4096);
		for(final Power power: allPowers)
		{
			// SKIP power if eliminated.
			if(!position.isEliminated(power))
			{
				// power name
				sb.append("<div class=\"indent1cm\"><b>");
				sb.append(power);
				sb.append(':');
				sb.append("</b>");
				
				// non-order results
				printNonOrderResultsForPower(sb, power, otherResults);
				
				// order results
				printOrderResultsForPower(sb, power, orderResults);
				
				sb.append("</div>");
			}
		}
		
		return sb.toString();
	}// getPerPowerResults()
	
	
	/** Print non order results for a power. */
	private void printNonOrderResultsForPower(StringBuffer sb, Power power, List<Result> results)
	{
		StringBuffer text = new StringBuffer(1024);
		
		boolean foundAnOtherResult = false;
		Iterator<Result> iter = results.iterator();
		while(iter.hasNext())
		{
			Result result = iter.next();
			if(power.equals(result.getPower()))
			{
				text.append( result.getMessage(ofo) );
				text.append("<br>\n");
				foundAnOtherResult = true;
			}
		}
		
		// if we found any results, append them. 
		// otherwise, we do nothing.
		if(foundAnOtherResult)
		{
			sb.append("<div class=\"indent1cm\">");
			sb.append(text);
			sb.append("</div>");		
		}
	}// printGeneralResultsForPower()
	
	
	/**
	*	Print the order results for a given power. If the order failed,
	*	underline it. Failure reasons are always in italics. If there is only
	*	one failure reason, it is appended to the end of the order. If 
	*	there are multiple failure reasons, they are indented underneath
	*	the order.
	*/
	private void printOrderResultsForPower(StringBuffer sb, Power power, List<OrderResult> results)
	{
		// create a mapping of orders -> a list of results. As we find results, add
		// it to the map.
		final LinkedHashMap<Orderable, List<OrderResult>> ordMap = 
                        new LinkedHashMap<Orderable, List<OrderResult>>(17);
		final ArrayList<OrderResult> substList = new ArrayList<OrderResult>();
		
		for(final OrderResult or: results) {
			Orderable order = or.getOrder();
			
			// only use orders for the given power.
			if(power == or.getPower())
			{
				if(order == null)
				{
					// usually a substituted order; add to substList.
					substList.add(or);
				}
				else
				{
					if(!ordMap.containsKey(order))
					{
						// create the entry
						final List<OrderResult> list = new ArrayList<OrderResult>();
						list.add(or);
						ordMap.put(order, list);
					}
					else
					{
						// add to the list
						final List<OrderResult> list = ordMap.get(order);
						list.add(or);
					}
				}
			}
		}
		
		// iterate through substList, printing results
		// if we have any substituted order results, print
		// a blank line afterwards
		sb.append("<div class=\"indent1cm\">\n");
		
		boolean substOrderFound = false;
		for(final OrderResult or: substList) {
			substOrderFound = true;
			
			if(or.getOrder() != null)
			{
				sb.append( or.getOrder().toFormattedString(ofo) );	// use OrderFormat
			}
			
			sb.append(" <i>");
			sb.append(or.getMessage(ofo));
			sb.append("</i>");
			sb.append("<br>\n");
		}
		
		if(substOrderFound)
		{
			sb.append("<br>\n");
		}
		
		
		// iterate through ordMap, chaining the results, if there are more than one.
		for(final List<OrderResult> orderResultList: ordMap.values()) {
			Orderable order = null;
			boolean hasFailed = false;
			
			// find if we have failed or not
			for(final OrderResult or: orderResultList) {
				ResultType rt = or.getResultType();
				
				order = or.getOrder();
				
				if( rt == ResultType.FAILURE ||
					rt == ResultType.DISLODGED ||
					rt == ResultType.VALIDATION_FAILURE ) {
					hasFailed = true;
					break;
				}
			}
			
			// print the order
			// underline order if failure
			if(hasFailed) {
				sb.append("<u>");
				sb.append(order.toFormattedString(ofo));
				sb.append("</u>");
			} else {
				sb.append(order.toFormattedString(ofo));
			}
			
			// print the messages; they should always be in italics.
			// we always print non-empty messages indented and underneath if there are any.
			// 
			// make a list of non-empty messages. (strings)
			//
			final List<String> nonEmptyList = new ArrayList<String>(orderResultList.size());
			for(final OrderResult or: orderResultList) {
				final String msg = or.getMessage(ofo);
				if(msg.length() > 0) {
					nonEmptyList.add(msg);
				}
			}
			
			if(nonEmptyList.isEmpty())
			{
				sb.append("<br>\n");
			}
			else
			{
				sb.append("<div class=\"indent1cm\" style=\"margin-bottom:3pt;\">");
				
                                
				final java.util.Iterator<String> it = nonEmptyList.iterator();
				while(it.hasNext())
				{
					final String msg = it.next();
					sb.append("<i> ");
					sb.append(msg);
					sb.append(" </i>\n");
					if(it.hasNext())
					{
						sb.append("<br>\n");
					}
				}
				
				sb.append("</div>");
			}
		}
		
		sb.append("</div>");
	}// printOrderResultsForPower()
	
	
}// class ResultWriter

