package comm.rep.RepCommlink;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConvoTab#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConvoTab extends Fragment {
  
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_CONVONAME = "convoName";
  private static final String ARG_CONVOLASTMSG = "convoLastMessage";
  private static final String ARG_PHOTO = "photo";
  
  // TODO: Rename and change types of parameters
  private String convoName;
  private String convoLastMessage;
  private String photo;
  private Bitmap photoAsBitmap;
  
  public ConvoTab() {
    // Required empty public constructor
  }
  
  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param convoName Parameter 1.
   * @param lastMsg Parameter 2.
   * @return A new instance of fragment ConvoTab.
   */
  // TODO: Rename and change types and number of parameters
  public static ConvoTab newInstance(String convoName, String lastMsg, String photo) {
    ConvoTab fragment = new ConvoTab();
    Bundle args = new Bundle();
    args.putString(ARG_CONVONAME, convoName);
    args.putString(ARG_CONVOLASTMSG, lastMsg);
    args.putString(ARG_PHOTO, photo);
    
    fragment.setArguments(args);
    return fragment;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      convoName = getArguments().getString(ARG_CONVONAME);
      convoLastMessage = getArguments().getString(ARG_CONVOLASTMSG);
      photo = getArguments().getString(ARG_PHOTO);
      if (photo != null) photoAsBitmap = Contacts.stringToBitmap(photo);
    }
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = inflater.inflate(R.layout.fragment_convo_tab, container, false);
    
  
    ((TextView) v.findViewById(R.id.name)).setText(getArguments().getString(ARG_CONVONAME));
    ((TextView) v.findViewById(R.id.lastMessage)).setText(getArguments().getString(ARG_CONVOLASTMSG));
    
    if (photoAsBitmap != null) {
      ImageView imageView = (ImageView)v.findViewById(R.id.imageView);
  
      RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), photoAsBitmap);
      roundDrawable.setCircular(true);
      imageView.setImageDrawable(roundDrawable);
    }
    
    return v;
  }
}