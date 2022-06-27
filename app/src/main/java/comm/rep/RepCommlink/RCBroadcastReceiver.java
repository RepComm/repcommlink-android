package comm.rep.RepCommlink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class RCBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = RCBroadcastReceiver.class.getSimpleName();
  public static final String pdu_type = "pdus";
  
  public NotificationCompat.Builder builder;
  public @NonNull String builderChannelId;
  public @NonNull String builderChannelDesc;
  public @NonNull String builderChannelName;
  public String builderTitle;
  public NotificationCompat.BigTextStyle builderStyle;
  
  public NotificationManagerCompat notifier;
  
  public RCBroadcastReceiver () {
    this.builderChannelId = "10209";
    this.builderChannelDesc = "";
    this.builderChannelName = "notifier";
    this.builderTitle = "RepCommlink";
    
    this.builder = new NotificationCompat.Builder(MainActivity.getCtx(), this.builderChannelId)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle(this.builderTitle)
        .setContentText("")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    
    this.builderStyle = new NotificationCompat.BigTextStyle();
    
    this.builder.setStyle(this.builderStyle);
    
    this.createNotificationChannel();
  
    this.notifier = NotificationManagerCompat.from(MainActivity.getCtx());
  
  }
  
  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(
          this.builderChannelId,
          this.builderChannelName,
          importance
      );
      channel.setDescription(this.builderChannelDesc);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = MainActivity
          .getCtx()
          .getSystemService(NotificationManager.class);
      
      notificationManager.createNotificationChannel(channel);
    }
  }
  
  
  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void onReceive(Context context, Intent intent) {
    System.out.println("Receiving messages");
    // Get the SMS message.
    Bundle bundle = intent.getExtras();
    SmsMessage[] msgs;
    String strMessage = "";
    String format = bundle.getString("format");
    // Retrieve the SMS message received.
    Object[] pdus = (Object[]) bundle.get(pdu_type);
    if (pdus != null) {
      // Check the Android version.
      boolean isVersionM =
          (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
      // Fill the msgs array.
      msgs = new SmsMessage[pdus.length];
      for (int i = 0; i < msgs.length; i++) {
        // Check Android version and use appropriate createFromPdu.
        if (isVersionM) {
          // If Android version M or newer:
          msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
        } else {
          // If Android version L or older:
          msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        // Build the message to show.
        strMessage += "SMS from " + msgs[i].getOriginatingAddress();
        strMessage += " :" + msgs[i].getMessageBody() + "\n";
        // Log and display the SMS message.
        Log.d(TAG, "onReceive: " + strMessage);
//        Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
        
        this.builder.setContentTitle(
            msgs[i].getDisplayOriginatingAddress()
        );
        
        this.builder.setContentText(
            msgs[i].getMessageBody()
        );
  
        this.builderStyle.bigText(
            msgs[i].getMessageBody()
        );
        
        int notificationId = (new Random()).nextInt();
        
        this.notifier.notify(notificationId, builder.build());
      }
    }
  }
}