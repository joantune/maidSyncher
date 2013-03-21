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
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class ProjectSync
{

    public static void main(String[] args) throws IOException
    {
        // setup ActiveCollab
        Properties acConfigurationProperties = new Properties();
        acConfigurationProperties.load(ProjectSync.class.getResourceAsStream("/configuration.properties"));

        ACContext.setServer(acConfigurationProperties.getProperty("ac.server.host"));
        ACContext.setToken(acConfigurationProperties.getProperty("ac.server.token"));

        ACInstance instanceForDSI = ACInstance.getInstanceForCompanyName();
        System.out.println("ACInstance: " + instanceForDSI.getName());

        List<ACUser> users = instanceForDSI.getUsers();
        for (ACUser user : users) {
            System.out.println("ACUser: " + user.getName());
        }

        //let's proccess all of the project labels
        for (ACProjectLabel acProjectLabel : ACContext.getACProjectLabels()) {
            System.out.println("ACProjectLabel: " + acProjectLabel.getName());
        }

        //let's proccess all of the task assignment labels
        for (ACTaskLabel acTaskLabel : ACContext.getACTaskLabels()) {
            System.out.println("ACTaskLabel: " + acTaskLabel.getName());
        }


        // load ActiveCollab project
        List<ACProject> acProjects = ACContext.getProjects();
        Iterator<ACProject> it = acProjects.iterator();
        while(it.hasNext()) {

            ACProject project = it.next();

            //let's randomly create a test task on the projects
            //we get
            System.out.println("Creating a new test task");
            ACTask newTask = new ACTask();
            newTask.setName("task de teste - criada pelo syncher");
            newTask.setVisibility(true);
            ACTask.createTask(newTask, project);

            System.out
            .println(" Project " + project.getId() + " " + project.getName() + " updated on: " + project.getUpdatedOn());

            System.out.println("Listing all of the milestones associated with this project");
            List<ACMilestone> milestones = project.getMilestones();
            for (ACMilestone milestone : milestones) {
                System.out.println("  Milestone: " + milestone.getName() + " updated on: " + milestone.getUpdatedOn() + " id: "
                        + milestone.getId());
            }

            //let's get all of the task categories for this project
            Set<ACCategory> taskCategories = project.getTaskCategories();
            for (ACCategory taskCategory : taskCategories) {
                System.out.println("  Task Category: " + taskCategory.getName() + " id: " + taskCategory.getId());
            }

            List<ACTask> acTasks = project.getTasks();
            Iterator<ACTask> itt = acTasks.iterator();
            while(itt.hasNext()) {
                ACTask task = itt.next();
                System.out.println("\tTask " + task.getId() + " " + task.getName() + " " + task.getDueOn() + " updated on: "
                        + task.getUpdatedOn());
//                System.out.println("\t  SER " + task.toJSONString());
                ACCategory category = task.getCategory();
                if (category != null)
                    System.out.println("\t Category: " + category.getId() + " name: " + category.getName() + " updated on: "
                            + category.getUpdatedOn());

                //let's take care of the subtasks
                for (ACSubTask acSubTask : task.getSubTasks()) {
                    System.out.println("\t\tSubTask: " + acSubTask.getId() + " " + acSubTask.getName() + " updated on: "
                            + acSubTask.getUpdatedOn());
                }

                //getting and printing all of the comments
                Set<ACComment> comments = task.getComments();
                System.out.println("\t\t Printing all the comments");
                for (ACComment comment : comments) {
                    System.out.println("\t\tComment: " + comment.getId() + " content: " + comment.getBody() + " updated on: "
                            + comment.getUpdatedOn());
                }
            }

            //let's print all of the logged times for this project
            List<ACLoggedTime> loggedTimes = project.getLoggedTimes();
            if (loggedTimes.isEmpty() == false) {
                System.out.println("\tPrinting logged times: ");
                for (ACLoggedTime acLoggedTime : loggedTimes) {
                    System.out.println("\t  Logged time: " + acLoggedTime.getId() + " " + acLoggedTime.getUpdatedOn() + " "
                            + acLoggedTime.getName() + " time: " + acLoggedTime.getValue());
                }
            }
        }

/*
		List<ACTask> acTasks = acp.getTasks();
	    Iterator<ACTask> it = acTasks.iterator();
	    while(it.hasNext()) {
	    	ACTask task = it.next();
	    	System.out.println(" Task " + task.getId() + " " + task.getName() + " " + task.getDueOn());
	    	System.out.println(" SER " + task.toString());
	    }
 */
/*
	    HashMap<String,String> hm = new HashMap<String,String>();
	    hm.put("subtask[name]","This is a subtask of task 10!");
	    hm.put("subtask[body]","Body of task!");
//	    hm.put("task[visibility]","1");
//	    hm.put("task[category_id]","1");
	    hm.put("subtask[label_id]","1");
//	    hm.put("task[milestone_id]","1");
	    hm.put("subtask[priority]","5");
	    hm.put("subtask[due_on]","2013-10-01");
	    ACContext.processPost("projects/1/tasks/10/subtasks/add", hm);
 */
    }
}
