package custom.dhi;

import custom.utils.ConnectorException;
import custom.utils.GenericRestConnector;
import morph.base.actions.Action;
import morph.base.actions.impl.PublishMessageAction;
import morph.base.beans.simplifiedmessage.Button;
import morph.base.beans.simplifiedmessage.SimplifiedMessage;
import morph.base.beans.simplifiedmessage.TextMessagePayload;
import morph.base.beans.variables.BotContext;
import morph.base.modules.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (C) 2017 Scupids - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * @author aapa
 */
@Service
public class PayULink implements Module {

    private static final GenericRestConnector genericRestConnector = new GenericRestConnector();
    private ObjectMapper objectMapper;

    public PayULink() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String getModuleName() {
        return "PAYU_LINK";
    }

    public static void main(String[] args) {
        PayULink payULink = new PayULink();
        payULink.execute(null);
    }

    @Override
    public List<Action> execute(BotContext botContext) {
        WebTarget webTargetForUrl = genericRestConnector
                .getWebTargetForUrl("https://secure.snd.payu.com/pl/standard/user/oauth/authorize");
        Invocation.Builder requestBuilder = webTargetForUrl.request(MediaType.APPLICATION_JSON_TYPE);

        Form form = new Form();
        form.param("grant_type", "client_credentials");
        form.param("client_id", "301453");
        form.param("client_secret", "8566024f3773f6b9234531a93b84ac22");

        PayUAccessTokenDetails token = genericRestConnector
                .post(requestBuilder, form, MediaType.APPLICATION_FORM_URLENCODED_TYPE, PayUAccessTokenDetails.class);
        String accessToken = token.access_token;

        WebTarget orderWT = genericRestConnector.getWebTargetForUrl("https://secure.snd.payu.com/api/v2_1/orders/");
        Invocation.Builder orderRequestBuilder = orderWT.request(MediaType.APPLICATION_JSON_TYPE);
        orderRequestBuilder = orderRequestBuilder.header("Authorization", "Bearer " + accessToken);

        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("notifyUrl", "https://your.eshop.com/notify");
        bodyNode.put("customerIp", "127.0.0.1");
        bodyNode.put("merchantPosId", "301453");
        bodyNode.put("description", "RTV market");
        bodyNode.put("currencyCode", "PLN");
        bodyNode.put("totalAmount", "200");
        ArrayNode products = objectMapper.createArrayNode();
        ObjectNode product = objectMapper.createObjectNode();
        product.put("name", "DHI Appointment");
        product.put("unitPrice", "200");
        product.put("quantity", "1");
        products.add(product);
        bodyNode.put("products", products);

        String url = null;
        try {
            genericRestConnector.post(orderRequestBuilder, bodyNode, MediaType.APPLICATION_JSON_TYPE, String.class);
        } catch (ConnectorException e) {
            if (e.getCode() == 302) {
                url = (String) e.getResponse().getHeaders().getFirst("Location");
            }
        }

        SimplifiedMessage message = new SimplifiedMessage();
        TextMessagePayload payload = new TextMessagePayload();
        payload.setText(
                "Thank you for booking the appointment. Please click on the button below to pay appointment fees.");
        Button button = new Button();
        button.setTitle("Pay");
        button.setButtonType(Button.ButtonType.URL);
        button.setUrl(url);
        button.setWebviewHeightRatio(Button.WebviewHeightRatio.TALL);
        payload.setButtons(Collections.singletonList(button));
        message.setPayloads(Collections.singletonList(payload));
        PublishMessageAction e = new PublishMessageAction();
        e.setSimplifiedMessage(message);

        return Collections.singletonList(e);
    }

    private static class PayUAccessTokenDetails {
        private String access_token;
        private String token_type;
        private int expires_in;
        private String grant_type;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public int getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(int expires_in) {
            this.expires_in = expires_in;
        }

        public String getGrant_type() {
            return grant_type;
        }

        public void setGrant_type(String grant_type) {
            this.grant_type = grant_type;
        }
    }
}