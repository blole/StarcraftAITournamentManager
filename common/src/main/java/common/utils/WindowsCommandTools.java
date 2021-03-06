package common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class WindowsCommandTools
{
	public static void killProcess(int pid)
	{
		WindowsCommandTools.RunWindowsCommand("taskkill /F /PID " + pid, true, false);
	}
	
	public static void killProcess(String processName)
	{
		while (WindowsCommandTools.IsWindowsProcessRunning(processName))
		{
			System.out.println("killing "+processName);
			WindowsCommandTools.RunWindowsCommand("taskkill /F /IM "+processName, true, false);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
		}
	}
	
	public static void KillExcessWindowsProccess(Vector<Integer> doNotKill)
	{
		if (doNotKill == null)
		{
			return;
		}
	
		Vector<Integer> running = GetRunningProcesses();
		for (int i = 0; i < running.size(); i++)
		{
			if (!(doNotKill.contains(running.get(i))))
			{
				killProcess(running.get(i));
			}
		}
	}
	
	public static Vector<Integer> GetRunningProcesses()
	{
		Vector<Integer> running = new Vector<Integer>();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec("tasklist.exe /FO LIST");

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			
			String[] splitout;
			while ((line = stdInput.readLine()) != null)
			{
				if (line.startsWith("PID:"))
				{
					splitout = line.split(":");
					if (splitout.length > 1)
					{
						running.add(Integer.parseInt(splitout[1].trim()));
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return running;
	}
	
	public static boolean IsWindowsProcessRunning(String process)
	{
		try
		{
			Process p = Runtime.getRuntime().exec("tasklist.exe");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = stdInput.readLine()) != null)
			{
				if (line.contains(process))
				{
					return true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void RunWindowsCommandAsync(String command)
	{
		try
		{
			String windowsCommandPrefix = "CMD /C ";
			String completeCommand = windowsCommandPrefix + command;
			System.out.println("\n" + completeCommand);
			Runtime.getRuntime().exec(command);
		}
		catch (Exception e)
		{
			
		}
	}
	
	public static void RunWindowsCommand(String command, boolean waitFor, boolean exitIfException)
	{
		try
		{
			String windowsCommandPrefix = "CMD /C ";
			String completeCommand = windowsCommandPrefix + command;
			System.out.println("\n" + completeCommand);
			Process proc = Runtime.getRuntime().exec(windowsCommandPrefix + command);
			
			BufferedReader pOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = pOut.readLine()) != null)
			{
				System.out.println(line);
			}
			
			if (waitFor)
			{
				proc.waitFor();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("\n\nCommandTools: Could not run command:\n" + command + "\n\n");
			
			if (exitIfException)
			{
				System.exit(-1);
			}
		}
	}
	
	public static void RunWindowsCommandAsAdmin(String command, boolean waitFor, boolean exitIfException)
	{
		try
		{
			String windowsCommandPrefix = "CMD /C runas /user:Administrator ";
			String completeCommand = windowsCommandPrefix + command;
			System.out.println("\n" + completeCommand);
			Process proc = Runtime.getRuntime().exec(windowsCommandPrefix + command);
			
			BufferedReader pOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = pOut.readLine()) != null)
			{
				System.out.println(line);
			}
			
			if (waitFor)
			{
				proc.waitFor();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("\n\nCommandTools: Could not run command:\n" + command + "\n\n");
			
			if (exitIfException)
			{
				System.exit(-1);
			}
		}
	}
}
