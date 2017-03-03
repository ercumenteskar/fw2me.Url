package com.fw2me.url;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

	WebView	                    webView;
	private CustomWebViewClient	webViewClient;
	private String	            url	           = "http://fw2me.com";
	ProgressDialog	            mProgressDialog;
	Context	                    context;
	String	                    loadingMessage	= "";
	String	                    pwMessage	= "";
	ArrayList<String>	          shorted	       = new ArrayList<String>();

  public void CreateNtf(String text)
{   

  NotificationCompat.Builder notificationView;
  PendingIntent pendingIntent = PendingIntent.getActivity( MainActivity.this,0,getIntent(),Intent.FLAG_ACTIVITY_NEW_TASK);
  if(Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 15){

       notificationView = new NotificationCompat.Builder(context);
      notificationView.setContentTitle(getResources().getString(R.string.autoShortConfTitle))
      .setContentText(text)
      .setSmallIcon(R.drawable.ic_dialog_logo)
      //.setTicker(getResources().getString(R.string.app_name) + " " + text)
      .setWhen(System.currentTimeMillis())
      .setContentIntent(pendingIntent)
      //.setDefaults(Notification.DEFAULT_SOUND)
      .setAutoCancel(true)
      .setSmallIcon(R.drawable.ic_launcher_sb)
      .getNotification();
  }else{
           notificationView = new NotificationCompat.Builder(context);
          notificationView.setContentTitle(getResources().getString(R.string.autoShortConfTitle))
          .setSmallIcon(R.drawable.ic_dialog_logo)
			          .setContentText(text)
          //.setTicker(getResources().getString(R.string.app_name) + " " + text)
          .setWhen(System.currentTimeMillis())
          .setContentIntent(pendingIntent)
          //.setDefaults(Notification.DEFAULT_SOUND)
          .setAutoCancel(true)
          .setSmallIcon(R.drawable.ic_launcher_sb)
          .build();

  }
//  notificationView.flags |= Notification.FLAG_NO_CLEAR;
//  notificationView.flags |= Notification.FLAG_ONGOING_EVENT;
	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	notificationManager.notify(1, notificationView.build());
}
	private void performClipboardCheck() {
		String str = pasteFromClipboard();
		if (Patterns.WEB_URL.matcher(str).matches())
			if (!shorted.contains(str))
		    CreateNtf(str);
}

