package comm.rep.RepCommlink;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class ContactInfo {
  String displayName;
  String address;
  String photo;
  String id;
}

public class Contacts {
  static Map<String, ContactInfo> imported = new HashMap();
  
  public static String bitmapToString (Bitmap bitmap) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream .toByteArray();
    return Base64.encodeToString(byteArray, Base64.DEFAULT);
  }
  public static Bitmap stringToBitmap (String data) {
    byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
  }
  
  public static Bitmap contactIdToBitmap (String id) {
    Bitmap result = null;
    try {
      if(id != null) {
        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
            MainActivity.getCtx().getContentResolver(),
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id))
        );
        
        if (inputStream != null) {
          result = BitmapFactory.decodeStream(inputStream);
          inputStream.close();
        }
        
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
  
  @RequiresApi(api = Build.VERSION_CODES.N)
  public static ContactInfo contactFromId (String id) {
    AtomicReference<ContactInfo> result = new AtomicReference<>();
    
    imported.forEach((String address, ContactInfo ci)->{
      if (ci.id == id) {
        result.set(ci);
        return;
      }
    });
    if (result.get() == null) {
      ContactInfo ci = new ContactInfo();
      ci.id = id;
      result.set(ci);
    }
    
    return result.get();
  }
  
  public static ContactInfo contactFromAddress (String address) {
    ContactInfo ci = imported.get(address);
    if (ci == null) {
      ci = new ContactInfo();
      ci.address = address;
      imported.put(address, ci);
    }
    return ci;
  }
  
  public static void refreshImport () {
    ContentResolver cr = MainActivity.getCtx().getContentResolver();
    Cursor cur = cr.query(
        ContactsContract.Contacts.CONTENT_URI,
        null, null, null, null
    );
  
    PhoneNumber phoneNumber = new PhoneNumber();
  
    if ((cur != null ? cur.getCount() : 0) > 0) {
      while (cur != null && cur.moveToNext()) {
        int idx = cur.getColumnIndex(ContactsContract.Contacts._ID);
        String id = cur.getString(idx);
      
        idx = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        String displayName = cur.getString(idx);
      
        Bitmap bitmap = contactIdToBitmap(id);
        String photo = null;
        if (bitmap != null) photo = bitmapToString(bitmap);
      
        idx = cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER);
        if (cur.getInt(idx) > 0) {
          Cursor pCur = cr.query(
              ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
              null,
              ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
              new String[]{id}, null);
          while (pCur.moveToNext()) {
            idx = pCur.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            );
            String address = pCur.getString(idx);
            phoneNumber.parse(address);
            address = phoneNumber.toString();
  
            ContactInfo ci = contactFromAddress(address);
            ci.displayName = displayName;
            ci.photo = photo;
            ci.id = id;
          }
          pCur.close();
        }
      
      }
    }
    if (cur != null) {
      cur.close();
    }
  
  }
  
  public static List<ContactInfo> getAllContacts (Context ctx) {
    List<ContactInfo> result = new ArrayList();
    
    ContentResolver cr = ctx.getContentResolver();
    Cursor cur = cr.query(
        ContactsContract.Contacts.CONTENT_URI,
        null, null, null, null
    );
    
    PhoneNumber phoneNumber = new PhoneNumber();
    
    if ((cur != null ? cur.getCount() : 0) > 0) {
      while (cur != null && cur.moveToNext()) {
        ContactInfo ci = new ContactInfo();
        result.add(ci);
        
        int idx = cur.getColumnIndex(ContactsContract.Contacts._ID);
        ci.id = cur.getString(idx);
        
        idx = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        ci.displayName = cur.getString(idx);
        
        Bitmap bitmap = contactIdToBitmap(ci.id);
        if (bitmap != null) ci.photo = bitmapToString(bitmap);
        
        idx = cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER);
        if (cur.getInt(idx) > 0) {
          Cursor pCur = cr.query(
              ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
              null,
              ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
              new String[]{ci.id}, null);
          while (pCur.moveToNext()) {
            idx = pCur.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            );
            ci.address = pCur.getString(idx);
            phoneNumber.parse(ci.address);
            ci.address = phoneNumber.toString();
            
          }
          pCur.close();
        }
        
      }
    }
    if (cur != null) {
      cur.close();
    }
    
    return result;
  }
  
}
