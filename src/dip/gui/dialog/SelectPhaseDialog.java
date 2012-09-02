//
//  @(#)SelectPhaseDialog.java	1.00	4/1/2002
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

package dip.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import dip.gui.ClientFrame;
import dip.gui.swing.XJScrollPane;
import dip.misc.Utils;
import dip.world.Phase;

/**
*
*	Shows a list of Phases, for a given world, so that the user
*	may select a Phase.
*
*
*
*/
public class SelectPhaseDialog extends HeaderDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// i18n constants
	private static final String TITLE = "SPD.title";
	private static final String HEADER_LOCATION = "SPD.location.header";
	
	// instance variables
	private ClientFrame		clientFrame;
	private JScrollPane		phaseScrollPane = null;
	private JList 			list = null;
	
	
	/**
	*	Displays the Phases for the current World object.
	*	<p>
	*	Returns the Phase selected, or <code>null</code> if no 
	* 	Phase was selected, or dialog was cancelled.
	*/
	public static Phase displayDialog(ClientFrame cf)
	{
		SelectPhaseDialog spd = new SelectPhaseDialog(cf);
		spd.pack();		
		spd.setSize(new Dimension(500, 450));
		Utils.centerInScreen(spd);
		spd.setVisible(true);		
		return spd.getSelectedPhase();
	}// displayDialog()
	
	
	private SelectPhaseDialog(ClientFrame clientFrame)
	{
		super(clientFrame, Utils.getLocalString(TITLE), true);
		this.clientFrame = clientFrame;
		
		makePhaseList();
		
		setHeaderText( Utils.getText(Utils.getLocalString(HEADER_LOCATION)) );
		
		// if we don't put the scroller in a JPanel, the scroller's border
		// isn't drawn.
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(phaseScrollPane, BorderLayout.CENTER);
		createDefaultContentBorder(contentPanel);
		setContentPane(contentPanel);
		
		addTwoButtons( makeCancelButton(), makeOKButton(), false, true );
		setHelpID(dip.misc.Help.HelpID.Dialog_PhaseSelect);
	}// SelectPhaseDialog()
	
	
	private Phase getSelectedPhase()
	{
		if(getReturnedActionCommand().equals(ACTION_OK))
		{
			ListRow lr = (ListRow) list.getSelectedValue();
			if(lr != null)
			{
				return lr.getPhase();
			}
		}
		
		return null;
	}// getSelectedPhase()
	
	
	private void makePhaseList()
	{
                
		// create ListRows
		final List<ListRow> lrList = new LinkedList<ListRow>();
		final Set<Phase> phaseSet = clientFrame.getWorld().getPhaseSet();
		int idx = 1;
		for (final Phase phase: phaseSet) {
                    lrList.add(new ListRow(phase, idx++));
                }
                
		
		// create & populate JList
		list = new JList(lrList.toArray(new ListRow[lrList.size()]));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		phaseScrollPane = new XJScrollPane(list);
	}// makePhaseList()
	
	
	private class ListRow
	{
		private final Phase phase;
		private final int num;
		
		public ListRow(Phase phase, int n)
		{
			this.phase = phase;
			this.num = n;
		}// ListRow()
		
		public Phase getPhase()
		{
			return phase;
		}// getPhase()
		
		@Override
		public String toString()
		{
			StringBuffer sb = new StringBuffer(64);
			sb.append(String.valueOf(num));
			sb.append(".  ");
			sb.append(phase);
			return sb.toString();
		}// toString()
		
	}// inner class ListRow
	
	
}// class SelectPhaseDialog
