package core;

import bean.ColumnInfo;
import bean.TableInfo;
import utils.JDBCUtils;
import utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责查询（对外提供服务的核心类）
 */
@SuppressWarnings("all")
public abstract class Query implements Cloneable{

    /**
     * 利用模板方法模式将JDBC操作封装成模板，便于重用
     * @param sql sql语句
     * @param params 参数
     * @param clazz 记录要封装到的java类
     * @param back CallBack的实现类，实现回调
     * @return 查询结果
     */
    public Object executeQueryTemplate(String sql, Object[] params, Class clazz, CallBack back){
        Connection con = DBManager.getConn();
        List list = null; //存储查询结果
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            JDBCUtils.handleParams(ps,params);
            rs = ps.executeQuery();
            return back.doExecute(con,ps,rs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBManager.close(ps,con);
        }
    }
    /**
     * 直接执行一个DML语句
     * @param sql sql语句
     * @param params 参数
     * @return 影响记录的行数
     */
    public int executeDML(String sql,Object[] params){
        Connection con = DBManager.getConn();
        PreparedStatement ps = null;
        int count = 0;
        try {
            ps = con.prepareStatement(sql);
            JDBCUtils.handleParams(ps,params);
            count = ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(ps,con);
        }
        return count;
    }

    /**
     * 将一个对象存储到数据库中
     * @param obj 要存储的对象
     */
    public void insert(Object obj){
        Class c = obj.getClass();
        List<Object> params = new ArrayList<>();
        TableInfo ti = TableContext.poClassTableMap.get(c);
        StringBuilder sql = new StringBuilder("insert into " + ti.getTname() + " (");
        int countNotNull = 0;//计算不为空的属性值
        Field[] fs = c.getDeclaredFields();
        for(Field f : fs){
            String fieldName = f.getName();
            Object fieldValue = ReflectUtils.invokeGet(fieldName,obj);
            if(fieldValue != null){
                sql.append(fieldName + ",");
                countNotNull++;
                params.add(fieldValue);
            }
        }
        sql.setCharAt(sql.length() - 1,')');
        sql.append(" values (");
        for(int i = 0;i < countNotNull;i++){
            sql.append("?,");
        }
        sql.setCharAt(sql.length() - 1,')');
        executeDML(sql.toString(),params.toArray());
    }

    /**
     * 删除clazz表示类对应的表中的记录
     * @param clazz 跟表对应的类
     * @param id 主键的值
     */
    public void delete(Class clazz,Object id){
        //通过Class对象找TableInfo
        TableInfo ti = TableContext.poClassTableMap.get(clazz);
        //获得主键
        ColumnInfo onlyPriKey = ti.getOnlyPrikey();
        //sql语句
        String sql = "delete from " + ti.getTname() + " where " + onlyPriKey.getName() + " = ?";
        //System.out.println(sql);
        executeDML(sql,new Object[]{id});
    }

    /**
     * 删除对象在数据库中的记录
     * @param obj
     */
    public void delete(Object obj){
        Class clazz = obj.getClass();
        TableInfo ti = TableContext.poClassTableMap.get(clazz);
        ColumnInfo onlyPrikey = ti.getOnlyPrikey();

        Object priKeyValue = ReflectUtils.invokeGet(onlyPrikey.getName(),obj);
        delete(clazz,priKeyValue);
    }

    /**
     * 更新对象对应的记录，并更新指定字段的值
     * @param obj 所要更新的对象
     * @param fieldNames 指定字段的值
     * @return
     */
    public int update(Object obj, String[] fieldNames){
        Class c = obj.getClass();
        List<Object> params = new ArrayList<>();
        TableInfo ti = TableContext.poClassTableMap.get(c);
        ColumnInfo onlyPriKey = ti.getOnlyPrikey();
        StringBuilder sql = new StringBuilder("update " + ti.getTname() + " set ");

        for(String fieldName : fieldNames){
            Object fvalue = ReflectUtils.invokeGet(fieldName,obj);
            params.add(fvalue);
            sql.append(fieldName + " = ?,");
        }
        sql.setCharAt(sql.length() - 1,' ');
        sql.append(" where ");
        sql.append(onlyPriKey.getName() + " = ?");
        params.add(ReflectUtils.invokeGet(onlyPriKey.getName(),obj));
        return executeDML(sql.toString(),params.toArray());
    }

    /**
     * 查询返回多行记录，并将记录封装到clazz指定的类的对象中
     * @param sql 查询语句
     * @param clazz 封装数据的javabean类的class对象
     * @param params sql的参数
     * @return 查询道德结果
     */
    public List queryRows(String sql,Class clazz,Object[] params){
        return (List)executeQueryTemplate(sql, params, clazz, new CallBack() {
            @Override
            public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
                ResultSetMetaData metaData = null;
                List list = null;
                try {
                    metaData = rs.getMetaData();
                    while (rs.next()){//多行
                        if(list == null) list = new ArrayList();
                        Object rowObj = clazz.newInstance(); //调用无参构造器
                        //多列
                        for(int i = 0;i < metaData.getColumnCount();i++){
                            String columnName = metaData.getColumnLabel(i + 1);
                            Object columnValue = rs.getObject(i + 1);
                            ReflectUtils.invokeSet(rowObj,columnValue,columnName);
                        }
                        list.add(rowObj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }
        });
    }

    /**
     * 查询返回一行记录，并将记录封装到clazz指定的类的对象中
     * @param sql 查询语句
     * @param clazz 封装数据的javabean类的class对象
     * @param params sql的参数
     * @return 查询道德结果
     */
    public Object queryUniqueRow(String sql,Class clazz,Object[] params){
        List list = queryRows(sql,clazz,params);
        return (list != null&&list.size() > 0) ? list.get(0) : null;
    }

    /**
     * 查询返回一个值，并将改值返回
     * @param sql 查询语句
     * @param params sql的参数
     * @return 查询道德结果
     */
    public Object queryValue(String sql,Object[] params){
        return executeQueryTemplate(sql, params, null, new CallBack() {
            @Override
            public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
                Object value = null;
                try {
                    while (rs.next()) {
                        value = rs.getObject(1);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                return value;
            }
        });
    }

    /**
     * 查询返回一个数字，并将改值返回
     * @param sql 查询语句
     * @param params sql的参数
     * @return 查询到的数字
     */
    public Number queryNumber(String sql,Object[] params){
        return (Number)queryValue(sql,params);
    }

    /**
     * 分页查询
     * @param pageNum 第几页数据
     * @param size 每页显示多少记录
     * @return
     */
    public abstract Object queryPagenate(int pageNum,int size);

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
