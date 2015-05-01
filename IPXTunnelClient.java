import java.net.*;
import java.util.*;

public class IPXTunnelClient
{	// IP
	public InetAddress myip; 		// client IP as seen by the server
	public int myport;		 		// client IP port as seen by the server
	// IPX
	public long myipxNodeNumber;	// IPX address as assigned by the server
	public int myipxNetNumber;
	// stats
	public int broadcastPacketsSent=0;
	public int broadcastPacketsReceived=0;
	public int unicastPacketsSent=0;
	public int unicastPacketsReceived=0;
	public int trafficRX = 0;
	public int trafficTX = 0;
	public String gamedescription="";
	//private int lastBroadcastPort=0;
	
	// ping
	private long lastPingSent;
	private boolean ping_waiting=false;
	private boolean ping_late=false;
	private long lateping;
	private long lastPingReceived;
	public int maxPing;
	public int minPing;
	public int currentPing;
	final static private int timeout=120000;
	private long lastRXThroughputRequest;
	private int lastRXThroughputNumber;
	private long lastTXThroughputRequest;
	private int lastTXThroughputNumber;
	
	public int getLateValue()
	{
		if(ping_waiting!=ping_late)
		{
			if(ping_waiting)
			{
				ping_late=true;
				lateping=System.currentTimeMillis();
				return timeout;
			}
			else // got a ping in time, reset
			{
				ping_late=false;
				return timeout;
			}
		}
		else
		{
			if(ping_late)
			{
				return (int)(timeout-(System.currentTimeMillis()-lateping));
			}
			else return timeout;
		}
		
	}

	public float getRXThroughput()
	{
		float retval;
		long current=System.currentTimeMillis();
		long timeMillis=current-lastRXThroughputRequest;
		int diff=trafficRX-lastRXThroughputNumber;
		
		if(timeMillis!=0)retval=(float)diff/(float)timeMillis;
		else retval=0;
	
		lastRXThroughputNumber=trafficRX;
		lastRXThroughputRequest=current;
		return retval;
	}
	public float getTXThroughput()
	{
		float retval;
		long current=System.currentTimeMillis();
		long timeMillis=current-lastTXThroughputRequest;
		int diff=trafficTX-lastTXThroughputNumber;
		if(timeMillis!=0)retval=(float)diff/(float)timeMillis;
		else retval=0;
		
		lastTXThroughputNumber=trafficTX;
		lastTXThroughputRequest=current;
		return retval;
	}
	
	private static TunnelIPXPacket pingpacket= new TunnelIPXPacket();
	static
	{
		pingpacket.source_IPXnet	= 1;
		pingpacket.source_IPXsocket	= 2;
		pingpacket.packetType		= 2;
		pingpacket.source_IPXnode	= 0xACDE48000000l;
		
		pingpacket.dest_IPXnet		= 1;
		pingpacket.dest_IPXsocket	= 2;
		pingpacket.dest_IPXnode		= 0xffffffffffffl;
		pingpacket.length			= 60;
		pingpacket.pack.setLength(60);
		pingpacket.prepare();
	}
		
	// Storage of IPXTunnelClients
	private static Vector clientVector = new Vector();
	
	static Vector getClientVector(){return clientVector;}
	
	static synchronized void pingall()
	{
		int i = clientVector.size();
		for(i--; i >=0; i--)
		{
			IPXTunnelClient c = (IPXTunnelClient)clientVector.elementAt(i);
			if((System.currentTimeMillis()-c.lastPingReceived)>timeout)
				c.disconnect();
			else c.sendPing();
		}
	}
	
	public synchronized boolean disconnect() {
		return clientVector.remove(this);}
	
	public synchronized void sendPing()
	{
		pingpacket.setIPDestination(myip, myport);
		lastPingSent=System.currentTimeMillis();
		ping_waiting=true;
		pingpacket.send();
	}
	
	
	// false: unknown packet
	static boolean receivePacket(TunnelIPXPacket tip)
	{
		int z = clientVector.size();
		IPXTunnelClient c;
		for(int i = 0; i < z; i++)
		{
			c=(IPXTunnelClient)clientVector.elementAt(i);
			if(c!=null)
				if(c.isMyPacket(tip)) return true;
		}
		// System.out.println("unknown packet");
		return false;
	}
	public boolean isMyPacket(TunnelIPXPacket tip)
	{
		if(tip.pack.getAddress().equals(this.myip))
			if(tip.pack.getPort()==this.myport)
			{
				if(tip.dest_IPXnode==0xACDE48000000l && ping_waiting)
				{
					lastPingReceived=System.currentTimeMillis();
					currentPing = (int)(lastPingReceived-lastPingSent);
					if(minPing>currentPing)minPing=currentPing;
					if(maxPing<currentPing)maxPing=currentPing;
					ping_waiting=false;
					//System.out.println("ping:"+l);
					return true;
				}
				
				if(tip.isBroadcast())
				{
					broadcastPacketsSent++;
					//if(lastBroadcastPort!=tip.dest_IPXsocket)//TODO: filter "IPX RIP REQUEST"
					analyzePacket(tip);
				}
				else unicastPacketsSent++;
				trafficTX+=tip.length;
				return true;
			}
				return false; // TODO: check IPX part
	}
	
