package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Game;
import common.exceptions.AllStarcraftInstanceSlotsAlreadyBusyException;

public interface RemoteClient extends Remote
{
	RemoteStarcraft startMatch(Game game, int index) throws RemoteException, AllStarcraftInstanceSlotsAlreadyBusyException;
	
	void checkAlive() throws RemoteException;
	void kill() throws RemoteException;
	void executeCommand(String command) throws RemoteException;
	byte[] screenshot() throws RemoteException;
	int getOpenStarcraftInstanceSlotCount() throws RemoteException;
	String endpointAddress() throws RemoteException;
}
