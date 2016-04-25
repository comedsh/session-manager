package org.ranran.tomcat.redissessions;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.junit.Test;

/**
 * 
 * because there is a gap between tomcat session and redis session. 
 * 
 * Tomcat session stored in memory and redis session is serialized into redis memory, sometimes they are not synchronized, 
 * 
 * So, create this Test cases to verify the session from redis, to check if the session value from redis is synchronized with tomcat session or not. 
 * 
 * @author shangyang
 *
 * @version 
 * 
 * create time: 2016年4月24日 上午11:19:12
 *
 */
public class RedisSessionManagerTest {
	
	// modify the session id according with the real session id
	static final String SESSION_ID = "12E0D1D1E4B545AA6BE426881979AC3E";
	
	
	/**
	 * 测试流程，
	 * 
	 * 1. 先启动 Tomcat，访问 http://localhsot:8080/session-manager/set.jsp, 生成 session，这时，会在 redis 中生成 session。
	 * 
	 * 2. 查看 redis 中的 SESSIONID
	 *    $ redis-cli -h 10.211.55.8 -p 6379 -a yourpassword
	 *    $ keys *
	 * 
	 * 3. 替换 SESSION_ID
	 * 
	 * 
	 * 
	 * @throws IOException
	 * @throws LifecycleException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testFindSession() throws IOException, LifecycleException, ClassNotFoundException, IllegalAccessException, InstantiationException{
		
		RedisSessionManager redisSessionManager = this.getRedisSessionManager();
		
		Session session = redisSessionManager.findSession( SESSION_ID );
		
		HttpSession httpSession = session.getSession();
		
		Enumeration<String> er = httpSession.getAttributeNames();
		
		while( er.hasMoreElements() ){
			
			String key = er.nextElement();
			
			System.out.println( "key: " + key + "; value: "+ httpSession.getAttribute( key ) );
		
		}
			
	}
	
	RedisSessionManager getRedisSessionManager() throws LifecycleException, ClassNotFoundException, IllegalAccessException, InstantiationException{
		
		RedisSessionManager redisSessionManager = new RedisSessionManager();
		
		// first, setup the reids connection 
		redisSessionManager.setHost("10.211.55.8");
		
		redisSessionManager.setPort(6379);
		
		redisSessionManager.setTimeout(60); // seconds
		
		redisSessionManager.setPassword("fhredispass007");
		
		redisSessionManager.initializeDatabaseConnection();
		
		// second, prepare the Serializer.
		redisSessionManager.initializeSerializer();
		
		// third, prepare the container
		redisSessionManager.setContainer( new StandardContext() );		
		
		return redisSessionManager;
	}
	
	RedisSessionManager getRedisClusterSessionManager(){
		return null;
	}
	
	
}
