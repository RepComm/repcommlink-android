package comm.rep.RepCommlink;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Msg {
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
    result.hasBeenRead = resolveField(c, "read").equals("1");
    result.millis = Long.parseLong( resolveField(c, "date"));
    result.receiveDayTime = new SimpleDateFormat("EEE, MMM d, ''yy h:mm a", Locale.US).format(new Date(result.millis));
    
    result.folderName = resolveField(c, "type").contains("1") ? "inbox" : "sent";
    
    return result;
  }
}

