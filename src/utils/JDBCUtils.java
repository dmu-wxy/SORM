package utils;

import java.sql.PreparedStatement;

/**
 * 封装了JDBC查询常用的操作
 */
public class JDBCUtils {

    /**
     * 给sql语句传参
     * @param ps 预编译sql语句对象
     * @param params 参数
     */
    public static void handleParams(PreparedStatement ps,Object[] params){
        try {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(1 + i, params[i]);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
