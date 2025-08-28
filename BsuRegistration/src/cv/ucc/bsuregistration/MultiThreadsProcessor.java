package cv.ucc.bsuregistration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadsProcessor {

	static class FileTask implements Runnable {
		private final String fileNameR;
		private final String fileNameW;
		
		public FileTask(String fileNameR, String fileNameW) {
			this.fileNameR = fileNameR;
			this.fileNameW = fileNameW;
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(fileNameR));
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileNameW));
				
				List<String> passes = new ArrayList<>();
				
				passes.add("admin");
				passes.add("");
				
				String line;
				
				while((line = reader.readLine()) != null) {
	
					BSURegistration su = new BSURegistration(line);				
					
					if (su.isOnline(su.ip)) {
						su.status = "online";
						for(String pass : passes) {
							if(su.connect(su.ip, pass)) {
								su.registration = su.getRegistration();
								if (su.registration != null) {
									su.radio_name = su.registration.split(",")[0];
									su.ssid = su.registration.split(",")[1];
								}
								System.out.println(su);
								su.disconnect();
								break;
							}
						}
					}
					
					
					writer.write(su.toString());
					writer.newLine();
					System.out.println(Thread.currentThread().getName() + " processando IP " + line);
					su = null;
				}
				
				reader.close();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void runThreads(int numParts) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(numParts);
		
		for (int i = 0; i < numParts; i++) {
			executor.submit(new FileTask("part_"+i+".txt", "registration_"+i+".txt"));
		}
		
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		System.out.println("Processamento concluído");
	}
}
