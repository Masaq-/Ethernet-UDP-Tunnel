import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

public class Main
{
	static ServerFrame sf;
	static Thread MainThread;
	static int pingvictim=-1;
	public static String appname="IPX Tunneling Server 0.11";
	private static DatagramSocket tryOpenUDPPort(int port)
	{
		DatagramSocket retval;
		try
		{
			retval=new DatagramSocket(port);
		}
		catch(Exception e)
		{
			retval=null;
		}
		return retval;
	}
	public static DatagramSocket sock;
	public static void main( String args[] ) throws Exception
	{
		int port=213;
		if(args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
				if(port>65535 || port < 1) port=213;
			} catch(Exception e) {
				port=213;
			}
		}
		
		MainThread=Thread.currentThread();
					   sock = tryOpenUDPPort(port);
		if(sock==null) sock = tryOpenUDPPort(213);
		if(sock==null) sock = tryOpenUDPPort(1213);
		if(sock==null) sock = tryOpenUDPPort(1214);
		if(sock==null) sock = tryOpenUDPPort(1215);
		if(sock==null) sock = tryOpenUDPPort(1216);
		if(sock==null) sock = tryOpenUDPPort(60000);
		if(sock==null) sock = tryOpenUDPPort(60001);
		if(sock==null) sock = tryOpenUDPPort(60002);
		if(sock==null)
		{
			
			JOptionPane.showMessageDialog(null,
				"Error: Could not open UDP Port.",
				appname,
				JOptionPane.ERROR_MESSAGE,
				null);
			System.exit(1);
		}
		
		TunnelIPXPacket.mysock=sock;
		TunnelIPXPacket tip= new TunnelIPXPacket();
		Vector clv=IPXTunnelClient.getClientVector(); 
		
		sf = new ServerFrame();
		sf.pack();
		sf.setBounds(0,0,640,200);
		sf.setVisible(true);
		sf.setDataVector(clv);
		
		//System.out.println("local addr:"+sock.getLocalSocketAddress());
		System.gc();

		while(true)
		{
			sock.receive(tip.pack);
			tip.analyze();
			if(IPXTunnelClient.receivePacket(tip))
			{
				int z=clv.size();
				IPXTunnelClient cli;
				if(tip.isBroadcast())
				{
					for(int i = 0; i < z;i++)
					{
						cli=(IPXTunnelClient)clv.elementAt(i);
						if(cli!=null)
							cli.sendPacketToMe(tip);
					}
				}
				else	// unicast
				{
					for(int i = 0; i < z;i++)
					{
						IPXTunnelClient ic = (IPXTunnelClient)
							clv.elementAt(i);
						
						if(ic!=null&&tip.dest_IPXnode==ic.myipxNodeNumber)
						{
							ic.sendPacketToMe(tip);
							break;
						}
					}	
				}
			}
			else if(tip.source_IPXsocket==2)
			{
				new IPXTunnelClient(tip);
				tip.prepare();
				sock.send(tip.pack);
				sf.updateTable();
			}
			//sf.updateTable();
		}
	}
}
 