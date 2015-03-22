package server.master;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Snapshot implements Serializable {

	private Unit[][] map;
	private int lastUnitID = 0;

	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private HashMap<String, Unit> units;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3368639092651276469L;

	public Snapshot() {
		super();
	}

	public Unit[][] getMap() {
		return map;
	}

	public void setMap(Unit[][] map) {
		this.map = map;
	}

	public int getLastUnitID() {
		return lastUnitID;
	}

	public void setLastUnitID(int lastUnitID) {
		this.lastUnitID = lastUnitID;
	}

	public HashMap<String, Unit> getUnits() {
		return units;
	}

	public void setUnits(HashMap<String, Unit> units) {
		this.units = units;
	}

	public static int getMapWidth() {
		return MAP_WIDTH;
	}

	public static int getMapHeight() {
		return MAP_HEIGHT;
	}

}