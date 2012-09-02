//
//  @(#)GUIDefineState.java	12/2002
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
package dip.gui.order;

import dip.misc.Utils;
import dip.order.DefineState;
import dip.order.OrderException;
import dip.order.Orderable;
import dip.world.Location;
import dip.world.Power;
import dip.world.Unit;

/**
*
*	GUIOrder subclass of DefineState order.
*	<p>
*	This is essentially a placeholder. It is incomplete, and should only
*	be used (derived from) existing DefineState orders. No locations may
*	be set via GUIOrder methods, and the order will not be valid if 
*	created without derivation. This may change in future implementations.
*
*/
public class GUIDefineState extends DefineState implements GUIOrder
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Creates a GUIDefineState */
	protected GUIDefineState()
	{
		super();
	}// GUIDefineState()
	
	
	/** Creates a GUIDefineState */
	protected GUIDefineState(Power power, Location source, Unit.Type sourceUnitType)
	throws OrderException
	{
		super(power, source, sourceUnitType);
	}// GUIDefineState()
	
	
	/** This only accepts DefineState orders. All others will throw an IllegalArgumentException. */
	@Override
	public void deriveFrom(Orderable order)
	{
		if( !(order instanceof DefineState) )
		{
			throw new IllegalArgumentException();
		}
		
		DefineState defState = (DefineState) order;
		power = defState.getPower();
		src = defState.getSource();
		srcUnitType = defState.getSourceUnitType();
	}// deriveFrom()
	
	/** Always returns false. */
	@Override
	public boolean testLocation(StateInfo stateInfo, Location location, StringBuffer sb)
	{
		sb.setLength(0);
		sb.append( Utils.getLocalString(GUIOrder.COMPLETE, getFullName()) );
		return false;
	}// testLocation()
	
	/** Always returns false. */
	@Override
	public boolean clearLocations()
	{
		return false;
	}// clearLocations()
	
	/** Always returns false. */
	@Override
	public boolean setLocation(StateInfo stateInfo, Location location, StringBuffer sb)
	{
		sb.setLength(0);
		sb.append( Utils.getLocalString(GUIOrder.COMPLETE, getFullName()) );
		return false;
	}// setLocation()
	
	/** Always returns true. */
	@Override
	public boolean isComplete()
	{
		return true;
	}// isComplete()
	
	/** Always returns 0. */
	@Override
	public int getNumRequiredLocations()		{ return 0; }
	
	/** Always returns 0. */
	@Override
	public int getCurrentLocationNum()			{ return 0; }
	
	
	/** Always throws an IllegalArgumentException */
	@Override
	public void setParam(Parameter param, Object value)	{ throw new IllegalArgumentException(); }
	
	/** Always throws an IllegalArgumentException */
	@Override
	public Object getParam(Parameter param)	{ throw new IllegalArgumentException(); }
	
	
	
	@Override
	public void removeFromDOM(MapInfo mapInfo)
	{
	}// removeFromDOM()
	
	
	@Override
	public void updateDOM(MapInfo mapInfo)
	{
	}// updateDOM()	
	
	@Override
	public boolean isDependent()	{ return false; }
	
	
}// class GUIDefineState
