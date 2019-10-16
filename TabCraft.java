import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Map;
import javax.imageio.ImageIO;
import org.rsbot.bot.Bot;
import org.rsbot.bot.input.Mouse;
import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Calculations;
import org.rsbot.script.Constants;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.Skills;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.GrandExchange;

import java.io.*;
import java.net.*;


@ScriptManifest(authors = { "CookayMonsta" }, category = "Runecraft", name = "Tab Runecrafting", version = 1.0, description = 
	"<html><head>" +
	"</head><body>" +
	"<center><strong><h2>CookayMonsta's TAB Runecrafting</h2></strong><br />" + 
   	"<b><font size=\"4\" color=\"black\">What would you like to craft?</font></b>" + 
    	"<br></br>" + 
    	"<select name='runeType'>" + 
    	"<option>Laws" + 
    	"<option>Nats" + 
    	"<br></br>" + 
   	"<b><font size=\"3\" color=\"black\">Note: Please have all the following visible in current bank tab</font></b><br>" + 
   	"<font size=\"2\" color=\"black\">	- Type of rune crafting</font><br>" + 
   	"<font size=\"2\" color=\"black\">	- Pure Essence</font><br>" + 
   	"<font size=\"2\" color=\"black\">	- Tabs</font><br>" + 
   	"<font size=\"2\" color=\"black\">	- Astral, Air, and Cosmic runes</font><br>" + 
	"</body></html>")

public class TabCrafting extends Script implements PaintListener,ServerMessageListener
{

	//Banker
	private static final int FremennikBankerID = 9710;

	//Pouches
	private final int pouch0ID = 5509;
	private final int pouch1ID = 5510;
	private final int pouch2ID = 5512;
	private final int pouch3ID = 5514;
	private final int pouchBrokenID = 5515;
	private final int largeBrokenID = 5513;
	private final int medBrokenID = 5511;	
	private final int tabid = 13608;


	//Prices
	private int runePrice = 0;
	private int essPrice = 0;
	public int GainedProfit;
	public int runesCrafted = 0;


	//Ring
	private final int[] RingOfKinshipIDs = { 15707, 15707, 15707, 15707, 15707, 15707, 15707, 15707 };

        //Runes
	private final int pureEssence = 7936;
	private final int rune = 563;
	private final int lawRune = 563;
	private final int astralRune = 9075;
	private final int cosmicRune = 564;
	private final int airRune = 556;

	//Tiles
	private final RSTile bankTile = new RSTile(3448, 3720);
	private final RSTile portalTile = new RSTile(2464, 4818);
	private final RSTile altarTile = new RSTile(2464, 4830);
	private final RSTile entranceTile = new RSTile(2858, 3378);
	private final RSTile teleTile = new RSTile(3448, 3700);

	//Objects
	private final int altar = 2485;
	private final int entrance = 2459;
	private final int banker = 9710;
	
	//Paths
	private final RSTile[] toEntrance = { new RSTile(2858, 3378)};
	private final RSTile[] toAltar = { new RSTile(2464, 4829), new RSTile(2464, 4830)};
	private final RSTile[] toBank = { new RSTile(3449, 3698), new RSTile(3449, 3699), new RSTile(3449, 3700), new RSTile(3449, 3701), new RSTile(3449, 3702), new RSTile(3449, 3703), new RSTile(3449, 3704), new RSTile(3449, 3705), new RSTile(3449, 3706), new RSTile(3449, 3707), new RSTile(3449, 3708), new RSTile(3449, 3709), new RSTile(3449, 3710), new RSTile(3449, 3711), new RSTile(3449, 3712), new RSTile(3449, 3712), new RSTile(3449, 3714), new RSTile(3449, 3715), new RSTile(3449, 3716), new RSTile(3448, 3717), new RSTile(3448, 3718)};
	

	//---Variables---//
	// To know if the pouches are full or empty, since the IDs of empty and full pouches are the same :>
	private boolean pouchesFull[] = {false, false, false, false, false};
	// Error counter
	private int fatalCount = 0;	
	private int failCount = 0;
	private int outOfRunes = 0;
	// Altitude checker
	private boolean setAltitude = false;
	// Paint

	private int startLVL = 0;
	private int startXP = 0;
	private int pouchRepaired = 0;
	private long scriptStartTIME = 0;
	private boolean hidePaint = false;
	// Rune used to open the bank
	private int runeUsed = 0;
	// If bank is full
	private boolean bankFull = true;
	// Food used
	private int foodID = 7946;
	// Pouches used
	private boolean usesFourPouches = true;
	// Image
	private BufferedImage zmiIMG = null;
	// Anti-ban EXP check
	private int previousEXP = 0;
	private long lastTimeEXP = 0;
	// New Version
	private boolean newVersion = false;
	private boolean noNewVersion = false;
	int mSpeed = 8; // the lower it is the faster it get 



	private enum State {goInAltar,  goToAltar, walkToAltar, withdrawTabBak, withdrawEssBak, craftRunes, teleport, teleBreak, walkToBank, bank, fillingPouches, emptyPouches, repairPouch,  error };
	
