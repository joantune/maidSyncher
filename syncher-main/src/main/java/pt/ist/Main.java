package pt.ist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
import pt.ist.maidSyncher.domain.MaidRoot;
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
    public static void main(String[] args) {
        FenixFramework.bootStrap(config);
        FenixFramework.initialize();
        applicationCodeGoesHere();
    }

    public static void applicationCodeGoesHere() {

        if (Thread.currentThread().getContextClassLoader().getResourceAsStream("/log4j.properties") == null)
            System.out.println("couldn't find log4j.properties");
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
