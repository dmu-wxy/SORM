package bean;

/**
 * 封装了java属性和get、set方法
 */
public class JavaFieldGetSet {
    /**
     * 属性的原码信息，如：private int UserId;
     */
    private String fieldInfo;
    /**
     * set方法的源码信息，如：public void setUserId(int id){this.id = id;}
     */
    private String setInfo;
    /**
     * get方法的源码信息，如：public int getUserId(){return this.id;}
     */
    private String getInfo;

    @Override
    public String toString() {
        System.out.println(fieldInfo);
        System.out.println(setInfo);
        System.out.println(getInfo);
        return null;
    }

    public JavaFieldGetSet(){}
    public JavaFieldGetSet(String fieldInfo, String setInfo, String getInfo) {
        this.fieldInfo = fieldInfo;
        this.setInfo = setInfo;
        this.getInfo = getInfo;
    }

    public String getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(String fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    public String getSetInfo() {
        return setInfo;
    }

    public void setSetInfo(String setInfo) {
        this.setInfo = setInfo;
    }

    public String getGetInfo() {
        return getInfo;
    }

    public void setGetInfo(String getInfo) {
        this.getInfo = getInfo;
    }
}
