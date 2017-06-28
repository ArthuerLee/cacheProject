/**
 *  Copyright (c) 2011 Terracotta, Inc.
 *  Copyright (c) 2011 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package com.hualife.foundation.component.cache.config;

import java.util.concurrent.TimeUnit;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.InvalidConfigurationException;
import com.hualife.foundation.component.cache.config.model.CacheListener;
import com.hualife.foundation.component.cache.config.model.CacheLoader;
import com.hualife.foundation.component.cache.config.model.CacheWriter;
import com.hualife.foundation.component.cache.config.model.SerializeType;
import com.hualife.foundation.component.cache.transaction.IsolationLevel;
import com.hualife.foundation.component.cache.transaction.Mode;
import com.hualife.foundation.component.common.ComponentConfiguration;

/**
 * Information on how a cache is configured.
 * <p/>
 * A Cache may be constructed by {@link CacheManager} using a configuration instance.
 * <p/>
 * At runtime it is used by javax.cache to decide how to behave. For example the behaviour of put
 * will vary depending on whether the cache is write-through.
 * <p/>
 * Finally, a cache makes it's configuration visible via this interface.
 *
 * Only those configurations which can be changed at runtime (if supported by the underlying implementation)
 * have setters in this interface. Those that can only be set prior to cache construction have setters in
 * {@link CacheBuilder}.
 *
 * @author Greg Luck
 * @author Yannis Cosmadopoulos
 * @since 1.0
 */
public interface CacheConfiguration extends ComponentConfiguration{
	/**
	 * Get Cache Name.
	 * @return cache name
	 */
	String getName();
	
	String getCacheProvider();
	
	CacheListener getCacheEntryCreatedListener();
	void setCacheEntryCreatedListener(CacheListener listener);
	
	CacheListener getCacheEntryRemovedListener();
	void setCacheEntryRemovedListener(CacheListener listener);
	
	CacheListener getCacheEntryUpdatedListener();
	void setCacheEntryUpdatedListener(CacheListener listener);
	
	CacheListener getCacheEntryReadListener();
	void setCacheEntryReadListner(CacheListener listener);
	
	CacheListener getCacheEntryExpiredListener();
	void setCacheEntryExpiredListener(CacheListener listener);
	
    CacheLoader getCacheLoader();
    void setCacheLoader(CacheLoader loader);
    
    CacheWriter getCacheWriter();
    void setCacheWriter(CacheWriter writer);
    
//    CacheShareScope getCacheShareScope();
//    void setCacheShareScope(CacheShareScope scope);
//    
//    Set<CacheShareOperation> getCacheShareOperations();
//    void setCacheShareOperations(Set<CacheShareOperation> operations);
    

    /**
     * Whether storeByValue (true) or storeByReference (false).
     * When true, both keys and values are stored by value.
     * <p/>
     * When false, both keys and values are stored by reference.
     * Caches stored by reference are capable of mutation by any threads holding
     * the reference. The effects are:
     * <ul>
     * <li>if the key is mutated, then the key may not be retrievable or removable</li>
     * <li>if the value is mutated, then any threads holding references will see the changes</li>
     * </ul>
     * Storage by reference only applies to local heap. If an entry is moved outside local heap it will
     * need to be transformed into a representation. Any mutations that occur after transformation will
     * not be reflected in the cache.
     * <p/>
     * Default value is true.
     *
     * @return true if the cache is store by value
     */
    boolean isStoreByValue();
    
    boolean isRestartWithClearData();
    
    SerializeType getSerializeType();

    /**
     * Checks whether statistics collection is enabled in this cache.
     * <p/>
     * Default value is false.
     *
     * @return true if statistics collection is enabled
     */
    boolean isStatisticsEnabled();

    /**
     * Sets whether statistics gathering is enabled  on this cache.
     *
     * @param enableStatistics true to enable statistics, false to disable
     */
    void setStatisticsEnabled(boolean enableStatistics);

    /**
     * Checks whether transaction are enabled for this cache.
     * <p/>
     * Default value is false.
     *
     * @return true if transaction are enabled
     */
    boolean isTransactionEnabled();

    /**
     * Gets the transaction isolation level.
     * @return the isolation level. null if this cache is not transactional
     */
    IsolationLevel getTransactionIsolationLevel();

    /**
     * Gets the transaction mode.
     * @return the the mode of the cache. null if this cache is not transactional
     */
    Mode getTransactionMode();

