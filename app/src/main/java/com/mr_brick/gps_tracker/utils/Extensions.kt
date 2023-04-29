package com.mr_brick.gps_tracker.utils

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mr_brick.gps_tracker.R

fun Fragment.openFragment(f: Fragment){
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction().setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        .replace(R.id.placeHolder, f).commit()
}

fun AppCompatActivity.openFragment(f: Fragment){
    //Если текущий пул фрагментов не пуст
    if(supportFragmentManager.fragments.isNotEmpty()){
        //Если имя текущего фрагмента и фрагмента, который мы хотим открыть совпадают, то выходим из функции
        if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return
    }
    //Иначе заменяем текущий фрагмент на выбранный
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        .replace(R.id.placeHolder, f).commit()
}

fun Fragment.showToast(s: String){
    Toast.makeText(activity, s,Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.showToast(s: String){
    Toast.makeText(this, s,Toast.LENGTH_SHORT).show()
}

fun Fragment.checkPermisson(p: String) : Boolean{
    return when(PackageManager.PERMISSION_GRANTED){
        ContextCompat.checkSelfPermission(activity as AppCompatActivity, p) -> true
        else -> false
    }
}