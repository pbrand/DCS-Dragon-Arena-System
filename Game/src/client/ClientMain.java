package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.Common;
import common.IBattleField;
import common.Enums.Direction;
import common.IPlayerController;

public class ClientMain {

	private static String helper_host;
	private static int helper_port;

	public static void main(String[] args) throws RemoteException {

		IPlayerController player = null;
		IPlayerController stub = null;
		try {
			String playerName = null;
			double lifespan = Integer.MAX_VALUE;

			if (args.length < 2) {
				playerName = "p_" + Common.randomString(10);
			} else {
				playerName = args[1];
				lifespan = Double.parseDouble(args[2]);
			}

			String serverID = playerName;

			String battleServer = "main_battle_server";
			/**
			 * Location should be provided in host:port format
			 */
			String battleServerLocation = args[0];
			String res = getHelperServer(battleServerLocation, battleServer);
			if (res == null || res.equals("noServers")) {
				return;
			}

			String[] helper = res.split(":");

			String battle_helper = helper[0];
			helper_host = helper[1];
			helper_port = Integer.parseInt(helper[2]);

			log(playerName, "host: " + helper_host + ":" + helper_port);

			player = new PlayerController(playerName, helper_host, helper_port,
					battle_helper, battleServerLocation, battleServer, lifespan);

			stub = (IPlayerController) UnicastRemoteObject.exportObject(player,
					0);

			Registry reg = LocateRegistry.getRegistry(helper_host, helper_port);

			reg.rebind(playerName, stub);
			log(playerName, "PlayerController running, server: " + serverID
					+ ", reg: " + reg.toString());
			//printRegistry(reg.list());
			player.spawnPlayer();
			
			playerCommander(playerName);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private static void playerCommander(final String player) {
		Runnable myRunnable = new Runnable() {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));

			public void run() {
				log(player, "Player commander Running");
				while (true) {

					String line = "";

					try {
						line = in.readLine();
						String[] res = line.split(" ");
						if (res.length > 0 && res[0].equals("m")) {
							Direction dir =  Direction.values()[(int) (Direction.values().length * Math
									.random())];
							movePlayer(player, dir);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}

	private static void movePlayer(String player, Direction direction) {
		try {
			IPlayerController RMIServer = (IPlayerController) Naming
					.lookup("rmi://" + helper_host + ":" + helper_port + "/"
							+ player);
			RMIServer.movePlayer(direction);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}

	public static String getHelperServer(String battleServerLocation,
			String battleServer) {
		String helper = null;

		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);

		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			helper = RMIServer.getRandomHelper();
			while (helper == null) {
				helper = RMIServer.getRandomHelper();
			}
			if (helper.equals("noServers")) {
				log("", "No server online to connect to");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log("", "Not able to connect to main server");
		}

		return helper;
	}

	@SuppressWarnings("unused")
	private static void printRegistry(String[] array) {
		for (int i = 0; i < array.length; i++) {
			log("","item[" + i + "]: " + array[i]);
		}
	}
		
	private static void log(String serverID, String text) {
		common.Log.log(serverID, text);
	}

}