package com.example.isharing.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.isharing.R;
import com.example.isharing.databinding.FragmentFriendsBinding;

public class HomeFragment extends Fragment {

    private FragmentFriendsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SearchView searchView = root.findViewById(R.id.search_view);
        searchView.setQueryHint("Your placeholder text here");

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}