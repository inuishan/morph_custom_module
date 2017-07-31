package custom.dhi;

import custom.utils.ConnectorException;
import custom.utils.GenericRestConnector;
import jersey.repackaged.com.google.common.collect.Maps;
import morph.base.actions.Action;
import morph.base.actions.impl.PublishMessageAction;
import morph.base.beans.simplifiedmessage.Button;
import morph.base.beans.simplifiedmessage.SimplifiedMessage;
import morph.base.beans.simplifiedmessage.TextMessagePayload;
import morph.base.beans.variables.BotContext;
import morph.base.modules.Module;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Copyright (C) 2017 Scupids - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * @author aapa
 */
@Service
public class PayULink implements Module {

    private static final Map<String, Double> CITY_VS_AMOUNT = Maps.newHashMap();


    static {
        CITY_VS_AMOUNT.put("delhi", 500D); //north
        CITY_VS_AMOUNT.put("gurugram", 500D); //north
        CITY_VS_AMOUNT.put("mumbai", 500D);//west
        CITY_VS_AMOUNT.put("bangalore", 500D);//south
        CITY_VS_AMOUNT.put("pune", 400D);//west
        CITY_VS_AMOUNT.put("chennai", 350D);//south
        CITY_VS_AMOUNT.put("kolkata", 350D);//east
        CITY_VS_AMOUNT.put("kochi", 350D);//south
        CITY_VS_AMOUNT.put("ahmedabad", 350D);//west
        CITY_VS_AMOUNT.put("hyderabad", 350D);//south
        CITY_VS_AMOUNT.put("chandigarh", 350D);//north
        CITY_VS_AMOUNT.put("ludhiana", 350D);//north
        CITY_VS_AMOUNT.put("jaipur", 350D);//west
        CITY_VS_AMOUNT.put("surat", 250D);//west
        CITY_VS_AMOUNT.put("calicut", 350D);//south
        CITY_VS_AMOUNT.put("saharanpur", 350D);//north
        CITY_VS_AMOUNT.put("guwahati", 350D);//east
        CITY_VS_AMOUNT.put("lucknow", 350D);//north
    }

    private static final GenericRestConnector genericRestConnector = new GenericRestConnector();

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

        Optional<Object> flowVariable = botContext.getFlowVariable("5935644354e6637f6a976d99");
        Object o = flowVariable.get();
        String city = (String) o;
        double amount = CITY_VS_AMOUNT.get(city.toLowerCase());


        WebTarget webTargetForUrl = genericRestConnector.getWebTargetForUrl("https://secure.payu.in/_payment");
        webTargetForUrl = webTargetForUrl.property(ClientProperties.FOLLOW_REDIRECTS, false);
        Invocation.Builder requestBuilder = webTargetForUrl.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
//
// hash=cc605fb7acad1690c661f8c600d3858df7e01124fe308a5ab56f59015efc7105498f2e3acbeea344a3c04f221eb9fbd7437e6482f48bbc9d14769e24b38b8f3b
        Form form = new Form();
        String key = "KQ68DK";
        form.param("key", key);
        String transactionId = UUID.randomUUID().toString().substring(0, 23);
        form.param("txnid", transactionId);
        form.param("amount", String.valueOf(amount));
        String productInfo = "DHI Appointment booking";
        form.param("productinfo", productInfo);
        String name = getStringValueUserVar(botContext, "#SYS_NAME");
        form.param("firstname", name);
        String email = getStringValueUserVar(botContext, "#SYS_EMAIL");
        form.param("email", email);
        String phoneNumber = getStringValueUserVar(botContext, "_PHONE_NUMBER");
        form.param("phone", phoneNumber);
        String successUrl = "https://app.morph.ai/pages/payment-success.html";
        form.param("surl", successUrl);
        String failureUrl = "https://app.morph.ai/pages/payment-failed.html";
        form.param("furl", failureUrl);
        String hash = hashCal("SHA-512",
                key + "|" + transactionId + "|" + amount + "|" + productInfo + "|" + name + "|" + email +
                        "|||||||||||" + "MCtAa9kT");
        form.param("hash", hash);

        String url = null;
        try {
            Response response = requestBuilder.post(Entity.entity(form, MediaType.APPLICATION_JSON_TYPE));
            url = response.getHeaderString("Location");
        } catch (ConnectorException e) {
            if (e.getCode() == 302) {
                url = (String) e.getResponse().getHeaders().getFirst("Location");
            }
        }


//        PayUAccessTokenDetails token = genericRestConnector
//                .post(requestBuilder, form, MediaType.APPLICATION_FORM_URLENCODED_TYPE, PayUAccessTokenDetails.class);
//        String accessToken = token.access_token;
//
//        WebTarget orderWT = genericRestConnector.getWebTargetForUrl("https://secure.snd.payu.com/api/v2_1/orders/");
//        Invocation.Builder orderRequestBuilder = orderWT.request(MediaType.APPLICATION_JSON_TYPE);
//        orderRequestBuilder = orderRequestBuilder.header("Authorization", "Bearer " + accessToken);
//
//        ObjectNode bodyNode = objectMapper.createObjectNode();
//        bodyNode.put("notifyUrl", "https://your.eshop.com/notify");
//        bodyNode.put("customerIp", "127.0.0.1");
//        bodyNode.put("merchantPosId", "301453");
//        bodyNode.put("description", "RTV market");
//        bodyNode.put("currencyCode", "PLN");
//        bodyNode.put("totalAmount", "200");
//        ArrayNode products = objectMapper.createArrayNode();
//        ObjectNode product = objectMapper.createObjectNode();
//        product.put("name", "DHI Appointment");
//        product.put("unitPrice", "200");
//        product.put("quantity", "1");
//        products.add(product);
//        bodyNode.put("products", products);

//        String url = null;
//        try {
//            genericRestConnector.post(orderRequestBuilder, bodyNode, MediaType.APPLICATION_JSON_TYPE, String.class);
//        } catch (ConnectorException e) {
//            if (e.getCode() == 302) {
//                url = (String) e.getResponse().getHeaders().getFirst("Location");
//            }
//        }

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
        message.setMessages(Collections.singletonList(payload));
        PublishMessageAction e = new PublishMessageAction();
        e.setSimplifiedMessage(message);

        return Collections.singletonList(e);
    }


    private String hashCal(String type, String str) {
        byte[] hashseq = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();


            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1) hexString.append("0");
                hexString.append(hex);
            }

        } catch (NoSuchAlgorithmException nsae) {
        }

        return hexString.toString();
    }

    private String getStringValueUserVar(BotContext botContext, String key) {

        Optional<Object> flowVariable = botContext.getUserVariable(key);
        Object o = flowVariable.get();
        return (String) o;
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