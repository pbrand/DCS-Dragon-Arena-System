package client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import server.helper.IBattleField;
import common.IPlayerController;

public class ClientMain {

	public static String serverID;

	private static String helper_host;
	private static int helper_port;

	public static void main(String[] args) throws RemoteException {

		IPlayerController player = null;
		IPlayerController stub = null;
		try {
			String playerName = null;
			
			if (args.length < 2) {
				playerName = "p_" + randomString(10);
			} else {
				playerName = args[1];
			}
			
			serverID = playerName;

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

			System.out.println("host: " + helper_host + ":" + helper_port);

			player = new PlayerController(playerName, helper_host, helper_port,
					battle_helper, battleServerLocation, battleServer);

			stub = (IPlayerController) UnicastRemoteObject.exportObject(player,
					0);

			Registry reg = LocateRegistry.getRegistry(helper_host, helper_port);

			reg.rebind(serverID, stub);
			System.out.println("PlayerController running, server: " + serverID
					+ ", reg: " + reg.toString());
			printRegistry(reg.list());
			player.spawnPlayer();

		} catch (RemoteException e) {
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
				System.out.println("No server online to connect to");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Not able to connect to main server");
		}

		return helper;
	}

	private static void printRegistry(String[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.println("item[" + i + "]: " + array[i]);
		}
	}
	
	private static String randomString(int len) {
		final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

}
