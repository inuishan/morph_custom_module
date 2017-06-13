package custom.utils;


import org.codehaus.jackson.JsonNode;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by jayeshathila
 * on 19/05/17.
 */
public class GenericRestConnector {


    public static final Client JERSEY_CLIENT = ClientBuilder.newBuilder()
            .withConfig(new ClientConfig().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true))
            .register(JacksonFeature.class).build();

    private static final Client JERSEY_HTTPS_CLIENT;

    static {
        /**
         * https://gist.github.com/outbounder/1069465
         * https://jersey.java.net/documentation/latest/client.html#d0e5229
         * http://stackoverflow.com/questions/2145431/https-using-jersey-client
         * https://jersey.java.net/documentation/latest/migration.html
         */
        TrustManager[] certs = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        }};

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (java.security.GeneralSecurityException ignored) {
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        try {
            clientBuilder.sslContext(ctx);
            clientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
        }

        JERSEY_HTTPS_CLIENT = clientBuilder
                .withConfig(new ClientConfig().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true))
                .register(JacksonFeature.class).build();
    }


    private String hostname;
    private String basePath;
    private Client client;

    public Response get(String url) {
        WebTarget webTarget = getWebTargetForUrl(url);

        return webTarget.request().get();
    }

    public <T> T get(Invocation.Builder request, Class<T> responseClass) {
        return validateAndGetResponse(request, request.get(), responseClass);
    }

    public <T> T get(String url, Class<T> responseClass) {
        WebTarget webTarget = getWebTargetForUrl(url);
        Invocation.Builder request = webTarget.request();
        return get(request, responseClass);
    }

    public Document getJsoupDocument(String urlString) {
        Document document = Jsoup.parse(get(urlString, String.class));
        document.setBaseUri(urlString);
        return document;
    }

    public <T> T post(String url, JsonNode body, Class<T> responseClass) {
        WebTarget webTarget = getWebTargetForUrl(url);

        Invocation.Builder request = webTarget.request();

        return validateAndGetResponse(request, request.post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE)),
                responseClass);
    }

    public <T> T post(Invocation.Builder request, JsonNode body, Class<T> responseClass) {
        return validateAndGetResponse(request, request.post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE)),
                responseClass);
    }

    protected <T> T validateAndGetResponse(Invocation.Builder requestBuilder, Response requestResponse,
                                           Class<T> clazz) {
        return (T) validateAndGetResponse(requestBuilder, requestResponse, new GenericType<>(clazz));
    }

    public WebTarget getWebTargetForUrl(String url) {
        Client client;
        if (url != null && url.startsWith("https")) {
            client = JERSEY_HTTPS_CLIENT;
        } else {
            client = JERSEY_CLIENT;
        }

        return getWebTarget(url, null, client);
    }

    protected <T> T validateAndGetResponse(Invocation.Builder requestBuilder, Response requestResponse,
                                           GenericType<T> type) {
        Integer responseCode = requestResponse.getStatus();

        if (isSuccess(requestResponse)) {
            return requestResponse.readEntity(type);
        } else {
            String body = requestResponse.readEntity(String.class);

            String message = "[" + responseCode + " " + requestResponse.getStatusInfo().getReasonPhrase() + "] " + body;
            ConnectorException connectorException = new ConnectorException(requestResponse, requestBuilder,
                    responseCode, body, message);

            throw connectorException;
        }
    }

    private WebTarget getWebTarget(String hostname, String path, Client client) {
        WebTarget webTarget = client.target(hostname);
        if (basePath != null) {
            webTarget = webTarget.path(basePath);
        }
        if (path != null) {
            webTarget = webTarget.path(path);
        }


        return webTarget;
    }

    private boolean isSuccess(Response response) {
        if (response == null) {
            return false;
        }
        return Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily());
    }

}
