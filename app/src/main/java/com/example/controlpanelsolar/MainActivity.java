package com.example.controlpanelsolar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference db_reference;

    String ControlMode = "Manual";

    TextView txtFR1, txtFR2, txtAnglePos1, txtAnglePos2, txtAngleSP, txtAngleCalc;

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
        txtAngleCalc = findViewById(R.id.idcalculatedAngle);

        btnEnAuto = findViewById(R.id.idbtnEnAuto);;
        btnEnManual = findViewById(R.id.idbtnEnManual);

        skbAngleSP = findViewById(R.id.idSeekBarAngleSP);


        startDataBase();
        loadData();

        skbAngleSP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                db_reference.child("AngleSetPoint").setValue((Math.round(progress*(90.0/100.0)-45)*100.0)/100.0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No es necesario realizar ninguna acción aquí si no lo deseas
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No es necesario realizar ninguna acción aquí si no lo deseas
            }
        });
    }

    public void startDataBase() {
        db_reference = FirebaseDatabase.getInstance().getReference().child("Data");
        db_reference.child("ControlMode").setValue("Manual");
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

                        txtAnglePos1.setText(dataSnapshot.getValue().toString());

                        txtAnglePos2.setText(dataSnapshot.getValue().toString());



                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("AngleCalculated")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());
                        txtAngleCalc.setText(dataSnapshot.getValue().toString());
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
                        txtAngleSP.setText(dataSnapshot.getValue().toString());

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("FotoResistor1")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());
                        txtFR1.setText(dataSnapshot.getValue().toString()+" mV");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println(error.toException());
                    }
                });

        db_reference.child("FotoResistor2")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("TAG", dataSnapshot.getValue().toString());
                        txtFR2.setText(dataSnapshot.getValue().toString()+" mV");

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