package pt.ist.syncherWebRestserver;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SyncSystemInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SyncherSystem.init();
            SyncherSystem.initSchedule();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }


}
