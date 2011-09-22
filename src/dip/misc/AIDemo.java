//
//  @(#)AIDemo.java		12/2003
//
//  Copyright 2003 Zachary DelProposto.
//	
//  This module (AIDemo.java) is example code; all or part of this code
//	may be freely used in your own programs without attribution and without
//	restriction. Note that that is not the case for most other jDip source 
//	and binary modules; see their licensing information for details.
//
package dip.misc;

import dip.order.*;								// orders
import dip.order.result.*;						// results of orders
import dip.world.*;								// 'main' things (Units, Provinces, etc.)
import dip.world.variant.VariantManager;		// Loads variants (maps, etc.)
import dip.world.variant.data.*;				// variant data
import dip.process.*;							// adjudication

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.*;

/**
*	This class is meant to be an illustrative example of how to use
*	the adjudication routines to evaluate moves, as might be needed
*	by an AI (computer) player or the like.
*	<p>
*	It is to be run from the command line. Most exceptions will cause
*	an abort, and be displayed on the command line.
*	<p>
*	Normally, jDip will create a World object, which has an initial TurnState.
*	Orders and units are added to the TurnState. When the TurnState is resolved,
*	a new TurnState is added to the world. 
*	<p>
*	In our case, we will be doing things a bit differently. We are not trying to
*	create an entire game (though we could!).
*	<p>
*	This demo will set up a position involving all 7 powers on a Standard map
*	(this position is NOT the initial starting position). The goal is to find 
*	the set of orders which will allow Germany to take the Russian province
*	of Warsaw, which contains a supply center. We use the adjudicator to 
*	evaluate several order sets until we find the order set that will accomplish
*	our goal. A secondary goal is to ensure any German units don't get dislodged
*	by Russia.
*	<p>
*	Note that this is not an AI, and just a demo. As such, the other powers
*	are all assumed to have their units Hold in position. Furthermore, the order
*	sets are not generated by an algorithm, but pre-selected.
*	<p>
*	N.B.: note the potential naming conflict between dip.world.Map and java.util.Map.
*/
public class AIDemo
{
	// constants
	/** Directory name where variants are stored */
	private static final String VARIANT_DIR	= "variants";
	private static final String VARIANT_NAME = "Standard";
	
	
	
	/** Command-line entry point */
	public static void main(String[] args)
	throws Exception	// allow any exceptions through
	{
		new AIDemo();
	}// main()
	
	
	/** Create the AIDemo */
	public AIDemo()
	throws Exception
	{
		// create the World
		World world = createStandardWorld();
		
		// create a position to evaluate
		Position position = createPosition(world);
		
		// create order sets
		createOrders(world.getMap(), position);
		
		// evaluate order sets
		
		
	}// AIDemo()
	
	
	
	
	
