package pt.ist.syncherWebRestserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.lang.StringUtils;

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.groups.DynamicGroup;
import pt.ist.bennu.core.domain.groups.UserGroup;
import pt.ist.fenixframework.Atomic;
import pt.ist.maidSyncher.domain.MaidRoot;

@WebListener
public class SyncSystemInitializerListener implements ServletContextListener {
    public static final String ADMIN_USERNAME_PROPERTY = "admin.username";
    public static final String ADMIN_PASSWORD_PROPERTY = "admin.firstPassword";
    public static final String ADMIN_ENFORCE_PROPERTY = "admin.enforce";
    public static final String ADMIN_GROUP_PROPERTY = "admins.group";

    @Override
    @Atomic
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SyncherSystem.init();
            SyncherSystem.initSchedule();
            MaidRoot.getInstance();
            //let's add the user
            Properties configurationProperties = new Properties();
            InputStream configurationInputStream = MaidRoot.class.getResourceAsStream("/configuration.properties");
            if (configurationInputStream != null) {
                configurationProperties.load(configurationInputStream);
            }
            Boolean enforceAdmin = Boolean.valueOf(configurationProperties.getProperty(ADMIN_ENFORCE_PROPERTY, "false"));
            String adminUsername = configurationProperties.getProperty(ADMIN_USERNAME_PROPERTY);
            User adminUser = null;
            if (adminUsername != null && (enforceAdmin || User.findByUsername(adminUsername) == null)) {
                adminUser = User.findByUsername(adminUsername);
                String password = configurationProperties.getProperty(ADMIN_PASSWORD_PROPERTY);
                if (adminUser == null) {
                    adminUser = new User(adminUsername);
                    adminUser.setPassword(password);
                } else if (enforceAdmin) {
                    adminUser.setPassword(password);
                }

            }

            String adminUsernames = configurationProperties.getProperty(ADMIN_GROUP_PROPERTY);
            if (adminUsernames == null && adminUser != null)
            {
                DynamicGroup.initialize("managers", UserGroup.getInstance(adminUser));
            }
            if (adminUsernames != null) {
                Set<User> admins = new HashSet<>();
                String[] adminUsernamesSplitted = StringUtils.split(adminUsernames);
                for (String username : adminUsernamesSplitted) {
                    if (username == null)
                        continue;
                    User user = User.findByUsername(username);
                    if (user == null)
                    {
                        user = new User(username);
                    }
                    admins.add(user);
                }
                admins.add(adminUser);
                DynamicGroup.initialize("managers", UserGroup.getInstance(admins));
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