	protected int getMouseSpeed() {
	return mSpeed;
	}

	private double getVersion()
	{
		return 1.00 ;
	}	 
	
	public boolean onStart( Map<String, String>args) {
		scriptStartTIME = System.currentTimeMillis();
		try {
			final URL url = new URL("http://img291.imageshack.us/img291/3104/zmi.gif");
			zmiIMG = ImageIO.read(url);
		} catch (final Exception e) {
			log("Failed to load the paint picture.");
		}
		essPrice = new GrandExchange().loadItemInfo(pureEssence).getMarketPrice();
		runePrice = new GrandExchange().loadItemInfo(rune).getMarketPrice();
		log("Loading info...");		
		return true;
	}
		 
	/**
	  * Get the current state of the player so the program can know what to do next.
	  *  
	  * @return <b>State</b> : The current state of the player.
	  */
	private State getState() 
	{
		if (fatalCount > 200)
			return State.error;
		// If the player is near the bank
		try
		{
		if (distanceBetween(getLocation(), bankTile) <= random(4,5)) 
		{
			if (inventoryContains(astralRune) && (getInventoryCount(airRune) > 1) 
				&& inventoryContains(cosmicRune) && (inventoryContains(pouchBrokenID) 
				|| inventoryContains(largeBrokenID) || inventoryContains(medBrokenID)))
			{
				return State.repairPouch;
			}
			if (isInventoryFull() && (getInventoryCount(tabid) < 1))
			{
				return State.withdrawTabBak;
			}if (!isInventoryFull() && (inventoryContains(pureEssence)))
			{
				return State.withdrawEssBak;
			}		
			// Bank if the player's inventory isn't full or doesn't contains pure essences
			if (!inventoryContains(pureEssence) || getInventoryCount(pureEssence) < 20)
				return State.bank;
		}
		// Is near the altar
		if (distanceBetween(getLocation(), altarTile) <= random(10,15))
		{
			// If there is essences in the inventory, craft the essences
			if (inventoryContains(pureEssence))
			{
				return State.craftRunes;
			}
			// If the pouches aren't full, empty them		
			else if (!arePouchesEmpty())
			{
				return State.emptyPouches;
			}			
		}
		if (distanceBetween(getLocation(), entranceTile) <= random(10,15))
		{
				return State.goInAltar;		
		}	
		if (distanceBetween(getLocation(), altarTile) <= random(5,6) && inventoryContains(rune) && getMyPlayer().getAnimation() != 13652)
		{
				return State.teleport;		
		}
		if (distanceBetween(getLocation(), altarTile) <= random(5,6) && inventoryContains(rune) && getMyPlayer().getAnimation() == 13652)
		{
				return State.teleBreak;		
		}
		if (distanceBetween(getLocation(), portalTile) <= random(2,3))
		{
				return State.walkToAltar;		
		}	
		if (distanceBetween(getLocation(), teleTile) <= random(9,10))
		{
				return State.walkToBank;		
		}	
		if (inventoryContains(pureEssence))
		{
			// State is filling pouches if the pouches aren't full
			if (arePouchesFull())
			{	
				return State.goToAltar;
			}
			else
			{
				return State.fillingPouches;
			}	
		}
		// If the player doesn't have essences, the only option left is teleport 
		// or walking back to the bank
		else 
		{
			if (inventoryContains(rune))
				return State.walkToBank;
			else
				return State.walkToBank;
		}
		}catch (Exception e) {
			return State.error;
		}
	}	 


	public int loop() 
	{
		if (!isLoggedIn())
		{
			return 1;
		}
		if(!setAltitude) 
		{
			setCameraAltitude(true);
			setCameraRotation(random(350,10));
			wait(250, 500);
			setAltitude = true;			
			return 1;			
		}		
		if (runeUsed == 0)
		{
			runeUsed = getRuneBank();
			usesFourPouches = getNumberPouches();
			if (usesFourPouches)
			{
				log("Using four pouches.");				
			}else{
				log("Using three pouches.");
			}
			log("Starting up.");
		}	
		
		if(getEnergy()>= random(20,25))			
			setRun(true);
		
		if (getMyPlayer().isMoving())
			antiban();
		
		switch (getState()) 
		{	
			case goInAltar:
				walkPath(toEntrance);
				clickEntrance();			
				break;
			case walkToBank:
        			openTab(Constants.TAB_INVENTORY);
				walkPath(toBank);
				break;
			case craftRunes:
				craftRunes();
				break;
			case teleport:				
				teleport();				
				break;
			case withdrawTabBak:				
				withdrawTabBak();
				break;
			case withdrawEssBak:				
				withdrawEssBak();
				break;
			case bank:				
				bank();
				break;
			case teleBreak:
				wait(1000,1500);
				break;
			case fillingPouches:				
				fillPouches();
				break;
			case emptyPouches:				
				emptyPouches();		
				break;
			case repairPouch:				
				repairingPouches();
				break;
			case goToAltar:
				breaktab();
				break;
			case walkToAltar:
				walkPath(toAltar);
				break;
			case error:		
				break;
		}
		return 0;
	}


