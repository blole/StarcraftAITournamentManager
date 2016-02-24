package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

import common.file.CopyFile;
import common.file.MyFile;
import common.yaml.MyConstructor;

public class Bot implements Serializable
{
	private static final long serialVersionUID = 2690734629985126222L;
	public static final TypeDescription typeDescription = new TypeDescription(Bot.class, "!bot");
	static
	{
		typeDescription.putListPropertyType("extraFiles", CopyFile.class);
		typeDescription.putMapPropertyType("env", String.class, String.class);
	}
	
	
	
	public final String name;
	public final Race race;
	public final BotExecutableType type;
	public final BwapiVersion bwapiVersion;
	public final List<CopyFile> extraFiles;
	public final LinkedHashMap<String,String> environmentVariables;
	
	private Bot() //dummy constructor for yaml
	{
		name = null;
		race = null;
		type = null;
		bwapiVersion = null;
		extraFiles = new ArrayList<CopyFile>();
		environmentVariables = new LinkedHashMap<String,String>();
	}
	
	public static Bot load(CommonEnvironment env, String name) throws IOException
	{
		MyFile dir = new MyFile(env.botDir(), name);
		if (!dir.exists())
			throw new FileNotFoundException("Bot directory '"+dir+"' does not exist");
		Yaml yaml = new Yaml(new MyConstructor(env));
		String yamlData = FileUtils.readFileToString(new File(dir, "bot.yaml"));
		return yaml.loadAs(yamlData, Bot.class);
	}
	
	
	
	@Override
	public String toString()
	{
		return String.format("{Bot %s}", name);
	}
	
	public MyFile getDir(CommonEnvironment env)
	{
		return new MyFile(env.botDir(), name);
	}

	public MyFile getReadDir(CommonEnvironment env)
	{
		return new MyFile(getDir(env), "read");
	}

	public MyFile getWriteDir(CommonEnvironment env)
	{
		return new MyFile(getDir(env), "write");
	}

	public MyFile getDll(CommonEnvironment env)
	{
		return new MyFile(getDir(env), name+".dll");
	}

	public String displayName()
	{
		return name;
	}
}
