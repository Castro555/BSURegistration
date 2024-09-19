package cv.ucc.bsuregistration;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.net.SocketFactory;

import examples.Config;
import me.legrange.mikrotik.*;
import me.legrange.mikrotik.impl.*;
import examples.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class BSURegistration {
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		List<BSURegistration> bsus = new ArrayList<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("IpList.txt"));
			String line;
			while((line = reader.readLine()) != null) {
				BSURegistration bsu = new BSURegistration(line);
				//System.out.println(bsu.ip);
				
				if(bsu.connect(bsu.ip)) {
					bsu.registration = bsu.getRegistration();
					bsu.disconnect();
				} else {
					System.out.println("Not connected");
				}
				
				bsus.add(bsu);
				bsu = null;
			}
			
			for (BSURegistration su : bsus) {
				System.out.println(su);
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	
	
	public BSURegistration(String ip) {
		super();
		this.ip = ip;
	}

	protected boolean connect(String address) throws Exception {
        try {
        	con = ApiConnection.connect(SocketFactory.getDefault(), address, ApiConnection.DEFAULT_PORT, 2000);
        	try {
        		con.login(Config.USERNAME, Config.PASSWORD);
        	} catch(MikrotikApiException ex) {
        		ex.printStackTrace();
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
    	List<Map<String, String>> results 
    		= con.execute("/interface/wireless/registration-table/print count-only");
        
    	String registration = null;
    	for (Map<String, String> result : results) {
    		registration = result.values().toString();
            //System.out.println(registration);
        }
    	
    	return registration;
    }
    
    @Override
    public String toString(){
    	return "ip:"+this.ip + " " + "registration:"+this.registration; 
    }
    
    protected ApiConnection con;
    protected String ip;
    protected String registration;
}
