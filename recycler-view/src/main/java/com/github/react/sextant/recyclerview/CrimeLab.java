package com.github.react.sextant.recyclerview;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 创建单例
 *
 * **/
public class CrimeLab {

    private static CrimeLab sCrimeLab;  //Android开发的命名约定：s前缀为静态变量
    private List<Crime> mCrimeList;

    /**
     * 创建sCrimeLab单例
     * **/
    public static CrimeLab get(Context context){
        if(sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context){
        mCrimeList = new ArrayList<>();

        //pull data
        for(int i=0;i<100;i++){
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 == 0);    //Every other one 偶数为true
            mCrimeList.add(crime);
        }
    }

    public List<Crime> getmCrimeList(){
        return mCrimeList;
    }

    public Crime getCrime(UUID id){
        for(Crime crime:mCrimeList){
            if(crime.getId().equals(id)){
                return crime;
            }
        }

        return null;
    }
}