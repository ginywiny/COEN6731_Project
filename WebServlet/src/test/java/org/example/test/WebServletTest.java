package org.example.test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

class WebServletTest {

	//@Test
	void testSkiServletGet() throws Exception {
		HttpClient client = new HttpClient();
        client.start();
        ContentResponse res = client.GET("http://168.138.75.189:8080/skiresortApp/SkiServlet");
        System.out.println(res.getContentAsString());
        client.stop();
	}
}