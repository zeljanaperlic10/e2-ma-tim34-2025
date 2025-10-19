package com.example.myapplication.presentation.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.presentation.LoginActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProfileFragment extends Fragment {

    private ImageView avatarImageView, qrCodeImageView;
    private TextView usernameTextView;
    private EditText oldPasswordField, newPasswordField, confirmNewPasswordField;
    private Button changePasswordBtn, logoutBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        avatarImageView = view.findViewById(R.id.avatarImageView);
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        oldPasswordField = view.findViewById(R.id.oldPasswordField);
        newPasswordField = view.findViewById(R.id.newPasswordField);
        confirmNewPasswordField = view.findViewById(R.id.confirmNewPasswordField);
        changePasswordBtn = view.findViewById(R.id.changePasswordBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);

        loadUserData();
        generateQRCode();

        changePasswordBtn.setOnClickListener(v -> changePassword());
        logoutBtn.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserData() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String avatar = documentSnapshot.getString("avatar");

                        usernameTextView.setText(username);

                        // Postavi avatar sliku
                        if (avatar != null) {
                            int avatarResId = getAvatarResourceId(avatar);
                            if (avatarResId != 0) {
                                avatarImageView.setImageResource(avatarResId);
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Greška pri učitavanju podataka", Toast.LENGTH_SHORT).show()
                );
    }

    private int getAvatarResourceId(String avatarName) {
        switch (avatarName) {
            case "person1":
                return R.drawable.person1;
            case "person2":
                return R.drawable.person2;
            case "person3":
                return R.drawable.person3;
            case "person4":
                return R.drawable.person4;
            case "person5":
                return R.drawable.person5;
            default:
                return 0;
        }
    }

    private void generateQRCode() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(getContext(), "Greška pri generisanju QR koda", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        String oldPassword = oldPasswordField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordField.getText().toString().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(getContext(), "Popuni sva polja!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(getContext(), "Nove lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "Nova lozinka mora imati najmanje 6 karaktera!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) return;

        // Reautentifikacija korisnika sa starom lozinkom
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Promeni lozinku
                    currentUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "Lozinka uspešno promenjena!", Toast.LENGTH_SHORT).show();
                                oldPasswordField.setText("");
                                newPasswordField.setText("");
                                confirmNewPasswordField.setText("");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Greška pri promeni lozinke!", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Stara lozinka nije tačna!", Toast.LENGTH_SHORT).show()
                );
    }

    private void logout() {
        auth.signOut();

        // Vrati korisnika na LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
