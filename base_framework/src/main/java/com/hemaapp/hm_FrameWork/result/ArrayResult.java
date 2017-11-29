package com.hemaapp.hm_FrameWork.result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import com.hemaapp.hm_FrameWork.exception.DataParseException;

/**
 * 对BaseResult的拓展，适用返回数据中有数组的情况
 */
public abstract class ArrayResult<T> extends BaseResult {
    private ArrayList<T> objects = new ArrayList();
    private int totalCount = 0;
    private int vipTotalCount = 0;
    private JSONObject jsonObject;

    public ArrayResult(JSONObject jsonObject, Class<T> tClass) throws DataParseException {
        super(jsonObject);
        this.jsonObject = jsonObject;
        if (jsonObject != null) {
            try {
                if (!jsonObject.isNull("infor") && !this.isNull(jsonObject.getString("infor"))) {
                    Object e = (new JSONTokener(jsonObject.getString("infor"))).nextValue();
                    int i;
                    if (e instanceof JSONObject) {
                        JSONObject jsonList = jsonObject.getJSONObject("infor");
                        if (!jsonList.isNull("totalCount")) {
                            this.totalCount = jsonList.getInt("totalCount");
                        }
                        if (!jsonList.isNull("vipTotalCount")) {
                            this.vipTotalCount = jsonList.getInt("vipTotalCount");
                        }

                        if (!jsonList.isNull("listItems") && !this.isNull(jsonList.getString("listItems"))) {
                            JSONArray size = jsonList.getJSONArray("listItems");
                            i = size.length();

                            for (int i1 = 0; i1 < i; ++i1) {
                                this.objects.add(this.parse(size.getJSONObject(i1), tClass));
                            }
                        }
                    } else if (e instanceof JSONArray) {
                        JSONArray var9 = jsonObject.getJSONArray("infor");
                        int var10 = var9.length();

                        for (i = 0; i < var10; ++i) {
                            this.objects.add(this.parse(var9.getJSONObject(i), tClass));
                        }
                    }
                }
            } catch (JSONException var8) {
                throw new DataParseException(var8);
            }
        }

    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public int getVipTotalCount() {
        return vipTotalCount;
    }

    public ArrayList<T> getObjects() {
        return this.objects;
    }

    public abstract T parse(JSONObject var1, Class<T> var2) throws DataParseException;

    public String getJsonString() {
        return jsonObject.toString();
    }
}
