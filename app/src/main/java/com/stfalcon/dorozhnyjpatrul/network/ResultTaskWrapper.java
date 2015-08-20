package com.stfalcon.dorozhnyjpatrul.network;

import java.util.Map;

/**
 * Created by alexandr on 18/08/15.
 */
public class ResultTaskWrapper {
    public static final int ROUTE_INSTRUCTIONS = 0;
    public static final int LOCATION_PROGICTION = 1;

    public static final int CODE_OK = 200;
    public static final int CODE_CREATED = 201;
    public static final int CODE_DELETED = 204;
    public static final int CODE_TIMEOUT = 408; //FXIME: change it to 60x value
    public static final int CODE_NOT_VALID_DATA = 400;
    public static final int CODE_NO_INTERNET_CONNECTION = 404; //FIXME: change it to 60x value
    public static final int CODE_INTERNAL_SERVER_ERROR = 500;
    public static final int CODE_BAD_GATEWAY = 502;

    private int type;
    private int code = 200;
    private Object result;
    private Map<String, Object> hashMapResult;


    public int getType() {
        return type;
    }


    public void setType(int type) {
        this.type = type;
    }


    public Object getResult() {
        return result;
    }


    public void setResult(Object result) {
        this.result = result;
    }


    public Map<String, Object> getHashMapResult() {
        return hashMapResult;
    }


    public void setHashMapResult(Map<String, Object> hashMapResult) {
        this.hashMapResult = hashMapResult;
    }


    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }


    /**
     * @return
     */
    public boolean isOK() {
        switch (code) {
            case CODE_DELETED:
            case CODE_CREATED:
            case CODE_OK:
                return true;

            case CODE_TIMEOUT:
            case CODE_NO_INTERNET_CONNECTION:
            case CODE_NOT_VALID_DATA:
                return false;

            case CODE_INTERNAL_SERVER_ERROR:
            case CODE_BAD_GATEWAY:
            default:
                /*Context context = RoadApp.getInstance().getApplicationContext();
                Toast.makeText(context, "404", Toast.LENGTH_SHORT).show();
                Log.i("logerr", "RESULT IS NOT OK; type = " + type + "; code = " + code);*/
                return false;
        }
    }
}