	@Override
	public void stopScript(boolean logout)
	{
		if (logout)
		{
			if (bank.isOpen())
			{
				closeBank();
				while(bank.isOpen() && failCount < 30)
				{
					wait(40, 50);
	                failCount++;
				}
				failCount = 0;
			}
			
			while (!isOnLogoutTab()) 
			{
				atInterface(548, 178);
				while (!isOnLogoutTab() && failCount < 5) {
	                wait(200, 400);
	                failCount++;
	            }
			}
			wait(350, 459);
			atInterface(182, 15);
			wait(1500, 2000);
			stopScript();
		}else
		{
			stopScript();
		}
	}

	private int getRuneBank()
	{
		return airRune;		
	}
	/**
	 * Anti-Ban. Logs you out when you don't gain any exp in 5 to 7 minutes (random).
	 */
	private void checkExp()
	{
		if(previousEXP == 0)
		{
			previousEXP = skills.getCurrentSkillExp(STAT_RUNECRAFTING);
			lastTimeEXP = System.currentTimeMillis();
		}else{
			if(previousEXP != skills.getCurrentSkillExp(STAT_RUNECRAFTING))
			{
				previousEXP = skills.getCurrentSkillExp(STAT_RUNECRAFTING);
				lastTimeEXP = System.currentTimeMillis();
			}
			if ((System.currentTimeMillis() - lastTimeEXP )  >= random(300000, 420000)){
				log("Error ! You haven't gained any exp in the last 5-7 minutes. Logging out.");
				stopScript(true);				
			}			
		}
		
	}


	private void antiban() 
	{
		int rng = random(1, 55);
		switch (rng) {
		case 1:
			if (random(1, 55) != 1)
				return;
			checkExp();
			moveMouse(random(10, 750), random(10, 495));
			return;
		case 2:
			if (random(1, 19) != 1)
				return;
			int angle = getCameraAngle() + random(-30, 30);
			if (angle < 0) {
				angle = random(0, 10);
			}
			if (angle > 359) {
				angle = random(0, 10);
			}
			checkExp();
			setCameraRotation(angle);
			return;
		case 3:
			if (random(1, 20) != 1)
				return;
			try {
				checkExp();
			} catch (Exception e) {				
			}			
			return;
		default:
			return;
		}		
	}



	/**
	 * Function to bank
	 */
	private void bank() 
	{
		openBank();	
		// Deposits items
		if(bank.isOpen())
		{
			outOfRunes = 0;
			if ((getInventoryCount(pureEssence) == 0 ) || 
				(isInventoryFull() && getInventoryCount(pureEssence) <= 17)) {
				if (bankFull){
					runesCrafted += getInventoryCount(rune);
					bank.depositAllExcept(tabid, pouch0ID, pouch1ID, pouch2ID, pouch3ID, pouchBrokenID, 
										largeBrokenID, medBrokenID, pureEssence);
				}			
			}			
			
		}

		// Withdraw tab
		if(!inventoryContains(tabid))
		{
			withdrawTab();

		}
		// Withdraw repair mats if needed
		if((inventoryContains(pouchBrokenID) || inventoryContains(largeBrokenID) 
			|| inventoryContains(medBrokenID)) && !arePouchesFull())
		{
			withdrawRepairMats();
		}
		// Withdraw Essences
		if (!inventoryContains(pureEssence) || !isInventoryFull()) {
			if (bank.atItem(pureEssence, "Withdraw-All")) {
				wait(1000, 1200);				
				failCount = 0;
				while (!inventoryContains(pureEssence) && failCount < 30) {
					wait(50, 100);
					failCount++;
				}
				if(bank.getCount(pureEssence) <= 30 && bank.getCount(pureEssence) > 0 && bank.isOpen())
				{
					closeBank();
					wait(500,600);
					log("Out of pure essence. Logging off.");
					stopScript();			
				}
			} else fatalCount++;
		}
	}



	/**
	 * Function that opens the bank
	 */
	private void openBank()
	{

		if (!bank.isOpen()) 
		{
			if (!getInterface(619).isValid() && !getMyPlayer().isMoving())
			{
				try
				{	
				if (atNPC(getNearestNPCByID(banker), "Bank Frem")) {
					wait(800, 900);				
				}
				}catch (Exception e) {
				}
			}
			wait(200,300);
		}		
	}




	private void closeBank()
	{
		// Close the bank
		if (bank.isOpen()) {
			if (bank.close()) {
				wait(100, 200);
				failCount = 0;
				while (bank.isOpen() && failCount < 20) {
					wait(50, 100);
					failCount++;
				}
			}	
		}		
	}	




