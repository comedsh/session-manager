package org.ranran.tomcat.redissessions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * 
 * provided the functions that serialize and deserialize
 * 
 * create time: 2016年4月28日 上午11:32:54
 *
 */
public class JavaSerializer implements Serializer {
	
  private ClassLoader loader;

  private final Log log = LogFactory.getLog(JavaSerializer.class);

  @Override
  public void setClassLoader(ClassLoader loader) {
    this.loader = loader;
  }
  
  /*
   * (non-Javadoc)
   * @see org.ranran.tomcat.redissessions.Serializer#makeBytes(org.ranran.tomcat.redissessions.RedisSession)
   */
  public byte[] makeBindaryData( RedisSession session ) throws IOException {
	  
    HashMap<String,Object> attributes = new HashMap<String,Object>();
    
    for (Enumeration<String> enumerator = session.getAttributeNames(); enumerator.hasMoreElements();) {
    	
      String key = enumerator.nextElement();
      
      attributes.put(key, session.getAttribute(key));
      
    }

    byte[] serialized = null;

    try (
    		
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		
         ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( bos ) );
    		
    ) {
    	
      oos.writeUnshared( attributes );
      
      oos.flush();
      
      serialized = bos.toByteArray();
      
    }

    MessageDigest digester = null;
    
    try {
    	
      digester = MessageDigest.getInstance("MD5");
      
    } catch (NoSuchAlgorithmException e) {
    	
      log.error("Unable to get MessageDigest instance for MD5");
      
    }
    
    return digester.digest( serialized );
    
  }
  
  /**
   * 
   * 这个方法描绘了作者的 serialize 的数据结构。God, 不敢相信他的做法真是天马行空...
   * 
   * serial data structure as below,
   * 
   * (第一级数据结构)
   * -> SessionSerializationMetadata serial object data
   *    -> session attribute serial object data
   *    
   * (第二级数据结构)   
   * -> StandardSession.attributes.key serial object data
   * -> StandardSession.attributes.value serial object data
   * -> StandardSession.attributes.key serial object data
   * -> StandardSession.attributes.value serial object data
   * -> StandardSession.attributes.key serial object data
   * -> StandardSession.attributes.value serial object data
   * .... 
   * 
   * 作者使用了两级结构，
   * 第一级，是通过 SessionSerializationMetadata 对象将 session key-value 封装后，进行的一次序列化
   * 第二级，是通过 StandardSession.writeObjectData( out ) 方法，将 key 和 value 单独作为 Object 进行序列化
   * 
   * 疑问，第一级和第二级对 session key-value 重复序列化操作，因为两者是相同的，作者为什么要重复做这个序列化的操作？
   * 1. 难道是为了方便 Array.equals() ( @see RedisSessionManager#saveInternal(JedisAdapter, org.apache.catalina.Session, boolean) 方法中的) ?
   * 2. ... 
   * 
   */
  @Override
  public byte[] makeSerialData( RedisSession session, SessionSerializationMetadata metadata ) throws IOException {
	  
    byte[] serialized = null;

    try (
    		
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		
         ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( bos ) );
    		
    ) {
      	
      oos.writeObject( metadata );

      session.writeObjectData( oos );
      
      oos.flush(); // write into bos
      
      serialized = bos.toByteArray();
      
    }

    return serialized;
    
  }

  @Override
  public void deserialize(byte[] data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException {
    
	try(
			
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));
			
        ObjectInputStream ois = new CustomObjectInputStream(bis, loader);
			
    ) {
		
      SessionSerializationMetadata serializedMetadata = ( SessionSerializationMetadata ) ois.readObject();
      
      metadata.setSerialData( serializedMetadata.getSerialData() );
      
      session.readObjectData( ois );
      
    }
  }
}
