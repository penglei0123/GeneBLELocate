package com.genepoint.blelocate.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsj on 2017/6/15.
 */

public class ResultLoc {
    public  int status;

    public ResultLoc(int status){
        this.status=status;
    }

    public List<LocPoint> resultsLoc;
    public ResultLoc(LocPoint myFinalLoc,LocPoint preLoc){
        resultsLoc=new ArrayList<>();
        resultsLoc.add(myFinalLoc);
        resultsLoc.add(preLoc);
    }


}
