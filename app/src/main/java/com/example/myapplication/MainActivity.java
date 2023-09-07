package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {
    private  EditText email,password;
    private  Button login;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.email);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        auth=FirebaseAuth.getInstance();

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
        if(result.getContents() != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            }).show();
        }
    });

}