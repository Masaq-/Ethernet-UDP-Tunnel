import javax.swing.table.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

import javax.swing.JLabel;
public class ServerFrame
extends JFrame
implements ActionListener, WindowListener
{	
	private int pingdelay=0;
	public void actionPerformed(ActionEvent arg0)
	{
		if(arg0.getSource()==t)
		{
			pingdelay++;
			if(pingdelay>2)
			{
				IPXTunnelClient.pingall();
				pingdelay=0;
			}
			updateTable();
			
		}
		
		else if(arg0.getActionCommand().equals("Exit"))
				System.exit(1);
		else if(arg0.getActionCommand().equals("Disconnect"))
		{
			disconnect();
			updateTable();//tryUpdate();
		}
		else if(arg0.getActionCommand().equals("Ping"))
		{
			ping();
		}
				
		
		//System.out.println("action: "+arg0.getActionCommand());
	}
	public void windowDeiconified(WindowEvent arg0) {
		//System.out.println("windowDeiconified");
	}
	public void windowActivated(WindowEvent arg0) {
		updateTable();
		//System.out.println("windowActivated");
	}
	public void windowOpened(WindowEvent arg0) {
		//System.out.println("windowOpened");
	}
	public void windowIconified(WindowEvent arg0) {
		//System.out.println("windowIconified");
	}
	public void windowClosing(WindowEvent arg0) {
		System.exit(0);
		//System.out.println("windowClosing");
	}
	public void windowDeactivated(WindowEvent arg0) {
		
		//System.out.println("windowDeactivated");
	}
	public void windowClosed(WindowEvent arg0) {
		//System.out.println("windowClosed");
	}
	private javax.swing.Timer t;
	
	//private boolean updatetimerrunning=false;
	/*public synchronized void tryUpdate()
	{
		if(!updatetimerrunning)
		{
			updatetimerrunning=true;
			t.start();
			
		}
	}*/
	
	
	private javax.swing.JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JButton PingButton = null;
	private JButton DiscButton = null;
	private JTable ClientTable = null;
	private JScrollPane jScrollPane = null;
	private JButton ExitButton = null;
	/**
	 * This is the default constructor
	 */
	public ServerFrame() {
		
		super();
		initialize();
		t = new javax.swing.Timer(3000,this);
		t.setRepeats(true);
		t.start();
		
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setBounds(0, 0, 600, 100);
		this.setContentPane(getJContentPane());
		this.setTitle(Main.appname);
		this.addWindowListener(this);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 1;
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.weighty = 1.0;
			gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
			jContentPane.add(getJPanel(), gridBagConstraints8);
			jContentPane.add(getJScrollPane(), gridBagConstraints9);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel = new JLabel();
			jPanel = new JPanel();
			jLabel.setText("UDP Port: "+Main.sock.getLocalPort());
			jPanel.add(jLabel, null);
			jPanel.add(getPingButton(), null);
			jPanel.add(getDiscButton(), null);
			jPanel.add(getExitButton(), null);
		}
		return jPanel;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getPingButton() {
		if (PingButton == null) {
			PingButton = new JButton();
			PingButton.setName("Ping");
			PingButton.setText("Ping");
			PingButton.addActionListener(this);
		}
		return PingButton;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getDiscButton() {
		if (DiscButton == null) {
			DiscButton = new JButton();
			DiscButton.setText("Disconnect");
			DiscButton.addActionListener(this);
		}
		return DiscButton;
	}
	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */ 
	public synchronized void ping()
	{
		int z = ClientTable.getSelectedRow();
		if(z>=0 && (tabledatavector.size()-1>=z))
		{
			//System.out.println(
		((IPXTunnelClient)tabledatavector.elementAt(z)).sendPing();
		}
	}
	
	public synchronized void disconnect()
	{
		
		//while(ClientTable.getSelectedRow()>=0)
		{
			int z = ClientTable.getSelectedRow();
			if(z>=0 && (tabledatavector.size()-1>=z))
			{
				//System.out.println(
			((IPXTunnelClient)tabledatavector.elementAt(z)).disconnect();
			}
		//);
		}
		ClientTable.updateUI();
	}
	
	private TableModel model;
	private static Vector tabledatavector;
	DecimalFormat df1 = new DecimalFormat("###0.00");
	public void setDataVector(Vector v)
	{
		tabledatavector=v;
	}
	
	final String[] tableheaders=new String[]
		{"Ether","IP","UDP","Broadcast",
		 "Unicast","Traffic","Traffic kbyte/s","Activity",
		 "PingMin","Timeout","PingNow"};
	private JLabel jLabel = null;
	private JTable getClientTable() {
		if (ClientTable == null)
		{
			model = new AbstractTableModel()
			{
			    public int getRowCount()
			    {
			    	if(ServerFrame.tabledatavector!=null)
			    		return ServerFrame.tabledatavector.size();
			    	else return 0;
			    }
			    public int getColumnCount () { return tableheaders.length; }
			    public Object getValueAt (int row, int column)
			    {
			    	IPXTunnelClient c = (IPXTunnelClient)ServerFrame.tabledatavector.elementAt(row);
			    	if(c!=null)
			    	{
			    		switch(column)
						{
			    			case 0:
			    				return Integer.toHexString(c.myipxNetNumber)+
									":"+Long.toHexString(c.myipxNodeNumber);
			    			case 1:
			    				String s = c.myip.toString();
			    				s=s.substring(1,s.length());
			    				return s;
			    			case 2:
			    				return Integer.toString(c.myport);
			    			case 3: // broadcast
			    				return "TX:"+
									Integer.toString(c.broadcastPacketsSent)
									+" RX:"+
									Integer.toString(c.broadcastPacketsReceived);
			    			case 4: // unicast
			    				return "TX:"+
									Integer.toString(c.unicastPacketsSent)
									+" RX:"+
									Integer.toString(c.unicastPacketsReceived);
			    			case 5: 
			    				return "TX:"+
								Integer.toString(c.trafficTX)
								+" RX:"+
								Integer.toString(c.trafficRX);
			    			case 6:
			    				return "RX:"+df1.format(c.getRXThroughput())+
			    				" TX:"+df1.format(c.getTXThroughput());
			    			case 7:
			    				return c.gamedescription;
			    			case 8:
			    				return Integer.toString(c.minPing);
			    			case 9:
			    				return Integer.toString(c.getLateValue()/1000);//c.maxPing);
			    			case 10:
			    				return Integer.toString(c.currentPing);
			    						    				
			    			default: return "unimplemented";
						}
			    	}
			    	else return "--";
			    }
			    
			    public String getColumnName (int column)
			    {
			    	return tableheaders[column];
			    }
			  };
			
			
			ClientTable = new JTable(model);
			ClientTable.setColumnSelectionAllowed(false);
			ClientTable.getSelectionModel().
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
		}
		return ClientTable;
	}
	public synchronized void updateTable()
	{
		ClientTable.updateUI();
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getClientTable());
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getExitButton() {
		if (ExitButton == null) {
			ExitButton = new JButton();
			ExitButton.setText("Exit");
			ExitButton.addActionListener(this);
		}
		return ExitButton;
	}
  }  //  @jve:decl-index=0:visual-constraint="71,50"
