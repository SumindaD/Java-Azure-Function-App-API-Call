package aialk;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function1 {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpsTestWithCert")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");
        

        try
        {
            context.getLogger().info("Sending API Request");

            KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null,null);

            KeyManagerFactory keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, null);

            TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
              keyManagerFactory.getKeyManagers(),
              trustManagerFactory.getTrustManagers(),
              new SecureRandom());

            HttpPost post = new HttpPost("https://httpbin.org/post");

            // add request parameter, form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("username", "abc"));
            urlParameters.add(new BasicNameValuePair("password", "123"));
            urlParameters.add(new BasicNameValuePair("custom", "secret"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            CloseableHttpClient httpClient = HttpClients
                .custom()
                .setSSLContext(sslContext).build();
            
            CloseableHttpResponse response = httpClient.execute(post);

            return request.createResponseBuilder(HttpStatus.OK).body(EntityUtils.toString(response.getEntity())).build();
            
        }
        catch(Exception ex)
        {
            context.getLogger().info(ex.getMessage());
            return request.createResponseBuilder(HttpStatus.OK).body(ex.getMessage()).build();
        }

    }

}


