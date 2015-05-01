import java.net.*;


public class TunnelIPXPacket {
	
	public DatagramPacket pack;
	public byte[] data = new byte[2000]; 
	public InetAddress originalip;
	public int originalport;
	public static DatagramSocket mysock;
	public TunnelIPXPacket()
	{
		// data socket
		pack = new DatagramPacket(data,data.length);
	
	}
	public boolean send()
	{
		boolean retval=true;
		try{mysock.send(pack);}catch(Exception e){retval=false;};
		return retval;
	}
	public void setIPDestination(InetAddress ip, int port)
	{
		pack.setAddress(ip);
		pack.setPort(port);
	}
	
	long source_IPXnode;
	int  source_IPXnet;
	int  source_IPXsocket;
	
	long dest_IPXnode;
	int  dest_IPXnet;
	int  dest_IPXsocket;
	
	//int transportControl;
	//int checksum;
	int packetType; // 0=regular
	int length; // header + data (min 30)
	// IPX packet
	
	public void prepare()
	{
		// Checksum
		data[0]=(byte)0xff;
		data[1]=(byte)0xff;
		//	Length
		data[3]=(byte)(length&0xff);
		data[2]=(byte)((length>>8)&0xff);
		
		// packet type
		data[5]=(byte)packetType;
		
		// dest
		data[6+10+1]=(byte)(dest_IPXsocket&0xff);
		data[6+10] =(byte)((dest_IPXsocket>>8)&0xff); 
		
		data[6+0+3] = (byte)(dest_IPXnet&0xff); 
		data[6+0+2] = (byte)((dest_IPXnet>>8)&0xff);
		data[6+0+1] = (byte)((dest_IPXnet>>16)&0xff);
		data[6+0+0] = (byte)((dest_IPXnet>>24)&0xff);
		
		//data[6+0+3]=(byte)0x55;
		
		data[6+4+5] = (byte)(dest_IPXnode&0xff);
		data[6+4+4] = (byte)((dest_IPXnode>>8)&0xff);
		data[6+4+3] = (byte)((dest_IPXnode>>16)&0xff);
		data[6+4+2] = (byte)((dest_IPXnode>>24)&0xff);
		data[6+4+1] = (byte)((dest_IPXnode>>32)&0xff);
		data[6+4+0] = (byte)((dest_IPXnode>>40)&0xff);

		
		// source
		data[18+10+1]=(byte)(source_IPXsocket&0xff);
		data[18+10] =(byte)((source_IPXsocket>>8)&0xff); 
		
		data[18+0+3] = (byte)(source_IPXnet&0xff); 
		data[18+0+2] = (byte)((source_IPXnet>>8)&0xff);
		data[18+0+1] = (byte)((source_IPXnet>>16)&0xff);
		data[18+0+0] = (byte)((source_IPXnet>>24)&0xff);
		
		
		
		data[18+4+5] = (byte)(source_IPXnode&0xff);
		data[18+4+4] = (byte)((source_IPXnode>>8)&0xff);
		data[18+4+3] = (byte)((source_IPXnode>>16)&0xff);
		data[18+4+2] = (byte)((source_IPXnode>>24)&0xff);
		data[18+4+1] = (byte)((source_IPXnode>>32)&0xff);
		data[18+4+0] = (byte)((source_IPXnode>>40)&0xff);
		//pack.setData(data);
		//pack.setLength(length);
	}
	public boolean isBroadcast()
	{
		return dest_IPXnode==0x0FFFFFFFFFFFFl;
	}
	public boolean analyze()
	{
		originalip=pack.getAddress();
		originalport=pack.getPort();
		// Length
		length=data[3]&0xff;
		length|=data[2]<<8;
		if(length>data.length||length<30) return false;
		// packet type
		packetType = data[5]&0xff;
		
		// dest
		dest_IPXsocket =  data[6+10+1]&0xff;
		dest_IPXsocket |= ((data[6+10]<<8)&0xff00);
		
		dest_IPXnet = data[6+0+3]&0xff;
		dest_IPXnet |= data[6+0+2]<<8;
		dest_IPXnet |= data[6+0+1]<<16;
		dest_IPXnet |= data[6+0+0]<<24;
		
		dest_IPXnode = data[6+4+5]&0xff;
		dest_IPXnode |= (data[6+4+4]<<8)&0xff00;
		dest_IPXnode |= (data[6+4+3]<<16)&0xff0000;
		dest_IPXnode |= (long)((long)data[6+4+2]<<24)&0xff000000l;
		dest_IPXnode |= (long)((long)data[6+4+1]<<32)&0xff00000000l;
		dest_IPXnode |= (long)((long)data[6+4+0]<<40)&0xff0000000000l;
		
		
		// source
		source_IPXsocket =  data[18+10+1]&0xff;
		source_IPXsocket |= ((data[18+10]<<8)&0xff00);
		
		source_IPXnet = data[18+0+3]&0xff;
		source_IPXnet |= data[18+0+2]<<8;
		source_IPXnet |= data[18+0+1]<<16;
		source_IPXnet |= data[18+0+0]<<24;
		
		source_IPXnode = data[18+4+5]&0xff;
		
		source_IPXnode |= (data[18+4+4]<<8)&0xff00;
		source_IPXnode |= (data[18+4+3]<<16)&0xff0000;
		source_IPXnode |= (long)((long)data[18+4+2]<<24)&0xff000000;
		source_IPXnode |= (long)((long)data[18+4+1]<<32)&0xff00000000l;
		source_IPXnode |= (long)((long)data[18+4+0]<<40)&0xff0000000000l;
		
		return true;
	}
	
}
