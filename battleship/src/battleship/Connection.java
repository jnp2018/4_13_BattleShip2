package battleship;

import java.net.*;
import java.io.*;


public class Connection implements Runnable
{
	private String IPAddress;
	private int port;
	private boolean connected = false;
	private Socket link;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	private volatile Object objectQueue[] = new Object[0];


	public Connection(int portNumber, int seconds) throws SocketException
	{
		port = portNumber;
		try
		{
			ServerSocket socket = new ServerSocket(port, 1);
			socket.setSoTimeout(seconds*1000);
			link = socket.accept();
			output = new ObjectOutputStream( link.getOutputStream() );
			output.flush();

			input = new ObjectInputStream( link.getInputStream() );

			IPAddress = link.getInetAddress().toString();
			connected = true;
			Thread go = new Thread(this, "ObjectQueue");
			go.setDaemon(true);
			go.start();
		}
		catch (IOException e)
		{
			e.printStackTrace(); //code to handle error here
		}
	}


	public Connection(int portNumber) throws SocketException
	{
		this(portNumber, 0);
	}


	public Connection(String address, int portNumber) throws UnknownHostException
	{
		IPAddress = address;
		port = portNumber;
		try
		{
			link = new Socket(IPAddress, port);
			output = new ObjectOutputStream( link.getOutputStream() );
			output.flush();

			input = new ObjectInputStream( link.getInputStream() );

			connected = true;
			Thread go = new Thread(this, "ObjectQueue");
			go.setDaemon(true);
			go.start();
		}
		catch (IOException e)
		{
			e.printStackTrace(); //code to handle error here
		}
	}


	public void run()
	{
		while (connected)
		{
			try
			{
				Object temp[] = new Object[objectQueue.length+1];
				for (int i=0;i<objectQueue.length;i++) temp[i]=objectQueue[i];
				temp[temp.length-1] = input.readObject();
				objectQueue = temp;

				Thread.sleep(0);		//not necessary unless something goes wrong
			}
			catch(IOException e)
			{
				e.printStackTrace();
				connected = false;
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
				connected = false;
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}


	public boolean established()
	{
		return connected;
	}


	public Object getObject()
	{
		Object obj = null;

		if (objectQueue.length>0)
		{
			obj = objectQueue[0];

			Object temp[] = new Object[objectQueue.length-1];
			for (int i=0;i<temp.length;i++) temp[i]=objectQueue[i+1];
			objectQueue = temp;
		}
		return obj;
	}


	public void sendObject(Object obj)
	{
		if (connected)
		{
			try
			{
				output.writeObject(obj);
				output.flush();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				connected = false;
				//javax.swing.JOptionPane.showMessageDialog(null, "Disconnected");
			}
		}
	}


	public int getPort()   {   return port;   }


	public String getOtherIP()
	{
		return (connected) ? IPAddress : "Not Connected";
	}


	static public String getMyIP()
	{
		try
		{
			return InetAddress.getLocalHost().toString();
		}
		catch(UnknownHostException e)
		{
			return "Unknown Host";
		}
	}


	protected void finalize()
	{
		try
		{
			output.close();
			link.close();
			super.finalize();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
}