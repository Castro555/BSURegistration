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

public class LeituraBSUether {

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
			
			writer.write("IP,Interface,Status,Rate,Full-Duplex\n");
			
			while((line = reader.readLine()) != null) {
				LeituraBSUether bsu = new LeituraBSUether(line);				
				
				for(String pass : passes) {
					if(bsu.connect(bsu.ip, pass)) {
						bsu.registration = bsu.getRegistration();
						System.out.println(bsu.toString());
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
	
	public LeituraBSUether(String ip) {
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
	    	List<Map<String, String>> results = con.execute("/interface/ethernet/monitor numbers=0 once");
	        
	    	String name = null;
	    	String status = null;
	    	String rate = null;
	    	String full_duplex = null;
	    	
	    	for (Map<String, String> result : results) {
	    		
	    		if(result.get("name") != null) {
	    			name = result.get("name");
	    		}
	    		if(result.get("status") != null) {
	    			status = result.get("status");
	    		}
	    		if(result.get("rate") != null) {
	    			rate = result.get("rate");
	    		}
	    		if(result.get("full-duplex") != null) {
	    			full_duplex = result.get("full-duplex");
	    		}
	        }
	    	
	    	
	    	if (rate == null) {
	    		results = con.execute("/interface/ethernet/monitor numbers=1 once");
	    		
	    		for (Map<String, String> result : results) {
		    		
	    			if(result.get("name") != null) {
		    			name = result.get("name");
		    		}
		    		if(result.get("status") != null) {
		    			status = result.get("status");
		    		}
		    		if(result.get("rate") != null) {
		    			rate = result.get("rate");
		    		}
		    		if(result.get("full-duplex") != null) {
		    			full_duplex = result.get("full-duplex");
		    		}
		        }
	    	}
	    	
	    	//System.out.println(status+","+rate+","+full_duplex);
	    	return name+","+status+","+rate+","+full_duplex;
	    	
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
