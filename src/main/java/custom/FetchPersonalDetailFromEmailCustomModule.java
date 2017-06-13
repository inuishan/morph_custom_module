package custom;

import custom.clearbit.model.ClearbitPersonalInfo;
import custom.utils.GenericRestConnector;
import jersey.repackaged.com.google.common.collect.Lists;
import morph.base.actions.Action;
import morph.base.actions.StringVariable;
import morph.base.actions.VariableScope;
import morph.base.actions.impl.SetVariableAction;
import morph.base.beans.variables.BotContext;
import morph.base.modules.Module;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by jayeshathila
 * on 20/05/17.
 */
@Service
public class FetchPersonalDetailFromEmailCustomModule implements Module {

    private static final String CLEARBIT_API_KEY = "<CLEARBIT_API_KEY>";
    private static final String clearbitPath = "https://person.clearbit.com/v1/people/email/";

    private static final GenericRestConnector genericRestConnector = new GenericRestConnector();


    @Override
    public String getModuleName() {
        return "fetchEmailDetails";
    }

    @Override
    public List<Action> execute(BotContext botContext) {
        Optional<Object> emailId = botContext.getFlowVariable("emailId");
        if (!emailId.isPresent()) {
            return Collections.emptyList();
        }

        WebTarget webTargetForUrl = genericRestConnector.getWebTargetForUrl(clearbitPath + emailId.get());
        Invocation.Builder requestBuilder = webTargetForUrl.request(MediaType.APPLICATION_JSON_TYPE);
        requestBuilder.header("Authorization", "Bearer " + CLEARBIT_API_KEY);
        ClearbitPersonalInfo clearbitPersonalInfo = genericRestConnector.get(requestBuilder, ClearbitPersonalInfo
                .class);

        if (clearbitPersonalInfo == null || clearbitPersonalInfo.getGeo() == null) {
            return Collections.emptyList();
        }

        SetVariableAction setVariableAction = new SetVariableAction(VariableScope.FLOW, "country", new StringVariable
                ().value(clearbitPersonalInfo.getGeo().getCountry()));
        return Lists.<Action>newArrayList(setVariableAction);
    }
}