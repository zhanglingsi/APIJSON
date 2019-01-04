```$xslt
   Controller层的职责
   1. 验证收到的请求  异常：返回异常结果，和异常码， 必须的请求参数是否为空，格式是否正确，解析是否正常等
   2. 校验请求正常（表示该请求可以被处理）：发送给Service层调用 并返回结果
   3. 返回结果处理（将用户信息添加到session，操作cookie等）
   
   service层的职责
   1. 业务逻辑校验  注册时 输入的两次密码不一致  登陆时用户名不存在 等业务逻辑校验
   2. 处理业务，包括事物原子性等 调用DAO层 访问数据库  并缓存查询结果  key：sql语句  value：ResultSet
   3. 封装返回结果
   
   DAO层的职责
   1. 查询参数校验，查询对象非空校验，插入表数据的唯一校验，以及非空校验等
   2. 访问数据库，从数据源连接池获取Connection对象，并创建PreparedStatement，设置sql语句，并设置所需参数。
   3. 执行SQL，并对返回的ResultSet进行转换，封装成对象，返回给service层
   
   Controller把通过验证的【原始的请求JSON串】 传递给Service层
   
   Service层 做业务，封装不同的SQLConfig对象（JSONObject）传递给DAO层 
   
   DAO层 解析SQLConfig对象 生成可执行的SQL语句，执行后得到结果，封装成JSONObject对象返回。
 
```