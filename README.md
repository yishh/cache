java 透明化 cache 实现
=====================
简单实现了一个透明化的Cache机制，使用这种方式，可以尽可能的避免在业务逻辑中出现如下硬编码的Cache操作代码:

	public LikeCount getCount(String appId, String contentId) {
		Object value = cache.get("CACHE");
		if(value == null){
   			value = ...
   			cache.set("CACHE", value)
		}	
		return value;
	}	
		
主要的思路为：

- 针对数据的操作应该尽量作为一个独立的方法存在，这样才能应用透明化cache机制
- 利用Spring的 BeanFactoryPostProcessor 扩展点，在bean初始化之前利用cglib对bean进行代理。
- 使用redis作为缓存存储，可以利用其list数据类型。

使用方式如下：


   
	@Cache(key = @CacheKey(template = "CACHE/${p0}/${p1}"))
    public LikeCount getCount(String appId, String contentId) {
       //Do something ...
    }
   


${p0} 表示取方法的第一个参数
  
## 拦截器

Cache框架通过Spring配置的拦截器来实现缓存的快捷操作，目前有3中拦截器类型：

1. MemcachedInterceptor  使用memcached进行缓存，配置方式如下:

	
		<bean id="memcachedClient" class="net.spy.memcached.spring.MemcachedClientFactoryBean">
        	<property name="servers" value="${cache.server}"/>
        	<property name="protocol" value="BINARY"/>
        	<property name="transcoder">
            	<bean class="net.spy.memcached.transcoders.SerializingTranscoder">
                	<property name="compressionThreshold" value="1024"/>
            	</bean>
        	</property>
        	<property name="opTimeout" value="1000"/>
        	<property name="timeoutExceptionThreshold" value="1998"/>
        	<property name="hashAlg" value="KETAMA_HASH"/>
        	<property name="locatorType" value="CONSISTENT"/>
        	<property name="failureMode" value="Redistribute"/>
        	<property name="useNagleAlgorithm" value="false"/>
    	</bean>
		<bean id="memcachedInterceptor" class="com.skymobi.sns.cache.memcached.MemcachedInterceptor">
        	<constructor-arg ref="memcachedClient"/>
    	</bean>
    	
2. RedisInterceptor 使用redis进行缓存，支持list类型的数据


		 <bean id="masterRedisClient" class="com.skymobi.sns.cache.redis.RedisClient">
        	<constructor-arg value="${redis.url}"/>
    	</bean>

    	<bean id="slaveRedisClient" class="com.skymobi.sns.cache.redis.RedisClient">
       		<constructor-arg value="${redis-slave.url}"/>
    	</bean>

    	<bean id="redisInterceptor" class="com.skymobi.sns.cache.redis.RedisInterceptor">
        	<constructor-arg ref="masterRedisClient"/>
        	<constructor-arg ref="slaveRedisClient"/>
    	</bean>


3. HybridInterceptor 混合类型的拦截器 

 		
 		<bean id="hybridInterceptor" class="com.skymobi.sns.cache.hybrid.HybridInterceptor">
        	<property name="defaultInterceptor" ref="redisInterceptor"/>
        	<property name="interceptors">
            	<map>
                	<entry key="default" value-ref="redisInterceptor"></entry>
                	<entry key="memcached" value-ref="memcachedInterceptor"></entry>
            	</map>
        	</property>
    	</bean>
    	
    	
   如果配置使用这种类型的拦截器，则在注解中可以通过type字段来指定使用的拦截器，将在下面具体说明。


最后需要在spring中配置一个Proxy bean，如下:
 
    
    <bean id="cacheProxy" class="com.skymobi.sns.cache.EasyCacheProxy">
        <constructor-arg ref="hybridInterceptor"/>        
    </bean>

  
## 注解 

1. @CacheProxy

	需要在要使用cache支持的class上加上@CacheProxy注解。参数如下:
	
		String type() default ""; //当配置了HybridInterceptor类型的拦截器时，可在这里指定整个bean默认使用的拦截器类型
	
2. @SimpleCache 

	最简单的缓存注解，缓存的key是简单字符串。参数如下：
	
		String key(); //简单字符串,缓存的key
    	String type() default ""; //当配置了HybridInterceptor类型的拦截器时，可在这里指定当前方法使用的拦截器类型，将覆盖@CacheProxy的定义
    	int expire();//缓存过期时间，秒为单位
    	String expireTime() default ""; //过期时间，详细信息参见[#exireTime格式]
    	boolean cacheNull() default false; //是否缓存返回值为null的结果
    	
    	
