package comm.rep.RepCommlink;

public class BackwardsStringConsumer {
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
