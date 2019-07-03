package com.neko.seed;

import java.io.File;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainTest {
	public static void test1(){
		String url="http://localhost:8081/v1/swift_upload";
		OkHttpClient client = new OkHttpClient();
		RequestBody body = new FormBody.Builder()
//        .add("键", "值")
//        .add("键", "值")
        .build();
		File file = new File("f://repository.zip");
		//small file and biger has two diffent result!! //TODO 
//		File file = new File("d://1.txt");
		MediaType mediaType = MediaType.parse("image/jpeg; charset=utf-8");
        RequestBody fileBody = RequestBody.create(mediaType, file);
        RequestBody multipartBody = new MultipartBody.Builder()
			        .setType(MultipartBody.ALTERNATIVE)
				//一样的效果
			        .addPart(Headers.of(
			            "Content-Disposition",
			            "form-data; name=\"params\"")
			                ,body)
			        .addPart(Headers.of(
			            "Content-Disposition",
			            "form-data; name=\"file\"; filename=\"1.txt\"")
			                , fileBody).build();
		Request request = new Request.Builder()
					        .url(url)
					        .post(multipartBody)
					        .build();
        Response response;
		try {
			response = client.newCall(request).execute();
			String result= response.body().string();
			System.out.println("result:"+result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		test1();
		
	}
}
