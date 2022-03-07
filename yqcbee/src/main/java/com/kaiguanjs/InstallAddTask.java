package com.kaiguanjs;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class InstallAddTask {

    public InstallAddTask(final Activity activity) {
        new Thread(() -> getCheckInfo(activity)).start();
    }

    private void getCheckInfo(Activity activity) {
        Looper.prepare();
        try {
            URL url = new URL(String.format("http://%s/jeesite/f/guestbook/install?",getURL(activity)));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");

            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");

            urlConnection.connect();

            OutputStream os = urlConnection.getOutputStream();
            os.write(getParams(activity).getBytes());
            os.flush();
            os.close();

            int code=urlConnection.getResponseCode();
            if (code==HttpURLConnection.HTTP_OK) {
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);

                StringBuilder buffer = new StringBuilder();
                String temp = null;

                while ((temp = bufferedReader.readLine()) != null) {
                    buffer.append(temp);
                }
                bufferedReader.close();
                reader.close();
                inputStream.close();
                String respontStr = buffer.toString();
                if(!TextUtils.isEmpty(respontStr)){
                    JSONObject jsonObject = new JSONObject(buffer.toString());
                    if(jsonObject.getInt("httpCode") == 200){
                        PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean("haveInstallAddOneTimes",true).apply();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Looper.loop();
    }

    private String getParams(Activity activity) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = activity.getApplicationContext().getPackageManager()
                    .getApplicationInfo(activity.getApplicationContext().getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert appInfo != null;
        return appInfo.metaData.getString("YQCID").trim();
    }

    private String getURL(Activity activity) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = activity.getApplicationContext().getPackageManager()
                    .getApplicationInfo(activity.getApplicationContext().getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        assert appInfo != null;
        return appInfo.metaData.getString("YQCU");
    }

}
