package myxof.git.zookeeper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractZookeeperClient {
	private String CONF_FILE_PATH = "/nrsync.properties";
	protected String serverList;
	protected String rootPath;
	protected String zNodeName;
	protected int sessionTimeout;

	private static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);

	public AbstractZookeeperClient() throws IOException {
		readConfig();
	}

	private void readConfig() throws IOException {
		Properties props = new Properties();
		try (InputStream in = AbstractZookeeperClient.class.getResourceAsStream(CONF_FILE_PATH)) {
			props.load(in);
			serverList = props.getProperty("zookeeper_serverList");
			rootPath = props.getProperty("zookeeper_rootPath");
			sessionTimeout = Integer.parseInt(props.getProperty("zookeeper_sessionTimeout"));
		} catch (FileNotFoundException e) {
			logger.error("tt_common_nrsync Zookeeper config file :"+CONF_FILE_PATH+" does not exisits!", e);
			throw e;
		} catch (IOException e) {
			logger.error("tt_common_nrsync Zookeeper fails to load configuration in "+CONF_FILE_PATH, e);
			throw e;
		}
		logger.debug("tt_common_nrsync Zookeeper read config finish");
	}
}
