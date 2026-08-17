package cv.ucc.bsuregistration;

import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import me.legrange.mikrotik.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class LeituraBSU {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		try {
			BufferedReader reader = new BufferedReader(new FileReader("IpList.txt"));
			String line;
			
			//File file = new File("registration.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter("registration.csv"));
			
			while((line = reader.readLine()) != null) {
				LeituraBSU bsu = new LeituraBSU(line);				
				
				System.out.println(bsu.toString());
				
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
	    	
	    	// Se a registration-table não devolver nenhum resultado, assume-se 0 registos.
	    	if (registration == null) {
	    		registration = "0";
	    	}
	    	
	    	if (registration.equals("[0]") || registration.equals("0")) {
	    		results = con.execute("/interface/w60g/station/print count-only");
	    		
	    		String w60gRegistration = null;
	    		for (Map<String, String> result : results) {
	    			System.out.println(result.toString());
		    		w60gRegistration = result.values().toString();
		            //System.out.println(registration);
		        }
	    		
	    		// Idem para o w60g: se vazio, mantém-se em 0.
	    		registration = (w60gRegistration != null) ? w60gRegistration : "0";
	    	}
	    	
	    	return registration;
    	} catch(Exception ex) {
			ex.printStackTrace();
			return "0";
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
