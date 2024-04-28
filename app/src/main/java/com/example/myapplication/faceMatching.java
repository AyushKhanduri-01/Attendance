package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import com.example.myapplication.databinding.ActivityFaceMatchingBinding;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class faceMatching extends AppCompatActivity {
    private  String image1;
    private  String image2;
    private TextView t1;
    private  String path;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    ProgressBar progressBar ;


    // changes=============================================================================================================

    TextView textView;
    private ImageCapture imageCapture;
    private ActivityFaceMatchingBinding mainBinding;


    String intro = "Click image with in 15 sec : \n";
    String smile="With Smile ";
    String notSmile ="Without Smile ";
    String leftEyeOpen = "With left Eye Open ";
    String leftEyeClose = "With Left Eye Close ";
    String rightEyeOpen = "With Right Eye Open ";
    String rightEyeClose= "With Right Eye Close ";

    String tasks1[] = {smile,notSmile};
    String tasks2[]={leftEyeOpen,leftEyeClose};
    String tasks3[] = {rightEyeOpen,rightEyeClose};
    int task1,task2,task3;
    Random random = new Random();

    boolean Smile=false,lefteyeopen=false,righteyeopen=false;
    boolean result=false;

    ProcessCameraProvider cameraProvider;

    //changes======================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_matching);


        // changes==========================================================================
        mainBinding = ActivityFaceMatchingBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        textView = mainBinding.textView;

        //   commad for click image
        task1 = random.nextInt(2);
        task2=random.nextInt(2);
        task3=random.nextInt(2);
        Toast.makeText(this, ""+task2+task3, Toast.LENGTH_SHORT).show();
        textView.setText(intro + tasks1[task1] +", " + tasks2[task2] +" and "+ tasks3[task3]);
       // end of command
        Handler handler1=new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("Your Time Expired: login agian");

            }
        },15000);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                task1=task2=task3=-1;
            }
        },20000);


        mainBinding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
            }
        });
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderListenableFuture.get();
                    startCamereax(cameraProvider);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, ContextCompat.getMainExecutor(this));


        //changes==================================================================================================



       // t1=findViewById(R.id.result);
        progressBar=mainBinding.progressBar;
       // progressBar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Intent intent1=getIntent();
        path=intent1.getStringExtra("id");


//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent,101);

    }



    // Changes=========================================================================


    private void capturePhoto() {
        if(imageCapture == null)return;
        String name = System.currentTimeMillis()+"";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        }
        ImageCapture.OutputFileOptions outputFileOptions= new ImageCapture.OutputFileOptions.Builder(getContentResolver(),MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                    // Getting image bitmap
                    Uri savedImageUri = outputFileResults.getSavedUri();
                    if (savedImageUri == null) {
//                       ;
                    }
                    Bitmap bitmap = null;
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(savedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);



                        // Processing image .. using ml kit
                        Bitmap rotatedBitmap = rotateBitmap(bitmap, 270);
                        processImage(rotatedBitmap);

                        // ending of  ml kit processing

                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    // Toast.makeText(MainActivity.this, "captured  : bitmap = " + bitmap, Toast.LENGTH_SHORT).show();
                    cameraProvider.unbindAll();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Toast.makeText(faceMatching.this, "not saved" + exception.toString() , Toast.LENGTH_SHORT).show();
                    textView.setText(exception.toString());
                    exception.printStackTrace();
                }
            });
        }
    }

    private Bitmap rotateBitmap(Bitmap source, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }



    private void processImage(Bitmap bitmap) {
        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options =  new FirebaseVisionFaceDetectorOptions.Builder()
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        Task<List<FirebaseVisionFace>> task = detector.detectInImage(visionImage);

        task.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        if (faces.isEmpty()) {
                            Toast.makeText(faceMatching.this, "No faces detected in image", Toast.LENGTH_SHORT).show();
                        } else {
                            for (FirebaseVisionFace face : faces) {
                                float smileProbability = face.getSmilingProbability();
//
                                float rightEyeOpenProbability = face.getLeftEyeOpenProbability();
                                float leftEyeOpenProbability = face.getRightEyeOpenProbability();

                                if (smileProbability > 0.6f) {
                                    Smile = true;
                                   Toast.makeText(faceMatching.this, "nice smiling " + smileProbability, Toast.LENGTH_SHORT).show();

                                }
                                else {
                                   Toast.makeText(faceMatching.this, "Face is not smiling "+ smileProbability, Toast.LENGTH_SHORT).show();

                                }

                                if (rightEyeOpenProbability > 0.4f) {
                                    righteyeopen = true;
                                    Toast.makeText(faceMatching.this, "right eye is open " + rightEyeOpenProbability, Toast.LENGTH_SHORT).show();

                                }
                                else {
                                   Toast.makeText(faceMatching.this, " right eye not open "+ rightEyeOpenProbability, Toast.LENGTH_SHORT).show();

                                }

                                if (leftEyeOpenProbability > 0.4f) {
                                    lefteyeopen=true;
                                   Toast.makeText(faceMatching.this, " left eye is open " + leftEyeOpenProbability, Toast.LENGTH_SHORT).show();

                                }
                                else {

                                   Toast.makeText(faceMatching.this, "left eye not open "+ leftEyeOpenProbability, Toast.LENGTH_SHORT).show();

                                }

                                if((task1==0 && Smile==true || task1==1 && Smile==false) && (task2==0 && lefteyeopen==true || task2==1 && lefteyeopen==false) && (task3==0 && righteyeopen ==true|| task3==1 && righteyeopen==false)){
                                    result = true;
                                }
                                if(result){
                                    textView.setText("Image is live");
                                    progressBar.setVisibility(View.VISIBLE);
                                    // adding photo to firebase for generation url
                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                                    byte bb[] = bytes.toByteArray();
                                    addToFirebase(bb);

                                    //  end of adding photo to firebase for generation url
                                }
                                else{
                                    Toast.makeText(faceMatching.this, "Not a Required Picture", Toast.LENGTH_SHORT).show();
                                    textView.setText("Image is not live");
                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);

                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SmileDetection", "Error detecting faces: " + e.getMessage());
                    }
                });


    }

    private void startCamereax(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(mainBinding.previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        try{
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture);

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    // changes====================================================================================


  //  @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode==RESULT_OK){
//            if(requestCode == 101){
//
//
//                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
//                byte bb[] = bytes.toByteArray();
//
//                addToFirebase(bb);
//            }
//        }
//        else {
//            Toast.makeText(this, "Invalid Image Capture Again", Toast.LENGTH_SHORT).show();
//            Intent intent3=new Intent(getApplicationContext(),faceMatching.class);
//            intent3.putExtra("id",path);
//            startActivity(intent3);
//        }
//    }


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