	public IPXTunnelClient(TunnelIPXPacket tip)
	{
		// make address
		tip.dest_IPXnet=1;
		tip.dest_IPXnode=((long)(tip.pack.getAddress().hashCode())<<16)
			&0xffffffffffffl;
		tip.dest_IPXnode|=(tip.pack.getPort());
		
		tip.prepare();
		// set variables	
		myport=tip.pack.getPort();
		myip=tip.pack.getAddress();
		myipxNodeNumber = tip.dest_IPXnode;
		myipxNetNumber = tip.dest_IPXnet;
		clientVector.add(this);
		lastPingReceived=System.currentTimeMillis();
		maxPing=0;
		minPing=99999;
		currentPing=0;
	}
	
	public void sendPacketToMe(TunnelIPXPacket tip)
	{
		DatagramPacket dp = tip.pack;
		
		if(!myip.equals(tip.originalip)||myport!=tip.originalport)
		{
			if(tip.isBroadcast()) broadcastPacketsReceived++;
			else unicastPacketsReceived++;
			trafficRX+=tip.length;
			tip.setIPDestination(myip, myport);		
			tip.send();
		}
	}
	int locktype=0;
	final int Z_LOCK = 1;
	final int RADIX2_LOCK=2;
	final int RADIXDEMO_LOCK=3;
	
	public void analyzePacket(TunnelIPXPacket tip)
	{
		boolean unknown=false;
		byte[] packetdata=tip.data;
		
		switch(tip.dest_IPXsocket)
		{
		case 0:
		{
			return;
		}
		case 0x769d: // Radix
		{
			if(//4e 45 55 52 41 4c = NEURAL
				packetdata[30+0]==0x4e &&
				packetdata[30+1]==0x45 &&
				packetdata[30+2]==0x55 &&
				packetdata[30+3]==0x52 &&
				packetdata[30+4]==0x41 &&
				packetdata[30+5]==0x4c
				)
			{
				if(// 34 2e 32 2e 33 
					packetdata[30+21]==0x34 &&
					packetdata[30+22]==0x2e &&
					packetdata[30+23]==0x32 &&
					packetdata[30+24]==0x2e &&
					packetdata[30+25]==0x33
					)
				{
					gamedescription="Radix 2.0";
		//			lastBroadcastPort=tip.dest_IPXsocket;
					locktype=RADIX2_LOCK;
				}
				else if(// 33 2e 31 2e 36
						packetdata[30+21]==0x33 &&
						packetdata[30+22]==0x2e &&
						packetdata[30+23]==0x31 &&
						packetdata[30+24]==0x2e &&
						packetdata[30+25]==0x36
						)
				{
					gamedescription="Radix Shareware";
		//			lastBroadcastPort=tip.dest_IPXsocket;
					locktype=RADIXDEMO_LOCK;
				}
				else 
				{
					if(!(locktype==RADIXDEMO_LOCK||locktype==RADIX2_LOCK))
						unknown=true;
				}
				
			} else
			{
				if(!(locktype==RADIXDEMO_LOCK||locktype==RADIX2_LOCK))
				unknown=true;
			}
			break;
		}
		case 0x4545: // Z
		{
			
			if(	packetdata[30+4]==0x01 &&
				packetdata[30+5]==0x00 &&
				packetdata[30+6]==0x16 &&
				packetdata[30+7]==0x00    )
			{
				if(packetdata[30+8]>=0x02&&packetdata[30+8]<=0x04)
				{	
					gamedescription="Z "+packetdata[30+8]+" Players";
		//			lastBroadcastPort=tip.dest_IPXsocket;
					locktype=Z_LOCK;
				}
				else
				{
					if(!(locktype==Z_LOCK))unknown=true;
				}
			} if(!(locktype==Z_LOCK))unknown=true;
			break;
		}
		case 0x3d: // Epic Challenge Arena
		{/*
			if(
				packetdata[30+0]==0x4e &&
				packetdata[30+1]==0x40 &&
				packetdata[30+2]==0xd0 &&
				packetdata[30+3]==0x3a		
			)
			{
				lastBroadcastPort=0xffff;
				gamedescription="Tyrian 2.0";
			}
			else if(
				packetdata[30+0]==0x09 &&
				packetdata[30+1]==0x00 &&
				packetdata[30+2]==0x64 &&
				packetdata[30+3]==0x00	
			)
			{
				lastBroadcastPort=0xffff;
				gamedescription="OneMustFall 2.1";
			}
			else*/
			{
				gamedescription="Epic Challenge Arena";
	//			lastBroadcastPort=0x3d;
			}
			//lastBroadcastPort=0xffff; // check again
			break;
		}
		case 0x455:
		{
			gamedescription="Netbios";
	//		lastBroadcastPort=tip.dest_IPXsocket;
			break;
		}
		default: unknown = true;
		}
		if(unknown==true)
		{
			gamedescription="Socket "+Integer.toHexString(tip.dest_IPXsocket);
	//		lastBroadcastPort=tip.dest_IPXsocket;
			locktype=0;
		}
	}
}
