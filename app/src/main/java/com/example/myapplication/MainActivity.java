package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    SharedPreferences.Editor _sharedPrefsEdit;
    SharedPreferences _appSettingPrefs;

    Boolean _isNightModeOn;
    Dialog _dialog;

    private final String PASSWORD_PATTERN = "^" + ".{6,}"+"$";
    private final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";

    FirebaseAuth _auth;
    GoogleSignInClient _googleSignInClient;
    Drawable dr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Drawable dr = getResources().getDrawable(R.drawable.googleimage);
        _appSettingPrefs = getSharedPreferences("AppSettingPrefs",0); // get the storage reference
        _sharedPrefsEdit = _appSettingPrefs.edit(); //set the storage editor reference

        _dialog = new Dialog(this);

        //region night mode for activity

        _isNightModeOn = _appSettingPrefs.getBoolean("NightMode",false); //check the variable status

        if(_isNightModeOn) // set the mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //night
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //day

        //endregion

        ImageButton btnLogo  = findViewById(R.id.btnlogo_welcome);
        btnLogo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChangeTheme();
            }
        });

        Button btnLogin  = findViewById(R.id.btnLogin_welcome);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowLoginPopUp();
            }
        });

        Button btnSignUp  = findViewById(R.id.btnSignup_welcome);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowSignUpPopUp();
            }
        });

        _auth = FirebaseAuth.getInstance();
        CreateRequest();

        Button btnGoogle  = findViewById(R.id.btnGoogleSignIn_welcome);
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SignInWithGoogle();
            }
        });
        dr.setBounds(0,0,90,90);
        btnGoogle.setCompoundDrawables(dr, null, null, null);
    }

    void ChangeTheme() {

        if(_isNightModeOn) // variable set in OnCreate
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            _sharedPrefsEdit.putBoolean("NightMode",false);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            _sharedPrefsEdit.putBoolean("NightMode",true);
        }
        _sharedPrefsEdit.apply();
    }

    void EnterApp()
    {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class); //replace with check login
        startActivity(intent);
    }

    //region Login
    void ShowLoginPopUp(){
        _dialog.setContentView(R.layout.login_popup);

        //region Cancel
        Button btnCancel = _dialog.findViewById(R.id.btnCancel_LoginPopup);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                _dialog.dismiss();
            }
        });
        //endregion

        TextInputLayout txtEmail = _dialog.findViewById((R.id.edt_EmailAddress_login));
        TextInputLayout txtPassword = _dialog.findViewById((R.id.edt_Password_login));


        //region Login
        Button btnDone = _dialog.findViewById(R.id.btnDone_LoginPopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!txtEmail.getEditText().getText().equals(""))
                {
                    txtEmail.setError("");
                    if (!txtPassword.getEditText().getText().equals(""))
                    {
                        txtPassword.setError("");
                        VerifyInputs(txtEmail, txtPassword);
                    }else
                        txtPassword.setError("Invalid password");
                }else
                    txtEmail.setError("Invalid email address");

            }
        });


        _dialog.show();
    }//called by btnLogin

    void VerifyInputs(TextInputLayout txtEmail, TextInputLayout txtPassword){
        String email = txtEmail.getEditText().getText().toString();
        String password = txtPassword.getEditText().getText().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean foundEmail = false;
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if (dataSnapshot.child("email").getValue(String.class).equals(email))
                    {
                        txtEmail.setError("");
                        foundEmail = true;
                        if (dataSnapshot.child("password").getValue(String.class).equals(password)) {
                            Profile.getInstance().setName(dataSnapshot.child("name").getValue(String.class));
                            Profile.getInstance().setEmail(dataSnapshot.child("email").getValue(String.class));
                            Profile.getInstance().setPassword(dataSnapshot.child("password").getValue(String.class));
                            Profile.getInstance().setMetricSystem(dataSnapshot.child("metricSystem").getValue(Boolean.class));
                            Profile.getInstance().setReference(dataSnapshot.getRef());
                            Profile.getInstance().setAuthStatus(false);
                            EnterApp();
                        }else {
                            txtPassword.setError("Incorrect password");
                        }
                        break;
                    }
                }

                if (!foundEmail)
                    txtEmail.setError("Incorrect email address");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //endregion Login

    //region SignUp
    void ShowSignUpPopUp(){
        _dialog.setContentView(R.layout.signup_popup);

        //region Cancel
        Button btnCancel = _dialog.findViewById(R.id.btnCancel_signUpPopup);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                _dialog.dismiss();
            }
        });
        //endregion


        TextInputLayout txtName = _dialog.findViewById((R.id.edtName_signup));
        TextInputLayout txtEmail = _dialog.findViewById((R.id.edtEmailAddress_signup));
        TextInputLayout txtConfirmEmail = _dialog.findViewById((R.id.edtConfirmEmailAddress_signup));
        TextInputLayout txtPassword = _dialog.findViewById((R.id.edtPassword_signup));
        TextInputLayout txtConfirmPassword = _dialog.findViewById((R.id.edtConfirmPassword_signup));

        //region SignUp
        Button btnDone = _dialog.findViewById(R.id.btnDone_signUpPopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ValidDetails(txtName,txtEmail, txtConfirmEmail,txtPassword,txtConfirmPassword))
                    EnterApp();
            }
        });

        _dialog.show();

    }//called by btnSignUp

    boolean ValidDetails(TextInputLayout txtName, TextInputLayout txtEmail, TextInputLayout txtConfirmEmail,
                         TextInputLayout txtPassword, TextInputLayout txtConfirmPassword)
    {
        String email = txtEmail.getEditText().getText().toString().trim();
        if (!ValidEmail(email))
        {
            txtEmail.setError("Invalid email address");
            return false;
        }
        txtEmail.setError("");

        String confirmEmail = txtConfirmEmail.getEditText().getText().toString().trim();
        if (!email.equals(confirmEmail))
        {
            txtConfirmEmail.setError("Confirmation email must match");
            return false;
        }
        txtConfirmEmail.setError("");

        String password = txtPassword.getEditText().getText().toString();
        if (!ValidPassword(password))
        {
            txtPassword.setError("Password must be 6 characters long");
            return false;
        }
        txtPassword.setError("");

        String confirmPassword = txtConfirmPassword.getEditText().getText().toString();
        if (!password.equals(confirmPassword))
        {
            txtConfirmPassword.setError("Confirmation password must match");
            return false;
        }
        txtConfirmPassword.setError("");

        String name = txtName.getEditText().getText().toString().trim();
        SignUp(name,email, password );
        return true;
    }

    void SignUp(String name, String email, String password){
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference reference = rootNode.getReference("users");


        UserHelperClass user = new UserHelperClass(name,email,password,true);
        String id = name.charAt(0) + String.valueOf(System.currentTimeMillis());
        reference.child(id).setValue(user);

        Profile.getInstance().setName(name);
        Profile.getInstance().setEmail(email);
        Profile.getInstance().setPassword(password);
        Profile.getInstance().setAuthStatus(false);
        Profile.getInstance().setMetricSystem(true);
        Profile.getInstance().setReference( rootNode.getReference("users").child(id));
    }

    boolean ValidPassword(String password){
        if(password.matches(PASSWORD_PATTERN))
            return true;
        return false;
    }

    boolean ValidEmail(String email){
        if(email.matches(EMAIL_PATTERN))
            return true;
        return false;
    }

    //endregion SignUp

    //region Sign in with Google

    void CreateRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        _googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void SignInWithGoogle() {
        Intent signInIntent = _googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
             
                GoogleSignInAccount account = task.getResult(ApiException.class);

                FirebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(MainActivity.this, "Sorry auth failed here", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void FirebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        _auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = _auth.getCurrentUser();
                            SetGoogleProfile();
                            EnterApp();

                        } else {
                            Toast.makeText(MainActivity.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    void SetGoogleProfile(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Profile.getInstance().setAuthStatus(true);
        Profile.getInstance().setMetricSystem(true);
        Profile.getInstance().setName(account.getDisplayName());

    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser user = _auth.getCurrentUser();
        if(user!=null){
            SetGoogleProfile();
            EnterApp();
        }
    }

    //endregion Sign in with Google



}