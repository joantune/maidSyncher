package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ProjectSync
{

    public static void main(String[] args) throws IOException
    {
        // setup ActiveCollab
        ACContext.setServer("censored");
        ACContext.setToken("censored");

        // setup GitHub
        GHContext.setServer("api.github.com");
        GHContext.setUsername("");
        GHContext.setPassword("");

        // load ActiveCollab project
        List<ACProject> acProjects = ACContext.getProjects();
        Iterator<ACProject> it = acProjects.iterator();
        while(it.hasNext()) {
            ACProject project = it.next();
            System.out.println(" Project " + project.getId() + " " + project.getName());
            List<ACTask> acTasks = project.getTasks();
            Iterator<ACTask> itt = acTasks.iterator();
            while(itt.hasNext()) {
                ACTask task = itt.next();
                System.out.println("     Task " + task.getId() + " " + task.getName() + " " + task.getDueOn());
                System.out.println("        SER " + task.toString());
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
        // load GitHub repo
        GHRepo ghr = new GHRepo("ljbmgs/test");
        System.out.println("--- GitHub Project [" + ghr.getName() + "] -----------------");

        List<GHIssue> ghIssues = ghr.getIssues();
        Iterator<GHIssue> iti = ghIssues.iterator();
        while(iti.hasNext()) {
            GHIssue issue = iti.next();
            System.out.println(" Issue " + issue.getId() + " " + issue.getTitle());
            List<GHComment> ghComments = issue.getComments();
            Iterator<GHComment> itc = ghComments.iterator();
            while(itc.hasNext()) {
                GHComment comment = itc.next();
                System.out.println("   Comment " + comment.getId() + " " + comment.getBody() + " " + comment.getUser());
            }
        }
    }
}