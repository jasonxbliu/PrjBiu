package com.jaf.bean;

/**
 * Created by jarrah on 2015/4/15.
 */
public class BeanRequestNearby extends BeanRequest {
    private int idType;
    private int lastId;

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }
}