	private void clickEntrance()
	{
		RSObject altarentrance = getNearestObjectByID(entrance);
		int rng = random(1,8);
		int invCount = getInventoryCount(rune);
		if (atObject(altarentrance, "Enter")) {
			fatalCount = 0;
			wait(50, 100);
			failCount = 0;
			while (invCount == getInventoryCount(rune) && failCount < 40) {
				wait(50, 100);
				failCount++;
			}
		}
	}


	private void withdrawTabBak() 
	{
		openBank();	
			if (bank.isOpen()) {
				atInventoryItem(pureEssence, "Deposit-1");
				withdrawTab();
				wait(1500,2500);
		}
	}

	private void withdrawEssBak() 
	{
		openBank();	
			if (bank.isOpen()) {
				bank.atItem(pureEssence, "Withdraw-All");
				wait(100,200);
		}
	}

	private void breaktab()
	{
			closeBank();
			atInventoryItem(tabid, "Break");
			wait(5000,6000);
		}
		
	/**
	 * Function that withdraws mats to repair the pouches
	 */
	private void withdrawRepairMats()
	{
		if (bank.isOpen())
		{
			if (bank.atItem(airRune, "Withdraw-1"))
			{				
				bank.atItem(airRune, "Withdraw-1");
				failCount = 0;
				while (!inventoryContains(airRune) && failCount < 30) {
					wait(50, 100);
					failCount++;
				}
			}	
			wait(200, 250);
			if (bank.atItem(astralRune, "Withdraw-1")) {
				failCount = 0;
				while (!inventoryContains(astralRune) && failCount < 30) {
					wait(50, 100);
					failCount++;
				}
			}
			wait(200, 300);
			if (bank.atItem(cosmicRune, "Withdraw-1")) {
				failCount = 0;
				while (!inventoryContains(cosmicRune) && failCount < 30) {
					wait(50, 100);
					failCount++;
				}
			}
		}
		
	}

	private void withdrawTab()
	{
		if (bank.isOpen())
		{
			if (bank.atItem(tabid, "Withdraw-1"))
			wait(50, 100);
				
		}
		
	}


		/**
	 * Function that teleports 
	 */
	private void teleport()
	{
	if(atEquipment("heim Ring", RingOfKinshipIDs)){
		wait(500,800);
                }

	}


	public boolean atEquipment(final String action, final int... itemID) {
                for (final int item : itemID) {
                        if (atEquipment(action, item)) {
                                return true;
                        }
                }
                return false;
        }

	        private boolean atEquipment(final String action, final int itemID) {
                final int[] equipmentArray = getEquipmentArray();
                int pos = 0;
                while (pos < equipmentArray.length) {
                        if (equipmentArray[pos] == itemID) {
                                break;
                        }
                        pos++;
                }
                if (pos == equipmentArray.length) {
                        return false;
                }
                Point tl, br;
                switch (pos) {
                case 3:
                        tl = new Point(575, 292);
                        br = new Point(599, 317);
                        break;
                case 9:
                        tl = new Point(686, 372);
                        br = new Point(711, 396);
                        break;
                default:
                        log("Currently atEquipment is only implemented for Weapons and Rings. Pos:" + pos);
                        return false;
                }
                int error = 0;
                while (getCurrentTab() != Constants.TAB_EQUIPMENT) {
                        openTab(Constants.TAB_EQUIPMENT);
                        wait(random(50, 100));
                        error++;
                        if (error > 5) {
                                return false;
                        }
                }
                moveMouse(random(tl.x, br.x), random(tl.y, br.y));
                wait(random(50, 100));
                return atMenu(action);
        }

	/**
	 * Function that craft the runes
	 */
	private void craftRunes()
	{
		final RSObject altarr = getNearestObjectByID(altar);
		int rng = random(1,8);
		int invCount = getInventoryCount(rune);
		if (atObject(altarr, "Craft")) {
			fatalCount = 0;
			wait(200, 400);
			failCount = 0;
			while (invCount == getInventoryCount(rune) && failCount < 40) {
				wait(50, 100);
				failCount++;
			}
		}
	}


