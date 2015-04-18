package common;

import java.io.File;
import java.io.Serializable;

import org.yaml.snakeyaml.TypeDescription;

import common.file.MyFile;

public class Map implements Serializable
{
	private static final long serialVersionUID = 8525996517284059304L;
	public static final TypeDescription typeDescription = new TypeDescription(Map.class, "!map");
	
	
	
	public final String path;
	
	
	public Map(String path)
	{
		this.path = path;
	}
	
	public String getName()
	{
		return new File(path).getName();
	}

	@Override
	public String toString()
	{
		return String.format("{Map:%s}", getName());
	}
	
	public File getFile(File mapDir)
	{
		return new MyFile(mapDir, path);
	}
}