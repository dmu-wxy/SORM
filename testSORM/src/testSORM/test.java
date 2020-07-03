package testSORM;

import core.Query;
import core.QueryFactory;
import core.TableContext;

public class test {
	public static void main(String[] args) {
		TableContext.updateJavaPOFile();
		Query q = QueryFactory.createQuery();
		System.out.println(q.queryValue("select empName from emp where id = ?", new Object[] {1}));
	}
}
