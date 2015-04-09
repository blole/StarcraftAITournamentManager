package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.yaml.snakeyaml.TypeDescription;

import common.BwapiVersion;
import common.MyFile;
import common.RequiredFile;
import common.TargetFile;

public class ServerEnvironment implements Serializable
{
	private static final long serialVersionUID = -6040955128596907730L;
	public static final TypeDescription typeDescription = new TypeDescription(ServerEnvironment.class);
	static
	{
		typeDescription.putListPropertyType("filesToCopyToClientBeforeEachNewMatch", TargetFile.class);
		typeDescription.putMapPropertyType("bwapiVersions", BwapiVersion.class, TargetFile.class);
	}
	
	
	
	public String serverUrlPath = null;
	public int port = 1099;
	
	public double pollPeriod = 1;
	public boolean killClientsOnExit = false;
	
	public List<TargetFile> filesToCopyToClientBeforeEachNewMatch = new ArrayList<>();
	public LinkedHashMap<BwapiVersion, TargetFile> bwapiVersions = new LinkedHashMap<>();
	
	public RequiredFile gameList;
	public MyFile botDir;
	public MyFile mapDir;
	public MyFile replaysDir;
	public MyFile characterFileMultiplayer;
	public MyFile characterFileSingleplayer;
	public MyFile defaultBwapiIni;
}