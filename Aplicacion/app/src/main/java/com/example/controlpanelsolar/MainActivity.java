package com.example.controlpanelsolar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    DatabaseReference db_reference;

    String ControlMode = "Manual";

    TextView txtFR1, txtFR2, txtAnglePos1, txtAnglePos2, txtAngleSP;

    Button btnEnAuto, btnEnManual;

    SeekBar skbAngleSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtFR1 = findViewById(R.id.idFotoResistorValue1);
        txtFR2 = findViewById(R.id.idFotoResistorValue2);
        txtAnglePos1 = findViewById(R.id.idAnglePosition1);
        txtAnglePos2 = findViewById(R.id.idAnglePosition2);
        txtAngleSP = findViewById(R.id.idAngleSP);

        btnEnAuto = findViewById(R.id.idbtnEnAuto);
        btnEnManual = findViewById(R.id.idbtnEnManual);

        skbAngleSP = findViewById(R.id.idSeekBarAngleSP);

        skbAngleSP.setMax(128);
        skbAngleSP.setProgress(14);

        startDataBase();
        loadData();

        skbAngleSP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double angleSetPoint = (progress*1.8)-25.2;
                db_reference.child("AngleSetPoint").setValue(String.format("%.1f", angleSetPoint));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void startDataBase() {
        db_reference = FirebaseDatabase.getInstance().getReference().child("Data");
        db_reference.child("ControlMode").setValue("Manual");
        db_reference.child("AngleSetPoint").setValue("0.0");
    }

    public void loadData() {
        db_reference.child("ControlMode")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());
                        ControlMode =  dataSnapshot.getValue().toString();
                        if (ControlMode ==  "Auto"){
                            btnEnAuto.setEnabled(false);
                            btnEnManual.setEnabled(true);
                            skbAngleSP.setEnabled(false);

                        } else if (ControlMode == "Manual") {
                            btnEnManual.setEnabled(false);
                            btnEnAuto.setEnabled(true);
                            skbAngleSP.setEnabled(true);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("AnglePosition")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());

                        String anglePositionSTR = dataSnapshot.getValue().toString();

                        double anglePosition = Double.parseDouble(anglePositionSTR);

                        txtAnglePos1.setText(String.format("%.1f", anglePosition)+" °");
                        txtAnglePos2.setText(String.format("%.1f", anglePosition)+" °");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("AngleSetPoint")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());

                        txtAngleSP.setText(dataSnapshot.getValue().toString()+" °");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("PhotoResistorT")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());
                        DecimalFormat decimalFormat = new DecimalFormat("#");
                        String formattedValue = decimalFormat.format(dataSnapshot.getValue());

                        txtFR1.setText(formattedValue+" mV");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("PhotoResistorB")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());
                        DecimalFormat decimalFormat = new DecimalFormat("#");
                        String formattedValue = decimalFormat.format(dataSnapshot.getValue());

                        txtFR2.setText(formattedValue+" mV");

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });
    }

    public void onClickEnAuto(View v){
        db_reference.child("ControlMode").setValue("Auto");
    }

    public void onClickEnManual(View v){
        db_reference.child("ControlMode").setValue("Manual");
    }

}