	/**
     * Function used to fill pouches.
     *
     * @return error Code
     */
	private int fillPouches()
	{
		closeBank();
		if (usesFourPouches)
		{		
			if (!pouchesFull[1] || !pouchesFull[2])
			{
				if (atInventoryItem(pouch1ID, "Fill"))
				{
					wait(100, 200);					
				}else if(inventoryContains(medBrokenID) && atInventoryItem(medBrokenID, "Fill")) {
					wait(200, 300);						
				}
				if (atInventoryItem(pouch2ID, "Fill"))
				{
					wait(100, 200);					
				}else if(inventoryContains(largeBrokenID) && atInventoryItem(largeBrokenID, "Fill")) {
					wait(200, 300);						
				}
				pouchesFull[1] = true;
				pouchesFull[2] = true;
				// Open the bank
				openBank();	
				// Withdraw Ess
				if (!isInventoryFull()) {
					wait(100,200);
					if (bank.atItem(pureEssence, "Withdraw-All")) {
						wait(200, 400);
						failCount = 0;
						closeBank();
						while (!isInventoryFull() && failCount < 30) {
							wait(50, 100);
							failCount++;
						}
					}
				}
			}
			else
			{
				if (atInventoryItem(pouch0ID, "Fill"))
				{
					pouchesFull[0] = true;
					wait(100, 200);					
				}
				if (atInventoryItem(pouch3ID, "Fill"))
				{
					pouchesFull[3] = true;
					pouchesFull[4] = true;
					wait(100, 200);					
				}else if(inventoryContains(pouchBrokenID) && atInventoryItem(pouchBrokenID, "Fill")) {
					pouchesFull[3] = true;
					pouchesFull[4] = true;
					wait(200, 300);						
				}				
				
				openBank();
				// Withdraw Ess
				if (!isInventoryFull()) {
					wait(100,200);
					if (bank.atItem(pureEssence, "Withdraw-All")) {
						wait(200, 400);
						failCount = 0;
						closeBank();
						while (!isInventoryFull() && failCount < 30) {
							wait(50, 100);
							failCount++;
						}
					} 
				}
			}
		}else{
			if (atInventoryItem(pouch0ID, "Fill"))
			{
				wait(100, 200);	
				pouchesFull[0] = true;
			}
			if (atInventoryItem(pouch1ID, "Fill"))
			{
				wait(100, 200);	
				pouchesFull[1] = true;
			}else if(inventoryContains(medBrokenID) && atInventoryItem(medBrokenID, "Fill")) {
				wait(200, 300);	
				pouchesFull[1] = true;
			}
			if (atInventoryItem(pouch2ID, "Fill"))
			{
				wait(100, 200);	
				pouchesFull[2] = true;
			}else if(inventoryContains(largeBrokenID) && atInventoryItem(largeBrokenID, "Fill")) {
				pouchesFull[2] = true;
				wait(200, 300);						
			}				
			
			openBank();
			// Withdraw Ess
			if (!isInventoryFull()) {
				wait(500,800);
				if (bank.atItem(pureEssence, "Withdraw-All")) {
					wait(200, 400);
					failCount = 0;
					closeBank();
					while (!isInventoryFull() && failCount < 30) {
						wait(50, 100);
						failCount++;
					}
				} 
			}
		}
		return 0;
	}	
	
	/**
     * Function used to empty pouches.
     *
     * @return error Code
     */
	private int emptyPouches()
	{
		// Four pouches
		if (usesFourPouches)
		{
			if (pouchesFull[2] || pouchesFull[1])
			{
				if (atInventoryItem(pouch1ID, "Empty"))
				{
					pouchesFull[1] = false;
					wait(200, 300);					
				}else if(inventoryContains(medBrokenID) && atInventoryItem(medBrokenID, "Empty")) {
					pouchesFull[1] = false;	
					wait(200, 300);						
				}
				if (atInventoryItem(pouch2ID, "Empty"))
				{
					pouchesFull[2] = false;	
					wait(200, 300);					
				}else if(inventoryContains(largeBrokenID) && atInventoryItem(largeBrokenID, "Empty")) {
					pouchesFull[2] = false;	
					wait(200, 300);						
				}
			}
			else if(pouchesFull[0] && pouchesFull[3])
			{
				if (atInventoryItem(pouch0ID, "Empty"))
				{
					pouchesFull[0] = false;
					wait(200, 300);					
				}
				if (atInventoryItem(pouch3ID, "Empty"))
				{
					pouchesFull[3] = false;	
					wait(200, 300);					
				}else if(inventoryContains(pouchBrokenID) && atInventoryItem(pouchBrokenID, "Empty")) {
					pouchesFull[3] = false;	
					wait(200, 300);						
				}
			}
			else if (pouchesFull[4])
			{
				if (atInventoryItem(pouch2ID, "Empty"))
				{
					wait(200, 300);					
				}else if(inventoryContains(largeBrokenID) && atInventoryItem(largeBrokenID, "Empty")) {
					wait(200, 300);						
				}
				if (atInventoryItem(pouch3ID, "Empty"))
				{
					wait(200, 300);					
				}else if(inventoryContains(pouchBrokenID) && atInventoryItem(pouchBrokenID, "Empty")) {
					wait(200, 300);						
				}
				pouchesFull[4] = false;	
				craftRunes();
			}		
		}else{ // Three pouches
			if(pouchesFull[0] || pouchesFull[1])
			{
	
				if (atInventoryItem(pouch0ID, "Empty"))
				{
					pouchesFull[0] = false;
					wait(200, 300);					
				}
				if (atInventoryItem(pouch1ID, "Empty"))
				{
					pouchesFull[1] = false;	
					wait(200, 300);					
				}else if(inventoryContains(medBrokenID) && atInventoryItem(medBrokenID, "Empty")) {
					wait(200, 300);	
					pouchesFull[1] = false;
				}
				if (atInventoryItem(pouch2ID, "Empty"))
				{
					pouchesFull[2] = false;
					wait(200, 300);		
				}else if(inventoryContains(largeBrokenID) && atInventoryItem(largeBrokenID, "Empty")) {
					wait(200, 300);	
					pouchesFull[2] = false;
					craftRunes();
				}
				pouchesFull[0] = false;
				pouchesFull[1] = false;
				pouchesFull[2] = false;
				craftRunes();
			}
			else
			{
				craftRunes();
			}
		}
		return 0;
	}

	
	/**
     * Function used to repair pouches.
     *
     * @return Error code
     */
	private int repairingPouches()
	{
		closeBank();
		// Open Magic Book tab
		if (getCurrentTab() != TAB_MAGIC) {
			openTab(TAB_MAGIC);
			failCount = 0;
			while (getCurrentTab() != TAB_MAGIC && failCount < 10) {
				wait(50, 100);
				failCount++;
			}
		}
		// Click NPC Contact spell
		if (atInterface(430, 26)) 
		{
			wait(500, 750);
			failCount = 0;
			while (getMyPlayer().getAnimation() != -1) {
				wait(50, 100);
			}
		}		
		// Move to slider
		wait(500, 750);
		moveMouse(random(4,6), 462, 270, 5, 25);
		// Click on slider
		wait(100, 250);
		clickMouse(true);			
		wait(100, 250);
		// Move to Darkmage
		moveMouse(random(4,6), 415, 122, 30, 55);
		wait(100, 250);
		// Click on Darkmage
		clickMouse(true);			
		wait(6000, 6500);
		// Click continue
		while(canContinue())
		{
			wait(80,100);
			clickContinue();
			wait(200,300);
		}
		// Click on repair ess
		if (getInterface(230).getChild(3).isValid() && getInterface(230, 3).containsText("Can you repair my pouches?")) {
			atInterface(230, 3);
			wait(400, 600);
	
		}else if (getInterface(228).getChild(2).isValid() && getInterface(228, 2).containsText("Can you repair my pouches?")) {
				atInterface(228, 2);
				wait(400, 600);
		
		}
		// Click Continue
		failCount = 0;
		while(!canContinue() && failCount < 40)
		{
			wait(100,150);
			++failCount;
		}
		failCount = 0;		
		if (!clickContinue())
		{
			return -1;
		}
		++pouchRepaired;
		// pouch repaired !
		return 0;
	}


