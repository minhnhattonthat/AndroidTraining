package com.nhatton.ggtalkvn;

public class Sound {
    private String mDescription = "";
    private String mTag = "";
    private int mSoundResourceId = -1;

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setSoundResourceId(int id) {
        mSoundResourceId = id;
    }

    public int getSoundResourceId() {
        return mSoundResourceId;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

}
