package com.github.react.sextant.recyclerview;

import java.util.Date;
import java.util.UUID;

/**
 * var Crime = {
 *      mId:UUID.randomUUID,
 *      mTitle:"",
 *      mDate: new Date(),
 *      mSolved:false
 *  }
 * **/
public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;

    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }
}