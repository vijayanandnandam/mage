package helpers;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * @author Pawan
 *
 */
public class HttpResponseInterceptor implements org.apache.http.HttpResponseInterceptor {

	@Override
	public void process(HttpResponse response, HttpContext context)throws HttpException, IOException {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			Header ceheader = entity.getContentEncoding();
			if (ceheader != null) {
                HeaderElement[] codecs = ceheader.getElements();
                for (int i = 0; i < codecs.length; i++) {
                    if (codecs[i].getName().indexOf("gzip")!=-1) {
                    	//response.setEntity(new GzipDecompressingEntity(entity));
                    	return;
                    }else if (codecs[i].getName().indexOf("deflate")!=-1) {
                    	//response.setEntity(new DeflateDecompressingEntity(entity));
                        return;
                    }
                }
            }
        }
	}
}
