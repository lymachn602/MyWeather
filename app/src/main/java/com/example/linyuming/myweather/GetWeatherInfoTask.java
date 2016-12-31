package com.example.linyuming.myweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linyuming on 2016/12/19.
 */
public class GetWeatherInfoTask extends AsyncTask<String,Void,List<Map<String,Object>>>{
    //activity的上下文
    private Activity context;
    //加载提示窗口
    private ProgressDialog progressDialog;
    //错误信息
    private String errorMsg="网络错误";
    //天气信息列表
    private ListView weather_info;
    //网络请求的基础URl
    private static String BASE_URL="http://v.juhe.cn/weather/index?format=2&cityname=";
    private static String key="99ad6b0c42204464b168e0638cfb9ec0";
    //网络访问时的进度对话框
    public GetWeatherInfoTask(Activity context){
        this.context=context;
        //获取天气时的提示框
        progressDialog =new ProgressDialog(context);
        progressDialog.setMessage("正在获取天气,请稍后...");
        progressDialog.setCancelable(false);
    }
    protected  void onPreExecute(){
        super.onPreExecute();
        progressDialog.show();
    }
    protected void  onPostExecute(List<Map<String,Object>> result){
        super.onPostExecute(result);
        progressDialog.dismiss();
        if (result.size()>0){
            weather_info = (ListView) context.findViewById(R.id.weather_info);
            //更新天气列表
            SimpleAdapter simpleAdapter =new SimpleAdapter(context,result,R.layout.weather_item,new String[]{
                    "temperature","weather","date","week","weather_icon"},new int[]{
                    R.id.temperature,R.id.weather,R.id.date,R.id.week,R.id.weather_icon
            });
            weather_info.setAdapter(simpleAdapter);
        }else{
            Toast.makeText(context,errorMsg,Toast.LENGTH_LONG).show();
        }
    }
    protected List<Map<String ,Object>> doInBackground(String...params){
        List<Map<String,Object>> list =new ArrayList<Map<String,Object>>();
        try{
            //连接网络
            HttpClient httpClient =new DefaultHttpClient();
            //创建访问url，参数转码
            String url =BASE_URL+ URLEncoder.encode(params[0],"UTF-8")+key;
            //创建httpget对象
            HttpGet httpget =new HttpGet(url);
            //httpClient执行httpget,获取response
            HttpResponse response = httpClient.execute(httpget);
            if (response.getStatusLine().getStatusCode()==200){
                //如果服务器做了gzip压缩的话，首先要设置gzip流解码
                //response,setEntity(new GzipDecompressingEntity(response.getEntity()));
                //获取Json字符串
                String jsonString = EntityUtils.toString(response.getEntity(),"UTF-8");
                JSONObject jsondata =new JSONObject(jsonString);
                if (jsondata.getInt("resultcode")==200){
                    JSONObject result =jsondata.getJSONObject("result");
                    JSONArray weatherList = result.getJSONArray("future");
                    for (int i=0; i<7;i++){
                        Map<String ,Object>  item =new HashMap<String ,Object>();
                        JSONObject weatObject = weatherList.getJSONObject(i);
                        //获取温度
                        item.put("temperature",weatObject.getString("temperature"));
                        //获取天气类型
                        item.put("weather",weatObject.getString("weather"));
                        //获取日期
                        item.put("date",weatObject.getString("date"));
                        //获取星期
                        item.put("week",weatObject.getString("week"));
                        //获取风向风力
                        item.put("wind",weatObject.getString("wind"));
                        //获取天气图标
                        JSONObject wid =weatObject.getJSONObject("weather_id");
                        int weather_icon=wid.getInt("fa");
                        //获取对应编号的天气图标
                        item.put("weather_icon",WeathIcon.weather_icons[weather_icon]);
                        list.add(item);
                    }
                }else{
                    errorMsg="非常抱歉，本应该暂不支持您所请求的城市!! ";
                }
            }else{
                errorMsg="网络错误，请检查手机是否开启了网络！！";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
