package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import common.RunnableWithShutdownHook;
import common.yaml.MyConstructor;


public class ServerMain
{
	public static void main(String[] args) throws RemoteException, FileNotFoundException, IOException
	{
		if (args.length < 1)
		{
			System.err.println("usage: java -jar server.jar SETTINGS.YAML");
			System.exit(-1);
		}
		else
		{
			String environmentText = FileUtils.readFileToString(new File(args[0]));
			Yaml yaml = new Yaml(new MyConstructor());
			ServerEnvironment env = yaml.loadAs(environmentText, ServerEnvironment.class);
			
			LocateRegistry.createRegistry(env.port);
			
			Server server = new Server(env);
			RunnableWithShutdownHook.addShutdownHook(server);
			server.run();
		}
	}
}