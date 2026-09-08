package cv.ucc.bsuregistration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.SocketFactory;

import me.legrange.mikrotik.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class LeituraBSUTable {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		try {
			BufferedReader reader = new BufferedReader(new FileReader("IpList.txt"));
			String line;
			
			//File file = new File("registration.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter("registration-table.csv"));
			
			writer.write("IP,tx-rate,rx-rate\n");
			
			while((line = reader.readLine()) != null) {
				LeituraBSUTable bsu = new LeituraBSUTable(line);				
				
				for(String pass : Config.PASSWORDS) {
					if(bsu.connect(bsu.ip, pass)) {
						bsu.registration = bsu.getRegistration();
						writer.write(bsu.toString());
						writer.newLine();
						bsu.disconnect();
						break;
					}
				}
				bsu = null;
			}
			reader.close();
			writer.close();
			
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}	
	
	public LeituraBSUTable(String ip) {
		super();
		this.ip = ip;
	}

	protected boolean connect(String address, String pass) throws Exception {
        try {
        	con = ApiConnection.connect(SocketFactory.getDefault(), address, ApiConnection.DEFAULT_PORT, 2000);
        	try {
        		con.login(Config.USERNAME, pass);
        	} catch(MikrotikApiException ex) {
        		return false;
        	}
        	return true;
        } catch(ApiConnectionException ex) {        	
        	return false;
        }
    }

    protected void disconnect() throws Exception {
        con.close();
    }
    
    protected String getRegistration() throws MikrotikApiException, InterruptedException {
    	try {
	    	List<Map<String, String>> results 
	    		= con.execute("/interface/wireless/registration-table/print");
	        
	    	
	    	Set<String> wirelessTableColumns = results.get(0).keySet();
	    	
	    	String registration = "";
	 
	    		
    		for (Map<String, String> result : results) {
    			registration = registration + ip + ",";
    		   	for (String column : wirelessTableColumns) {
    		   		if (column.equals("tx-rate") || column.equals("rx-rate"))
    		   			registration = registration + result.get(column) + ",";
    		   	}
    		   	registration = registration + "\n";
	    	}
    		
    		System.out.println(registration);
    		
	    	return registration;
    	} catch(Exception ex) {
			ex.printStackTrace();
			return "0";
    	}
    }
    
    @Override
    public String toString(){
    	return this.registration; 
    }
    
    protected ApiConnection con;
    protected String ip;
    protected String registration;
}
