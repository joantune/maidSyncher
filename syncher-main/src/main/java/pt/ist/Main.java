package pt.ist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.FenixFrameworkPlugin;
import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;
import pt.ist.maidSyncher.api.activeCollab.ACCategory;
import pt.ist.maidSyncher.api.activeCollab.ACComment;
import pt.ist.maidSyncher.api.activeCollab.ACContext;
import pt.ist.maidSyncher.api.activeCollab.ACInstance;
import pt.ist.maidSyncher.api.activeCollab.ACLoggedTime;
import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.api.activeCollab.ACProjectLabel;
import pt.ist.maidSyncher.api.activeCollab.ACSubTask;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.api.activeCollab.ACTaskLabel;
import pt.ist.maidSyncher.api.activeCollab.ACUser;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.github.GHComment;
import pt.ist.maidSyncher.domain.github.GHIssue;
import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;

public class Main {

    private static Config config = null;

    static final Properties ffProperties = new Properties();

    private static List<URL> urls = null;

    static {
        try {
            ffProperties.load(Main.class.getResourceAsStream("/configuration.properties"));
            config = new Config() {
                {
                    this.domainModelPaths = new String[0];
                    this.dbAlias = ffProperties.getProperty("db.alias");
                    this.dbUsername = ffProperties.getProperty("db.user");
                    this.dbPassword = ffProperties.getProperty("db.pass");
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
    }

    // FenixFramework will try automatic initialization when first accessed
    public static void main(String[] args) throws IOException {
        FenixFramework.bootStrap(config);
        FenixFramework.initialize();
        syncGitHub();
        syncActiveCollab();
    }

    private static void syncActiveCollab() throws IOException {
        // setup ActiveCollab
        Properties acConfigurationProperties = new Properties();
        acConfigurationProperties.load(Main.class.getResourceAsStream("/configuration.properties"));

        ACContext.setServer(acConfigurationProperties.getProperty("ac.server.host"));
        ACContext.setToken(acConfigurationProperties.getProperty("ac.server.token"));

        ACInstance instanceForDSI = ACInstance.getInstanceForCompanyName();
        pt.ist.maidSyncher.domain.activeCollab.ACInstance.process(instanceForDSI);
        System.out.println("ACInstance: " + instanceForDSI.getName());

        List<ACUser> users = instanceForDSI.getUsers();
        for (ACUser user : users) {
            System.out.println("ACUser: " + user.getName());
            pt.ist.maidSyncher.domain.activeCollab.ACUser.process(user);
        }


        //let's proccess all of the project labels
        for (ACProjectLabel acProjectLabel : ACContext.getACProjectLabels()) {
            System.out.println("ACProjectLabel: " + acProjectLabel.getName());
            pt.ist.maidSyncher.domain.activeCollab.ACProjectLabel.process(acProjectLabel);
        }

        //let's proccess all of the task assignment labels
        for (ACTaskLabel acTaskLabel : ACContext.getACTaskLabels()) {
            System.out.println("ACTaskLabel: " + acTaskLabel.getName());
            pt.ist.maidSyncher.domain.activeCollab.ACTaskLabel.process(acTaskLabel);
        }

        // load ActiveCollab project
        List<ACProject> acProjects = ACContext.getProjects();
        Iterator<ACProject> it = acProjects.iterator();
        while(it.hasNext()) {
            ACProject project = it.next();
            System.out
            .println(" Project " + project.getId() + " " + project.getName() + " updated on: " + project.getUpdatedOn());
            pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);

            System.out.println("Listing all of the milestones associated with this project");
            List<ACMilestone> milestones = project.getMilestones();
            for (ACMilestone milestone : milestones) {
                System.out.println("  Milestone: " + milestone.getName() + " updated on: " + milestone.getUpdatedOn() + " id: "
                        + milestone.getId());
                pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(milestone, project);
            }

            //let's get all of the task categories for this project
            Set<ACCategory> taskCategories = project.getTaskCategories();
            for (ACCategory taskCategory : taskCategories) {
                System.out.println("  Task Category: " + taskCategory.getName() + " id: " + taskCategory.getId());
            }
            ACTaskCategory.process(taskCategories, project);

            List<ACTask> acTasks = project.getTasks();
            Iterator<ACTask> itt = acTasks.iterator();
            while(itt.hasNext()) {
                ACTask task = itt.next();
                System.out.println("\tTask " + task.getId() + " " + task.getName() + " " + task.getDueOn() + " updated on: "
                        + task.getUpdatedOn());
                pt.ist.maidSyncher.domain.activeCollab.ACTask.process(task, project);

                ACCategory category = task.getCategory();
                if (category != null)
                    System.out.println("\t Category: " + category.getId() + " name: " + category.getName() + " updated on: "
                            + category.getUpdatedOn());

                //let's take care of the subtasks
                Set<ACSubTask> subTasks = task.getSubTasks();
                for (ACSubTask acSubTask : subTasks) {
                    System.out.println("\t\tSubTask: " + acSubTask.getId() + " " + acSubTask.getName() + " updated on: "
                            + acSubTask.getUpdatedOn());
                }
                pt.ist.maidSyncher.domain.activeCollab.ACSubTask.process(subTasks, task);

                //getting and printing all of the comments
                Set<ACComment> comments = task.getComments();
                System.out.println("\t\t Printing all the comments");
                for (ACComment comment : comments) {
                    System.out.println("\t\tComment: " + comment.getId() + " content: " + comment.getBody() + " updated on: "
                            + comment.getUpdatedOn());
                }
                pt.ist.maidSyncher.domain.activeCollab.ACComment.process(comments, task);
            }

            //let's print all of the logged times for this project
            List<ACLoggedTime> loggedTimes = project.getLoggedTimes();
            if (loggedTimes.isEmpty() == false) {
                System.out.println("\tPrinting logged times: ");
                for (ACLoggedTime acLoggedTime : loggedTimes) {
                    System.out.println("\t  Logged time: " + acLoggedTime.getId() + " " + acLoggedTime.getUpdatedOn() + " "
                            + acLoggedTime.getName() + " time: " + acLoggedTime.getValue());
                    pt.ist.maidSyncher.domain.activeCollab.ACLoggedTime.process(acLoggedTime);
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
//      hm.put("task[visibility]","1");
//      hm.put("task[category_id]","1");
        hm.put("subtask[label_id]","1");
//      hm.put("task[milestone_id]","1");
        hm.put("subtask[priority]","5");
        hm.put("subtask[due_on]","2013-10-01");
        ACContext.processPost("projects/1/tasks/10/subtasks/add", hm);
 */
    }


    private static void syncGitHub() {

        //let's try to connect to the GH Account
        Properties configurationProperties = new Properties();
        InputStream configurationInputStream = Main.class.getResourceAsStream("/configuration.properties");
        try {
            configurationProperties.load(configurationInputStream);
        } catch (IOException e) {
            throw new Error(e);
        }

        //let's try to authenticate and get the user and repository list

        String oauth2Token = configurationProperties.getProperty("github.oauth2.token");
        String organizationName = configurationProperties.getProperty("github.organization.name");

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(oauth2Token);

        RepositoryService repositoryService = new RepositoryService(client);
        OrganizationService organizationService = new OrganizationService(client);
        IssueService issueService = new IssueService(client);
        MilestoneService milestoneServiceService = new MilestoneService(client);
        LabelService labelService = new LabelService(client);

        List<User> orgMembers = null;
        List<Repository> repositories = null;
        try {
            orgMembers = organizationService.getMembers(organizationName);


            orgMembers.addAll(organizationService.getPublicMembers(organizationName));

            User organization = organizationService.getOrganization(organizationName);

            repositories = repositoryService.getRepositories(organization.getLogin());

            System.out.println("List of repositories:");
            for (Repository repository : repositories) {
                System.out.println(repository.getName());
                GHRepository.process(repository);

                List<Label> labels = labelService.getLabels(repository);
                System.out.println(" Has " + labels.size() + " labels, listing them");
                for (Label label : labels) {
                    System.out.println("  Label: " + label.getName());
                    GHLabel.process(label, repository);
                }

                List<Milestone> milestones = new ArrayList<Milestone>(milestoneServiceService.getMilestones(repository, "open"));

                milestones.addAll(milestoneServiceService.getMilestones(repository, "closed"));

                System.out.println(" Got " + milestones.size() + " milestones, listing them");
                for (Milestone milestone : milestones) {
                    System.out.println("  Milestone " + milestone.getTitle() + " url: " + milestone.getUrl() + " closed issues: "
                            + milestone.getClosedIssues() + " open issues: " + milestone.getOpenIssues());
                    GHMilestone.process(milestone, repository);
                }



                List<Issue> issues = issueService.getIssues(repository, new HashMap<String, String>());
                HashMap<String, String> stateClosedMap = new HashMap<>();
                stateClosedMap.put("state", "closed");
                issues.addAll(issueService.getIssues(repository, stateClosedMap));
                System.out.println(" Has " + issues.size()+" issues, listing them");
                for (Issue issue : issues) {
                    Milestone milestone = issue.getMilestone();
                    String milestoneString = milestone == null ? "-" : milestone.getTitle();
                    System.out.print("  Issue " + issue.getNumber() + " name: " + issue.getTitle() + " milestone: "
                            + milestoneString + " labels: ");
                    GHIssue.process(issue, repository);
                    for (Label label : issue.getLabels()) {
                        System.out.print(label.getName() + " ");
                    }
                    System.out.println();

                    int comments = issue.getComments();
                    if (comments > 0)
                    {
                        System.out.println("    got " + comments + " comments. Showing them:");
                        for (Comment comment : issueService.getComments(repository, issue.getNumber())) {
                            System.out.println("      Comment " + comment.getId() + " " + comment.getBody());
                            GHComment.process(comment, issue);
                        }
                    }
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }


        if (orgMembers != null && orgMembers.isEmpty() == false) {
            System.out.println("List of members:");
            for (User user : orgMembers) {
                System.out.println(user.getName() + user.getCompany() + user.getLogin());
            }
        }

    }


//    @Atomic
//    public static void frameworkInit() {
//        int teste;
//        if (FenixFramework.getRoot() == null) {
//            System.out.println("oops");
//        }
//
//    }
}
