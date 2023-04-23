-- 访问频率控制，传入的参数KEYS[1]为对应的SessionID，ARGV[1]为时间长度，ARGV[2]为频率，
-- 如传参为ChenLiaoYuan , 10 3即代表的是ChenLiaoYuan的会话10秒内只能访问3次
-- 获取访问次数（incr命令如果不存在指定的key，则会创建key，返回值是增加后的数）
local times = redis.call('incr',KEYS[1])

-- 如果是10秒初次访问，则设置过期时间为10秒
if times == 1 then
    redis.call('expire',KEYS[1],ARGV[1])
end

-- 如果大于3次，这返回0，不让其访问
-- 需要注意的是redis进来的默认为字符串，lua只能和相同的类型比较，所以要转成tonumber
if times > tonumber(ARGV[2]) then
    return 0
end

-- 不超过3次，返回正常，允许访问
return 1



