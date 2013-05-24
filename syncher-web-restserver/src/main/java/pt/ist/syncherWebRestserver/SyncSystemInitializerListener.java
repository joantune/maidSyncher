package pt.ist.syncherWebRestserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SyncSystemInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SyncherSystem.init();

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }


}
