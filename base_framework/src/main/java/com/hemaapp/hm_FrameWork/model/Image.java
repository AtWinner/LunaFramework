package com.hemaapp.hm_FrameWork.model;

import com.hemaapp.hm_FrameWork.PoplarObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


/**
 * 帖子图片
 */
public class Image extends PoplarObject implements Serializable {

    private static final long serialVersionUID = -7795537779923986245L;
    private String id;// 图片主键id
    private String client_id;// 所属用户id
    private String title;
    private String content;// 图片说明
    private String imgurl;// 小图地址
    private String imgurlbig;// 大图地址
    private String orderby;// 排序规则

    public Image(JSONObject jsonObject) {
        try {
            id = get(jsonObject, "id");
            client_id = get(jsonObject, "client_id");
            title = get(jsonObject, "title");
            content = get(jsonObject, "content");
            imgurl = get(jsonObject, "imgurl");
            imgurlbig = get(jsonObject, "imgurlbig");
            orderby = get(jsonObject, "orderby");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Image() {

    }

    public Image(String id, String client_id, String title, String content,
                 String imgurl, String imgurlbig, String orderby) {
        super();
        this.id = id;
        this.client_id = client_id;
        this.title = title;
        this.content = content;
        this.imgurl = imgurl;
        this.imgurlbig = imgurlbig;
        this.orderby = orderby;
    }

    @Override
    public String toString() {
        return "Image [id=" + id + ", client_id=" + client_id + ", title="
                + title + ", content=" + content + ", imgurl=" + imgurl
                + ", imgurlbig=" + imgurlbig + ", orderby=" + orderby + "]";
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the client_id
     */
    public String getClient_id() {
        return client_id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the imgurl
     */
    public String getImgurl() {
        return imgurl;
    }

    /**
     * @return the imgurlbig
     */
    public String getImgurlbig() {
        return imgurlbig;
    }

    /**
     * @return the orderby
     */
    public String getOrderby() {
        return orderby;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public void setImgurlbig(String imgurlbig) {
        this.imgurlbig = imgurlbig;
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }
}
