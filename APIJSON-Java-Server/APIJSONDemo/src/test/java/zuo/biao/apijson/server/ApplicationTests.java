package zuo.biao.apijson.server;

import apijson.demo.server.Application;
import apijson.demo.server.mapper.StandardMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zhangls.apijson.base.Json;
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

		String select = "SELECT * FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0";


		String insert = "INSERT INTO `apijson`.`apijson_privacy`(id,certified,phone,balance,_password,_payPassword) VALUES(123988,1,18939242601,12.56,'123456','654321')";

		String update = "UPDATE apijson_privacy SET _password='098765' WHERE phone=18939242601";

		String delete = "delete from apijson_privacy where phone=18939242601";

		String head = "select count(1) num from apijson_privacy";

		List<LinkedHashMap<String, Object>> ls = mapper.standardSelect(select);

        String str = JSON.toJSONString(ls, SerializerFeature.PrettyFormat);

//        Integer in = mapper.standardInsert(delete);

		System.out.println("ls.toString() = " + ls.toString());

        System.out.println(str);
    }

}
