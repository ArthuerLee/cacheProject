package com.hualife.foundation.component.cache;

import java.util.List;

import com.hualife.foundation.component.cache.config.model.SerializeType;
import com.hualife.foundation.component.cache.task.ChangeMessage;

public interface RemoteCache<K, V> extends Cache<K, V>{
	/**
	 * 检查是否具备同步数据的权限
	 * @return
	 */
	public boolean checkSynPrevilige();
	/**
	 * 获取同步数据的权限
	 * @return
	 */
	public boolean grabSynPrevilige();
	/**
	 * 刷新同步数据状态
	 * @return
	 */
	public boolean refreshSynStatus();
	/**
	 * 检查切换同步数据权限的条件
	 * @return
	 */
	public boolean checkSwitchSynCondition();
	/**
	 * 添加数据变更消息到远程缓存队列
	 * @param message
	 */
	public void addChangedMessage(ChangeMessage<K, V> message);
	/**
	 * 获取数据变更消息。
	 * 		如果消息队列大于1000，则取1000个变更
	 * 		如果消息队列小于1000，则全部获取
	 * @return
	 */
	public List<ChangeMessage<K,V>> getChangedMessages(); 
	
	/**
	 * 是否是主节点：
	 * 		每个Cache选主过程，相互独立。
	 * @return
	 */
	public boolean isMaster();
	
	public void setMaster(boolean master);
	
	public RemoteCache<String, String> getSystemCache();
	
	boolean expiry(K key , int seconds) throws CacheException;
	
	public SerializeType getSerializeType();
}
