package custom.dhi;

import morph.base.actions.Action;
import morph.base.actions.VariableScope;
import morph.base.actions.impl.GoToFlowAction;
import morph.base.actions.impl.SetVariableAction;
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
        VALID_CITY_NAMES.add("gurgaon");
        VALID_CITY_NAMES.add("delhi");
    }


    @Override
    public String getModuleName() {
        return "CITY_VERIFICATION";
    }

    @Override
    public List<Action> execute(BotContext botContext) {
        Optional<Object> flowVariable = botContext.getFlowVariable("#understanderResponse_#message");
        Object o = flowVariable.get();
        String lastMessage = (String) o;
        if (VALID_CITY_NAMES.contains(lastMessage.toLowerCase())) {
           return Collections.<Action>singletonList(new SetVariableAction(VariableScope.FLOW, "sjdks", lastMessage.toLowerCase()));
        }
        return Collections.<Action>singletonList(new GoToFlowAction("ddd", false));
    }
}