package comm.rep.RepCommlink;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
  public static MainActivity SINGLETON;
  
  public static Context getCtx () {
    return SINGLETON.getApplicationContext();
  }
  
  private final int PERM_REQ = 1;
  
  LinearLayout convos;
  
  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    SINGLETON = this;
    
    setContentView(R.layout.activity_main);
    
    convos = (LinearLayout) findViewById(R.id.convos);
//    SmsManager sms = SmsManager.getDefault();
  
    ActivityCompat.requestPermissions(this, new String[] {
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    }, PERM_REQ );
  
    this.rebuildConvoDisplay();
  
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
  }
  
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void rebuildConvoDisplay() {
    long date = new Date(System.currentTimeMillis() - 14L * 24 * 3600 * 1000).getTime();
    
    Cursor cursor = getContentResolver()
        .query(Uri.parse("content://sms/inbox"),
            null,
            "date" + ">?",
            new String[]{""+date},
            "date DESC"
        );
    
    if (cursor.moveToFirst()) { // must check the result to prevent exception
      do {
        Msg msg = Msg.from(cursor);
        Convo.group(msg);
        
      } while (cursor.moveToNext());
    } else {
      // empty box, no SMS
    }
    
//    List<ContactInfo> contacts = Contacts.getAllContacts(this);
    Contacts.refreshImport();
    
    Convo.all.forEach((String address, Convo c)->{
      String convoName = "(?) " + address;
      String photo = null;
      
      ContactInfo contact = Contacts.contactFromAddress(address);
      convoName = contact.displayName;
      photo = contact.photo;
      
//      System.out.println("Last Msg: " + c.lastMsg.msg);
      ConvoTab msgFrag = ConvoTab.newInstance(convoName, c.lastMsg.msg, photo);
      
      int viewId = View.generateViewId();
      String id = String.valueOf(viewId);
      
      getFragmentManager()
          .beginTransaction()
          .add(convos.getId(), msgFrag, "frag-" + id)
          .commit();
    });
  }
}