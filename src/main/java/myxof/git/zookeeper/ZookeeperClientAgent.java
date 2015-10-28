package myxof.git.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClientAgent extends AbstractZookeeperClient implements Watcher{
	private static final Logger logger = LoggerFactory.getLogger(ZookeeperClientAgent.class);

	private ZooKeeper zooKeeper;
	private List<String> childrenList;
	private Map<String, String> childrenMap = new HashMap<String, String>();

	/**
	 * @param zNodeName which path to save and get Map data
	 * @throws IOException 
	 */
	public ZookeeperClientAgent(String zNodeName) throws IOException {
		super();
		this.zNodeName = zNodeName;
		try {			
			zooKeeper = new ZooKeeper(serverList, sessionTimeout, this);
		} catch (IOException e) {
			logger.error("tt_common_nrsync ZookeeperClientAgent failed to create connection from server",e);
			throw e;
		}
		logger.info("tt_common_nrsync request {} tt_dbcore_nrsync initially connects ZooKeeper '{}'", 0, serverList);
	}

	/**
	 * get list at zNodeName path and its data,
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 * @throws UnsupportedEncodingException 
	 */
	public void service() throws KeeperException, InterruptedException, UnsupportedEncodingException {
		getChildrenList(true);
		updateChildrenMap(true);
	}
	
	public void close() throws InterruptedException{
		zooKeeper.close();
		if(childrenList != null)
			childrenList.clear();
		if(childrenMap != null)
			childrenMap.clear();
	}

	private void getChildrenList(boolean isWatch) throws KeeperException, InterruptedException {
		try {
			childrenList = zooKeeper.getChildren(rootPath + "/" + zNodeName,isWatch);
		} catch (KeeperException e) {
			logger.error("tt_common_nrsync ZookeeperClientAgent server signals an error when get children list at path: "+ rootPath + "/" + zNodeName, e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("tt_common_nrsync ZookeeperClientAgent server transaction is interrupted when get children list at path: " + rootPath + "/" + zNodeName, e);
			throw e;
		}
		logger.info("tt_common_nrsync request {} tt_dbcore_nrsync gets zookeeper children list", 0);
	}

	private void updateSingleChildNode(String key, String path, boolean isWatch) throws KeeperException, InterruptedException, UnsupportedEncodingException {
		try {
			byte[] data = zooKeeper.getData(path, isWatch, null);
			childrenMap.put(key, new String(data, "UTF-8"));
		} catch (KeeperException e) {
			logger.error("tt_common_nrsync ZookeeperClientAgent server signals an error when get data at path: " + path, e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("tt_common_nrsync ZookeeperClientAgent server transaction is interrupted when get data at path: " + path, e);
			throw e;
		} catch (UnsupportedEncodingException e) {
			logger.error("tt_common_nrsync ZookeeperClientAgent cannot transform byte[] to string", e);
			throw e;
		}
		logger.debug("tt_common_nrsync request {} ZookeeperClientAgent sets zookeeper node value '{}'", 0, key);
	}

	private void updateChildrenMap(boolean isWatch) throws UnsupportedEncodingException, KeeperException, InterruptedException {
		if (childrenList == null) {
			logger.error("tt_common_nrsync ZookeeperClientAgent cannot get children list from server");
			return;
		}
		for (String child : childrenList) {
			updateSingleChildNode(child, rootPath + "/" + zNodeName + "/" + child, isWatch);
		}
	}

	public String getValueFromChildrenMap(String key) {
		return childrenMap.get(key);
	}
	
	public Map<String, String> getChildrenMap(){
		return childrenMap;
	}

	@Override
	public void process(WatchedEvent event) {
		String path = event.getPath();
		String nodePath = rootPath + "/" + zNodeName;
		switch (event.getType()) {
		case None:
			switch (event.getState()) {
			case SyncConnected:
				// In this particular example we don't need to do anything
				// here - watches are automatically re-registered with
				// server and any watches triggered while the client was
				// disconnected will be delivered (in order of course)
				break;
			case Expired:
				// It's all over
				logger.warn("tt_common_nrsync ZookeeperClientAgent disconnects from server.");
				break;
			default:
				break;
			}
			break;
		case NodeChildrenChanged:
			if (path != null && path.equals(nodePath)) {
				try {
					service();
				} catch (UnsupportedEncodingException | KeeperException
						| InterruptedException e) {
					logger.error("tt_common_nrsync ZookeeperClientAgent watch fail when NodeChildrenChanged",e);
				}
			}
			break;
		case NodeCreated:
		case NodeDataChanged:
			if (path != null && path.length() > rootPath.length()) {
				String key = path.substring(nodePath.length() + 1);
				try {
					updateSingleChildNode(key, path, true);
				} catch (UnsupportedEncodingException | KeeperException
						| InterruptedException e) {
					logger.error("tt_common_nrsync ZookeeperClientAgent watch fail when NodeDataChanged",e);
				}
			}
			break;
		case NodeDeleted:
			if (path != null && path.length() > rootPath.length()) {
				String key = path.substring(nodePath.length() + 1);
				childrenMap.remove(key);
			}
			break;
		default:
			break;
		}

	}
}
