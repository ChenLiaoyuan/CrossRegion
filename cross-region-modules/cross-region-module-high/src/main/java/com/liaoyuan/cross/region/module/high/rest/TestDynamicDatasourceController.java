package com.liaoyuan.cross.region.module.high.rest;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liaoyuan.cross.region.common.core.model.ClyTable;
import com.liaoyuan.cross.region.common.datasource.annotation.Master;
import com.liaoyuan.cross.region.common.datasource.annotation.Slave;
import com.liaoyuan.cross.region.module.high.service.ClyTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CLY
 * @date 2023/3/15 11:41
 **/
@Api(tags = "主从数据库测试")
@RestController
// 从Nacos Config更新name1配置
@RefreshScope
public class TestDynamicDatasourceController {

    // 在nacos配置的
    @Value("${name}")
    private String name1;

    @Autowired
    private ClyTableService clyTableService;

    @ApiOperation("从主数据库查询数据")
    @GetMapping("/master/{name}")
    @Master
    // 自定义Sentinel埋点
    @SentinelResource("getMasterTable")
    public Page<ClyTable> getMasterTable(@ApiParam("cly_table的name") @PathVariable String name, @ApiParam(hidden = true) @RequestHeader(name = "Authorization",required = false) String authorization){
        System.out.println(name1);
        System.out.println(authorization);
        // 页数从1开始
        Page<ClyTable> page = Page.of(1,100);
        return clyTableService.page(page,new QueryWrapper<ClyTable>().eq("name", name));
    }

    @ApiOperation("从从数据库查询数据")
    // 一般隐式的参数才用@ApiImplicitParam，比如在request，未指明参数名称的；指明参数名称的直接用@Apiparam即可
    @ApiImplicitParam(name = "name",value = "cly_table的name",dataType = "string",paramType = "path")
    @GetMapping("/slave/{name}")
    @Slave
    public Page<ClyTable> getSlaveTable(@PathVariable String name){
        // 页数从1开始
        Page<ClyTable> page = Page.of(1,100);
        return clyTableService.page(page,new QueryWrapper<ClyTable>().eq("name", name));
    }

}
