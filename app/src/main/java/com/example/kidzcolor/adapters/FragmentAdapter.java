package com.example.kidzcolor.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.kidzcolor.LibraryFragment;
import com.example.kidzcolor.MyColorsFragment;

import org.jetbrains.annotations.NotNull;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0 :
                return new LibraryFragment();
        }

        return new MyColorsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
