# My-RPC
- 支持多种注册中心(包括Zk和Nacos)
- 支持Java序列化和protobuf序列化以及Hessian序列化协议
- 支持多种客户端负载均衡（随机、轮询、加权轮询和平滑加权轮询）算法

# 食用不完全指北
1.clone代码到本地
```bash
git clone 
```
2.打包到本地maven仓库
```bash
mvn clean install -Dmaven.test.skip=true
```
3.引入maven坐标
```xml
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>My-RPC</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```
##客户端
使用@RpcClient注入远程方法
```java
@RestController
@RequestMapping("test")
public class TestController {

    @RpcClient
    private UserService userService;

    @GetMapping("/user")
    public ApiResult<User> getUser(@RequestParam("id")Long id){
        ApiResult<User> user = userService.getUser(id);
        return user;
    }
}
```
配置项：
|    属性 |含义      |  可选项   |
| --- | --- | --- |
|   anti.rpc.protocol  | 消息序列化协议        |  java，protobuf，hessian   |
|   anti.rpc.register-type  |  注册中心类型    |      zk,nacos             |
|   anti.rpc.address |  注册中心地址      |  默认localhost:2181   |
|   anti.rpc.load-balance |  负载均衡算法     | random<br>round<br>weightRound<br>smoothWeightRound|

##服务端
提供远程方法
```java
@RpcServer
public class UserServiceImpl implements UserService{

    private static  Logger logger = LoggerFactory.getLogger(UserService.class);


    @Override
    public ApiResult<User> getUser(Long id) {
        logger.info("现在是【A】机器提供服务");
        User user = new User(1L,"XX",2,"www.aa.com");
        return ApiResult.success(user);
    }
}
```
配置项：
|    属性 |含义      |  可选项   |
| --- | --- | --- |
|   anti.rpc.protocol  | 消息序列化协议        |  java，protobuf，hessian   |
|   anti.rpc.register-type  |  注册中心类型    |      zk,nacos             |
|   anti.rpc.address |  注册中心地址      |  默认localhost:2181   |
|   anti.rpc.server-port  |  服务端端口   |  自定义,不冲突即可      |
|   anti.rpc.load-balance |  负载均衡算法     | random<br>round<br>weightRound<br>smoothWeightRound|
|   anti.rpc.weight  |   当前服务所占权重  |    自定义             |

##性能测试篇
- 测试环境：
wsl Ubuntu-20.04
- 测试步骤:  
1.本地启动zk:
```bash
   zkServer.sh start 
```
2.启动三个服务端和一个客户端,平滑加权轮询算法:
![Image text]()
3.使用ab进行压力测试,4个线程发送10000个请求:
```bash
    ab -c 4 -n 1000 http://localhost:8081/test/user?id=1
```



