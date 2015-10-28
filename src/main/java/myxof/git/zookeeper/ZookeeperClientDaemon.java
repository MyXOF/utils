package myxof.git.zookeeper;

import java.io.IOException;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClientDaemon<T> extends AbstractZookeeperClient implements Watcher{
	private static final Logger logger = LoggerFactory.getLogger(ZookeeperClientDaemon.class);
	private ZooKeeper zooKeeper;
	
	private JSONObject result;
	/**
	 * @param zNodeName which path to save and get Map data
	 * @throws IOException 
	 * @throws KeeperException 
	 * @throws InterruptedException 
	 */
	public ZookeeperClientDaemon() throws IOException, KeeperException, InterruptedException{
		super();	
		try {
			zooKeeper = new ZooKeeper(serverList, sessionTimeout, this);
		} catch (IOException e) {
			logger.error("ZookeeperClientDaemon: faild to create connect between consumer and ZK server", e);
			throw e;
		}
		logger.info("request {} ZookeeperClientDaemon initially connects ZooKeeper '{}'", 0, serverList);
		
		try {
			if (zooKeeper.exists(rootPath, false) == null) {
				zooKeeper.create(rootPath, "init root node".getBytes(),Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else{
				zooKeeper.setData(rootPath, "init root node".getBytes(), -1);
			}
		} catch (KeeperException e) {
			logger.error("ZookeeperClientDaemon: server signals an error when check root node's existence",e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("ZookeeperClientDaemon: server transaction is interrupted when check root node's existence",e);
			throw e;
		}	
		logger.info("request {} ZookeeperClientDaemon set root node '{}'", 0, rootPath);


	}
	
	public void setZNode(String zNodeName) throws KeeperException, InterruptedException{
		this.zNodeName = zNodeName;
		try {
			if (zooKeeper.exists(rootPath + "/" + zNodeName, false) == null) {
				zooKeeper.create(rootPath + "/" + zNodeName,"init Map node".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
			}
			else{
				zooKeeper.setData(rootPath + "/" + zNodeName, "init Map node".getBytes(), -1);
			}
		} catch (KeeperException e) {
			logger.error("ZookeeperClientDaemon: server signals an error when check zNode's existence", e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("ZookeeperClientDaemon: server transaction is interrupted when check zNode's existence",e);
			throw e;
		}
		logger.info("request {} ZookeeperClientDaemon sets zNode '{}'", 0, zNodeName);
	}
	
	/**
	 * update new Map data to zookeeper server
	 * @param mapSet new Map data
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public JSONObject uploadAllMap(Map<String, T> mapSet) throws KeeperException, InterruptedException {
		result = new JSONObject();
		for (Map.Entry<String, T> singleMap : mapSet.entrySet()) {
			uploadSingleMap(singleMap.getKey(), singleMap.getValue());
		}
		return result;
	}

	private void uploadSingleMap(String node, T data) throws KeeperException, InterruptedException {
		try {
			if (zooKeeper.exists(rootPath + "/" + zNodeName + "/" + node, false) == null) {
				zooKeeper.create(rootPath + "/" + zNodeName + "/" + node,data.toString().getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
			} else {
				zooKeeper.setData(rootPath + "/" + zNodeName + "/" + node, data.toString().getBytes(), -1);
			}
		} catch (KeeperException e) {
			logger.error("ZookeeperClientDaemon: server signals an error when operate " + node, e);
			result.put(node, "fail");
			return;
		} catch (InterruptedException e) {
			logger.error("ZookeeperClientDaemon: server transaction is interrupted when operate "+ node, e);
			result.put(node, "fail");
			return;
		}
		logger.debug("request {} ZookeeperClientDaemon updates zNode value '{}'",0 , node);
		result.put(node, "succ");
	}

	@Override
	public void process(WatchedEvent arg0) {
	}

	public void shutdown() throws InterruptedException{
		zooKeeper.close();
		logger.info("request {} ZookeeperClientDaemon closes connection from ZooKeeper '{}'",0,serverList);
	}

}