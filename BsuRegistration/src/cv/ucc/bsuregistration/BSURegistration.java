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

public class BSURegistration {
	
	public static void main(String[] args) throws Exception {

		String inputFile = "IpList.txt";
		int numParts = 128;
		
		FileManagement.splitFile(inputFile, numParts);
		// MultiThreadsProcessor.runThreads(numParts);
		FileManagement.mergeFile(numParts);

	}	
	
	public BSURegistration(String ip) {
		super();
		this.ip = ip;
		this.status = "offline";
	}

	public boolean isOnline(String address) throws Exception {
		try {
			con = ApiConnection.connect(SocketFactory.getDefault(), address, ApiConnection.DEFAULT_PORT, 2000);
			return true;
		} catch(ApiConnectionException ex) {        	
        	return false;
        }
	}
	
	public boolean connect(String address, String pass) throws Exception {
		try {
    		con.login(Config.USERNAME, pass);
    	} catch(MikrotikApiException ex) {
    		return false;
    	}
    	return true;
    }

	public void disconnect() throws Exception {
        con.close();
    }
    
	public String getRegistration() throws MikrotikApiException, InterruptedException {
    	List<Map<String, String>> results = con.execute("/interface/wireless/print");
        
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
    		
    		registration = radio_name + "," + ssid;
        }
    	
    	return registration;
    }
    
    @Override
    public String toString(){
    	return this.ip + "," + this.ssid + "," + this.radio_name + "," + this.status;
    }
    
    protected ApiConnection con;
    protected String ip;
    protected String registration;
    protected String ssid;
    protected String radio_name;
    protected String status;
}
