package com.github.react.sextant.fragment.util;

import java.util.UUID;

public class Crime {
    private UUID mId;

    public Crime(){
        mId = UUID.randomUUID();
    }

    public UUID getId(){
        return mId;
    }
}
