package id.beetechmedia.exambroclientbk;

import static android.app.PendingIntent.getActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.common.internal.ImagesContract;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {
    private AdView adView;
    private Button btnNext;
    private CardView cardMenu1, cardMenu2, cardMenu3, cardMenu4;
    private EditText inputAddress;
    private TextInputLayout inputLayoutAddress;
    private InterstitialAd interstitialAd;
    private String urlStatus = "online";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        handleIntent();
        setupNetworkCheck();
        setupListeners();
        setupAds();
    }

    private void initUI() {
        inputLayoutAddress = findViewById(R.id.input_layout_address);
        inputAddress = findViewById(R.id.input_address);
        btnNext = findViewById(R.id.btn_lanjut);
        cardMenu1 = findViewById(R.id.cv_menu1);
        cardMenu2 = findViewById(R.id.cv_menu2);
        cardMenu3 = findViewById(R.id.cv_menu3);
        cardMenu4 = findViewById(R.id.cv_menu4);
        adView = findViewById(R.id.adView);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        urlStatus = intent.getStringExtra("valid");
        if ("offline".equals(urlStatus)) {
            Toast.makeText(this, "URL tidak valid/offline", Toast.LENGTH_LONG).show();
        }
    }

    private void setupNetworkCheck() {
        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Peringatan")
                    .setMessage("Tidak ada koneksi")
                    .setPositiveButton("Close", (dialog, which) -> finish())
                    .show();
        }
    }

    private void setupListeners() {
        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateAddress();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExamCbtActivity.class);
                intent.putExtra("url","http://10.251.0.2/login");
                startActivity(intent);
            }
        });

        cardMenu1.setOnClickListener(v -> openURL("https://play.google.com/store/apps/details?id=id.argocipta.clientbk"));
        cardMenu2.setOnClickListener(v -> shareApp());
        cardMenu3.setOnClickListener(v -> showInfoDialog());
        cardMenu4.setOnClickListener(v -> startQrCodeScanner());
    }

    private void setupAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        adView.loadAd(new AdRequest.Builder().build());
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(this, getString(R.string.ADSinterstitial), new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        interstitialAd = null;
                    }
                });
    }

    /*private void submitForm() {
        Intent intent = new Intent(this, ExamActivity.class);
        startActivity(intent);

    }*/

    private boolean validateAddress() {
        String address = inputAddress.getText().toString().trim();
        if (address.isEmpty()) {
            inputLayoutAddress.setError(getString(R.string.err_msg_name));
            inputAddress.requestFocus();
            return false;
        }
        inputLayoutAddress.setError(null);
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        //noinspection deprecation
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void openURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=id.argocipta.clientbk");
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage(getString(R.string.panduan))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void startQrCodeScanner() {
        //noinspection deprecation
        IntentIntegrator integrator = new IntentIntegrator(this);
        //noinspection deprecation
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR Code");
        // Lock orientation to portrait mode
        integrator.setOrientationLocked(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of the QR code scan
        //noinspection deprecation
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String url = result.getContents(); // Get the URL from the QR code
            // Pass the URL to CbtActivity
            Intent intent = new Intent(this, ExamCbtActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        } else {
            Toast.makeText(this, "QR Code scan failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Peringatan !!!");
        builder.setMessage("Apakah  Ingin Keluar ?");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
                stopLockTask();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask();
        }
        alert.show();
    }
    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }
}