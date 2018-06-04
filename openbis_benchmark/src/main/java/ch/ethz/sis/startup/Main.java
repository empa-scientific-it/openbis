package ch.ethz.sis.startup;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.BenchmarkConfig;
import ch.ethz.sis.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;
import ch.ethz.sis.logging.log4j.Log4J2LogFactory;

public class Main
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    private static Logger logger = LogManager.getLogger(Main.class);
    
    public static void main(String[] args) throws Exception
    {
    		logger.info("Current Workspace: " + (new File("").getAbsolutePath()));
    		
        File configFile;
        if (args.length < 1)
        {
            configFile = new File("./conf/config.json");
            if(configFile.exists()) {
            		logger.info("No arguments given, starting with default config file: " + (configFile.getAbsolutePath()));
            } else {
            		configFile = new File("./config.json");
            		if(configFile.exists()) {
                		logger.info("No arguments given, starting with default config file: " + (configFile.getAbsolutePath()));
                }
            }
        } else
        {
            configFile = new File(args[0]);
        }

        BenchmarkConfig[] benchmarkConfigs = JacksonObjectMapper.getInstance().readValue(new FileInputStream(configFile), BenchmarkConfig[].class);
        for(BenchmarkConfig benchmarkConfig:benchmarkConfigs) { // For each benchmark
        		logger.traceAccess(null, benchmarkConfig);
        		long start = System.currentTimeMillis();
        		List<BenchmarkThread> threadsToJoin = new ArrayList<>();
        		for(int t = 0; t < benchmarkConfig.getThreads(); t++) {
        			BenchmarkThread thread = new BenchmarkThread(benchmarkConfig) {
        				public void run() {
        					Benchmark benchmark = null;
        					try {
        						benchmark = (Benchmark) Class.forName(benchmarkConfig.getClassName()).newInstance();
						} catch (Exception e) {
							e.printStackTrace();
						}
        					benchmark.setConfiguration(benchmarkConfig);
        	        			benchmark.start();
        				}
        			};
        			threadsToJoin.add(thread);
        			thread.start();
        		}
                
        		for(BenchmarkThread thread:threadsToJoin) {
        			thread.join();
        		}
        		long end = System.currentTimeMillis();
        		logger.traceExit(benchmarkConfig);
        		logger.info("Benchmark took: " + (end-start) + " millis");
        }
    }
    
    static private abstract class BenchmarkThread extends Thread {
    		private final BenchmarkConfig benchmarkConfig;
    		
    		public BenchmarkThread(BenchmarkConfig benchmarkConfig) {
    			super();
    			this.benchmarkConfig = benchmarkConfig;
    		}
    }
}
