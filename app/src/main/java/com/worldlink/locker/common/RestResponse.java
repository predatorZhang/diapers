package com.worldlink.locker.common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2015/5/3.
 */
public class RestResponse
{

    private boolean success;

    private String message;

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public static RestResponse parseJson(String json)
    {
        RestResponse resp = new RestResponse();
        JSONObject jsonObject = null;
        if (json.isEmpty()) {
            return null;
        }
        try
        {
            jsonObject = new JSONObject(json);
            resp.setMessage(jsonObject.getString("message"));
            resp.setSuccess(jsonObject.getBoolean("success"));
            return resp;

        } catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }

    }
}
