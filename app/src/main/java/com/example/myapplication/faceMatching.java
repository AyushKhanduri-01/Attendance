package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class faceMatching extends AppCompatActivity {
    private  String image1;
    private  String image2;
    private TextView t1;
    private  String path;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    ProgressBar progressBar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_matching);
        t1=findViewById(R.id.result);
        progressBar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Intent intent1=getIntent();
        path=intent1.getStringExtra("id");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode == 101){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                byte bb[] = bytes.toByteArray();
                addToFirebase(bb);
            }
        }
        else {
            Toast.makeText(this, "Invalid Image Capture Again", Toast.LENGTH_SHORT).show();
            Intent intent3=new Intent(getApplicationContext(),faceMatching.class);
            intent3.putExtra("id",path);
            startActivity(intent3);
        }
    }


    public  void addToFirebase(byte bb[]){
        StorageReference ref = storageReference.child(path+"new");
        ref.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //================================================================================================================
                   ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                       @Override
                       public void onSuccess(Uri uri) {
                           image1 = uri.toString();
                           progressBar.setVisibility(View.VISIBLE);
                           faceRecognise();

                           //=================================================================
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                     }
                                 }).addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {

                                     }
                                 });
                       }
                   });

                //================================================================================================================

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void faceRecognise(){

        //Api configuration
        String url = "https://api-us.faceplusplus.com/facepp/v3/compare";
        String apikey = "AbFP2l7o8-_vC-9a2nqhhASIMMiK3Lks";
        String apiSecret = "1g4zEvczboQRhySWplg-dN5Ac4Rf7odX";

        StorageReference ref2= storageReference.child(path+".jpg");
        ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                image2 = uri.toString();

                //==============================================================================================
               RequestQueue queue = Volley.newRequestQueue(faceMatching.this);
               StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                   @Override
                   public void onResponse(String response) {
                       try {
                           JSONObject resultobj = new JSONObject(response);
                           Double confidence = resultobj.getDouble("confidence");
                           if(confidence > 70){
                               //new
                               StorageReference ref = storageReference.child(path+"new");
                               ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                     }
                                 }).addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {

                                     }
                                 });
                               Intent intent = new Intent(getApplicationContext(),Deatils_Form.class);
                               intent.putExtra("id",path);
                               startActivity(intent);
                           }
                           else{
                               Toast.makeText(faceMatching.this, "Image Not Matched", Toast.LENGTH_SHORT).show();
                               StorageReference ref = storageReference.child(path+"new");
                               ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {

                                   }
                               });
                               Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                               startActivity(intent);
                           }
                       } catch (JSONException e){
                           Toast.makeText(faceMatching.this, "error to fetch response", Toast.LENGTH_SHORT).show();
                           Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                           startActivity(intent);
                       }

                   }
               },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(faceMatching.this, "error "+ error, Toast.LENGTH_SHORT).show();
                        t1.setText(error.getMessage());

                        //new
                        Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                })
                {
                  public  Map<String,String> getParams(){
                     Map <String,String> params= new HashMap<String,String>();
                        params.put("api_key", apikey);
                        params.put("api_secret", apiSecret);
                        params.put("image_url1", image1);
                        params.put("image_url2", image2);
                        return params;
                    }
                };

                queue.add(stringRequest);
            }

                //==============================================================================================

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(faceMatching.this, "No record found", Toast.LENGTH_SHORT).show();
                //new intent
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);



            }
        });
  }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}