3. @CacheKey

	用来格式化缓存的key。参数如下:
	
		String template(); //spring el表达式格式的字符串，通过 ${p0} ${p1} 来获取方法的参数，0,1代表的是在方法参数中的位置，对于复杂类型的参数，可以通过 ${p0.name}的方式来获取其字段
    	@Deprecated
    	String[] els() default {}; //已过期，无需使用
    	boolean simple() default false; //


3. @Cache

	最常用的使用方式，被注解的方法在被调用时，首先尝试从缓存中加载数据，如果缓存命中，则直接返回。缓存中没有，则会调用方法本身，并在调用成功后将结果写入缓存。参数如下:
	
		CacheKey key();                     //缓存的key，通过@CacheKey指定
	    String type() default "";           //同@SimpleCache
    	String expireTime() default "";     //同@SimpleCache
    	int expire() default 0;             //同@SimpleCache
    	boolean cacheNull() default false;  //同@SimpleCache
	
		
4. @WriteCache

	将方法的结果或者参数写入缓存。参数如下:
	
 		CacheKey key();                //同@CacheKey
    	String type() default "";      //同@CacheKey
    	int expire() default 0;         //同@CacheKey
    	String expireTime() default "";   //同@CacheKey
    	boolean cacheNull() default false;     //同@CacheKey
    	boolean writeParameter() default false; //是否写入参数
    	int parameterIndex() default 0;      //要写入缓存的参数的index
    	boolean writeReturn() default true;  //是否将方法的结果写入缓存

5. @RemoveCache		
	
	在调用这个方法是移除指定key对应的缓存。参数如下:
	
		CacheKey[] key();              //同@CacheKey
    	String type() default "";       //同@CacheKey
		boolean markAsNull() default false;    //是否将改key对应的缓存数据标记为null数据
		int expire() default 0;          //同@CacheKey
		String expireTime() default "";      //同@CacheKey
		
6. ListedCache

	用来缓存列表类型的数据，仅在使用RedisInterceptor类型的拦截器时或使用混合类型拦截器，并指定type为RedisInterceptor类型拦截器时可用，参数如下:
	
		    CacheKey key();                 //同@CacheKey
   			String type() default "";	     //同@CacheKey
    		int offsetIndex() default 1;    //分页参数
    		int limitIndex() default 2;     //分页参数
    		
	比如查询 offset=10， limit=10的数据，将首先尝试从缓存中加载，如果缓存中仅有18条数据，则首先从缓存中加载 offset=10， limit=8的数据，然后将修改传递给方法调用的参数为 offset=18， limit =2 ，将剩余的数据补充上。
 
7. @WriteListCache 

	写入列表类型的缓存，仅在使用RedisInterceptor类型的拦截器时或使用混合类型拦截器，并指定type为RedisInterceptor类型拦截器时可用，参数如下:
  
  		CacheKey key();  //同@CacheKey
    	String type() default "";  //同@CacheKey
    	boolean writeParameter() default false;  //同@WriteCache
    	int parameterIndex() default 0;   //同@WriteCache
    	boolean writeReturn() default true;  //同@WriteCache
    	int max() default 1000;    //控制redis缓存中列表的最大大小
    	boolean append() default  false; //false时放在列表的最前面，为true则追加到列表的最后面。


## 注解的混合使用

只有@RemoveCache可以在同一方法上注解多个，如下:

		@RemoveCache(key = @CacheKey(template = "ddd", simple = true))
   	 	@RemoveCache(key = @CacheKey(template = "fff", simple = true))
    	public void testM1(){

    	}
    	
@RemoveCache还可以和其他的注解一起使用，但是必须写在最前面，如下:

		@RemoveCache(key = @CacheKey(template = "ddd", simple = true))
    	@Cache(key = @CacheKey(template = "fff", simple = true))
    	public void testM1(){

    	}
    	
    	
## redis客户端##

默认的redis客户端使用的是 async-redis-client,项目地址: [async-redis-client](https://github.com/yishh/async-redis-client)



## exireTime格式

expire现在支持用字符串表达式来指定，如下：

exireTime = "1h"

可以使用的方式有
1d ---> 1天

1h ---> 1小时

1mn ---> 1分钟

1s --> 1秒
