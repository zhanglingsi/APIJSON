package zuo.biao.apijson.server;

import apijson.demo.server.Application;
import apijson.demo.server.dao.UserDAO;
import apijson.demo.server.mapper.StandardMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest(classes = Application.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationTests {

	@Autowired
	private StandardMapper mapper;

	@Autowired
	private UserDAO userDAO;


	/**
	 * 使用Mybatis 耗时较长
	 */
	@Test
	@Repeat(10)
	public void contextLoads() {

//		String select = "SELECT * FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0";

		String sqlselectUser = "select * from apijson_user";
//		String insert = "INSERT INTO `apijson`.`apijson_privacy`(id,certified,phone,balance,_password,_payPassword) VALUES(123988,1,18939242601,12.56,'123456','654321')";
//
//		String update = "UPDATE apijson_privacy SET _password='098765' WHERE phone=18939242601";
//
//		String delete = "delete from apijson_privacy where phone=18939242601";
//
//		String head = "select count(1) num from apijson_privacy";

		Long now  = System.currentTimeMillis();
		List<LinkedHashMap<String, Object>> ls = mapper.standardSelect(sqlselectUser);
		System.out.println(System.currentTimeMillis() - now);
//		String str = JSON.toJSONString(ls, SerializerFeature.PrettyFormat);

//        Integer in = mapper.standardInsert(delete);

//		System.out.println("ls.toString() = " + ls.toString());

//        System.out.println(str);
    }


	/**
	 * jdbc 直连 花费时间相当少
	 */
	@Test
	@Repeat(10)
	public void jdbcTemplateTest(){
		Long now  = System.currentTimeMillis();
		List user = userDAO.queryAll("select * from apijson_user");
		System.out.println("测试耗时：" + (System.currentTimeMillis() - now));

	}

}
