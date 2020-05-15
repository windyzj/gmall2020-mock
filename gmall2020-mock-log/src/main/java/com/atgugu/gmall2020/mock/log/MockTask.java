package com.atgugu.gmall2020.mock.log;

import com.atgugu.gmall2020.mock.log.config.AppConfig;
import com.atguigu.gmall2020.mock.db.util.ConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;


@Component
public class MockTask {


    @Autowired
    ThreadPoolTaskExecutor poolExecutor;

    public void mainTask( ) {

        for (int i = 0; i < AppConfig.mock_count; i++) {
            //poolExecutor.execute(new Mocker());
            System.out.println("active+" + poolExecutor.getActiveCount());
              new Mocker().run();
        }
    }
}
