package org.ranran.tomcat.redissessions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.CustomObjectInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.ranran.domain.User;


/**
 * 
 * To understand the serialization logic; how difference with serialize against @see SessionSerializationMetadata and @see Map
 * 
 * @author shangyang
 *
 * @version 
 * 
 * create time: 2016年4月28日 下午2:18:01
 *
 */
public class SerializerTest {

	
	/**
	 * 目的是去搞懂 tomcat-redis-session-manager 本身的 serialize 和 deserialize 的逻辑关系 
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testSerialize1() throws IOException, ClassNotFoundException{
		
		RedisSessionManager manager = new RedisSessionManager();
		
		manager.setContainer( new StandardContext() );
		
		RedisSession session = new RedisSession( manager );
		
		session.setValid(true);
		
		session.setAttribute("hello", "world");
		
		session.setAttribute("lis1", Arrays.asList("A","B","C") );
		
		session.setAttribute("list2", Arrays.asList( new User("Zerk"), new User("Tony"), new User("Kane") ) );
		
		JavaSerializer serializer = new JavaSerializer();
		
		byte[] serialdata_1 = serializer.makeBindaryData(session);
		
		SessionSerializationMetadata metadata = new SessionSerializationMetadata();
		
		metadata.setSerialData( serialdata_1 );
		
		byte[] serialdata_2 = serializer.makeSerialData( session, metadata );
		
		Assert.assertFalse( "serial data 2 是序列化的封装了键值的对象 metadata, 而 serial data 1 是直接序列化的键值",  Arrays.equals( serialdata_1, serialdata_2 ) );
		
		// 从打印的结果可见，二者的序列化值是不同的。
		System.out.println("seria data 1: " + new String(serialdata_1) );
		
		System.out.println("seria data 2: " + new String(serialdata_2) );
		
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream( serialdata_2 ));
			
        @SuppressWarnings("resource")
		ObjectInputStream ois = new CustomObjectInputStream( bis, null );
				
        SessionSerializationMetadata smetadata = ( SessionSerializationMetadata ) ois.readObject();		
	
		Assert.assertTrue("the key-value serial data should be the same", Arrays.equals(serialdata_1, smetadata.getSerialData() ) );
		
	}
	
	
}
