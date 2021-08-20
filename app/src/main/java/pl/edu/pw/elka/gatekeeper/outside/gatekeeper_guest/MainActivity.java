package pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import lombok.SneakyThrows;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.model.GuestRequest;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.model.DoorState;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.model.DoorType;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.service.WebService;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.service.WebServiceClient;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.socket.ServerEvent;
import pl.edu.pw.elka.gatekeeper.outside.gatekeeper_guest.utils.IpConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    public static final String EXPIRATION_TIME = "EXPIRATION TIME: ";
    public static final String POSSIBLE_ACCESS = "YOU CAN ACCESS: ";

    private int access_left;

    private Snackbar snackbarConnection;

    private EditText stationIdEditText;

    private static Socket socket;

    private Button unlockButton;

    private TextView expirationTimeTV;

    private TextView accessTimeLeftTV;

    @Override
    protected void onDestroy() {
        socket.disconnect();

        socket.off(ServerEvent.CONNECTED.event, connected);

        super.onDestroy();
    }

    @SuppressLint({"CutPasteId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectButton = findViewById(R.id.connect);
        Button disconnectButton = findViewById(R.id.disconnect);
        stationIdEditText = findViewById(R.id.txt_station_id);

        expirationTimeTV = findViewById(R.id.tx_expiration_time);
        accessTimeLeftTV = findViewById(R.id.access_limit);

        unlockButton = findViewById(R.id.button_unlock);

        snackbarConnection = Snackbar.make(findViewById(R.id.myCoordinatorLayout),
                "CONNECTION STATUS: ", BaseTransientBottomBar.LENGTH_LONG);
        snackbarConnection.setAnchorView(stationIdEditText);

        WebService apiService = WebServiceClient.getClient().create(WebService.class);

        unlockButton.setOnClickListener(v -> {
            Call<GuestRequest> call = apiService
                    .guestRequestToOpen(
                            new GuestRequest(
                                    stationIdEditText.getText().toString(),
                                    DoorType.GATE,
                                    "OUTSIDE_STATION_1"));
            call.enqueue(new Callback<GuestRequest>() {
                @Override
                public void onResponse(@NonNull Call<GuestRequest> call,
                                       @NonNull Response<GuestRequest> response) {
                    GuestRequest responseFromApi = response.body();
                    Log.d("TAG","Response = " + responseFromApi);
                }

                @Override
                public void onFailure(@NonNull Call<GuestRequest> call, @NonNull Throwable t) {
                    Log.d("TAG","Response = "+t.toString());
                }
            });

            if(access_left > 0){
                access_left--;
            }
            if (access_left == 0){
                unlockButton.setVisibility(View.INVISIBLE);
            }

            accessTimeLeftTV.setText(POSSIBLE_ACCESS + access_left);
        });

        connectButton.setOnClickListener(v -> {
            if((!(stationIdEditText.getText().toString().isEmpty())) &&
                    (stationIdEditText.getText().toString().contains("GUEST_"))){
                String stationId = stationIdEditText.getText().toString();

                    String url = IpConfig.IP_ADDRESS_SOCKET;

                    try {
                        IO.Options options = new IO.Options();
                        options.transports = new String[] { "websocket" };
                        options.reconnectionAttempts = 2;
                        options.reconnectionDelay = 1000;
                        options.timeout = 500;
                        socket = IO.socket(url + "?deviceId=" +
                                stationId, options);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                socket.once(ServerEvent.CONNECTED.event, connected);

                socket.connect();
            }

            if(socket == null || (!(socket.connected()))){
                snackbarConnection.setText("CONNECTION: NOT ESTABLISHED");
                snackbarConnection.show();

                stationIdEditText.setEnabled(true);
            }
        });

        disconnectButton.setOnClickListener(v -> {

            if(socket != null && socket.connected()){
                snackbarConnection.setText("CONNECTION: DISCONNECTED");
                snackbarConnection.show();

                socket.off(ServerEvent.CONNECTED.event, connected);

                socket.disconnect();

                unlockButton.setVisibility(View.INVISIBLE);

                expirationTimeTV.setVisibility(View.INVISIBLE);

                accessTimeLeftTV.setVisibility(View.INVISIBLE);

                stationIdEditText.setEnabled(true);
            }
        });
    }

    private final Emitter.Listener connected =
            objects -> {


                List<String> guestInfo = Arrays.asList(Arrays.toString(objects)
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .replace(" ", "")
                        .replace(":", " ")
                        .replace("{", "")
                        .replace("}", "")
                        .split(",| "));


                System.out.println(Arrays.toString(guestInfo.toArray()));

                runOnUiThread(new Runnable() {
                    @SneakyThrows
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        snackbarConnection.setText("CONNECTION: ESTABLISHED");
                        snackbarConnection.show();
                        stationIdEditText.setEnabled(false);

                        unlockButton.setVisibility(View.VISIBLE);
                        expirationTimeTV.setVisibility(View.VISIBLE);
                        accessTimeLeftTV.setVisibility(View.VISIBLE);

                        access_left = Integer.parseInt(guestInfo.get(1))
                                - Integer.parseInt(guestInfo.get(0));

                        accessTimeLeftTV.setText(POSSIBLE_ACCESS + access_left);

                        String expirationTime = guestInfo.get(2);

                        expirationTimeTV.setText(EXPIRATION_TIME + expirationTime);
                    }
                });
                Log.d("SOCKET", "CONNECTED WITH SERVER SUCCESSFULLY");
            };
}