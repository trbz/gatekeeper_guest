package pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuestRequest {

    @SerializedName("guestId")
    private String guestId;

    @SerializedName("door")
    private DoorType door;

    @SerializedName("outsideStationId")
    private String outsideStationId;
}
