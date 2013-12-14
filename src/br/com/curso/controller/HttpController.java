package br.com.curso.controller;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 
 * Classe que encapsula o envio (POST/GET) para um servidor
 * utilizando a biblioteca LOOPJ
 * 
 * @author Gabriel Tavares
 *
 */
public class HttpController {

	public static final String PROTOCOL_NAME = "http://";
	public static final String WEB_SERVER_NAME = "/ExAula4DroidHttpServer";
	private static String BASE_URL = "";
	public static String IP_PORT = "";
	

	// Biblioteca que faz a comunicação assicrona com o servidor
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		BASE_URL = PROTOCOL_NAME + IP_PORT + WEB_SERVER_NAME;
		return BASE_URL + relativeUrl;
	}

}

