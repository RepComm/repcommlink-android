package comm.rep.RepCommlink;

public class PhoneNumber {
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
