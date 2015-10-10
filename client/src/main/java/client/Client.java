package client;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import common.Game;
import common.RunnableUnicastRemoteObject;
import common.exceptions.AllStarcraftInstanceSlotsAlreadyBusyException;
import common.file.CopyFile;
import common.file.MyFile;
import common.file.PackedFile;
import common.protocols.RemoteClient;
import common.protocols.RemoteServer;
import common.protocols.RemoteStarcraft;
import common.utils.Helper;
import common.utils.WindowsCommandTools;
import common.yaml.MyConstructor;

public class Client extends RunnableUnicastRemoteObject implements RemoteClient
{
	private static final long serialVersionUID = 3058199010003999345L;
	
	
	
	public final ClientEnvironment env;
	private RemoteServer server = null;
	private ArrayList<Starcraft> runningStarcrafts = new ArrayList<>();

	public Client(ClientEnvironment env) throws RemoteException
	{
		this.env = env;
	}
	
	@Override
	public synchronized void onRun() throws InterruptedException, IOException, AllStarcraftInstanceSlotsAlreadyBusyException
	{
		if (env.killOtherStarcraftProcessesOnStartup)
			WindowsCommandTools.killProcess("StarCraft.exe");
		if (env.addStarcraftFirewallExceptionOnStartup)
			WindowsCommandTools.RunWindowsCommand("netsh firewall add allowedprogram program = " + new File(env.starcraftDir, "starcraft.exe").getAbsolutePath() + " name = Starcraft mode = ENABLE scope = ALL", true, false);
		
		log("connecting to server '%s'", env.serverUrl);
		while (true)
		{
			server = null;
			
			for (int attempts=0; server==null; attempts++)
			{
				try
				{
					server = (RemoteServer) Naming.lookup(env.serverUrl);
					if (attempts > 0)
						log("");
				}
				catch (RemoteException | NotBoundException e)
				{
					if (attempts == 0)
					{
						if (e.getMessage().matches("Connection refused to host: .*?; nested exception is: \n\tjava.net.ConnectException: Connection refused: connect"))
							log("error: connection refused");
						else if (e instanceof NotBoundException)
							log("error: not bound %s", e.getMessage());
						else
							log("error: %s", e.getMessage());
					}
					log("retry %d failed...\r", attempts);
	
					Thread.sleep(1000);
				}
			}
			log("found server, getting files");
			
			PackedFile serverDataDir = null;
			try
			{
				serverDataDir = server.getDataDir();
			}
			catch (IOException e)
			{
				log("error getting server's data dir, reconnecting");
				e.printStackTrace();
				continue;
			}
			
			serverDataDir.syncTo(env.dataDir); //don't recover if this throws
			log("got files");
			
			MyFile extraFiles = new MyFile(env.dataDir, "extrafiles.yaml");
			if (extraFiles.exists())
			{
				String extraFilesString = FileUtils.readFileToString(extraFiles);
				Yaml yaml = new Yaml(new MyConstructor(env));
				@SuppressWarnings("unchecked")
				List<CopyFile> copyFiles = yaml.loadAs(extraFilesString, List.class);
				if (copyFiles != null)
				{
					for (CopyFile copyFile : copyFiles)
						copyFile.copyDiffering(env.lookupFile(copyFile.extractTo));
				}
			}
			else
				log("'"+extraFiles+"' not found, ignoring");
			
			try
			{
				server.connect(this);
				log("connected");
				
				while (true)
				{
					Thread.sleep((long) (env.serverAlivePollPeriod*1000));
					server.checkAlive();
				}
			}
			catch (RemoteException e)
			{
				log("server disconnected, reconnecting");
			}
			
			killAllStarcrafts();
		}
	}
	
	@Override
	public void onExit()
	{
		if (server != null)
		{
			try
			{
				server.disconnect(this); //courtesy
			}
			catch (RemoteException e)
			{
			}
		}
		
		killAllStarcrafts();
	}
	
	@Override
	public RemoteStarcraft startMatch(Game game, int index) throws RemoteException, AllStarcraftInstanceSlotsAlreadyBusyException
	{
		if (getOpenStarcraftInstanceSlotCount() <= 0)
			throw new AllStarcraftInstanceSlotsAlreadyBusyException(env.maxStarcraftInstances);
		Starcraft starcraft = new Starcraft(this, game, index);
		runningStarcrafts.add(starcraft);
		new Thread(starcraft).start();
		return starcraft;
	}

	public void onStarcraftExit(Starcraft starcraft)
	{
		runningStarcrafts.remove(starcraft);
	}
	
	private void killAllStarcrafts()
	{
		if (!runningStarcrafts.isEmpty())
		{
			for (Starcraft starcraft : new ArrayList<Starcraft>(runningStarcrafts))
				starcraft.killLocal();
			log("killed all starcraft instances");
		}
	}
	
	@Override
	public int getOpenStarcraftInstanceSlotCount()
	{
		return env.maxStarcraftInstances - runningStarcrafts.size();
	}
	
	@Override
	public void checkAlive()
	{
	}
	
	@Override
	public void kill()
	{
		log("killed by server");
		if (thread != null)
			thread.interrupt();
	}

	@Override
	public byte[] screenshot() throws RemoteException
	{
		try
		{
			Robot robot = new Robot();
		    Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		    BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
		    
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ImageIO.write(bufferedImage, "png", baos);
		    return baos.toByteArray();
		}
		catch (IOException | AWTException e)
		{
			log("error taking screenshot");
			throw new RemoteException("error taking screenshot", e);
		}
	}

	@Override
	public void executeCommand(String command)
	{
		log("server told us to execute command: " + command);
		WindowsCommandTools.RunWindowsCommandAsync(command);
	}

	public static void log(String format, Object... args)
	{
		String timeStamp = new SimpleDateFormat("[HH:mm:ss]").format(Calendar.getInstance().getTime());
		String line = timeStamp+" "+String.format(format, args);
		System.out.print(line + (line.endsWith("\r")?"":"\n"));
	}
	
	@Override
	public String endpointAddress()
	{
		return Helper.getEndpointAddress(this);
	}
	
	@Override
	public String toString()
	{
		return String.format("{Client %s}", Helper.getEndpointAddress(this));
	}
}
