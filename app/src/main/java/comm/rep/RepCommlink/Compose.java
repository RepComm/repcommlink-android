package comm.rep.RepCommlink;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Compose extends AppCompatActivity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_compose);
  }
  
  public void onAddContact (View view) {
  
  }
  public void onDialButton (View view) {
    Intent intent = new Intent(Intent.ACTION_DIAL);
//    intent.setData(Uri.parse("tel:0123456789"));
    startActivity(intent);
  }
}