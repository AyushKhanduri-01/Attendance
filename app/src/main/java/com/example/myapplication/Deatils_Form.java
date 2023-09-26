package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Deatils_Form extends AppCompatActivity {
    EditText name,section,rollno,sub_code;
    TextView id,Date;
    Button submit;
    String subcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatils_form);
        name=findViewById(R.id.name);
        section=findViewById(R.id.section);
        rollno=findViewById(R.id.rollno);
        Date=findViewById(R.id.Date);
        sub_code=findViewById(R.id.sub_code);
        id=findViewById(R.id.Id);
        submit=findViewById(R.id.submit);

        Intent intent = getIntent();
        String Studient_id = intent.getStringExtra("id");
        id.setText(Studient_id.toString());

        java.util.Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        Date.setText(formattedDate);

        subcode = sub_code.getText().toString();
        subcode= subcode.toUpperCase();
        subcode=subcode.replaceAll("[^a-zA-Z0-9]", "");



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String ,String> map = new HashMap<>();

               map.put("StudentID",Studient_id);
                map.put("Date",formattedDate);
                map.put("Subject_Code",subcode);
                map.put("NameofStd",name.getText().toString());
                map.put("Section",section.getText().toString().toUpperCase());
                map.put("RollNo",rollno.getText().toString());

                try {

                    FirebaseFirestore.getInstance().collection("TheStudents").document(id.getText().toString()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Deatils_Form.this, "Done", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    });
                }
                catch (Exception e){
                    e.toString();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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