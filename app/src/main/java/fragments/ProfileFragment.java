package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.foodshare.AboutActivity;
import com.example.foodshare.FAQActivity;
import com.example.foodshare.R;
import com.example.foodshare.StartActivity;
import com.google.firebase.auth.FirebaseAuth;

import managers.UserManager;

public class ProfileFragment extends Fragment {

    View view;

    Button FAQ;
    Button logout;
    Button about;
    TextView usernameView;

    public static String TO_PROFILE = "TO_PROFILE";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameView = view.findViewById(R.id.username);
        usernameView.setText(UserManager.getInstance().getCurrentUsername());

        FAQ = view.findViewById(R.id.FAQButton);
        FAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), FAQActivity.class));
            }
        });

        logout = view.findViewById(R.id.logoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), StartActivity.class);
                getActivity().startActivity(intent);
            }
        });

        about = view.findViewById(R.id.AboutButton);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AboutActivity.class));
            }
        });

        return view;
    }
}
