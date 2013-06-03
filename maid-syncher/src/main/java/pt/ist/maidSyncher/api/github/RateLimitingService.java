/**
 * 
 */
package pt.ist.maidSyncher.api.github;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.GitHubService;

import com.google.common.reflect.TypeToken;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 31 de Mai de 2013
 * 
 * 
 */
public class RateLimitingService extends GitHubService {

    private static final String SEGMENT_RATE_LIMIT = "/rate_limit";

    /**
     * 
     */
    public RateLimitingService() {
        super();
    }

    /**
     * @param client
     */
    public RateLimitingService(GitHubClient client) {
        super(client);
    }

    public static class RateLimits implements Serializable {
        /**
         * Default serial version
         */
        private static final long serialVersionUID = 1L;
        int remaining;
        int limit;

        public int getRemaining() {
            return remaining;
        }

        public void setRemaining(int remaining) {
            this.remaining = remaining;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

    }

    public RateLimits getRemainingHits() throws IOException {
        String uri = SEGMENT_RATE_LIMIT;
        GitHubRequest gitHubRequest = createRequest();
        gitHubRequest.setUri(uri);
        gitHubRequest.setType(new TypeToken<RateLimits>() {
        }.getType());
        return (RateLimits) client.get(gitHubRequest).getBody();

    }

}
