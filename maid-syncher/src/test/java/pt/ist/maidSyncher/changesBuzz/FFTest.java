/*******************************************************************************
 * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Luis Silva - ACGHSync
 *     João Antunes - initial API and implementation
 ******************************************************************************/
package pt.ist.maidSyncher.changesBuzz;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.FenixFrameworkPlugin;
import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;
import pt.ist.maidSyncher.domain.MaidRoot;

public class FFTest {

    private static List<URL> urls = null;

    protected static Config config;
    @BeforeClass
    public static void initTestFFramework() throws Exception {
        final Properties ffProperties = new Properties();
        config = null;
        try {
            InputStream resourceAsStream = FFTest.class.getResourceAsStream("/configuration.properties");
            org.junit.Assert.assertNotNull("no configuration for the test was found", resourceAsStream);
            ffProperties.load(resourceAsStream);
            config = new Config() {
                {
                    this.domainModelPaths = new String[0];
                    this.dbAlias = ffProperties.getProperty("test.db.alias");
                    this.dbUsername = ffProperties.getProperty("test.db.user");
                    this.dbPassword = ffProperties.getProperty("test.db.pass");
                    org.junit.Assert.assertNotNull("no configuration for the test was found", dbAlias);

                    this.appName = ffProperties.getProperty("app.name");
                    this.errorIfChangingDeletedObject = true;
//                    this.canCreateDomainMetaObjects = false;
                    this.updateRepositoryStructureIfNeeded = true;
                    this.rootClass = MaidRoot.class;
                    this.errorfIfDeletingObjectNotDisconnected = true;
                    this.plugins = new FenixFrameworkPlugin[0];
                }

                @Override
                public List<URL> getDomainModelURLs() {
                    if (urls == null) {
                        urls = new ArrayList<URL>();
                        try {
                            URL remote = Thread.currentThread().getContextClassLoader().getResource("remote.dml");
                            for (DmlFile dml : FenixFrameworkArtifact.fromName(ffProperties.getProperty("app.name"))
                                    .getFullDmlSortedList()) {
                                urls.add(dml.getUrl());
                                if (remote != null && dml.getUrl().toExternalForm().endsWith("remote-plugin.dml")) {
                                    urls.add(remote);
                                }
                            }
                        } catch (FenixFrameworkProjectException | IOException e) {
                            throw new Error(e);
                        }
                    }
                    return urls;
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Unable to load properties files.", e);
        }

        //let's clear the database
        dropTestDatabase(config);
        FenixFramework.bootStrap(config);
        FenixFramework.initialize();
    }

    protected static void dropTestDatabase(Config config) throws Exception {
        //just because, let's get all of the system properties

        Connection connect = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect =
                    DriverManager.getConnection("jdbc:mysql:" + config.getDbAlias() + "&user=" + config.getDbUsername()
                            + "&password=" + config.getDbPassword());

            String databaseSchema = config.getDbAlias().split("/")[3].split("\\?")[0];
            System.out.println("Database Schema: " + databaseSchema);
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            statement.execute("drop database " + databaseSchema);
            statement.close();


            statement = connect.createStatement();
            statement.execute("create database " + databaseSchema);

        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connect != null) {
                    connect.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {

            }
        }

    }

    @Test
    public void thisAlwaysPasses() {
        Assert.assertEquals(true, true);

    }

}
