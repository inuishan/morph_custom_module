package com.altin.custom_code;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jersey.repackaged.com.google.common.collect.Lists;
import morph.base.actions.Action;
import morph.base.actions.impl.PublishMessageAction;
import morph.base.beans.CustomCodeResponse;
import morph.base.beans.simplifiedmessage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ishanjain
 * @since 24/07/17
 */
public class MorphCustomCode implements RequestHandler<Map<String, Object>, CustomCodeResponse> {

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
    public CustomCodeResponse handleRequest(Map<String, Object> input, Context context) {

        Object userVariable = input.get("userVariable");

        List<String> feedIds = Lists.newArrayList();

        if(userVariable != null) {
            Map<String, Object> userVariables = (Map<String, Object>) userVariable;
            Object feed = userVariables.get("feed");
            if(feed == null) {
                feedIds = (List<String>) feed;
            }
        }


        if (feedIds == null) {
            feedIds = new ArrayList<>();
        }
        List<Action> rv = new ArrayList<Action>();

        rv.add(addWelcomeText());


        PublishMessageAction publishMessageAction = new PublishMessageAction();
        SimplifiedMessage simplifiedMessage = new SimplifiedMessage();
        ArrayList<SimplifiedMessagePayload> payloads = new ArrayList<SimplifiedMessagePayload>();
        CarousalMessagePayload carousalMessagePayload = new CarousalMessagePayload();
        payloads.add(carousalMessagePayload);
        simplifiedMessage.setMessages(payloads);
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
        CustomCodeResponse customCodeResponse = new CustomCodeResponse();
        customCodeResponse.setActions(rv);
        return customCodeResponse;

    }

    private Action addWelcomeText() {
        PublishMessageAction publishMessageAction = new PublishMessageAction();
        SimplifiedMessage simplifiedMessage = new SimplifiedMessage();
        ArrayList<SimplifiedMessagePayload> payloads = Lists.<SimplifiedMessagePayload>newArrayList();
        payloads.add(new TextMessagePayload().text("Hey there. Welcome to City on Facebook Messenger. Here, you can "
                + "sign up for daily news updates, breaking transfer stories, and key club information."));
        simplifiedMessage.setMessages(payloads);
        publishMessageAction.setSimplifiedMessage(simplifiedMessage);
        return publishMessageAction;

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