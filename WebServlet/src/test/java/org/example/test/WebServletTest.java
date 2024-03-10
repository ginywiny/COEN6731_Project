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
        ContentResponse res = client.GET("http://168.138.75.189:8080/skiers/SkiServlet");
        System.out.println(res.getContentAsString());
        client.stop();
	}
	
	
	//@Test
	void testBlockingServletGet() throws Exception {
		HttpClient client = new HttpClient();
        client.start();
        ContentResponse res = client.GET("http://168.138.75.189:8080/skiers/BlockingServlet");
        System.out.println(res.getContentAsString());
        client.stop();
	}
	
	//@Test
	void testAsyncServletGet() throws Exception {
		String url = "http://168.138.75.189:8080/skiers/longtask";
		HttpClient client = new HttpClient();
        client.start();
        ContentResponse response = client.GET(url);
		assertThat(response.getStatus(), equalTo(200));
		String responseContent = IOUtils.toString(response.getContent());
		System.out.println(responseContent);
	}

	
	// @Test
	// void testArtistsGet() throws Exception {
	// 	//String url = "http://localhost:9090/coen6317/artists";
	// 	String url = "http://168.138.75.189:8080/coen6317/artists";
	// 	HttpClient client = new HttpClient();
    //     client.start();

    //     Request request = client.newRequest(url);
    //     request.param("id","id200");
    //     ContentResponse response = request.send();
   

	// 	assertThat(response.getStatus(), equalTo(200));
		
	// 	String responseContent = IOUtils.toString(response.getContent());
		
	// 	System.out.println(responseContent);
	// 	client.stop();
		
	// }
	
	// @SuppressWarnings("deprecation")
	// @Test
	// void testArtistsPost() throws Exception {
		
	// 	//String url = "http://localhost:9090/coen6317/artists";
	// 	String url = "http://168.138.75.189:8080/coen6317/artists";
	// 	HttpClient client = new HttpClient();
    //     client.start();
        
    //     Request request = client.POST(url);
        
    //     request.param("id","id200");
    //     request.param("name","artist200");
        
    //     ContentResponse response = request.send();
	// 	String res = new String(response.getContent());
	// 	System.out.println(res);
	// 	client.stop();
	// }
}