    /**
     * Sets how long cache entries should live. If expiry is not set entries are eternal.
     *
     * It both types of expiry are set, then each is checked. If expiry has occurred in either
     * type then the entry is expired.
     * <p/>
     * Any operation that accesses an expired entry will behave as if it is not in the cache. So:
     * <ul>
     *     <li>a cache shall not return an expired entry</li>
     *     <li>{@link Cache#containsKey(Object)} will return false</li>
     * </ul>
     *
     * @param type the type of the expiry
     * @param duration how long, in the specified duration, the cache entries should live.
     * @throws NullPointerException is type or duration is null
     */
    void setExpiry(ExpiryType type, Duration duration);
    
    ExpiryType getExpiryType();
    
    Duration getExpiry();

    /**
     * Gets the cache's time to live setting,Sets how long cache entries should live. If expiry is not set entries are eternal.
     * <p/>
     * Default value is {@link Duration#ETERNAL}.
     *
     * @param type the type of the expiration
     * @return how long, in milliseconds, the specified units, the entry should live. 0 means eternal.
     */
//    Duration getExpiry(ExpiryType type);

    /**
     * A time duration.
     */
    public static class Duration {
        /**
         * ETERNAL
         */ 
        public static final Duration ETERNAL = new Duration(TimeUnit.SECONDS, 0);

        /**
         * The unit of time to specify time in. The minimum time unit is milliseconds.
         */
        private final TimeUnit timeUnit;

       /*
        * How long, in the specified units, the cache entries should live.
        * The lifetime is measured from the cache entry was last put (i.e. creation or modification for an update) or
        * the time accessed depending on the {@link ExpiryType}
        * 0 means eternal.
        *
        */
        private final long timeToLive;

        /**
         * Constructs a duration.
         *
         * @param timeUnit   the unit of time to specify time in. The minimum time unit is milliseconds.
         * @param timeToLive how long, in the specified units, the cache entries should live. 0 means eternal.
         * @throws InvalidConfigurationException if a TimeUnit less than milliseconds is specified.
         * @throws NullPointerException          if timeUnit is null
         * @throws IllegalArgumentException      if timeToLive is less than 0
         *                                       TODO: revisit above exceptions
         * todo: Change TimeUnit to our own TimeUnit which does not define nano and micro
         */
        public Duration(TimeUnit timeUnit, long timeToLive) {
            if (timeUnit == null) {
                throw new NullPointerException();
            }
            switch (timeUnit) {
                case NANOSECONDS:
                case MICROSECONDS:
                    throw new InvalidConfigurationException();
                default:
                    this.timeUnit = timeUnit;
                    break;
            }
            if (timeToLive < 0) {
                throw new IllegalArgumentException();
            }
            this.timeToLive = timeToLive;
        }

        /**
         * @return the TimeUnit used to specify this duration
         */
        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        /**
         * @return the number of TimeUnits which quantify this duration
         */
        public long getTimeToLive() {
            return timeToLive;
        }

    }

    /**
     * Type of Expiry
     */
    public enum ExpiryType {

        /**
         * Time since last modified. Creation is also considered a modification event.
         */
        MODIFIED,

        /**
         * Time since last <em>accessed</em>. Access means any access to an entry including
         * a modification event. Examples are:
         * <ul>
         *     <li>put</li>
         *     <li>get</li>
         *     <li>containsKey</li>
         *     <li>iteration</li>
         * </ul>
         *
         */
        ACCESSED
    }
    
    /**
     * Type of CacheLoader
     */
    public enum CacheLoadType {
    	/**
    	 * Asynchronous refresh.
    	 */
    	ASYN,
    	/**
    	 * Change notice. 
    	 */
    	NOTIFY,
    	/**
    	 * Read Through.
    	 */
    	READ_THROUGH,
    	/**
    	 * No need load.
    	 */
    	NONE
    	
    }
    /**
     * Type of CacheWriter
     */
    public enum CacheWriteType {
    	/**
    	 * Asynchronous refresh.
    	 */
    	ASYN,
    	/**
    	 * Just like write through.
    	 */
    	SYN,
    	/**
    	 * No need write.
    	 */
    	NONE
    }
    
    /**
     * cache share scope. 
     */
    public enum CacheShareScope {
    	/**
    	 * 在远程共享缓存的情况下，对其他应用程序公开。
    	 */
    	PUBLIC,
    	/**
    	 * 在远程共享缓存的情况下，只限本应用程序访问。
    	 */
    	PRIVATE
    }
    
    /**
     *  可以对远程共享缓存进行的操作。
     */
    public enum CacheShareOperation {
    	/**
    	 * 写操作
    	 */
    	WRITE,
    	/**
    	 * 删除操作
    	 */
    	REMOVE,
    	/**
    	 * 更新操作
    	 */
    	UPDATE,
    	/**
    	 * 读操作
    	 */
    	READ,
    }
    
    public enum CacheEntryListenerType {
    	CREATED,
    	REMOVED,
    	UPDATED,
    	READ,
    	EXPIRY
    }
}
