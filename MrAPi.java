import android.os.AsyncTask;
import android.os.HandlerThread;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

public class MrAPi extends AsyncTask<Void, Void, Void> {
    public final String HOST = "http://ticketyab.com/";
    private String response, token, target, method = "get", type = "string";
    private Integer status;
    private List<NameValuePair> nameValuePairs;
    private MrOnTaskExecute onTaskComplete;
    private MrOnTaskExecuteObject onTaskExecuteObject;
    private MrOnTaskExecuteList onTaskExecuteList;
    private Class object;

    /*
     * Developed by https://twitter.com/96rajabi
     */

    public MrAPi() {
        this.response = "";
        this.token = "";
        this.status = 0;
        this.target = "";
        this.nameValuePairs = new ArrayList<NameValuePair>();
    }

    public MrAPi(String uri, String method) {
        this.response = "";
        this.method = method;
        this.status = 0;
        this.token = "";
        this.target = HOST + uri;
        this.nameValuePairs = new ArrayList<NameValuePair>();
    }

    public MrAPi(String uri, String method, List<NameValuePair> nameValuePairs) {
        this.response = "";
        this.method = method;
        this.status = 0;
        this.token = "";
        this.target = HOST + uri;
        this.nameValuePairs = nameValuePairs;
    }

    public MrAPi(String uri, String method, List<NameValuePair> nameValuePairs, String token) {
        this.response = "";
        this.status = 0;
        this.token = token;
        this.target = HOST + uri;
        this.method = method;
        this.nameValuePairs = nameValuePairs;
    }

    public void getArrayList(Class object, MrOnTaskExecuteList onTaskExecuteList) {
        this.type = "list";
        this.object = object;
        this.onTaskExecuteList = onTaskExecuteList;
        this.execute();
    }

    public void getObject(Class object, MrOnTaskExecuteObject onTaskExecuteObject) {
        this.type = "object";
        this.object = object;
        this.onTaskExecuteObject = onTaskExecuteObject;
        this.execute();
    }

    public void getString(MrOnTaskExecute onTaskComplete) {
        this.type = "string";
        this.onTaskComplete = onTaskComplete;
        this.execute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (method.equals("post"))
                post_method();
            else
                get_method();
        } catch (Exception err) {
            Log.e("MrApi", "doInBackground", err);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            switch (type) {
                case "object":
                    if (status == 200) {
                        Gson gson = new Gson();
                        this.onTaskExecuteObject.onTaskSuccess(gson.fromJson(response, object));
                    } else
                        this.onTaskExecuteObject.onTaskFailure(status, response);
                    break;
                case "list":
                    if (status == 200) {
                        List<Object> objects = new ArrayList<Object>();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Gson gson = new Gson();
                            objects.add(gson.fromJson(String.valueOf(jsonArray.get(i)), object));
                        }
                        this.onTaskExecuteList.onTaskSuccess(objects);
                    } else
                        this.onTaskExecuteList.onTaskFailure(status, response);
                    break;
                default:
                    if (status == 200)
                        this.onTaskComplete.onTaskSuccess(response);
                    else
                        this.onTaskComplete.onTaskFailure(status, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void post_method() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Api connect " + target);

        Thread t = new HandlerThread("Handler") {
            @Override
            public void run() {
                try {
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost httpPost = new HttpPost(target);
                    System.out.println("Api body " + nameValuePairs.toString());
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    try {
                        if (token.length() > 0) {
                            httpPost.setHeader("Authorization", "Bearer " + token);
                            System.out.println("Api auth " + token);
                        }
                    } catch (Exception ignored) {
                    }
                    CloseableHttpResponse res = client.execute(httpPost);
                    HttpEntity entity = res.getEntity();
                    status = res.getStatusLine().getStatusCode();
                    response = EntityUtils.toString(entity, "utf-8");
                    System.out.println("Api response " + String.valueOf(getStatus()) + " : " + response);
                    client.close();
                } catch (Exception e) {
                    System.out.println("Api catch " + e.getMessage());
                }
                latch.countDown();
            }
        };
        t.start();
        latch.await();
    }

    private void get_method() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Api connect " + target);

        final Thread t = new HandlerThread("Handler") {
            @Override
            public void run() {
                try {
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpGet httpPost = new HttpGet(target);
                    System.out.println("Api body " + nameValuePairs.toString());
                    httpPost.setHeader("Content-Type", "application/json");
                    try {
                        if (token.length() > 0) {
                            httpPost.setHeader("Authorization", "Bearer " + token);
                            System.out.println("Api auth " + token);
                        }
                    } catch (Exception ignored) {
                    }
                    CloseableHttpResponse res = client.execute(httpPost);
                    HttpEntity entity = res.getEntity();
                    status = res.getStatusLine().getStatusCode();
                    response = EntityUtils.toString(entity, "utf-8");
                    System.out.println("Api response " + String.valueOf(getStatus()) + " : " + response);
                    client.close();
                } catch (Exception e) {
                    System.out.println("Api catch " + e.getMessage());
                }
                latch.countDown();
            }
        };
        t.start();
        latch.await();
    }

    public interface MrOnTaskExecute {
        void onTaskSuccess(String response);

        void onTaskFailure(Integer status, String response);
    }

    public interface MrOnTaskExecuteObject {
        void onTaskSuccess(Object object);

        void onTaskFailure(Integer status, String response);
    }

    public interface MrOnTaskExecuteList {
        void onTaskSuccess(List<Object> objects);

        void onTaskFailure(Integer status, String response);
    }
}
