# CrossRegion
低安全区访问高安全区的跨区代理

基于安全的考虑，低安全区不能直连高安全区，需要通过网闸单向摆渡访问，由此设计了一个通用的跨区代理访问方案。

# 交互方案图
图1：非敏感数据访问方案
![非敏感数据访问方案](https://github.com/ChenLiaoyuan/CrossRegion/blob/main/img/%E9%9D%9E%E6%95%8F%E6%84%9F%E6%95%B0%E6%8D%AE%E8%AE%BF%E9%97%AE%E6%96%B9%E6%A1%88.png)

图2：敏感数据访问方案
![敏感数据访问方案](https://github.com/ChenLiaoyuan/CrossRegion/blob/main/img/%E6%95%8F%E6%84%9F%E6%95%B0%E6%8D%AE%E8%AE%BF%E9%97%AE%E6%96%B9%E6%A1%88.png)

图3：跨区交互方案详情：
![跨区交互详情](https://github.com/ChenLiaoyuan/CrossRegion/blob/main/img/%E8%B7%A8%E5%8C%BA%E4%BA%A4%E4%BA%92%E6%96%B9%E6%A1%88.png)

# 跨区应用接入方案
跨区应用功能开发应使用前后分离的架构。在高安全区部署一套完整的前后台服务，供高安全区内部用户访问，此访问不用走网闸通道；在低安全区只部署前台服务，经过注册、审批、分配密钥和证书后，通过代理和网闸摆渡访问高安全区的后台服务，此通道供低安全用户访问。此方案只需在低安全区部署多一套前台应用即可，经过密钥加密、数字签名认证、网闸单向摆渡、和高可用高并发设计，既兼顾了安全、性能和数据一致性，又不会影响高安全区后台服务和其他模块服务的交互。

# 跨区应用访问高可用与高并发设计
为了应对低安全区应用海量的访问请求，需要保证功能的稳定性和快速响应：
1.	使用kafka集群作为代理反向摆渡跨区请求：kafka具有高吞吐低延迟的特性，可以做到每秒几十万的超高并发写入。其同样具备高可用的功能，一个broker节点故障不会影响整个集群的正常运作；
2.	使用Redis集群接收正向摆渡响应结果：Redis集群可做到每秒十万的超高并发写入，主从模式可在主节点故障后从节点自动升级为主节点继续处理请求，保证集群正常运作；
3.	使用Sentinel限流：根据每个DMZ应用的访问频率和网闸摆渡文件的性能，对跨区请求进行限流，避免突增流量造成上游系统不可用，形成雪崩效应；
4.	GateWay网关集群过滤非法、未认证请求：对于恶意攻击、未认证请求在前期就进行拦截，避免冲击网闸，造成拥堵；
5.	对于查询类、时效性低的数据采用读取数据库副本，不走网闸跨区请求，减轻网闸摆渡文件压力。

# 代码模块
```
CrossRegion
├── cross-region-common
│   ├── cross-region-common-core 公共实体类
│   ├── cross-region-common-datasource 主从数据库配置
│   ├── cross-region-common-kafka kafka集群配置
│   ├── cross-region-common-log 日志配置
│   ├── cross-region-common-redis redis集群配置
│   ├── cross-region-common-swagger swagger配置
│   └── pom.xml
├── cross-region-modules
│   ├── cross-region-module-gateway 网关配置
│   ├── cross-region-module-high 高安全区，主要用于重放请求
│   ├── cross-region-module-low 低安全区，主要用于封装跨区请求、序列化以及同步监听响应
│   └── pom.xml
└── pom.xml
```

跨区代理入口：
com.liaoyuan.cross.region.module.low.rest.ProxyController

# Nginx配置
```
server {
        listen 9898;
	      server_name localhost;
        
        location / {
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Original-URL $request_uri;
            
            proxy_connect_timeout 5s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;

            # Nginx 默认不支持 chunked 编码，因此指定Nginx使用HTTP 1.1 协议，HTTP 1.1 协议支持分块传输，
            proxy_http_version 1.1;
            # 并且将 Transfer-Encoding 标头设置为空。这将强制 Nginx 将所有传入的 chunked 编码数据解码为正常的 HTTP 数据。
            proxy_set_header Transfer-Encoding "";
            # 如果返回的响应头为Connection: close，但还带了Transfer-Encoding: chunked，nginx就无法正常处理，
            # 当设置为 "" 时，表示在发送请求时，不会在请求头中包含 Connection 标头。这通常是为了避免出现代理服务器错误地处理 Connection 标头，例如将其设置为 close，从而在每个请求之后关闭与上游服务器的连接。
            proxy_set_header Connection "";
            
            proxy_pass http://localhost:18888/proxy/;
        }

        error_page 500 502 503 504 /50x.html;
        location = /50x.html{
            root html;
        }
    }
```
