package zuo.biao.apijson.server;

import apijson.demo.server.Application;
import apijson.demo.server.mapper.StandardMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest(classes = Application.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationTests {

	@Autowired
	private StandardMapper mapper;


	@Test
	public void contextLoads() {

		String str = "SELECT * FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0";

		List<LinkedHashMap<String, Object>> ls = mapper.superManagerSelect(str);

		System.out.println("ls.toString() = " + ls.toString());
	}

}