	/**
     * Function used to know if the pouches are full.
     *
     * @return <b>True</b> : If all the pouches are full <br/>
     *		   <b>False</b> : If one or more pouches are empty.
     */
	private boolean arePouchesFull(){
		if (usesFourPouches)
		{
			if (pouchesFull[0] && pouchesFull[1] && pouchesFull[2] && pouchesFull[3] && pouchesFull[4])
			{
				return true;
			}		
			else
			{
				return false;
			}
		}else
		{
			if (pouchesFull[0] && pouchesFull[1] && pouchesFull[2])
			{
				return true;
			}		
			else
			{
				return false;
			}	
		}
	}
	
	/**
     * Function used to know if the pouches are empty.
     *
     * @return <b>True</b> : If all the pouches are empty <br/>
     *		   <b>False</b> : If one or more pouches are full.
     */
	private boolean arePouchesEmpty(){
		if (usesFourPouches)
		{
			if (!pouchesFull[0] && !pouchesFull[1] && !pouchesFull[2] && !pouchesFull[3] && !pouchesFull[4])
			{
				return true;
			}		
			else
			{
				return false;
			}
		}else{
			if (!pouchesFull[0] && !pouchesFull[1] && !pouchesFull[2])
			{
				return true;
			}		
			else
			{
				return false;
			}
		}
	}
	
	/**
	 * Function that detects if the players is using 3 or 4 pouches
	 * @return True if the player is using 4 pouches
	 * 		   False if the player is using 3 pouches
	 */

	private boolean getNumberPouches() 
	{
		if(inventoryContains(pouch3ID) || inventoryContains(pouchBrokenID))
			return true;
		return false;
	}


