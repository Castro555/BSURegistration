package cv.ucc.bsuregistration;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.net.SocketFactory;

import me.legrange.mikrotik.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

public class BSURegistration {
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		List<BSURegistration> bsus = new ArrayList<>();
		List<String> passes = new ArrayList<>();
		
		passes.add("TRM@Unitel@123");
		passes.add("%p#0ad1111n");
		passes.add("admin");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("IpList.txt"));
			String line;
			while((line = reader.readLine()) != null) {
				BSURegistration bsu = new BSURegistration(line);				
				
				for(String pass : passes) {
					if(bsu.connect(bsu.ip, pass)) {
						bsu.registration = bsu.getRegistration();
						bsu.disconnect();
						break;
					}
				}
				bsus.add(bsu);
				bsu = null;
			}
			
			//File file = new File("registration.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter("registration.csv"));
			for (BSURegistration su : bsus) {
				System.out.println(su);
				writer.write(su.toString());
				writer.newLine();
			}
			//writer.flush();
			writer.close();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}	
	
	public BSURegistration(String ip) {
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
    	return this.ip + "," + this.registration; 
    }
    
    protected ApiConnection con;
    protected String ip;
    protected String registration;
}
