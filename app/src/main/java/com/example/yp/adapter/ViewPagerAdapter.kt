package com.example.yp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yp.fragments.FavoriteFragment
import com.example.yp.fragments.HomeFragment
import com.example.yp.fragments.ProfileFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> FavoriteFragment()
            2 -> ProfileFragment.newInstance()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}