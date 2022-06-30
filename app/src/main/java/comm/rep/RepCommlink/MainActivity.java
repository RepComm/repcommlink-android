package comm.rep.RepCommlink;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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

class MainHandler {
  private static MainHandler SINGLETON;
  
  private Handler handler;
  
  private MainHandler() {
    this.handler = new Handler(Looper.getMainLooper());
  }
  
  public static MainHandler get() {
    if (SINGLETON == null) SINGLETON = new MainHandler();
    return SINGLETON;
  }
  
  public boolean delay(Runnable runnable, long millis) {
    return this.handler.postDelayed(runnable, millis);
  }
}

public class MainActivity extends AppCompatActivity {
  public static MainActivity SINGLETON;
  
  public static Context getCtx() {
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
    
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    }, PERM_REQ);
    
    this.rebuildConvoDisplay();
    
  }
  
  public void startConvoAction (View view) {
    startActivity(new Intent(this, Compose.class));
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
  }
  
  /**https://stackoverflow.com/a/35627860
   * Used to scroll to the given view.
   *
   * @param scrollViewParent Parent ScrollView
   * @param view View to which we need to scroll.
   */
  private static void scrollToView(final ScrollView scrollViewParent, final View view, long duration) {
    // Get deepChild Offset
    Point childOffset = new Point();
    getDeepChildOffset(scrollViewParent, view.getParent(), view, childOffset);
    // Scroll to child.
//    scrollViewParent.smoothScrollTo(0, childOffset.y);
    ObjectAnimator.ofInt(scrollViewParent, "scrollY",  childOffset.y).setDuration(duration).start();
  }
  
  /**https://stackoverflow.com/a/35627860
   * Used to get deep child offset.
   * <p/>
   * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
   * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
   *
   * @param mainParent        Main Top parent.
   * @param parent            Parent.
   * @param child             Child.
   * @param accumulatedOffset Accumulated Offset.
   */
  private static void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
    ViewGroup parentGroup = (ViewGroup) parent;
    accumulatedOffset.x += child.getLeft();
    accumulatedOffset.y += child.getTop();
    if (parentGroup.equals(mainParent)) {
      return;
    }
    getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
  }
  
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void rebuildConvoDisplay() {
    long date = new Date(System.currentTimeMillis() - 14L * 24 * 3600 * 1000).getTime();
    
    Cursor cursor = getContentResolver()
        .query(Uri.parse("content://sms/inbox"),
            null,
            "date" + ">?",
            new String[]{"" + date},
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
    
    Contacts.refreshImport();
    
    int count = 0;
    for (Map.Entry<String, Convo> pair : Convo.all.entrySet()) {
      count++;
      
      MainHandler.get().delay(() -> {
        String address = pair.getKey();
        Convo c = pair.getValue();
        
        String convoName = "(?) " + address;
        String photo = null;
        
        ContactInfo contact = Contacts.contactFromAddress(address);
        convoName = contact.displayName;
        photo = contact.photo;

//      System.out.println("Last Msg: " + c.lastMsg.msg);
        ConvoTab msgFrag = ConvoTab.newInstance(convoName, c.getLastMsg().msg, photo);
        
        int viewId = View.generateViewId();
        String id = String.valueOf(viewId);
        
        getFragmentManager()
            .beginTransaction()
            .add(convos.getId(), msgFrag, "frag-" + id)
            .commit();
        
        
      }, count * 150);
    }
  }
}