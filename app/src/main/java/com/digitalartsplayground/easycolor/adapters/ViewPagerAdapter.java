package com.digitalartsplayground.easycolor.adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.digitalartsplayground.easycolor.fragments.LibraryFragment;
import com.digitalartsplayground.easycolor.fragments.MyArtworkFragment;

import org.jetbrains.annotations.NotNull;

public class ViewPagerAdapter  extends FragmentStateAdapter {

    LibraryFragment libraryFragment;

    public ViewPagerAdapter(@NonNull @NotNull FragmentManager fragmentManager, @NonNull @NotNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new LibraryFragment();
            case 1:
                return new MyArtworkFragment();
        }

        return libraryFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }




}
