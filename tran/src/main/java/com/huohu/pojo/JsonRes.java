package com.huohu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JsonRes implements Serializable {
    /**
     * 状态码 0成功 1失败
     */
    private Integer code;
    /**
     * 状态信息
     */
    private String msg;
    /**
     * 数据
     */
    private Object data;
    /**
     * token
     */
    private String token;

    /**
     * 成功
     *
     * @param msg
     * @return
     */
    public static JsonRes success(String msg) {
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(0);
        jsonRes.setMsg(msg);
        return jsonRes;
    }

    /**
     * 成功2
     *
     * @param data
     * @return
     */
    public static JsonRes success(Object data) {
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(0);
        jsonRes.setMsg("成功");
        jsonRes.setData(data);
        return jsonRes;
    }

    /**
     * 成功3
     *
     * @param data
     * @param token
     * @return
     */
    public static JsonRes success(Object data, String token) {
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(0);
        jsonRes.setMsg("成功");
        jsonRes.setData(data);
        jsonRes.setToken(token);
        return jsonRes;
    }


    /**
     * 失败1
     *
     * @param msg
     * @return
     */
    public static JsonRes error(String msg) {
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(1);
        jsonRes.setMsg(msg);
        return jsonRes;
    }

    /**
     * @param data
     * @return
     */
    public static JsonRes error(Object data) {
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(1);
        jsonRes.setMsg("失败");
        jsonRes.setData(data);
        return jsonRes;
    }

    /**
     * 失败3
     *
     * @param data
     * @param token
     * @return
     */
    public static JsonRes error(Object data, String token) {
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(1);
        jsonRes.setMsg("失败");
        jsonRes.setData(data);
        jsonRes.setToken(token);
        return jsonRes;
    }
}
