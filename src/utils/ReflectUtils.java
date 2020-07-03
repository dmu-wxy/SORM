package utils;

import java.lang.reflect.Method;

/**
 * 封装了放射的常用操作
 */
public class ReflectUtils {

    /**
     * 调用obj对象对应fieldName对应的get方法
     * @param fieldName 属性
     * @param obj obj对象
     * @return 调用get方法放回的值
     */
    public static Object invokeGet(String fieldName,Object obj){
        try {
            Class clazz = obj.getClass();
            Method m = clazz.getDeclaredMethod("get" + StringUtils.firstChar2UpperCase(fieldName),null);
            return m.invoke(obj,null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void invokeSet(Object rowObj,Object columnValue,String columnName){
        Class clazz = rowObj.getClass();
        Method m = null;
        try {
            m = clazz.getDeclaredMethod("set" + StringUtils.firstChar2UpperCase(columnName),columnValue.getClass());
            m.invoke(rowObj,columnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
