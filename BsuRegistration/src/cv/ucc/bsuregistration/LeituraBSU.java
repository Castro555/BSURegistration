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

public class LeituraBSU {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		List<String> passes = new ArrayList<>();
		
		passes.add("TRM@Unitel@123");
		//passes.add("%p#0ad1111n");
		passes.add("admin");
		passes.add("");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("IpList.txt"));
			String line;
			
			//File file = new File("registration.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter("registration.csv"));
			
			while((line = reader.readLine()) != null) {
				LeituraBSU bsu = new LeituraBSU(line);				
				
				System.out.println(bsu.toString());
				
				for(String pass : passes) {
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
	
	public LeituraBSU(String ip) {
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
	    		= con.execute("/interface/wireless/registration-table/print count-only");
	        
	    	String registration = null;
	    	for (Map<String, String> result : results) {
	    		registration = result.values().toString();
	            //System.out.println(registration);
	        }
	    	
	    	return registration;
    	} catch(Exception ex) {
			ex.printStackTrace();
			return "";
    	}
    }
    
    @Override
    public String toString(){
    	return this.ip + "," + this.registration; 
    }
    
    protected ApiConnection con;
    protected String ip;
    protected String registration;
}
