package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.plugins.llms.LLM;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class ChatAction implements UnprotectedRootAction {

    @Override
    public String getIconFileName() {
        // This icon will be used in the Jenkins top-level menu.
        return "symbol-chatbubbles-outline plugin-ionicons-api";
    }

    @Override
    public String getDisplayName() {
        return "Jenkins AI Assistant";
    }

    @Override
    public String getUrlName() {
        return "llm-chat";
    }

    /**
     * The default view for the chat interface.
     * Jenkins will render the file at: resources/io/jenkins/plugins/sample/ChatAction/index.jelly.
     */
    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        req.getView(this, "index.jelly").forward(req, rsp);
    }

    /**
     * AJAX endpoint to process the chat request.
     * Accessible as: /llm-chat/chat?query=your+query
     */
    public void doChat(StaplerRequest req, StaplerResponse rsp, @QueryParameter("query") String query)
            throws IOException {
        rsp.setContentType("text/plain;charset=UTF-8");

        String answer = "There was an error in resolving this query !";
        try {
            LLM llm = new LLM();
            answer = llm.ragRetrievalWithLLM(query);
        } catch (Exception e) {

        } finally {
            rsp.getWriter().write(answer);
        }
    }
}
