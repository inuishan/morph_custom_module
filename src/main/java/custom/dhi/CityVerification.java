package custom.dhi;

import morph.base.actions.Action;
import morph.base.actions.VariableScope;
import morph.base.actions.impl.GoToFlowAction;
import morph.base.actions.impl.PublishMessageAction;
import morph.base.actions.impl.SetVariableAction;
import morph.base.beans.simplifiedmessage.SimplifiedMessage;
import morph.base.beans.simplifiedmessage.SimplifiedMessagePayload;
import morph.base.beans.simplifiedmessage.TextMessagePayload;
import morph.base.beans.variables.BotContext;
import morph.base.modules.Module;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author ishan
 * @since 05/06/17
 */
@Service
public class CityVerification implements Module {

    private static final Set<String> VALID_CITY_NAMES = new HashSet<String>();

    static {
        VALID_CITY_NAMES.add("delhi");
        VALID_CITY_NAMES.add("gurugram");
        VALID_CITY_NAMES.add("mumbai");
        VALID_CITY_NAMES.add("bangalore");
        VALID_CITY_NAMES.add("pune");
        VALID_CITY_NAMES.add("chennai");
        VALID_CITY_NAMES.add("kolkata");
        VALID_CITY_NAMES.add("kochi");
        VALID_CITY_NAMES.add("ahmedabad");
        VALID_CITY_NAMES.add("hyderabad");
        VALID_CITY_NAMES.add("chandigarh");
        VALID_CITY_NAMES.add("ludhiana");
        VALID_CITY_NAMES.add("jaipur");
        VALID_CITY_NAMES.add("surat");
        VALID_CITY_NAMES.add("calicut");
        VALID_CITY_NAMES.add("saharanpur");
        VALID_CITY_NAMES.add("guwahati");
        VALID_CITY_NAMES.add("lucknow");
    }


    @Override
    public String getModuleName() {
        return "CITY_VERIFICATION";
    }

    @Override
    public List<Action> execute(BotContext botContext) {
        Optional<Object> flowVariable = botContext.getFlowVariable("5936f80454e66318322eed25");
        List<Action> actions = new ArrayList<Action>();
        Object o = flowVariable.get();
        String lastMessage = (String) o;
        if (VALID_CITY_NAMES.contains(lastMessage.toLowerCase())) {
            actions.add(new SetVariableAction(VariableScope.USER, "5935644354e6637f6a976d99", lastMessage.toLowerCase()));
            SimplifiedMessage message = new SimplifiedMessage();
            TextMessagePayload payload = new TextMessagePayload();
            payload.setText("Hello variable set" + botContext.getUser().getName());
            ArrayList<SimplifiedMessagePayload> payloads = new ArrayList<SimplifiedMessagePayload>();
            payloads.add(payload);
            message.setPayloads(payloads);
            PublishMessageAction e = new PublishMessageAction();
            e.setSimplifiedMessage(message);
            actions.add(e);
        } else {
            SimplifiedMessage message = new SimplifiedMessage();
            TextMessagePayload payload = new TextMessagePayload();
            payload.setText("Hello variable not set" + botContext.getUser().getName());
            ArrayList<SimplifiedMessagePayload> payloads = new ArrayList<SimplifiedMessagePayload>();
            payloads.add(payload);
            message.setPayloads(payloads);
            PublishMessageAction e = new PublishMessageAction();
            e.setSimplifiedMessage(message);
            actions.add(e);
            actions.add(new GoToFlowAction("cityFailed", false));
        }
        return actions;
    }
}