	/**
	 * The paint
	 */
	public void onRepaint(Graphics g) {
		if (!isLoggedIn())
			return;
		
		if (startLVL == 0) {
			startXP = skills.getCurrentSkillExp(Skills.getStatIndex("runecrafting"));
			startLVL = skills.getCurrentSkillLevel(Skills.getStatIndex("runecrafting"));
		}
		final int currentXP = skills.getCurrentSkillExp(Skills.getStatIndex("runecrafting"));
		final long XPgained = currentXP - startXP;
		int XPperHour = 0;
		Font font;
		long seconds = 0;
		long minutes = 0;
		long hours = 0;
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		final long runTime = System.currentTimeMillis() - scriptStartTIME;
		seconds = runTime / 1000;
		if ( seconds >= 60 ) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if ( minutes >= 60 ) {
			hours = minutes / 60;
			minutes -= (hours * 60);
		}
		
		if ((runTime / 1000) > 0) 
		{
			XPperHour = (int) ((3600000.0 / (double) runTime) * XPgained);
		}
		GainedProfit = ((runesCrafted * runePrice));
		Mouse mouse = Bot.getClient().getMouse();
		if (hidePaint)
		{
			try {
				font = new Font("Tahoma", Font.BOLD, 14);
			} catch (Exception e) {
				font = new Font("Dialog", Font.BOLD, 14);
			}
			g.setColor(new Color(0, 0, 0, 75));
			g.fillRect(422, 315, 93, 22);
			g.setColor(new Color(178, 34, 34, 255));
			g.drawRect(422, 315, 93, 22);
			
			g.setFont(font);
			g.setColor(Color.WHITE);
			g.drawString("Click to show", 424, 332);	
			g.drawString(hours + ":" + nf.format(minutes) + ":" + nf.format(seconds), 422, 305);
			g.drawString(String.valueOf(XPperHour), 422, 285);
			
			if (mouse.x >= 422 && mouse.x <= (422 + 93) && mouse.y >= 315 && mouse.y <= (315 + 22) && mouse.pressed)
			{
				hidePaint = false;
			}			
		}
		else
		{
			final int currentLvl = skills.getCurrentSkillLevel(Skills.getStatIndex("runecrafting"));
			final int levelGained = currentLvl - startLVL;
			final int XPTL = skills.getXPToNextLevel(STAT_RUNECRAFTING);			
			final int percentTilNext = skills.getPercentToNextLevel(Skills.getStatIndex("runecrafting"));
			final int fillBar = (int) (2.45 * (double) percentTilNext);
			String XPgainedString = String.valueOf(XPgained);		
			String XPTLString = String.valueOf(XPTL);
			
			
			if (XPTL >= 10000)
			{
				XPTLString = String.valueOf((XPTL - (XPTL%100))/1000) + "k";
			}else if(XPgained >= 1000000)
			{
				XPTLString = String.valueOf((XPTL - (XPTL%100000))/1000000) + "M";
			}
			
			if (XPgained >= 10000)
			{
				XPgainedString = String.valueOf((XPgained - (XPgained%100))/1000) + "k";
			}else if(XPgained >= 1000000)
			{
				XPgainedString = String.valueOf((XPgained - (XPgained%100000))/1000000) + "M";
			}
			
			//Borders
			g.setColor(new Color(178, 34, 34, 255));
			g.drawRect(4, 265, 95, 23);
			g.drawRect(4, 288, 512, 50);
			g.drawRect(422, 4, 93, 24);
			
			// Backgrounds
			g.setColor(new Color(0, 0, 0, 75));
			g.fillRect(4, 265, 95, 23);
			g.fillRect(4, 288, 512, 50);
			g.fillRect(422, 4, 93, 24);
			
			// Texts
			try {
				font = new Font("Tahoma", Font.BOLD, 14);
			} catch (Exception e) {
				font = new Font("Dialog", Font.BOLD, 14);
			}
			g.setFont(font);
			// Check for new version
			if (mouse.x >= 422 && mouse.x <= (422 + 93) && mouse.y >= 4 && mouse.y <= (4 + 24))
			{
				if(mouse.pressed)
				{
					g.setColor(Color.WHITE);
					g.drawString(String.valueOf("Loading..."), 424, 21);
					checkVersion();
				}else
				{
					if (!newVersion && !noNewVersion)
					{
						g.setColor(Color.WHITE);
						g.drawString(String.valueOf("Check for new"), 424, 21);
					}
					else if (newVersion)
					{
						g.setColor(Color.RED);
						g.drawString(String.valueOf("New Avalaible"), 424, 21);
					}
					else if (noNewVersion)
					{
						g.setColor(Color.GREEN);
						g.drawString(String.valueOf("Have latest"), 424, 21);
					}
				}
			}else
			{
				if (!newVersion && !noNewVersion)
				{
					g.setColor(Color.WHITE);
					
					g.drawString("Version " + String.valueOf(getVersion()), 424, 21);
				}
				else if (newVersion)
				{
					g.setColor(Color.RED);

					g.drawString("Version " + String.valueOf(getVersion()), 424, 21);
				}
				else if (noNewVersion)
				{
					g.setColor(Color.GREEN);

					g.drawString("Version " + String.valueOf(getVersion()), 424, 21);
				}
			}
			
			g.setColor(Color.WHITE);

			// Click to hide paint
			if (mouse.x >= 4 && mouse.x <= (4 + 95) && mouse.y >= 265 && mouse.y <= (265 + 23))
			{
				g.drawString("Click to hide", 6, 282);
				if(mouse.pressed)
				{
					hidePaint = true;
				}
			}else
			{
				g.drawString(hours + ":" + nf.format(minutes) + ":" + nf.format(seconds), 6, 282);
			}
			
			// Set font
			try {
				font = new Font("Tahoma", Font.PLAIN, 12);
			} catch (Exception e) {
				font = new Font("Dialog", Font.PLAIN, 12);
			}
			
			// Other texts
			g.setFont(font);
			g.drawString("XP/Hour : " + XPperHour, 11, 308);
			g.drawString("XP Gained : " + XPgainedString, 11, 328);
			g.drawString("XP to next level : " + XPTLString, 116,308);
			g.drawString("Level " + currentLvl, 289, 328);
			g.drawString("Crafted " + runesCrafted + " runes", 355, 308);
			g.drawString("Gained " + GainedProfit + "GP in runes", 355, 328);
			
			if (levelGained != 1 && levelGained != 0)
			{
				g.drawString(levelGained + " Levels gained", 259, 308);
			}else
			{
				g.drawString(levelGained + " Level gained", 259, 308);
			}
			
			if(pouchRepaired != 1 && pouchRepaired != 0)
			{
				g.drawString("Pouches repaired " + pouchRepaired + " times", 116, 328);
			}else{
				g.drawString("Pouches repaired " + pouchRepaired + " time", 116, 328);
			}
			
			// Progress bar
			g.drawString("Progress Bar ",  185, 282);
			g.setColor(new Color(178, 34, 34, 150));
			g.fillRect(263, 273, 245, 12);
			g.setColor(new Color(50, 205, 50, 175));
			g.fillRect(263, 273, fillBar, 12);
		}
	}


