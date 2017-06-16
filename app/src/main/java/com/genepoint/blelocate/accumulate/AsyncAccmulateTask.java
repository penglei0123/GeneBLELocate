package com.genepoint.blelocate.accumulate;

import com.genepoint.blelocate.G;
import com.genepoint.blelocate.LogDebug;
import com.genepoint.blelocate.core.LocPoint;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class AsyncAccmulateTask implements Runnable {
    private static String TAG = "AsyncTask";
    private String deviceId = null;

    public AsyncAccmulateTask(String deviceId) {
        this.deviceId = deviceId;
    }

    public void run() {
        List<LocPoint> cacheList = new ArrayList<>();
        while (!G.locPointQueue.isEmpty()) {
            cacheList.add(G.locPointQueue.poll());
        }
        if (cacheList.size() > 0) {
            try {
                JSONObject uploadObject = new JSONObject();
                uploadObject.put("type", "locate_by_phone");
                uploadObject.put("building", G.buildingCode);
                JSONArray arr = new JSONArray();
                for (LocPoint p : cacheList) {
                    JSONObject obj = new JSONObject();
                    obj.put("mac", deviceId);
                    obj.put("building", G.buildingCode);
                    obj.put("floor", p.Floor); // 楼层
                    obj.put("corx", p.Xcor); // X
                    obj.put("cory", p.Ycor); // Y
                    obj.put("time", p.timeStamp); // Time
                    obj.put("coorType", "pix");
                    obj.put("locateType", "ble");
                    arr.put(obj);
                }
                uploadObject.put("data", arr);

                BasicHttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 1000);// 设置请求超时
                HttpConnectionParams.setSoTimeout(httpParams, 5000); // 设置等待数据超时
                DefaultHttpClient client = new DefaultHttpClient(httpParams);

                HttpPost post = new HttpPost(G.uploadURL);
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("data", uploadObject.toString()));
                post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                LogDebug.w(TAG, "async task start");

                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 上传成功
                    LogDebug.w(TAG, "async task success");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                LogDebug.w(TAG, "async task failure");
            }
            cacheList.clear();
        }
    }
}
