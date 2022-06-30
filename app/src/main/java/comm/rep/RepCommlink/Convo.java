package comm.rep.RepCommlink;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Convo {
  String address;
  
  List<Msg> msgs;
  
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
  }
  
  static void group (Msg msg) {
    if (last != null) {
      if (last.address.equals(msg.address)) {
        last.add(msg);
        return;
      }
    }
    Convo group = get(msg.address);
    group.add(msg);
  }
  
//  @RequiresApi(api = Build.VERSION_CODES.N)
//  public void sort () {
//    this.msgs.sort((Msg a, Msg b)->{
//      return Math.toIntExact(b.millis - a.millis);
//    });
//  }
  
  public Msg getLastMsg () {
    return this.msgs.get(this.msgs.size()-1);
  }
}

