package pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.utils.IpConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebServiceClient {

    public static Retrofit getClient(){

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)).build();

        return new Retrofit.Builder()
                .baseUrl(IpConfig.IP_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

    }
}
