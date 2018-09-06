package com.tetty.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.LoggerFactory;

import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;

import ch.qos.logback.classic.Logger;

/**
 * 使用jackson进行序列化与反序列化
 */
public class JsonUtil {
	static Logger log = (Logger) LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();
    static{
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }



    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error",e);
            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error",e);
            return null;
        }
    }





    public static <T> T string2Obj(String str,Class<T> clazz){
        if((str == null || str.length() == 0) || clazz == null){
            return null;
        }

        try {
            return clazz.equals(String.class)? (T)str : objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            log.warn("Parse String to Object error",e);
            return null;
        }
    }



    public static <T> T string2Obj(String str, TypeReference<T> typeReference){
        if((str == null || str.length() == 0)|| typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class)? str : objectMapper.readValue(str,typeReference));
        } catch (Exception e) {
            log.warn("Parse String to Object error",e);
            return null;
        }
    }


    public static <T> T string2Obj(String str,Class<?> collectionClass,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error",e);
            return null;
        }
    }


    public static void main(String[] args) {
    	TettyMessage tettyMessage = new TettyMessage();
    	
    	Map<String, Object> attributes = new HashMap<String, Object>();
    	attributes.put("key1", "value1");
    	attributes.put("key2", "value2");
    	
    	Header header = new Header();
    	header.setLength(100);
    	header.setPriority((byte)1);
    	header.setSessionId(1);
    	header.setType((byte)1);
    	header.setAttributes(attributes);
    	
    	tettyMessage.setHeader(header);
    	tettyMessage.setBody((byte)1);
    	
    	String json = JsonUtil.obj2String(tettyMessage);
    	System.out.println(json);
    	
    	TettyMessage tettyMessage2 = JsonUtil.string2Obj(json,TettyMessage.class);
    	System.out.println(tettyMessage.equals(tettyMessage2));
    }

}
