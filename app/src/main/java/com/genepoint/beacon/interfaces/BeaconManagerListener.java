package com.genepoint.beacon.interfaces;

import com.genepoint.beacon.sdk.bean.Beacon;
import com.genepoint.beacon.sdk.bean.BeaconThrowable;

import java.util.ArrayList;

public  interface BeaconManagerListener
{
 void onUpdateBeacon(ArrayList<Beacon> paramArrayList);

 void onError(BeaconThrowable paramThrowable);
}
