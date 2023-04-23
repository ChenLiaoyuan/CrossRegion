package com.haiyisoft.cross.region.common.core.response;

/**
 * @author CLY
 * @date 2022/12/9 15:44
 **/
public enum ResponseStatus {
    SUCCESS(200,"操作成功"),
    FAILED(999,"操作失败"),
    CROSS_REGION_RESPONSE_TIMEOUT(550,"跨区请求超时"),
    CROSS_REGION_RESPONSE_ERROR(551,"获取跨区请求响应结果出错");
    private Integer code;
    private String msg;

    ResponseStatus(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
