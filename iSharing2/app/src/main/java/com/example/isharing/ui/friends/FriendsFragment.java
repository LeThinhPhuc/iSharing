package com.example.isharing.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.isharing.CustomAdapter;
import com.example.isharing.R;
import com.example.isharing.databinding.FragmentFriendsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {
    private FragmentFriendsBinding binding;
    private ArrayList<String> userList;
    private CustomAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FriendsViewModel friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SearchView searchView = root.findViewById(R.id.search_view);
        searchView.setQueryHint("Search...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ListView mListView = root.findViewById(R.id.list_view);
        userList = new ArrayList<>();
        adapter = new CustomAdapter(requireContext(), userList);
        mListView.setAdapter(adapter);

        db.collection("users")
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    userList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String name = documentSnapshot.getString("first") + " " +
                                documentSnapshot.getString("middle") + " " +
                                documentSnapshot.getString("last");
                        userList.add(name);
                    }
                    adapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}