package helpers;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class PoolingClientConnectionManager {
	
	private CloseableHttpClient httpClient;
	
	public PoolingClientConnectionManager(int maxTotal,int defaultMaxPerRoute,int connectTimeout,int socketTimeout){
		
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(maxTotal);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
		// Increase max connections for localhost:80 to 50
		//HttpHost localhost = new HttpHost("localhost", 80);
		//cm.setMaxPerRoute(new HttpRoute(localhost), 50); 

		RequestConfig.Builder requestConfig = RequestConfig.custom();
		requestConfig.setConnectTimeout(connectTimeout);
		requestConfig.setSocketTimeout(socketTimeout);
		
		httpClient = HttpClients.custom()
								.setDefaultRequestConfig(requestConfig.build())
								.addInterceptorFirst(new HttpRequestInterceptor())
								.addInterceptorFirst(new HttpResponseInterceptor())
								.setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryStrategy())
						        .setConnectionManager(cm)
						        .build();
		
	}
	
	public CloseableHttpClient getHttpClient(){
		return httpClient;
	}
}