	private void checkVersion()
	{
        URLConnection url = null;
        BufferedReader in = null;
        BufferedWriter out = null; 	
        noNewVersion = false;
        newVersion = false;
		try{
             //Open the version text file
             url = new URL("http://zmicrafter.webs.com/scripts/zmicrafterversion.txt").openConnection();
             //Create an input stream for it
             in = new BufferedReader(new InputStreamReader(url.getInputStream()));
             //Check if the current version is outdated
             if(Double.parseDouble(in.readLine()) > getVersion()) 
             {
                 newVersion = true;
             }else{
            	 noNewVersion = true;
             }
             if(in != null)
                 in.close();
             if(out != null)
                 out.close();
         } catch (IOException e){
        	 log.severe("Problem getting version.");
         }
     }
	
	/**
	 * Function that allows to walk a path
	 * @param path The path to walk
	 * @return true : If the end is reached
	 *  	   false : If the end isn't reached yet
	 */
	private boolean walkPath(RSTile[] path) {
		if(!getMyPlayer().isMoving() || distanceTo(getDestination()) <= 7)
		{
			try{
				return walkTo(nextTile(path), 1, 1);
			}catch (Exception e) {
			}						
		}
		return false;
	}
	
	private boolean onTile(final RSTile tile, final String action, final double dx, final double dy, final int height) {
		if (!tile.isValid())
			return false;

		Point checkScreen = null;

		try {
			checkScreen = Calculations.tileToScreen(tile, dx, dy, height);
			if (!pointOnScreen(checkScreen)) {
				if (getMyPlayer().isMoving())
					return false;
				if (walkTileMM(tile))
					wait(750, 1000);
				return false;
			}
		} catch(Exception e) { }

		try {
			boolean stop = false;
			for (int i = 0; i <= 50; i++) {
				checkScreen = Calculations.tileToScreen(tile, dx, dy, height);
				if (!pointOnScreen(checkScreen))
					return false;
				moveMouse(checkScreen);
				final Object[] menuItems = getMenuItems().toArray();
				for (int a = 0; a < menuItems.length; a++) {
					if (menuItems[a].toString().toLowerCase().contains(action.toLowerCase())) {
						stop = true;
						break;
					}
				}
				if (stop)
					break;
			}
		} catch(Exception e) { }

		try {
			return atMenu(action);
		} catch(Exception e) { }
		return false;
	}
	public void wait(int min, int max)
	{
		wait(random(min,max));
	}
	public boolean walkTo(final RSTile t, final int x, final int y)
	{
		try {
			if (getDestination() != null)
				if (distanceBetween(getDestination(), t) < 3)
					return false;

			if (tileToMinimap(t).x == -1 || tileToMinimap(t).y == -1) {
				return walkTo(getClosestTileOnMap(t), x, y);
			} else {
				clickMouse(tileToMinimap(t), x, y, true);
			}
		} catch (Exception e) {
		}		
		return true;		
	}
	
	public void onFinish() {
		Bot.getEventManager().removeListener( PaintListener.class, this );
	}
	/**
	 * Server messages reciever
	 */
	@Override
	public void serverMessageRecieved(ServerMessageEvent arg0) {
		String serverString = arg0.getMessage();
		if (serverString.contains("<col=ffff00>System update in")) {
			log("There will be a system update soon, so we logged out.");
			stopScript(true);
		}
		if (serverString.contains("Your ring of life saves you")) {
			log("You almost died. Loging out.");
			stopScript(true);
		}
		if (serverString.contains("Oh dear, you are dead!")) {
			log("You died :(. Loging out.");
			stopScript(true);
		}		
		if (serverString.contains("You've just advanced")) 
		{
			log("Congrats on level up!");		
		}
	}
}
