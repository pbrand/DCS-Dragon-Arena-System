package server.master;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import server.helper.IBattleField;
import common.Enums.UnitType;
import common.IRunner;
import common.Message;
import common.MessageRequest;
import server.master.Player;
import server.master.Dragon;

public class BattleField implements IBattleField {

	/* The array of units */
	private Unit[][] map;

	/* The static singleton */
	private static BattleField battlefield;

	/* Primary socket of the battlefield */
	// private Socket serverSocket;

	/*
	 * The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;

	// public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private HashMap<String, Unit> units;

	private ArrayList<Message> messages;

	// private String serverLocation;

	/**
	 * Initialize the battlefield to the specified size
	 * 
	 * @param width
	 *            of the battlefield
	 * @param height
	 *            of the battlefield
	 * @throws RemoteException
	 */
	private BattleField() throws RemoteException {
		// Socket local = new LocalSocket();

		synchronized (this) {
			map = new Unit[MAP_WIDTH][MAP_HEIGHT];
			units = new HashMap<String, Unit>();
			messages = new ArrayList<Message>();
		}
		System.out.println("Battlefield created");
	}

	/**
	 * Singleton method which returns the sole instance of the battlefield.
	 * 
	 * @return the battlefield.
	 * @throws RemoteException
	 */
	public static BattleField getBattleField() throws RemoteException {
		if (battlefield == null)
			battlefield = new BattleField();
		return battlefield;
	}

	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub
		IRunner RMIServer = null;
		String clienthost = null;
		try {
			clienthost = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String urlServer = new String("rmi://" + clienthost + ":" + msg.getMiddlemanPort() + "/" + msg.getMiddleman());

		// Bind to RMIServer
		try {
			System.out.println(urlServer);
			RMIServer = (IRunner) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			RMIServer.receiveMessage(msg);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void receiveMessage(Message msg) {
		// TODO Auto-generated method stub
		String from = msg.getSender();
		String request = msg.getRequest();

		System.out.println("Message received from: " + msg.getSender()
				+ ", request: " + request);

		Message reply = null;
		Unit unit;
		switch (request) {
		case MessageRequest.spawnUnit: {
			System.out.println("Spawning: " + msg.getSender());
			Unit player = new Player();
			int[] pos = getAvailablePosition();
			boolean spawned = this.spawnUnit(from, player, pos[0], pos[1]);
			reply = new Message(from);
			reply.setRequest(MessageRequest.spawnAck);
			reply.put("spawned", spawned);
			reply.setMiddleman(msg.getMiddleman());
			reply.setMiddlemanPort(msg.getMiddlemanPort());
			break;
		}
		case MessageRequest.putUnit: {
			this.putUnit((Unit) msg.get("unit"), (Integer) msg.get("x"),
					(Integer) msg.get("y"));
			break;
		}
		case MessageRequest.getUnit: {
			reply = new Message(from);
			int x = (Integer) msg.get("x");
			int y = (Integer) msg.get("y");
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			reply.put("id", msg.get("id"));
			// Get the unit at the specific location
			reply.put("unit", getUnit(x, y));
			break;
		}
		case MessageRequest.getType: {
			reply = new Message(from);
			int x = (int) msg.get("x");
			int y = (int) msg.get("y");
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			reply.put("id", msg.get("id"));
			if (getUnit(x, y) instanceof Player)
				reply.put("type", UnitType.player);
			else if (getUnit(x, y) instanceof Dragon)
				reply.put("type", UnitType.dragon);
			else
				reply.put("type", UnitType.undefined);
			break;
		}
		case MessageRequest.dealDamage: {
			int x = (Integer) msg.get("x");
			int y = (Integer) msg.get("y");
			unit = this.getUnit(x, y);
			if (unit != null)
				unit.adjustHitPoints(-(Integer) msg.get("damage"));
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			break;
		}
		case MessageRequest.healDamage: {
			int x = (Integer) msg.get("x");
			int y = (Integer) msg.get("y");
			unit = this.getUnit(x, y);
			if (unit != null)
				unit.adjustHitPoints((Integer) msg.get("healed"));
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			break;
		}
		case MessageRequest.moveUnit: {
			//reply = new Message(from);
			this.moveUnit(units.get((String) msg.get("playerID")), (int) msg.get("x"),
					(int) msg.get("y"));
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			//reply.put("id", msg.get("id"))
			
			break;
		}
		case MessageRequest.removeUnit: {
			this.removeUnit((Integer) msg.get("x"), (Integer) msg.get("y"));
			return;
		}
//		case MessageRequest.getBattleFieldInfo: {
//			reply = new Message(from);
//			reply.setRequest(MessageRequest.getBattleFieldInfo);
//			reply.put("mapWidth", MAP_WIDTH);
//			reply.put("mapHeight", MAP_HEIGHT);
//		}
		}

		if (reply != null) {
			sendMessage(reply);
		}

	}
	
	public int[] getAvailablePosition() {
		int[] pos = null;
		
		Random random = new Random();
		boolean running = true;
		int x = 0;
		int y = 0;
		
		while(running) {
			x = (int) random.nextInt(MAP_WIDTH - 1);
			y = (int) random.nextInt(MAP_HEIGHT - 1);
			System.out.println("x: " + x + " ,y: " + y);
			if(getUnit(x ,y) == null) {
				running = false;
			}
		}
		pos = new int[2];
		pos[0] = x;
		pos[1] = y;
		System.out.println("Position: " + pos);
		return pos;
	}

	/**
	 * Put a unit at the specified position. First, it checks whether the
	 * position is empty, if not, it does nothing.
	 * 
	 * @param unit
	 *            is the actual unit being put on the specified position.
	 * @param x
	 *            is the x position.
	 * @param y
	 *            is the y position.
	 * @return true when the unit has been put on the specified position.
	 */
	private synchronized boolean putUnit(Unit unit, int x, int y) {
		if (map[x][y] != null)
			return false;

		map[x][y] = unit;
		unit.setPosition(x, y);

		return true;
	}

	/**
	 * Puts a new unit at the specified position. First, it checks whether the
	 * position is empty, if not, it does nothing. In addition, the unit is also
	 * put in the list of known units.
	 * 
	 * @param unit
	 *            is the actual unit being spawned on the specified position.
	 * @param x
	 *            is the x position.
	 * @param y
	 *            is the y position.
	 * @return true when the unit has been put on the specified position.
	 */
	private boolean spawnUnit(String id, Unit unit, int x, int y) {
		synchronized (this) {
			if (map[x][y] != null)
				return false;

			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.put(id, unit);
		System.out.println("Unit spwaned");

		return true;
	}

	/**
	 * Move the specified unit a certain number of steps.
	 * 
	 * @param unit
	 *            is the unit being moved.
	 * @param deltax
	 *            is the delta in the x position.
	 * @param deltay
	 *            is the delta in the y position.
	 * 
	 * @return true on success.
	 */
	private synchronized boolean moveUnit(Unit unit, int newX, int newY) {
		int originalX = unit.getX();
		int originalY = unit.getY();

//		if (unit.getHitPoints() <= 0)
//			return false;

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (putUnit(unit, newX, newY)) {
						map[originalX][originalY] = null;
						return true;
					}
				}

		return false;
	}

	/**
	 * Remove a unit from a specific position and makes the unit disconnect from
	 * the server.
	 * 
	 * @param x
	 *            position.
	 * @param y
	 *            position.
	 */
	private synchronized void removeUnit(int x, int y) {
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		unitToRemove.disconnect();
		units.remove(unitToRemove);
	}

	/**
	 * Get a unit from a position.
	 * 
	 * @param x
	 *            position.
	 * @param y
	 *            position.
	 * @return the unit at the specified position, or return null if there is no
	 *         unit at that specific position.
	 */
	public Unit getUnit(int x, int y) {
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;

		return map[x][y];
	}

	/**
	 * Returns a new unique unit ID.
	 * 
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	@Override
	public int[] getPosition(String id) throws RemoteException {
		int[] pos = new int[2];
		pos[0] = units.get(id).getX();
		pos[1] = units.get(id).getY();
		
		return pos;
	}

	@Override
	public int getMapHeight() throws RemoteException {
		return this.MAP_HEIGHT;
	}

	@Override
	public int getMapWidth() throws RemoteException {
		return this.MAP_WIDTH;
	}

	@Override
	public UnitType getType(int x, int y) throws RemoteException {
		if (getUnit(x, y) instanceof Player)
			return UnitType.player;
		else if (getUnit(x, y) instanceof Dragon)
			return UnitType.dragon;
		else return UnitType.undefined;
	}
}
