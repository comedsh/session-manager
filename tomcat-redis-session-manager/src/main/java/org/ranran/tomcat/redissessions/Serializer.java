package org.ranran.tomcat.redissessions;

import java.io.IOException;

public interface Serializer {
	
  void setClassLoader(ClassLoader loader);
  
  /**
   * 
   * Serialize the session attributes into bytes; <br> 
   * 
   * then, SessionSerializationMetadata will enwrap the bytes for serialization. <br>
   * 
   * 
   * @param session
   * @return
   * @throws IOException
   */
  byte[] makeBindaryData(RedisSession session) throws IOException;
  
  /**
   * 
   * Serialize the @see SessionSerializationMetadata which enwraps the binary data from @see JavaSerializer#makeBindaryData(RedisSession)
   * 
   * @param session
   * @param metadata
   * @return
   * @throws IOException
   */
  byte[] makeSerialData(RedisSession session, SessionSerializationMetadata metadata) throws IOException;
  
  /**
   * deserialize into session
   * 
   * @param data
   * @param session
   * @param metadata
   * @throws IOException
   * @throws ClassNotFoundException
   */
  void deserialize(byte[] data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException;

}
