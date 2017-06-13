package custom.mancity;

import morph.base.actions.Action;
import morph.base.actions.impl.PublishMessageAction;
import morph.base.beans.simplifiedmessage.*;
import morph.base.beans.variables.BotContext;
import morph.base.modules.Module;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author ishan
 * @since 09/06/17
 */
@Service
public class FeedInitialModule implements Module {

    private static final List<Feed> FEEDS = new ArrayList<Feed>();

    static {
        FEEDS.add(new Feed("Round Up", "5832d743ebf2f9c854634100", "A daily " + "round-up of the top trending and " +
                "shared stories on our website!", "https://s3-us-west-2.amazonaws" + ".com/storage.morph" +
                ".ai/p/mancity/Round+up.jpg"));
        FEEDS.add(new Feed("Social Buzz", "5832d810ebf2f9c854634101", "Top " + "trending City content and things " +
                "we love from around the web!", "https://s3-us-west-2.amazonaws" + ".com/storage.morph" +
                ".ai/p/mancity/Social+buzz+2.jpg"));
        FEEDS.add(new Feed("Pep Talk", "57f09a1f2ff6ad5c373ca4cc", "All the info " + "and latest news surrounding " +
                "our manager!", "https://s3-us-west-2.amazonaws.com/storage.morph" + ".ai/p/mancity/Pep+Talk.jpg"));

        FEEDS.add(new Feed("Matchday", "57f09a1f2ff6ad5c373ca4ca", "Days we live " + "for! Live alerts, news and more" +
                " on the day of the game.", "https://s3-us-west-2.amazonaws" + ".com/storage.morph" +
                ".ai/p/mancity/Matchday.jpg"));
    }


    @Override
    public String getModuleName() {
        return "CITY_FEED";
    }

    @Override
    public List<Action> execute(BotContext botContext) {

        Optional<Object> feed = botContext.getUserVariable("feed");
        Object o = feed.get();
        //noinspection unchecked
        List<String> feedIds = (List<String>) o;

        if (feedIds == null) {
            feedIds = new ArrayList<String>();
        }
        List<Action> rv = new ArrayList<Action>();
        PublishMessageAction publishMessageAction = new PublishMessageAction();
        SimplifiedMessage simplifiedMessage = new SimplifiedMessage();
        ArrayList<SimplifiedMessagePayload> payloads = new ArrayList<SimplifiedMessagePayload>();
        CarousalMessagePayload carousalMessagePayload = new CarousalMessagePayload();
        payloads.add(carousalMessagePayload);
        simplifiedMessage.setPayloads(payloads);
        publishMessageAction.setSimplifiedMessage(simplifiedMessage);
        rv.add(publishMessageAction);
        List<Element> elements = new ArrayList<Element>();
        for (Feed aFeed : FEEDS) {
            if (feedIds.contains(aFeed.id)) {
                elements.add(makeUnsubscribeCardForFeed(aFeed));
            } else {
                elements.add(makeSubscribeCardForFeed(aFeed));
            }
        }
        carousalMessagePayload.setCarousalElements(elements);
        return rv;
    }

    private Element makeSubscribeCardForFeed(Feed aFeed) {
        Element element = new Element();
        element.setImageUrl(aFeed.imageUrl);
        element.setTitle(aFeed.title);
        element.setSubtitle(aFeed.description);
        ArrayList<Button> buttons = new ArrayList<Button>();
        Button button = new Button();
        button.setTitle("Subscribe");
        button.setButtonType(Button.ButtonType.POSTBACK);
        buttons.add(button);
        element.setButtons(buttons);
        button.setPayload("#flowk_SYSTEM-FEED-SUBSCRIBE_" + aFeed.id);
        return element;
    }


    private Element makeUnsubscribeCardForFeed(Feed aFeed) {
        Element element = new Element();
        element.setImageUrl(aFeed.imageUrl);
        element.setTitle(aFeed.title);
        element.setSubtitle(aFeed.description);
        ArrayList<Button> buttons = new ArrayList<Button>();
        Button button = new Button();
        button.setTitle("Unsubscribe");
        button.setButtonType(Button.ButtonType.POSTBACK);
        buttons.add(button);
        element.setButtons(buttons);
        button.setPayload("#flowk_SYSTEM-FEED-UNSUBSCRIBE_" + aFeed.id);
        return element;
    }


    private static class Feed {
        private String title;
        private String id;
        private String description;
        private String imageUrl;

        public Feed(String title, String id, String description, String imageUrl) {
            this.title = title;
            this.id = id;
            this.description = description;
            this.imageUrl = imageUrl;
        }
    }
}