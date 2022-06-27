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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class Convo {
  String address;
  
  List<Msg> msgs;
  Msg lastMsg;
  
  public Convo (String address) {
    this.msgs = new ArrayList();
    
    this.address = address;
    all.put(address, this);
  }
  
  static Map<String, Convo> all = new HashMap();
  
  static Convo last;
  
  static Convo get (String address) {
    Convo result = Convo.all.get(address);
    if (result == null) result = new Convo(address);
    return result;
  }
  public void add (Msg msg) {
    this.msgs.add(msg);
    msg.group = this;
    this.lastMsg = msg;
  }
  
  static void group (Msg msg) {
    if (last != null) {
      if (last.address == msg.address) {
        last.add(msg);
        return;
      }
    }
    Convo group = get(msg.address);
    group.add(msg);
  }
}

class Msg {
  Convo group;
  
  String id;
  String address;
  String msg;
  boolean hasBeenRead;
//  String readState; //"0" for have not read sms and "1" for have read sms
  long millis;
  String receiveDayTime;
  String folderName;
  
  public static String resolveField (Cursor c, String id) {
    return c.getString(c.getColumnIndexOrThrow(id));
  }
  
  public static Msg from (Cursor c) {
    Msg result = new Msg();
    
    result.id = resolveField(c, "_id");
    result.address = resolveField(c, "address");
    PhoneNumber phoneNumber = new PhoneNumber();
    phoneNumber.parse(result.address);
    result.address = phoneNumber.toString();
    
    result.msg = resolveField(c, "body");
    result.hasBeenRead = resolveField(c, "read") == "1";
    result.millis = Long.valueOf( resolveField(c, "date"));
    result.receiveDayTime = new SimpleDateFormat("EEE, MMM d, ''yy h:mm a", Locale.US).format(new Date(result.millis));
    
    result.folderName = resolveField(c, "type").contains("1") ? "inbox" : "sent";
    
    return result;
  }
}

class BackwardsStringConsumer {
  String src;
  int offset = 0;
  public BackwardsStringConsumer (String src) {
    this.src = src;
    this.offset = this.src.length();
  }
  public String consume(int count) {
    int begin = offset-count;
    int end = offset;
    
    if (begin < 0) begin = 0;
    if (end < 0) end = 0;
    offset -= count;
    return this.src.substring(begin, end);
  }
  public int remaining () {
    return offset;
  }
}

class PhoneNumber {
  int countryCode = -1;
  int areaCode = -1;
  int localCodeA = -1;
  int localCodeB = -1;
  
  static String numerals = "0123456789";
  
  void parse (String str) {
    countryCode = 1;
    areaCode = -1;
    localCodeA = -1;
    localCodeB = -1;
    
    int len = str.length();
    
    char ch;
    
    String numbersOnly = "";
    
    for (int i=0; i<len; i++) {
      ch = str.charAt(i);
      if (numerals.indexOf(ch) > -1) numbersOnly += ch;
    }
    
    len = numbersOnly.length();
    
    BackwardsStringConsumer bsc = new BackwardsStringConsumer(numbersOnly);
    try {
      this.localCodeB = Integer.parseInt(bsc.consume(4));
      this.localCodeA = Integer.parseInt(bsc.consume(3));
      
      if (len == "1112222".length()) {
        //nothing else
      } else if (len == "0001112222".length()) {
        this.areaCode = Integer.parseInt(bsc.consume(3));
      } else {
        this.areaCode = Integer.parseInt(bsc.consume(3));
        this.countryCode = Integer.parseInt(bsc.consume(bsc.remaining()));
      }
    } catch (Exception ex) {
    
    }
  }
  
  public String toString() {
    String result = "";
    if (countryCode != -1 && areaCode != -1) {
      result += "+" + countryCode;
    }
    if (areaCode != -1) {
      result += "(" + areaCode + ")";
    }
    result += localCodeA + "-" + localCodeB;
    
    return result;
  }
}

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