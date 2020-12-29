package com.atgugu.gmall2020.mock.log;

import com.alibaba.fastjson.JSON;
import com.atgugu.gmall2020.mock.log.bean.*;
import com.atgugu.gmall2020.mock.log.config.AppConfig;
import com.atgugu.gmall2020.mock.log.enums.PageId;
import com.atgugu.gmall2020.mock.log.util.HttpUtil;
import com.atgugu.gmall2020.mock.log.util.KafkaUtil;
import com.atgugu.gmall2020.mock.log.util.LogUtil;
import com.atguigu.gmall2020.mock.db.util.ConfigUtil;
import com.atguigu.gmall2020.mock.db.util.ParamUtil;
import com.atguigu.gmall2020.mock.db.util.RandomNum;
import com.atguigu.gmall2020.mock.db.util.RandomOptionGroup;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class Mocker implements Runnable  {

    private Long ts;

    @Autowired
    KafkaTemplate kafkaTemplate;

    public  List<AppMain> doAppMock(){

        List<AppMain> logList=new ArrayList<>();

        Date curDate = ParamUtil.checkDate(AppConfig.mock_date);
        ts=  curDate.getTime();

       // 启动
        AppMain.AppMainBuilder appMainBuilder = AppMain.builder();
        AppCommon appCommon =   AppCommon.build() ;
        appMainBuilder.common(appCommon);

        appMainBuilder.checkError();
       AppStart appStart = new AppStart.Builder().build();
        appMainBuilder.start(appStart);
        appMainBuilder.ts(ts);

       logList.add(appMainBuilder.build());

       // 读取配置
       String jsonFile = ConfigUtil.loadJsonFile("path.json");
       List<Map> pathList = JSON.parseArray(jsonFile, Map.class);

       RandomOptionGroup.Builder<List> builder =   RandomOptionGroup.builder();

       //抽取一个访问路径
       for (Map map : pathList) {
           List path = (List) map.get("path");
           Integer rate = (Integer) map.get("rate");
           builder.add(path,rate);
       }
       List chosenPath = builder.build().getRandomOpt().getValue();
        //ts+=appStart.getLoading_time() ;
       //逐个输入日志
        // 每条日志  1  主行为  2 曝光  3 错误
       PageId lastPageId=null;
       for (Object o : chosenPath) {
           AppMain.AppMainBuilder pageBuilder = AppMain.builder().common(appCommon);

           String path = (String) o;

           int pageDuringTime = RandomNum.getRandInt(1000, AppConfig.page_during_max_ms);
           //添加页面
           PageId pageId = EnumUtils.getEnum(PageId.class, path);
           AppPage page =   AppPage.build (pageId,lastPageId,pageDuringTime) ;
           if(pageId==null){
               System.out.println();
           }
           pageBuilder.page(page);
           //置入上一个页面
           lastPageId=page.getPage_id();

           //页面中的动作
           List<AppAction> appActionList =   AppAction.buildList (page,ts,pageDuringTime) ;
           if(appActionList.size()>0){
               pageBuilder.actions(appActionList);
           }

           List<AppDisplay> displayList = AppDisplay.buildList(page);
           if(displayList.size()>0){
               pageBuilder.displays(displayList);
           }
           pageBuilder.ts(ts);
           pageBuilder.checkError();
           logList.add(pageBuilder.build());
          // ts+= pageDuringTime ;
       }

       //  随机发送通知日志
    //   System.out.println(logList);

        return logList;
   }








    public static void main(String[] args) {
       // System.out.println(RandomStringUtils.random(16,true,true));
        new Mocker( ).doAppMock();
    }


    public void run() {

        List<AppMain> appMainList = doAppMock();

        for (AppMain appMain : appMainList) {
            if(AppConfig.mock_type.equals("log")){
                LogUtil.log(appMain.toString());
            }else if(AppConfig.mock_type.equals("http")){
                HttpUtil.get(appMain.toString());
            }else if(AppConfig.mock_type.equals("kafka")){
                  KafkaUtil.send(AppConfig.kafka_topic,appMain.toString());
            }
            try {
                Thread.sleep(AppConfig.log_sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
