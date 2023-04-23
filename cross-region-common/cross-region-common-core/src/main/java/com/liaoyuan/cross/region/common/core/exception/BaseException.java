package com.liaoyuan.cross.region.common.core.exception;

/**
 * 基础异常
 * 
 * @author ruoyi
 */
public class BaseException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    public BaseException(String module, Integer code, String defaultMessage,Object... args)
    {
        super(defaultMessage);
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    public BaseException(String module, Integer code,Object... args)
    {
        this(module, code, null, args);
    }

    public BaseException(String module, String defaultMessage)
    {
        this(module, null, defaultMessage, null);
    }

    public BaseException(Integer code, Object[] args)
    {
        this(null, code, null, args);
    }

    public BaseException(String defaultMessage)
    {
        this(null, null, defaultMessage, null);
    }

    public String getModule()
    {
        return module;
    }

    public Integer getCode()
    {
        return code;
    }

    public Object[] getArgs()
    {
        return args;
    }

    public String getDefaultMessage()
    {
        return defaultMessage;
    }
}
