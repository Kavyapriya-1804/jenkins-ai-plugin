package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
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
        return "symbol-chat plugin-ionicons-api";
    }

    @Override
    public String getDisplayName() {
        return "LLM Chat";
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

        // Create an instance of your LLM wrapper
        HelloLlm llm = new HelloLlm();
        String answer = llm.talkToLLM(query);
        rsp.getWriter().write(answer);
    }
}
