package com.haiyisoft.cross.region.common.core.response;

import java.io.Serializable;

/**
 * @author CLY
 * @date 2022/12/9 15:44
 **/
public class CommonResponse implements Serializable {

    private Integer code;
    private String msg;
    private Object data;

    public CommonResponse(){}
    public CommonResponse(Integer code, String msg){
        this(code,msg,null);
    }
    public CommonResponse(Integer code, String msg, Object data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public CommonResponse(ResponseStatus responseStatus){
        this(responseStatus.getCode(), responseStatus.getMsg(),null);
    }

    public CommonResponse(ResponseStatus responseStatus, Object data){
        this(responseStatus.getCode(), responseStatus.getMsg(), data);
    }

    /**
     * 2**的编码为成功
     * @return
     */
    public boolean isSuccess(){
        return this.code.compareTo(300) == -1 ;
    }

    public static CommonResponse of(Integer code, String msg, Object data){
        return new CommonResponse(code,msg,data);
    }

    public static CommonResponse of(Integer code, String msg){
        return new CommonResponse(code,msg,null);
    }

    public static CommonResponse of(ResponseStatus responseStatus, Object data){
        return new CommonResponse(responseStatus, data);
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