	/**
	*	Create the World. The World, and its components, are required for
	*	most operations by jDip.
	*/
	private World createStandardWorld()
	throws Exception
	{
		// Get default variant directory, where the jDip variants are stored.
		// This assumes the standard jdip package layout.
		// 
		final File defaultVariantSearchDir = System.getProperty("user.dir") == null ?
                    new File(".", VARIANT_DIR) : new File(System.getProperty("user.dir"), VARIANT_DIR );

		System.out.println("Variant diretory: "+defaultVariantSearchDir);
		
		// Parse the variants in the variant directory/directories.
		// if no variants are found, this will display (using a Swing dialog)
		// an error message. The 'false' sets if we are using XML validation;
		// we generally do not want to.
		//
		VariantManager.init(new ArrayList<File>(){{add(defaultVariantSearchDir);}}, false);
		System.out.println("VariantManager initilization complete.");
		
		// Load the variant (VARIANT_NAME) that we want.
		// Throw an error if it isn't found!
		//
		Variant variant = VariantManager.getVariant(VARIANT_NAME, VariantManager.VERSION_NEWEST);
		if(variant == null)
		{
			throw new IOException("Cannot find variant "+VARIANT_NAME);
		}
		System.out.println("Variant "+VARIANT_NAME+" found.");
		
		// Create the World object. The World object contains the a dip.world.Map,
		// which contains Province and Power information, as well as TurnStates,
		// which hold turn and Position information.
		//
		World newWorld = WorldFactory.getInstance().createWorld(variant);
		System.out.println("World created!");
		
		// Set the RuleOptions in the World. This sets the RuleOptions to their
		// defaults, which is usually what we want.
		//
		newWorld.setRuleOptions(RuleOptions.createFromVariant(variant));
		
		// This is to illustrate some features of the dip.world.Map object.
		// 
		Map map = newWorld.getMap();
		Power[] powers = map.getPowers();
		System.out.println("\nPowers in this game:");
		for(int i=0; i<powers.length; i++)
		{
			System.out.println("  "+powers[i]);
		}
		
		// how do we get a specific power?
		// 
		Power aPower = map.getPower("france");
		System.out.println("\ngetPower(): "+aPower);
		System.out.println("People from "+aPower+" are called "+aPower.getAdjective());
		
		// what about a province?
		//
		Province prov = map.getProvince("spa");
		System.out.println("\nProvince testing:");
		System.out.println("  prov full name: "+prov.getFullName());
		System.out.println("  prov abbreviation: "+prov.getShortName());
		System.out.println("  all TOUCHING provinces:");
		Location[] touchLocs = prov.getAdjacentLocations(Coast.TOUCHING);
		for(int i=0; i<touchLocs.length; i++)
		{
			System.out.println("    "+touchLocs[i].getProvince());
		}
		
		
		// what about a Location? (A Location is a Province + a Coast)
		//
		Location loc1 = map.parseLocation("spa/sc");	// South Coast of Spain
		Location loc2 = map.parseLocation("spa/nc");	// North Coast of Spain
		System.out.println("\nLocation testing:");
		System.out.println("  "+loc1.toLongString()+" and "+loc2.toLongString());
		System.out.println("  same location?: "+loc1.equals(loc2));
		System.out.println("  same province?: "+loc1.isProvinceEqual(loc2));
		System.out.println("  adjacent? "+loc1.isAdjacent(loc2));
		System.out.println("  adjacent Locations to "+loc1.toString()+":");
		Location[] adjLocs = loc1.getProvince().getAdjacentLocations(loc1.getCoast());
		for(int i=0; i<adjLocs.length; i++)
		{
			System.out.println("    "+adjLocs[i]);
		}
		
		// a test: the province "spa" and the location "spa/sc" as well as 
		// "spa/nc" all share the EXACT SAME Province reference. This is not true
		// of Locations, however (why? because to do that, Locations would have 
		// to be interned like String objects can be).
		//
		// It is always safe to use equals() if you are not sure.
		//
		System.out.println("\nEqualities:");
		System.out.println("   loc1 and prov: same province? "+loc1.isProvinceEqual(prov));
		System.out.println("   loc2 and prov: same province? "+(loc2.getProvince() == prov));	// referential equality!
		
		return newWorld;
	}// createStandardWorld()
	
	
	/**
	*	Set up some positions (via a TurnState and a Position object) so that
	*	we can evaluate orders. This uses the Position within the very first 
	*	TurnState object in the World. In this case, the very first TurnState
	*	object in the Standard variant has:
	*	<ul>
	*		<li>All home supply centers have a unit</li>
	*		<li>Home supply centers are set with their home power</li>
	*		<li>No powers have been eliminated</li>
	*	</ul>
	*/
	private Position createPosition(World w)
	{
		// get the initial position
		Position pos = w.getLastTurnState().getPosition();
		
		// a Map reference, for convenience
		final Map map = w.getMap();
		
		// All Power and Province are immutable references. Thus they must
		// be obtained from the Map object.
		//
		// add some extra units
		// set extra German units
		//
		Power germany = map.getPower("germany");
		Unit u = new Unit(germany, Unit.Type.ARMY);
		u.setCoast(Coast.LAND);		// Army units always must be in Coast.LAND (== Coast.NONE)
		pos.setUnit(map.getProvince("pru"), u);
		
		// NOTE: it would be VERY BAD to use the same unit we created above, and also
		// insert it in another province. Why? Because when a one province has a unit
		// moved or destroyed, the other province would have the same. So don't do that.
		//
		u = new Unit(germany, Unit.Type.ARMY);
		u.setCoast(Coast.LAND);
		pos.setUnit(map.getProvince("sil"), u);
		
		u = new Unit(germany, Unit.Type.ARMY);
		u.setCoast(Coast.LAND);
		pos.setUnit(map.getProvince("gal"), u);
		
		// set extra Russian units
		//
		Power russia = map.getPower("russia");
		u = new Unit(russia, Unit.Type.ARMY);
		u.setCoast(Coast.LAND);
		pos.setUnit(map.getProvince("lvn"), u);
		
		System.out.println("\nInitial position created.");
		return pos;
	}// createPosition()
	
	
	/**
	*	Given the position that we created, make several different sets of
	*	orders that we can check to see which is best.
	*	<p>
	*	All non-russian, non-german units will not be given orders (they will
	*	Hold by default).
	*	<p>
	*	We return an Array of Lists (a somewhat unusual construct...)
	*/
	private List[] createOrders(Map map, Position pos)
	{
		// get the OrderFactory. The default order factory is OrderFactory.getDefault().
		OrderFactory orderFactory = OrderFactory.getDefault();
		
		// power constants (note: we could have set these globally, as they
		// are the same, referentially, as when we obtained them in 
		// createPosition()
		//
		final Power russia = map.getPower("russia");
		final Power germany = map.getPower("germany");
		
		// Russian Orders
		// ==============
		// 	A war S lvn-pru
		// 	A lvn-pru
		//	A mos-lvn
		List russianOrders = new ArrayList();
		russianOrders.add(orderFactory.createSupport(russia, 
						  makeLocation(pos, map.getProvince("war")),
						  Unit.Type.ARMY,
						  makeLocation(pos, map.getProvince("lvn")),
						  russia,
						  Unit.Type.ARMY,
						  makeLocation(pos, map.getProvince("pru"))
						  ));
		russianOrders.add(orderFactory.createMove(russia,
						  makeLocation(pos, map.getProvince("lvn")),
						  Unit.Type.ARMY,
						  makeLocation(pos, map.getProvince("pru"))
						  ));
		russianOrders.add(orderFactory.createMove(russia,
						  makeLocation(pos, map.getProvince("mos")),
						  Unit.Type.ARMY,
						  makeLocation(pos, map.getProvince("lvn"))
						  ));
		
		
		// we're just making 2 sets of german orders
		//
		List[] germanOrders = new List[2];
		
		// German Orders: 1
		// ================
		// 	A pru-war
		// 	A sil S A pru-war
		//	A gal S A pru-war
		germanOrders[0] = new ArrayList();
		germanOrders[0].add(orderFactory.createMove(
							germany,
						  	makeLocation(pos, map.getProvince("pru")),
							Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("war"))
						  	));
		germanOrders[0].add(orderFactory.createSupport(
							germany, 
						  	makeLocation(pos, map.getProvince("sil")),
						  	Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("pru")),
							germany,
							Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("war"))
						  	));
		germanOrders[0].add(orderFactory.createSupport(
							germany, 
						  	makeLocation(pos, map.getProvince("gal")),
						  	Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("pru")),
							germany,
							Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("war"))
						  	));
						  
		
		// German Orders: 2
		// ================
		// 	A pru S A sil-war
		// 	A sil-war
		//	A gal S A sil-war
		germanOrders[1] = new ArrayList();
		germanOrders[1].add(orderFactory.createSupport(
							germany, 
						  	makeLocation(pos, map.getProvince("pru")),
						  	Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("sil")),
							germany,
							Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("war"))
						  	));
		germanOrders[1].add(orderFactory.createMove(
							germany,
							makeLocation(pos, map.getProvince("sil")),
							Unit.Type.ARMY,
							makeLocation(pos, map.getProvince("war"))
							));
		germanOrders[1].add(orderFactory.createSupport(
							germany, 
							makeLocation(pos, map.getProvince("gal")),
						  	Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("sil")),
							germany,
							Unit.Type.ARMY,
						  	makeLocation(pos, map.getProvince("war"))
						  	));
		
		
		// create combined orders sets for all powers
		//
		List[] orderLists = new List[2];
		for(int i=0; i<orderLists.length; i++)
		{
			orderLists[i] = new ArrayList();
			orderLists[i].addAll(russianOrders);
			orderLists[i].addAll(germanOrders[i]);
		}
		
		System.out.println("Created "+orderLists.length+" sets of orders to evaluate.");
		
		return orderLists;
	}// createOrders()
	
	
	/** Make a Location for a Unit */
	private Location makeLocation(Position pos, Province prov)
	{
		return new Location(prov, pos.getUnit(prov).getCoast());
	}// makeLocation()
	
	
	/**
	*	Evaluate orders, stopping when we have reached our goal 
	*	(occupying Warsaw).
	*	<p>
	*	Note that there are many ways to evaluate the success of an order set.
	*	One method is to use the Position object, and check where units are.
	*	Another method is to look at the order results via TurnState.getResultList().
	*	Iterating through the OrderResults, once can determine which orders are
	*	successful and (if adjudicator statistical reporting is enabled) the 
	*	attack:defense statistics involved).
	*/
	private void evaluateOrders(World world, Position position, List[] orderSets)
	{
		/*
			we will evaluate by finding the BEST order that takes the sc
			(first check via hasUnit())
			
			then the one with the most attack strength
			
		*/
		
	}// evaluateOrders()
}// class AIDemo
