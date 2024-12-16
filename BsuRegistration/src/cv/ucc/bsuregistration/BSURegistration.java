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
		
		//passes.add("TRM@Unitel@123");
		passes.add("admin");
		passes.add("");
		int counter = 0;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("IpList.txt"));
			String line;
			while((line = reader.readLine()) != null) {
				BSURegistration bsu = new BSURegistration(line);				
				
				for(String pass : passes) {
					if(bsu.connect(bsu.ip, pass)) {
						bsu.registration = bsu.getRegistration();
						if (bsu.registration != null) {
							bsu.radio_name = bsu.registration.split(",")[0];
							bsu.ssid = bsu.registration.split(",")[1];
						}
						System.out.println(bsu);
						bsu.disconnect();
						break;
					}
				}
				bsus.add(bsu);
				bsu = null;
				counter++;
			}
			
			//File file = new File("registration.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter("registration.csv"));
			for (BSURegistration su : bsus) {
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
    		= con.execute("/interface/wireless/print");
        
    	String registration = null;
    	String ssid = null;
    	String radio_name = null;
    	for (Map<String, String> result : results) {
    		if(result.get("radio-name") != null) {
    			radio_name = result.get("radio-name");
    		}
    		if(result.get("ssid") != null) {
    			ssid = result.get("ssid");
    		}
    		
    		//System.out.println(result);
    		registration = radio_name + "," + ssid;
            //System.out.println(registration);
        }
    	
    	//System.out.println(radio_name);
    	//System.out.println(ssid);
    	//sSystem.out.println(registration);
    	
    	return registration;
    }
    
    @Override
    public String toString(){
    	return this.ip + "," + this.ssid + "," + this.radio_name;
    }
    
    protected ApiConnection con;
    protected String ip;
    protected String registration;
    protected String ssid;
    protected String radio_name;
}
