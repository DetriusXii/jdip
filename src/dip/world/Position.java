//
//  @(#)Position.java		2/2003
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

import java.util.HashMap;
import java.util.Map;

/**
*
*	Stores all the mutable (state) information for a given TurnState.
*	Immutable data is retained in the Power/Province/etc. objects; only
*	mutable data is stored here.
*	<p>
*	This object can be cloned, and should be cloned, when creating a new 
*	Position based upon previous Position data. Several clone methods are
*	available, each optimized for speed and cloning requirements.
*	<p>
*	WARNING: this code is not MT (Multithread) safe!
*	<p>
*	This class is heavily optimized, as adjudicator performance is highly dependent
*	upon the performance of this class.
*	<p>
*	The clone() methods are not strictly implemented; they call a constructor
*	to assist in cloning rather than call super.clone(). This is done for 
*	performance reasons.
*/
public class Position implements java.io.Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// size constants; these should be prime
	private static final int POWER_SIZE = 17;
	
	// instance variables
	protected final Map<Power, PowerData> powerMap = new HashMap<Power, PowerData>(POWER_SIZE);
	protected final ProvinceData[] provArray;
	protected final dip.world.Map map;
	private transient Province[] tmpProvArray = null;
	
	
	public Position(dip.world.Map map)
	{
		this.map = map;
		this.provArray = new ProvinceData[map.getProvinces().length];
	}// Position()
	
	
	
	
	/** The Number of Provinces in this Position */
	public final int size()
	{
		return provArray.length;
	}// size()
	
	
	/** Convenience method: Returns an array of Provinces */
	public final Province[] getProvinces()
	{
		return map.getProvinces();
	}// getProvinces()
	
	
	/** Returns true if this Power has been eliminated. False by default. */
	public boolean isEliminated(Power power)
	{
		final PowerData pd = powerMap.get(power);
		if(pd != null) {
			return pd.isEliminated();
		}
		return false;
	}// isEliminated()
	
	
	/** Set whether this Power has been eliminated. */
	public void setEliminated(Power power, boolean value)
	{
		final PowerData pd = getPowerData(power);
		pd.setEliminated(value);
	}// setEliminated()
	
	
	/** 
	*	Scans the Position; sets/unsets elimination depending upon if a given
	* 	Power has any units (including dislodged units) or supply centers on the map 
	*/
	public void setEliminationStatus(final Power[] powers)
	{
		final HashMap<Power, Boolean> pmap = new HashMap<Power, Boolean>(19);
		for(final Power power: powers) {
			pmap.put(power, Boolean.TRUE);
		}
		
		for(final ProvinceData pd: provArray)
		{
			Power power = null;
			
			if(pd != null)
			{
				Unit unit = pd.getUnit();
				if(unit != null)		// first check non-dislodged units
				{
					power = unit.getPower();
					if(pmap.get(power))
					{
						pmap.put(power, Boolean.FALSE);
					}
				}
				
				// then see if there's a dislodged unit
				unit = pd.getDislodgedUnit();
				if(unit != null)
				{
					power = unit.getPower();
					if(pmap.get(power))
					{
						pmap.put(power, Boolean.FALSE);
					}
				}
				
				// finally, see if we own a supply center
				power = pd.getSCOwner();
				if(power != null)
				{
					if(pmap.get(power))
					{
						pmap.put(power, Boolean.FALSE);
					}
				}
			}
		}
		
		for(final Power power: powers) {
			setEliminated(power, pmap.get(power));
		}		
	}// setEliminationStatus()
	
	
	/** Set the owner of the supply center. */
	public void setSupplyCenterOwner(Province province, Power power)	 		
	{
		ProvinceData pd = getProvinceData(province);
		pd.setSCOwner(power);
	}// setSupplyCenterOwner()
	
	
	/** Set the owner of a home supply center. */
	public void setSupplyCenterHomePower(Province province, Power power)	 		
	{
		ProvinceData pd = getProvinceData(province);
		pd.setSCHomePower(power);
	}// setSupplyCenterHomePower()
	
	
	/** Determine if this Province contains a supply center */
	public boolean hasSupplyCenterOwner(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.isSCOwned();
		}
		return false;
	}// hasSupplyCenterOwner()
	
	
	/** Determine if this Province contains a Home supply center */
	public boolean isSupplyCenterAHome(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.isSCAHome();
		}
		return false;
	}// isSupplyCenterAHome()
	
	
	/** Get the home power of the supply center; null if no supply center or home power */
	public Power getSupplyCenterHomePower(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.getSCHomePower();
		}
		return null;
	}// getSupplyCenterHomePower()
	
	
	/** Get the owner of the supply center; null if no owner or no supply center. */
	public Power getSupplyCenterOwner(Province province)	
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.getSCOwner();
		}
		return null;
	}// getSupplyCenterOwner()
	
	
	
	
	// non-dislodged unit	
	/** Set the unit contained in this province; null to eliminate an existing unit. */
	public void setUnit(Province province, Unit unit)
	{
		ProvinceData pd = getProvinceData(province);
		pd.setUnit(unit);
	}// setUnit()
	
	/** Determines if there is a unit present in this province. */
	public boolean hasUnit(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.hasUnit();
		}
		return false;
	}// hasUnit()
	
	/** Get the unit contained in this Province. Returns null if no unit exists. */
	public Unit getUnit(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.getUnit();
		}
		return null;
	}// getUnit()
	
	
	/** Test if the given type of unit is contained in this Province. */
	public boolean hasUnit(Province province, Unit.Type unitType)
	{
		Unit unit = getUnit(province);
		if(unit != null)
		{
			return(unit.getType().equals(unitType));
		}
		return false;
	}// hasUnit()
	
	/** Test if the given type of unit is contained in this Province. */
	public boolean hasDislodgedUnit(Province province, Unit.Type unitType)
	{
		Unit unit = getDislodgedUnit(province);
		if(unit != null)
		{
			return(unit.getType().equals(unitType));
		}
		return false;
	}// hasDislodgedUnit()
	
	
	
	
	// dislodged unit	
	/** Set the dislodged unit contained in this province; null to eliminate an existing unit. */
	public void setDislodgedUnit(Province province, Unit unit)
	{
		ProvinceData pd = getProvinceData(province);
		pd.setDislodgedUnit(unit);
	}// setDislodgedUnit()
	
	
	/** Get the dislodged unit in this Province. Returns null if no dislodged unit exists. */
	public Unit getDislodgedUnit(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.getDislodgedUnit();
		}
		return null;
	}// getDislodgedUnit()
	
	
	// last occupier
	/** 
	*	Sets the Power that last occupied a given space. Note that this
	*	is not intended to be used for Supply Center ownership (which only
	*	changes in the Fall season); use setSupplyCenterOwner() instead.
	*/
	public void setLastOccupier(Province province, Power power)
	{
		ProvinceData pd = getProvinceData(province);
		pd.setLastOccupier(power);
	}// setLastOccupier()
	
	
	/** 
	*	Returns the Power that last occupied a given space. Note that this
	*	is not intended to be used for Supply Center ownership (which only
	*	changes in the Fall season); use getSupplyCenterOwner() instead.
	*/
	public Power getLastOccupier(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.getLastOccupier();
		}
		return null;
	}// getLastOccupier()	
	
	
	/** Determines if there is a dislodged unit present in this province. */
	public boolean hasDislodgedUnit(Province province)
	{
		ProvinceData pd = provArray[province.getIndex()];
		if(pd != null)
		{
			return pd.hasDislodgedUnit();
		}
		return false;
	}// hasDislodgedUnit()
	
	
	
	
	/** Returns an array of provinces with non-dislodged units */
	public Province[] getUnitProvinces()
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.hasUnit())
			{
				tmpProvArray[arrSize] = map.reverseIndex(i);
				arrSize++;
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getUnitProvinces()
	
	
	
	/** Returns an array of provinces with dislodged units */
	public Province[] getDislodgedUnitProvinces()
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.hasDislodgedUnit())
			{
				tmpProvArray[arrSize] = map.reverseIndex(i);
				arrSize++;
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getDislodgedUnitProvinces()
	
	
	/** Returns the number of provinces with non-dislodged units */
	public int getUnitCount()
	{
		int count = 0;		
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.hasUnit())
			{
				count++;
			}
		}
		
		return count;
	}// getUnitCount()
	
	
	/** Returns the number of provinces with dislodged units */
	public int getDislodgedUnitCount()
	{
		int count = 0;		
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.hasDislodgedUnit())
			{
				count++;
			}
		}
		
		return count;
	}// getDislodgedUnitCount()
	
	
	/** Returns an array of provinces with home supply centers */
	public Province[] getHomeSupplyCenters()
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.isSCAHome())
			{
				tmpProvArray[arrSize] = map.reverseIndex(i);
				arrSize++;
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getHomeSupplyCenters()
	
	
	/** Returns an Array of the Home Supply Centers for a given power (whether or not they are owned by that power) */
	public Province[] getHomeSupplyCenters(Power power)
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.getSCHomePower() == power)
			{
				tmpProvArray[arrSize] = map.reverseIndex(i);
				arrSize++;
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getHomeSupplyCenters()
	
	
	/** 
	*	Determines if a Power has at least one owned Home Supply Center. 
	*	<p>
	*	An owned home supply center need not have a unit present.
	*/
	public boolean hasAnOwnedHomeSC(Power power)
	{
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.getSCHomePower() == power && pd.getSCOwner() == power)
			{
				return true;
			}
		}
		
		return false;
	}// hasAnOwnedHomeSC()
	
	
	/** Returns an Array of the owned Supply Centers for a given Power (whether or not they are home supply centers) */
	public Province[] getOwnedSupplyCenters(Power power)
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.getSCOwner() == power)
			{
				tmpProvArray[arrSize] = map.reverseIndex(i);
				arrSize++;
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getOwnedSupplyCenters()
	
	
	/** Returns an array of provinces with owned supply centers */
	public Province[] getOwnedSupplyCenters()
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null && pd.isSCOwned())
			{
				tmpProvArray[arrSize] = map.reverseIndex(i);
				arrSize++;
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getOwnedSupplyCenters()
	
	
	
	/** 
	*	Deep clone of the contents of this Position. 
	*/
	@Override
	public Object clone()
	{
		Position pos = new Position(map);
		
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			
			if(pd != null)
			{
				pos.provArray[i] = pd.normClone();
			}
		}
		
                for(final Power power: powerMap.keySet()) {
                    final PowerData pd = powerMap.get(power);
                    pos.powerMap.put(power, pd.normClone());
                }
		
		return pos;
	}// clone()
	
	/** 
	*	Deep clone of everything *except* dislodged & non-dislodged units;
	*	(e.g., SC ownership, Power Info, etc.)
	*/
	public Position cloneExceptUnits()
	{
		Position pos = new Position(map);
		
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			
			if(pd != null)
			{
				pos.provArray[i] = pd.cloneExceptUnits();
			}
		}
		
		for(final Power key: powerMap.keySet()) {
			PowerData pd = powerMap.get(key);
			pos.powerMap.put(key, pd.normClone());
		}
		
		return pos;
	}// cloneExceptUnits()
	
	
	/** Deep clone of everything <b>except</b> dislodged units. */
	public Position cloneExceptDislodged()
	{
		final Position pos = new Position(map);
		
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null)
			{
				pos.provArray[i] = pd.cloneExceptDislodged();
			}
		}
		
		for (final Power key: powerMap.keySet()) {
			final PowerData pd = powerMap.get(key);
			pos.powerMap.put(key, pd.normClone());
		}
		
		return pos;
	}// cloneExceptDislodged()
	
	
	/**
	*	Gets all the Provinces with non-dislodged 
	*	Units for a particular power.
	*
	*/
	public Province[] getUnitProvinces(Power power)
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null)
			{
				Unit unit = pd.getUnit();
				if(unit != null && unit.getPower() == power)
				{
					tmpProvArray[arrSize] = map.reverseIndex(i);
					arrSize++;
				}
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getUnitProvinces()
	
	
	/**
	*	Gets all the Provinces with dislodged 
	*	Units for a particular power.
	*/
	public Province[] getDislodgedUnitProvinces(Power power)
	{
		makeTmpProvArray();
		
		int arrSize = 0;
		for(int i=0; i<provArray.length; i++)
		{
			ProvinceData pd = provArray[i];
			if(pd != null)
			{
				Unit unit = pd.getDislodgedUnit();
				if(unit != null && unit.getPower() == power)
				{
					tmpProvArray[arrSize] = map.reverseIndex(i);
					arrSize++;
				}
			}
		}
		
		Province[] p = new Province[arrSize];
		System.arraycopy(tmpProvArray, 0, p, 0, arrSize);
		return p;
	}// getDislodgedUnitProvinces()
	
	
	
	
	/**
	*	Call this method FIRST before any set(); thus if ProvinceData
	*	does not exist, we will add one to the map.
	*
	*/
	private ProvinceData getProvinceData(Province province)
	{
		int idx = province.getIndex();
		ProvinceData pd = provArray[idx];
		if(pd == null)
		{
			pd = new ProvinceData();
			provArray[idx] = pd;
		}
		
		return pd;
	}// getProvinceData()
	
	/** Same type of functionality as getProvinceData() but for PowerData objects */
	private PowerData getPowerData(Power power) {
		final PowerData pd = powerMap.get(power);
                final PowerData createdPD = pd != null ? pd: new PowerData();
		if(pd == null) {
                    powerMap.put(power, createdPD);
                }
                
		return createdPD;
	}// getPowerData()
	
	
	/** All mutable Province data is kept here */
	private class ProvinceData implements java.io.Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// instance variables
		private Unit 	unit = null;
		private Unit 	dislodgedUnit = null;
		private Power 	SCOwner = null;
		private Power	SCHomePower = null;
		private Power	lastOccupier = null;
		
		// unit set/get
		public boolean hasUnit() 				{ return (unit != null); }
		public boolean hasDislodgedUnit()		{ return (dislodgedUnit != null); }
		
		public Unit getUnit()					{ return unit; }
		public Unit getDislodgedUnit()			{ return dislodgedUnit; }
		
		public void setUnit(Unit u)  			{ unit = u; }
		public void setDislodgedUnit(Unit u) 	{ dislodgedUnit = u; }
		
		
		// SC set/get
		public boolean isSCAHome() 			{ return (SCHomePower != null); }
		public boolean isSCOwned()			{ return (SCOwner != null); }
		
		public void setSCOwner(Power p)		{ SCOwner = p; }
		public void setSCHomePower(Power p)	{ SCHomePower = p; }
		
		public Power getSCOwner()			{ return SCOwner; }
		public Power getSCHomePower()		{ return SCHomePower; }
		
		// occupier set/get
		public Power getLastOccupier()			{ return lastOccupier; }
		public void setLastOccupier(Power p)	{ lastOccupier = p; }
		
		
		// normal clone
		public ProvinceData normClone()
		{
			ProvinceData pd = new ProvinceData();
			
			// deep copy unit information
			if(unit != null)
			{
				pd.unit = (Unit) unit.clone();
			}
			
			if(dislodgedUnit != null)
			{
				pd.dislodgedUnit = (Unit) dislodgedUnit.clone();
			}
			
			// shallow copy Powers [Power is immutable]
			pd.SCOwner = this.SCOwner;
			pd.SCHomePower = this.SCHomePower;
			pd.lastOccupier = this.lastOccupier;
			
			return pd;
		}// normClone()
		
		
		/** Returns null if no non-ownership information exists */
		public ProvinceData cloneExceptUnits()
		{
			// don't create an object if there is no ownership info.
			// this also compacts the Position map!
			if(SCOwner == null && SCHomePower == null && lastOccupier == null)
			{
				return null;
			}
			
			// create a ProvinceData object
			ProvinceData pd = new ProvinceData();
			
			// shallow copy Power [Power is immutable]
			pd.SCOwner = this.SCOwner;
			pd.SCHomePower = this.SCHomePower;
			pd.lastOccupier = this.lastOccupier;
			
			return pd;
		}// cloneExceptUnits()
		
		/** Returns null if no ownership info/unit exists */
		public ProvinceData cloneExceptDislodged()
		{
			// don't create an object if there is no ownership info.
			// this also compacts the Position map!
			if(SCOwner == null && SCHomePower == null && unit == null  && lastOccupier == null)
			{
				return null;
			}
			
			// create a ProvinceData object
			ProvinceData pd = new ProvinceData();
			
			// shallow copy Power [Power is immutable]
			pd.SCOwner = this.SCOwner;
			pd.SCHomePower = this.SCHomePower;
			pd.lastOccupier = this.lastOccupier;
			
			// deep copy unit
			if(unit != null)
			{
				pd.unit = (Unit) unit.clone();
			}
			
			return pd;
		}// cloneExceptUnits()
		
		
	}// inner class ProvinceData
	
	
	/** All mutable Power data is kept here */
	private class PowerData implements java.io.Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// instance variables
		private boolean isEliminated = false;
		
		
		public PowerData()
		{
		}// PowerData()
		
		public boolean isEliminated() 					{ return isEliminated; }
		public void setEliminated(boolean value)		{ isEliminated = value; }
		
		public PowerData normClone()
		{
			PowerData pd = new PowerData();
			pd.isEliminated = this.isEliminated;
			return pd;
		}// normClone()
	}// inner class PowerData
	
	
	/**
	*	Copying references into a large temporary array, then creating an array
	*	and copying the temp array into the correctly-sized array via 
	*	System.arraycopy() is about twice as fast as using an ArrayList, and 
	*	almost twice as fast as double-iterating (one to gauge the array size,
	*	and one to copy data).
	*	<p>
	*	This method creates a sufficiently large array to hold temporary data.
	*/
	private final void makeTmpProvArray()
	{
		if(tmpProvArray == null)
		{
			tmpProvArray = new Province[provArray.length];
		}
	}// makeTmpProvArray()
}// class Position

