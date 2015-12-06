package com.polyrob.robgps;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RestCall extends AsyncTask<Void, Void, Void> {

    private String endpoint;
    private RequestMethod method;
    private ArrayList<NameValuePair> params;

    public RestCall(RequestMethod method, String endpoint, ArrayList<NameValuePair> params) {
        this.endpoint = endpoint;
        this.method = method;
        this.params = params;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            execute(method, endpoint, null, params);
        } catch (Exception e) {
            Log.e("RestCall", "An exception has occurred! " + e.getMessage());
        }
        return null;
    }

    public enum RequestMethod {
        GET,
        POST
    }

    public int responseCode = 0;
    public String message;
    public String response;

    private void execute(RequestMethod method, String url, ArrayList<NameValuePair> headers, ArrayList<NameValuePair> params) throws Exception {
        switch (method) {
            case GET: {
                // add parameters
                String combinedParams = "";
                if (params != null) {
                    combinedParams += "?";
                    for (NameValuePair p : params) {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                        if (combinedParams.length() > 1)
                            combinedParams += "&" + paramString;
                        else
                            combinedParams += paramString;
                    }
                }
                HttpGet request = new HttpGet(url + combinedParams);
                // add headers
                if (headers != null) {
                    headers = addCommonHeaderField(headers);
                    for (NameValuePair h : headers)
                        request.addHeader(h.getName(), h.getValue());
                }
                executeRequest(request, url);
                break;
            }
            case POST: {
                HttpPost request = new HttpPost(url);
                // add headers
                if (headers != null) {
                    headers = addCommonHeaderField(headers);
                    for (NameValuePair h : headers)
                        request.addHeader(h.getName(), h.getValue());
                }
                if (params != null)
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                executeRequest(request, url);
                break;
            }
        }
    }

    private ArrayList<NameValuePair> addCommonHeaderField(ArrayList<NameValuePair> _header) {
        _header.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        return _header;
    }

    private void executeRequest(HttpUriRequest request, String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;

        httpResponse = client.execute(request);
        responseCode = httpResponse.getStatusLine().getStatusCode();
        message = httpResponse.getStatusLine().getReasonPhrase();
        HttpEntity entity = httpResponse.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            response = convertStreamToString(instream);
            instream.close();
        }

    }

    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();

        return sb.toString();
    }
}