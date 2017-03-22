package cunycodes.parkmatch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_register extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextName;
    private EditText editTextUsername;
    private EditText editTextEmailAddress;
    private EditText editTextPassword;
    private Button registerButton;
    private TextView textViewSignIn;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // initialize firebase
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog((this));
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextEmailAddress = (EditText) findViewById(R.id.editTextEmailAddress);
        registerButton = (Button) findViewById(R.id.registerButton);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        registerButton.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if(view == registerButton)
        {
            registerUser();
        }

        if(view == textViewSignIn)
        {
            Intent myIntent= new Intent (Activity_register.this,LoginActivity.class);
            Activity_register.this.startActivity(myIntent);
        }
    }

    private void registerUser()
    {
        String name = editTextName.getText().toString().trim();
        String userName = editTextUsername.getText().toString().trim();
        String email = editTextEmailAddress.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(name))
        {
            //name is empty
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(userName))
        {
            //username is empty
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(email))
        {
            //email is empty
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            //password is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        //Passes all Validations then Register
        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful())
            {

                //finish();
                this.writeToDatabase (firebaseAuth.getCurrentUser());
                startActivity(new Intent (getApplicationContext(), MapsActivity.class));

            }
                else
            {
                progressDialog.dismiss();
                Toast.makeText(Activity_register.this, "Registration failed...please try again!", Toast.LENGTH_SHORT).show();
            }
            }

            private void writeToDatabase (FirebaseUser currentUser) {
                String name = editTextName.getText().toString().trim();
                String userName = editTextUsername.getText().toString().trim();
                String email = editTextEmailAddress.getText().toString().trim();
                String id = currentUser.getUid();

                User newUser = new User (name, userName, email, id);

                mDatabase.child("users").child(id).setValue(newUser);
            }
        });


    }
    /*public void takeMetoLogin(View view){
        Intent myIntent= new Intent (Activity_register.this,LoginActivity.class);
        Activity_register.this.startActivity(myIntent);

    } */

}


