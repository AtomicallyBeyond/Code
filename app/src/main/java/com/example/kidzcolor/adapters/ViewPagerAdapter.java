package com.example.kidzcolor.adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.kidzcolor.fragments.LibraryFragment;
import com.example.kidzcolor.fragments.MyColorsFragment;

import org.jetbrains.annotations.NotNull;

public class ViewPagerAdapter  extends FragmentStateAdapter {

    LibraryFragment libraryFragment;
    MyColorsFragment myColorsFragment;

    public ViewPagerAdapter(@NonNull @NotNull FragmentManager fragmentManager, @NonNull @NotNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);

        libraryFragment = new LibraryFragment();
        myColorsFragment = new MyColorsFragment();
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return libraryFragment;
            case 1:
                return myColorsFragment;
        }

        return libraryFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
