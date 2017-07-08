package helpers;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class ServiceUnavailableRetryStrategy implements org.apache.http.client.ServiceUnavailableRetryStrategy{

	@Override
	public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
		// TODO Auto-generated method stub
		int statusCode = response.getStatusLine().getStatusCode();
        return statusCode != 200 && executionCount < 3;
	}

	@Override
	public long getRetryInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

}
