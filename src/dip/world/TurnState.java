//
//  @(#)TurnState.java		4/2002
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
package dip.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dip.order.Orderable;
import dip.order.result.OrderResult;
import dip.order.result.Result;

/**
*
*	A TurnState represents a snapshot of the game for the given Phase.
*	The essential components of a TurnState are, then:
*	<ul>
*		<li>A Phase object (represents the TurnState in time)
*		<li>A Position object (stores the current unit and powers state)
*		<li>Orders for the units
*		<li>Order Results (if the TurnState has been resolved)
*	</ul>
*	<p>
*	Note that we store Map (Province) adjacency data seperately from
*	the Map (it's in the World object), so Map objects are re-constituted
*	from a list of Provinces and Powers. This occurs behind-the-scenes when 
*	a World (or TurnState) object is deserialized.
*	<p>
*	This object is NOT SYNCHRONIZED and therefore not inherently threadsafe.
*	<p>
*	Also note that when a List of orders is obtained for a power, we do not
*	check that the list contains only orders for that power. (e.g., are
*	non-power orders 'snuck in' for a given power)
*
*/
public class TurnState implements Serializable
{
	// instance variables (we serialize all of this)
	private Phase phase = null;				
	private List<Result> resultList = null; 				// order results, post-adjudication
	private Map<Power, List<Orderable>> orderMap = null;				// Map of power=>orders
	private boolean isSCOwnerChanged = false;		// 'true' if any supply centers changed ownership
	private	Position position = null;				// Position data (majority of game state)
	private transient World world = null;				// makes it easier when we just pass a turnstate
	private boolean isEnded = false;				// true if game over (won, draw, etc.)
	private boolean isResolved = false;				// true if phase has been adjudicated
	private transient HashMap<Orderable, Boolean> resultMap = null;		// transient result map
	
	
	/** Creates a TurnState object. */
	protected TurnState()
	{
	}// TurnState()
	
	
	/** Creates a TurnState object. */
	public TurnState(Phase phase)
	{
		if(phase == null)
		{
			throw new IllegalArgumentException("null phase");
		}
		
		this.phase = phase;
		this.resultList = new ArrayList<Result>(80);
		this.orderMap = new HashMap<Power, List<Orderable>>();
	}// TurnState()
	
	/** 
	*	Set the World object associated with this TurnState. 
	*	A <code>null</code> World is not permitted.
	*/
	public void setWorld(World world)
	{
		if(world == null)
		{
			throw new IllegalArgumentException();
		}
		
		this.world = world;
	}// setWorld()
	
	
	/** Gets the World object associated with this TurnState. Never should be null. */
	public World getWorld()
	{
		return world;
	}// getWorld()
	
	/** Returns the current Phase */
	public Phase getPhase()
	{
		return phase;
	}// getTurnInfo()
	
	
	/** This should be used with the utmost care. Null Phases are not allowed. */
	public void setPhase(Phase phase)
	{
		if(phase == null)
		{
			throw new IllegalArgumentException("null phase");
		}
		this.phase = phase;
	}// setPhase()
	
	
	/** Gets the Position data for this TurnState */
	public Position getPosition()
	{
		return position;
	}// getPosition()
	
	
	/** Sets the Position data for this TurnState */
	public void setPosition(Position position)
	{
		if(position == null)
		{
			throw new IllegalArgumentException();
		}
		
		this.position = position;
	}// setPosition()
	
	
	/** Returns the result list */
	public List<Result> getResultList()
	{
		return resultList;
	}// getResultList()
	
	
	/** Sets the Result list, erasing any previously existing result list. */
	public void setResultList(final List<Result> list)
	{
		if(list == null)
		{
			throw new IllegalArgumentException("null result list");
		}
		
		resultList = list;
	}// setResultList()
	
	
	
	
	/** 
	*	A flag indicating if, after adjudication, any supply centers
	*	have changed ownership.	
	*/
	public boolean getSCOwnerChanged()
	{
		return isSCOwnerChanged;
	}// getSCOwnerChanged()
	
	
	/** 
	*	Sets the flag indicating if, after adjudication, any supply centers
	*	have changed ownership.	
	*/
	public void setSCOwnerChanged(boolean value)
	{
		isSCOwnerChanged = value;
	}// setSCOwnerChanged()
	
	
	
	/**
	*	Returns a List of orders for all powers.
	*	<p>
	*	Manipulations to this list will not be reflected in the TurnState object.
	*/
	public List<Orderable> getAllOrders()
	{
		final List<Orderable> list = new ArrayList<Orderable>();
		
                for(final Entry<Power, List<Orderable>> entry: orderMap.entrySet()) {
                    final List<Orderable> orderableList = entry.getValue();
                    for(final Orderable orderable: orderableList) {
                        list.add(orderable);
                    }
                }
                
		
		return list;
	}// getOrderList()
	
	
	/**
	*	Clear all Orders, for all Powers.
	*/
	public void clearAllOrders()
	{
		orderMap.clear();
	}// clearAllOrders()
	
	
	/**
	*	Returns the List of orders for a given Power. 
	*	<p>
	*	Note that modifications to the returned order List will be reflected
	*	in the TurnState.
	*/
	public List<Orderable> getOrders(final Power power)
	{
		if(power == null)
		{
			throw new IllegalArgumentException("null power");
		}
		
		List<Orderable> orderList = orderMap.get(power);
		if(orderList == null)
		{
			orderList = new ArrayList<Orderable>();
			orderMap.put(power, orderList);
		}
		
		return orderList;
	}// getOrders()
	
	/** Sets the orders for the given Power, deleting any existing orders for the power */
	public void setOrders(final Power power, final List<Orderable> list)
	{
		if(power == null || list == null)
		{
			throw new IllegalArgumentException("power or list null");
		}
		
		orderMap.put(power, list);
	}// setOrders()
	
	/** Set if game has ended for any reason */
	public void setEnded(boolean value)		{ isEnded = value; }
	
	/** Returns <code>true</code> if game has ended */
	public boolean isEnded()				{ return isEnded; }
		
	/** Set if the turn has been adjudicated. */
	public void setResolved(boolean value)		{ isResolved = value; }
	
	/** Returns the turn has been adjudicated */
	public boolean isResolved()					{ return isResolved; }
	
	/**
	*	Returns if an order has failed, based on results. Note that
	*	this only applies once the turnstate has been resolved. If 
	*	the TurnState is not resolved, this will always return true.
	*/
	public boolean isOrderSuccessful(Orderable o)
	{
            if(!isResolved)
            {
                    return true;
            }

            if(resultMap == null)
            {
                resultMap = new HashMap<Orderable, Boolean>();

                for(final Result result: getResultList()) {
                    if (result instanceof OrderResult) {
                        final OrderResult orderResult = (OrderResult) result;
                        if (orderResult.getResultType() == OrderResult.ResultType.SUCCESS) {
                            resultMap.put(orderResult.getOrder(), Boolean.TRUE);
                        }
                    }
                }    
            }

            if(resultMap.get(o) == Boolean.TRUE)
            {
                    return true;
            }

            return false;
	}// isFailedOrder()
	
}// class TurnState
