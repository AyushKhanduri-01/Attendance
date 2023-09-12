package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {
    private  EditText email,password;
    private  Button login;
    private String qrinput;
    private String qroutput;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference  dr ;

    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.email);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        auth=FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        dr=database.getReference("checkValidity");//.child("NdvFVqA7e5d1AxOjiFT").child("key");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authentication();
            }
        });
    }


    private  void authentication() {
        String Email = email.getText().toString();
        String Password = password.getText().toString();

        if (Email.isEmpty() || Password.isEmpty()) {
            Toast.makeText(this, "Invalid Email Address and Password", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login Succesfull", Toast.LENGTH_SHORT).show();
                        openScanner();
                        email.setText("");
                        password.setText("");
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Invalid Email address and Password",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void  openScanner(){

            ScanOptions options = new ScanOptions();
            options.setPrompt("Volume up to flash on");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(ScannerPage.class);
            barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(),result->{
        if(result.getContents() != null )
        {

             qroutput=result.getContents();
             dr.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {

                     if(snapshot.exists() && flag){
                         DataSnapshot firstChild = snapshot.getChildren().iterator().next();
                         qrinput = String.valueOf(firstChild.child("key").getValue());
                         if(qrinput.equals(qroutput)){
                             Intent intent=new Intent(getApplicationContext(),Deatils_Form.class);
                             startActivity(intent);
                             flag = false;

                         }
                         else{
                             Toast.makeText(MainActivity.this, "QR not matched : Scan Again", Toast.LENGTH_SHORT).show();
                             openScanner();
                         }
                     }
                     else {

                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });




        }
    });

}