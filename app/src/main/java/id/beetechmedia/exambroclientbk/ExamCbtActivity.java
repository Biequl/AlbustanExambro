package id.beetechmedia.exambroclientbk;

import static id.beetechmedia.exambroclientbk.util.DetectConnection.isNetworkStatusAvialable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ExamCbtActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examcbt);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        final String linkweb = getIntent().getStringExtra("url");
        final WebView view = findViewById(R.id.webview);

        // Konfigurasi WebView
        view.getSettings().setJavaScriptEnabled(true); // Aktifkan JavaScript jika diperlukan
        view.getSettings().setSupportZoom(true); // Mendukung zoom
        view.getSettings().setBuiltInZoomControls(true); // Menampilkan tombol zoom
        view.getSettings().setDisplayZoomControls(false); // Sembunyikan tombol zoom (opsional)
        view.getSettings().setUseWideViewPort(true); // Mengatur viewport agar sesuai layar
        view.getSettings().setLoadWithOverviewMode(true); // Memuat halaman penuh sesuai layar
        // Mengizinkan Mixed Content untuk HTTP dalam HTTPS (opsional)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Mengatur custom User-Agent
        String customUserAgent = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Mobile Safari/537.36 ExamBrowser";
        view.getSettings().setUserAgentString(customUserAgent);

        view.setWebViewClient(new ExamCbtActivity.ExamWebView());
        view.loadUrl(linkweb);

        // SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkStatusAvialable(ExamCbtActivity.this)) {
                    view.reload();
                    view.getSettings().setDomStorageEnabled(true);
                } else {
                    Toast.makeText(ExamCbtActivity.this, "Url tidak valid/offline", Toast.LENGTH_LONG).show();
                    view.loadDataWithBaseURL(null, "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>", "text/html", "UTF-8", null);
                    /*progressDialogModel.hideProgressDialog();*/
                    swipeRefreshLayout.setRefreshing(false);
                    Intent i = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

        // Tambahkan logika Lock Task Mode saat aplikasi dimulai
        lockTaskMode();
    }

    private class ExamWebView extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        /** @noinspection deprecation*/
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isNetworkStatusAvialable(ExamCbtActivity.this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (am != null && am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                            startLockTask();
                        }
                    }
                }
                view.loadUrl(url);
            } else {
                Toast.makeText(ExamCbtActivity.this, "Url tidak valid/offline", Toast.LENGTH_LONG).show();
                view.loadDataWithBaseURL(null, "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>", "text/html", "UTF-8", null);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            super.onPageFinished(view, url);
        }

        /** @noinspection deprecation*/
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Toast.makeText(ExamCbtActivity.this, "Terjadi kesalahan, silakan coba lagi.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getBaseContext(), MainActivity.class);
            i.putExtra("valid", "offline");
            startActivity(i);
            finish();
        }

        private boolean isNetworkStatusAvialable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
            return false;
        }
    }
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        showWarningDialog("Aplikasi tidak boleh ditinggalkan.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isChangingConfigurations()) {
            showWarningDialog("Aplikasi tidak boleh berjalan di latar belakang.");
        }
    }
    @Override
    public void onBackPressed() {
        showWarningDialog("Apakah Anda yakin ingin keluar?");
    }

    private void showWarningDialog(String message) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.start();

        new AlertDialog.Builder(this)
                .setTitle("Pelanggaran !!!!")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Kembali", (dialog, which) -> {
                    dialog.dismiss();
                    mediaPlayer.stop();
                })
                .setNegativeButton("Keluar", (dialog, which) -> {
                    dialog.dismiss();
                    mediaPlayer.stop();
                    finish();
                })
                .show();
    }

    private void lockTaskMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null && am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                // Memulai mode Lock Task jika belum dalam mode KIOSK
                startLockTask();
            }
        }
    }

    // Menambahkan menu ke Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Menangani pilihan menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            System.exit(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask();
        }
        return super.onOptionsItemSelected(item);
    }
}