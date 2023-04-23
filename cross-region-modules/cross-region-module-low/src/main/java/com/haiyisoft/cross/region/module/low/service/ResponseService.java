package com.haiyisoft.cross.region.module.low.service;

import com.alibaba.fastjson2.JSON;
import com.haiyisoft.cross.region.common.core.response.CommonResponse;
import com.haiyisoft.cross.region.common.core.response.ResponseStatus;
import org.apache.http.entity.ContentType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 返回响应结果
 * @author CLY
 * @date 2023/3/22 18:34
 **/
public class ResponseService {

    /**
     * 返回响应结果
     * @param response
     * @param message
     */
    public static void httpResponse(HttpServletResponse response,Integer code, String message){
        response.setContentType(ContentType.APPLICATION_JSON.toString());

        CommonResponse commonResponse = CommonResponse.of(code,message);
        try {
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(commonResponse));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void httpResponse(HttpServletResponse response, ResponseStatus responseStatus){
        httpResponse(response,responseStatus.getCode(),responseStatus.getMsg());
    }


}
