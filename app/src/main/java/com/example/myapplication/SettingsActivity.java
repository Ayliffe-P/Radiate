package com.example.myapplication;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences.Editor _sharedPrefsEdit;
    SharedPreferences _appSettingPrefs;
    Boolean _isNightModeOn;
    TextView banner;

    Dialog _dialog;

    private final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";
    private final String PASSWORD_PATTERN = "^" + ".{6,}"+"$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _dialog = new Dialog(this);

        _appSettingPrefs = getSharedPreferences("AppSettingPrefs",0); // get the storage reference
        _sharedPrefsEdit = _appSettingPrefs.edit(); //set the storage editor reference
        _isNightModeOn = _appSettingPrefs.getBoolean("NightMode",false); //check the variable status

        //region Account Settings
        Button btnChangeName  = findViewById(R.id.btnChangeName_settings);
        Button btnChangeEmail  = findViewById(R.id.btnChangeEmail_settings);
        Button btnChangePassword  = findViewById(R.id.btnChangePassword_settings);
        banner = findViewById(R.id.txtName_settings);
        banner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);;
                intent.putExtra("previousIntent", "Settings");
                startActivity(intent);
            }
        });

      if (!(Profile.getInstance().getAuthStatus())){
            btnChangeName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ChangeName();
                }
            });

            btnChangeEmail.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ChangeEmail();
                }
            });

            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ChangePassword();
                }
            });
        }else
        {
            btnChangeName.setEnabled(false);
            btnChangeEmail.setEnabled(false);
            btnChangePassword.setEnabled(false);
        }

        //endregion Account Settings

        //region App Settings

        Button btnChangeUnitSystem  = findViewById(R.id.btnChangeUnitSystem_settings);
        btnChangeUnitSystem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChangeUnitSystem();
            }
        });

        Switch swchTheme  = (Switch)findViewById(R.id.swchChangeTheme_settings);
        swchTheme.setChecked(_isNightModeOn);
        swchTheme.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (swchTheme.isChecked())
                    _isNightModeOn = true;
                else
                    _isNightModeOn = false;
                ChangeTheme();
            }
        });


        //endregion App Settings

        Button btnSignOut  = findViewById(R.id.btnSignOut_settings);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SignOut();
            }
        });

        Button btnDeleteAccount  = findViewById(R.id.btnDeleteAccount_settings);
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ConfirmAccountDeletion();
            }
        });

        DisplayName();
    }
    void DisplayName(){
        TextView txtName  = findViewById(R.id.txtName_settings);
        txtName.setText(Profile.getInstance().getName());
    }
    //region Account Settings
    void ChangeName(){
        _dialog.setContentView(R.layout.change_details_popup);

        ActivateCancelButton();

        TextView txtHeading = _dialog.findViewById(R.id.txtHeading_ChangeDetailsPopup);
        txtHeading.setText("Name");

        TextInputLayout edtOne = _dialog.findViewById(R.id.edt_one_ChangeDetailsPopup);
        edtOne.getEditText().setHint("Enter new name");

        TextInputLayout edtTwo = _dialog.findViewById(R.id.edt_two_ChangeDetailsPopup);
        edtTwo.getEditText().setHint("Confirm new name");

        Button btnDone = _dialog.findViewById(R.id.btnDone_ChangeDetailsPopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtOne.getEditText().getText().toString().trim();
                if(name.equals(edtTwo.getEditText().getText().toString().trim()))
                {
                    edtTwo.setError("");

                    if (VerifiedPassword());
                    {
                        Profile.getInstance().setName(name);
                        Profile.getInstance().getReference().child("name").setValue(name.toLowerCase(Locale.ROOT));
                        Toast.makeText(SettingsActivity.this, "Name has been successfully changed", Toast.LENGTH_LONG).show();
                        DisplayName();
                        _dialog.dismiss();
                    }
                }else
                    edtTwo.setError("Names must match");

            }
        });

        _dialog.show();
    } //done

    void ChangeEmail(){
        _dialog.setContentView(R.layout.change_details_popup);

        ActivateCancelButton();

        TextView txtHeading = _dialog.findViewById(R.id.txtHeading_ChangeDetailsPopup);
        txtHeading.setText("Email Address");

        TextInputLayout edtOne = _dialog.findViewById(R.id.edt_one_ChangeDetailsPopup);
        edtOne.getEditText().setHint("Enter new email address");

        TextInputLayout edtTwo = _dialog.findViewById(R.id.edt_two_ChangeDetailsPopup);
        edtTwo.getEditText().setHint("Confirm new email address");

        Button btnDone = _dialog.findViewById(R.id.btnDone_ChangeDetailsPopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtOne.getEditText().getText().toString().trim();
                if(email.matches(EMAIL_PATTERN)){
                    edtOne.setError("");

                    if(email.equals(edtTwo.getEditText().getText().toString().trim()))
                    {
                        edtTwo.setError("");

                        if (VerifiedPassword());
                        {
                            Profile.getInstance().setName(email);
                            Profile.getInstance().getReference().child("email").setValue(email.toLowerCase(Locale.ROOT));
                            Toast.makeText(SettingsActivity.this, "Email address has been successfully changed", Toast.LENGTH_LONG).show();
                            _dialog.dismiss();
                        }
                    }else
                        edtTwo.setError("Email addresses must match");
                }else
                    edtOne.setError("Invalid Email Address");


            }
        });

        _dialog.show();
    } //done

    void ChangePassword() {
        _dialog.setContentView(R.layout.change_details_popup);

        ActivateCancelButton();

        TextView txtHeading = _dialog.findViewById(R.id.txtHeading_ChangeDetailsPopup);
        txtHeading.setText("Password");

        TextInputLayout edtOne = _dialog.findViewById(R.id.edt_one_ChangeDetailsPopup);
        edtOne.getEditText().setHint("Enter new password");
        edtOne.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
        edtOne.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        TextInputLayout edtTwo = _dialog.findViewById(R.id.edt_two_ChangeDetailsPopup);
        edtTwo.getEditText().setHint("Confirm new password");
        edtTwo.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
        edtTwo.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button btnDone = _dialog.findViewById(R.id.btnDone_ChangeDetailsPopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edtOne.getEditText().getText().toString().trim();
                if(password.matches(PASSWORD_PATTERN)){
                    edtOne.setError("");

                    if(password.equals(edtTwo.getEditText().getText().toString().trim()))
                    {
                        edtTwo.setError("");

                        if (VerifiedPassword());
                        {
                            Profile.getInstance().setName(password);
                            Profile.getInstance().getReference().child("password").setValue(password);
                            Toast.makeText(SettingsActivity.this, "Password has been successfully changed", Toast.LENGTH_LONG).show();
                            _dialog.dismiss();
                        }
                    }else
                        edtTwo.setError("Passwords must match");
                }else
                    edtOne.setError("Password must have at least 6 characters");


            }
        });

        _dialog.show();
    } //do: hide text

    void ActivateCancelButton(){
       Button btnCancel = _dialog.findViewById(R.id.btnCancel_ChangeDetailsPopup);
       btnCancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               _dialog.dismiss();
           }
       });
    }

    Boolean VerifiedPassword(){
        TextInputLayout edtPassword = _dialog.findViewById(R.id.edt_Password_ChangeDetailsPopup);
        if(edtPassword.getEditText().getText().equals(Profile.getInstance().getPassword()))
        {
            edtPassword.setError("");
            return true;
        }

        edtPassword.setError("Incorrect Password");
        return false;

    }

    //endregion Account Settings

    //region App Settings
    void ChangeUnitSystem() {
        _dialog.setContentView(R.layout.change_unit_system_popup);
        RadioGroup radioGroup = _dialog.findViewById(R.id.rgboptions_ChangeUnitsPopup);
        int check;
        if(Profile.getInstance().getMetricSystem())
            check = 1;
        else
            check = 0;
        radioGroup.check(check);

        Button btnCancel = _dialog.findViewById(R.id.btnCancel_ChangeUnitsPopup);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dialog.dismiss();
            }
        });

        Button btnDone = _dialog.findViewById(R.id.btnDone_ChangeUnitsPopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() != -1) {
                    Toast.makeText(SettingsActivity.this, "here", Toast.LENGTH_SHORT);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = (RadioButton) _dialog.findViewById(selectedId);

                    if (selectedRadioButton.getText().equals(R.string.imperial)) {
                        Profile.getInstance().setMetricSystem(false);
                        if (!Profile.getInstance().getAuthStatus())
                            Profile.getInstance().getReference().child("metricSystem").setValue(false);
                    } else {
                        Profile.getInstance().setMetricSystem(true);
                        if (!Profile.getInstance().getAuthStatus())
                            Profile.getInstance().getReference().child("metricSystem").setValue(true);

                    }

                    _dialog.dismiss();


                }
            }
        });
        _dialog.show();

    } //do



    void ChangeTheme(){

        if(_isNightModeOn) // variable set in OnCreate
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            _sharedPrefsEdit.putBoolean("NightMode",true);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            _sharedPrefsEdit.putBoolean("NightMode",false);
        }
        _sharedPrefsEdit.apply();
    }

    //endregion App Settings

    void SignOut(){

        if (Profile.getInstance().getAuthStatus())
            FirebaseAuth.getInstance().signOut();
        Profile.getInstance().Clear();

        Intent intent = new Intent(SettingsActivity.this, MainActivity.class); //replace with check login
        startActivity(intent);

    } //done

    void ConfirmAccountDeletion(){
        _dialog.setContentView(R.layout.confirm_delete_popup);

        Button btnDone = _dialog.findViewById(R.id.btnConfirmed_ConfirmDeletePopup);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAccount();
            }
        });

        Button btnCancel = _dialog.findViewById(R.id.btnCancel_ConfirmDeletePopup);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dialog.dismiss();
            }
        });

        _dialog.show();
    }

    void DeleteAccount() {
        Profile.getInstance().getReference().removeValue();
        SignOut();
    }//done


}