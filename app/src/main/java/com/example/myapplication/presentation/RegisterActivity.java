package com.example.myapplication.presentation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.FirebaseRepository;
import com.example.myapplication.domain.service.UserService;

public class RegisterActivity extends AppCompatActivity {
    private class AvatarAdapter extends ArrayAdapter<Integer> {
        private final String[] names = {"Ana", "Marko", "Maria", "Stefan", "Sara"};

        public AvatarAdapter(Context context, Integer[] images) {
            super(context, R.layout.spinner_item, images);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        private View initView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
            }

            ImageView imageView = convertView.findViewById(R.id.avatarImage);
            TextView textView = convertView.findViewById(R.id.avatarName);

            if (position == 0) {
                textView.setText("Izaberi avatar");
                imageView.setImageResource(android.R.color.transparent);
            } else {
                Integer imageRes = getItem(position);
                if (imageRes != null) imageView.setImageResource(imageRes);

                // position-1 jer names ima 5 elemenata, a avatarImages 6 (prvi je null)
                textView.setText(names[position - 1]);
            }

            return convertView;
        }
    }

    private EditText emailField, passwordField, confirmPasswordField, usernameField;
    private Spinner avatarSpinner;
    private Button registerBtn;

    private final UserService userService = new UserService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        usernameField = findViewById(R.id.username);
        avatarSpinner = findViewById(R.id.avatarSpinner);
        registerBtn = findViewById(R.id.registerBtn);

        // Primer avatara:
        Spinner avatarSpinner = findViewById(R.id.avatarSpinner);

        Integer[] avatarImages = {
                null,
                R.drawable.person2,
                R.drawable.person5,
                R.drawable.person4,
                R.drawable.person3,
                R.drawable.person1
        };

        AvatarAdapter adapter = new AvatarAdapter(this, avatarImages);
        avatarSpinner.setAdapter(adapter);

        registerBtn.setOnClickListener(v -> registerUser());

        Button backToLoginBtn = findViewById(R.id.backToLoginBtn);
        backToLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String username = usernameField.getText().toString();
        int selectedPosition = avatarSpinner.getSelectedItemPosition();
        if (selectedPosition == 0) {
            Toast.makeText(this, "Molimo izaberi avatar!", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] avatarNames = {null, "person2", "person5", "person4", "person3", "person1"};
        String avatar = avatarNames[selectedPosition];

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Sva polja su obavezna!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
            return;
        }

        userService.register(email, password, username, avatar, new FirebaseRepository.OnUserRegistered() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegisterActivity.this,
                        "Registracija uspešna! Proveri email za aktivaciju naloga.",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(RegisterActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
