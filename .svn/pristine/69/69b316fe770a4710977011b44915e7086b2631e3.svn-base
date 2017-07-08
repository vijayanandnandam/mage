package helpers;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;

/**
 * @author Pawan
 *
 */
public class HttpRequestInterceptor implements org.apache.http.HttpRequestInterceptor {

	/* (non-Javadoc)
	 * @see org.apache.http.HttpRequestInterceptor#process(org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)
	 */
	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		/*if (request.containsHeader(HttpHeaders.TRANSFER_ENCODING)) {
			request.removeHeaders(HttpHeaders.TRANSFER_ENCODING);
		}*/
		if (request.containsHeader(HttpHeaders.CONTENT_LENGTH)) {
			request.removeHeaders(HttpHeaders.CONTENT_LENGTH);
		}
	}
}