@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = getApplicationContext();
		loadingMessage = getResources().getText(R.string.loading).toString();
		pwMessage = getResources().getText(R.string.pleasewait).toString();
		mProgressDialog = ProgressDialog.show(this, pwMessage, loadingMessage, true);
		webView = (WebView) findViewById(R.id.webview);
		webViewClient = new CustomWebViewClient();
		webView.getSettings().setBuiltInZoomControls(false); 

		webView.getSettings().setSupportZoom(false);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setHorizontalScrollBarEnabled(true);
		webView.setVerticalScrollBarEnabled(true);
		webView.setWebViewClient(webViewClient); 
		String ver = "";
    try {
	    ver = "?ver="+getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
	    e.printStackTrace();
    }
		webView.loadUrl(url+ver);
		//performClipboardCheck();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			final OnPrimaryClipChangedListener listener = new OnPrimaryClipChangedListener(){
		    @Override
        public void onPrimaryClipChanged() {performClipboardCheck();}
		  };
			((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).addPrimaryClipChangedListener(listener);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (webView.getVisibility() == View.GONE) {
			webView.loadData("<HTML><BODY></BODY></HTML>", "text/html", "utf-8");
			webView.setVisibility(View.VISIBLE);
			webView.loadUrl(url);
		}
		final String cBoard = pasteFromClipboard();
		if ((Patterns.WEB_URL.matcher(cBoard).matches()) && (!cBoard.contains("fw2.me")) && (!shorted.contains(cBoard))) {
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(getResources().getString(R.string.autoShortConfTitle))
			.setIcon(R.drawable.ic_dialog_logo)
			.setMessage(getResources().getString(R.string.autoShortConfDesc).replace("%s", cBoard))
			    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				    @Override
            public void onClick(DialogInterface dialog, int which) {
							webView.loadUrl(url + "/index.html?agreeTerms=1&submitted=1&longUrl=" + cBoard);
				    }
			    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				    @Override
            public void onClick(DialogInterface dialog, int which) {
					    // do nothing
				    }
			    })
			    // .setIcon(android.R.drawable.ic_dialog_email)
			    .show();
			shorted.add(cBoard);
		}
	};

	private String pasteFromClipboard() {
		String ret = "";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (myClipboard.hasPrimaryClip())
			{
				final ClipData abc = myClipboard.getPrimaryClip();
				try 
				{
					ret = abc.getItemAt(0).getText().toString();
        } catch (Exception e) {
  				ret = "";
        }
			}
		} else {
			final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboardManager.hasText())
				ret = clipboardManager.getText().toString();
		}
		return ret;
	}

	private void copyToClipboard(String text) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final android.content.ClipboardManager myClipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			final android.content.ClipData clip = ClipData.newPlainText("label", text);
			myClipboard.setPrimaryClip(clip);
		} else {
			final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setText(text);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	private class CustomWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) { // Sayfa
																																					// y�klenirken
																																					// �al���r
			super.onPageStarted(view, url, favicon);
			if (!mProgressDialog.isShowing())// mProgressDialog a��k m� kontrol
																			 // ediliyor
			{
				mProgressDialog.show();// mProgressDialog a��k de�ilse a��l�yor yani
															 // g�steriliyor ve y�kleniyor yaz�s� ��k�yor
			}

		}

		@Override
		public void onPageFinished(WebView view, String url) {// sayfam�z
																													// y�klendi�inde
																													// �al���yor.
			super.onPageFinished(view, url);
			if (mProgressDialog.isShowing()) {// mProgressDialog a��k m� kontrol
				mProgressDialog.dismiss();// mProgressDialog a��ksa kapat�l�yor
			}
			if (url.contains("?u=")) {
				webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
				/* WebViewClient must be set BEFORE calling loadUrl! */
				webView.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
					}
				});

			}

		}

		class MyJavaScriptInterface {
			@JavascriptInterface
			@SuppressWarnings("unused")
			public void processHTML(String html) {
				try {
					html = html.substring(html.indexOf("resultLink"));
					html = html.substring(html.indexOf("<a href="));
					html = html.substring(html.indexOf("\"") + 1);
					html = html.substring(0, html.indexOf("\""));
				} catch (Exception e) {
					// TODO: handle exception
				}
				final String shortUrl = html;
				if (Patterns.WEB_URL.matcher(shortUrl).matches()) {
					new AlertDialog.Builder(MainActivity.this)
					     .setTitle(getResources().getString(R.string.copyConfTitle)).setMessage(getResources().getString(R.string.copyConfDesc))
							 .setIcon(R.drawable.ic_dialog_logo)
							 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						    @Override
                public void onClick(DialogInterface dialog, int which) {
							    copyToClipboard(shortUrl);
						    }
					    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						    @Override
                public void onClick(DialogInterface dialog, int which) {
							    // do nothing
						    }
					    })
					    // .setIcon(android.R.drawable.ic_dialog_email)
					    .show();
				}
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// Bu method a��lan sayfa i�inden ba�ka linklere t�kland���nda a��lmas�na
			// yar�yor.
			// Bu methodu override etmez yada edip i�ini bo� b�rak�rsan�z ilk url den
			// a��lan sayfa d���nda ba�ka sayfaya ge�i� yapamaz

			view.loadUrl(url);// yeni t�klanan url i a��yor
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			view.setVisibility(View.GONE);
			mProgressDialog.dismiss();
			Toast.makeText(getApplicationContext(), R.string.noConnection, Toast.LENGTH_LONG).show();
			// BU method webview y�klenirken herhangi bir hatayla kar�ila�il�rsa hata
			// kodu d�n�yor.
			// D�nen hata koduna g�re kullan�c�y� bilgilendirebilir yada gerekli
			// i�lemleri yapabilirsiniz
			// errorCode ile hatay� alabilirsiniz
			// if(errorCode==-8){
			// Timeout
			// } �eklinde kullanabilirsiniz

			// Hata Kodlar� a�a��dad�r...

			/*
			 * /** Generic error public static final int ERROR_UNKNOWN = -1;
			 * 
			 * /** Server or proxy hostname lookup failed public static final int
			 * ERROR_HOST_LOOKUP = -2;
			 * 
			 * /** Unsupported authentication scheme (not basic or digest) public
			 * static final int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;
			 * 
			 * /** User authentication failed on server public static final int
			 * ERROR_AUTHENTICATION = -4;
			 * 
			 * /** User authentication failed on proxy public static final int
			 * ERROR_PROXY_AUTHENTICATION = -5;
			 * 
			 * /** Failed to connect to the server public static final int
			 * ERROR_CONNECT = -6;
			 * 
			 * /** Failed to read or write to the server public static final int
			 * ERROR_IO = -7;
			 * 
			 * /** Connection timed out public static final int ERROR_TIMEOUT = -8;
			 * 
			 * /** Too many redirects public static final int ERROR_REDIRECT_LOOP =
			 * -9;
			 * 
			 * /** Unsupported URI scheme public static final int
			 * ERROR_UNSUPPORTED_SCHEME = -10;
			 * 
			 * /** Failed to perform SSL handshake public static final int
			 * ERROR_FAILED_SSL_HANDSHAKE = -11;
			 * 
			 * /** Malformed URL public static final int ERROR_BAD_URL = -12;
			 * 
			 * /** Generic file error public static final int ERROR_FILE = -13;
			 * 
			 * /** File not found public static final int ERROR_FILE_NOT_FOUND = -14;
			 * 
			 * /** Too many requests during this load public static final int
			 * ERROR_TOO_MANY_REQUESTS = -15;
			 */

		}
	}

	@Override
  public void onBackPressed() // Android Back Buttonunu Handle ettik. Back
															// butonu bir �nceki sayfaya geri d�necek
	{
		if (webView.canGoBack()) {// e�er varsa bir �nceki sayfaya gidecek
			webView.goBack();
		} else {// Sayfa yoksa uygulamadan ��kacak
			super.onBackPressed();
		}
	}

}
