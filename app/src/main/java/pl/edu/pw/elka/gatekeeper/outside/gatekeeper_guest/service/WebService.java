package pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.service;

import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.model.GuestRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WebService {


    @POST("station/guest/request/to/open")
    Call<GuestRequest> guestRequestToOpen(@Body GuestRequest guestRequest);
}
