package com.ahmed.testindicator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ahmed.testindicator.libkotlin.InstaDotView

class MainActivity : AppCompatActivity() {
    private var page = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val increase = findViewById<Button>(R.id.increase_btn)
        val decrease = findViewById<Button>(R.id.decrease_btn)
        val counter = findViewById<TextView>(R.id.counter_view)
        val instaDotView = findViewById<InstaDotView>(R.id.instadot)
        val visibleDots = findViewById<EditText>(R.id.visible_dots_edittext)
        val itemSize = findViewById<EditText>(R.id.item_size_edittext)
        val updateVisibleDots = findViewById<Button>(R.id.updatebtn)
        instaDotView.setNoOfPages(20)
        updateVisibleDots.setOnClickListener {
            if (!TextUtils.isEmpty(visibleDots.text.toString())) instaDotView.setVisibleDotCounts(
                visibleDots.text.toString().toInt()
            )
            if (!TextUtils.isEmpty(itemSize.text.toString())) instaDotView.setNoOfPages(
                itemSize.text.toString().toInt()
            )
            page = 0
            counter.text = page.toString() + ""
        }
        increase.setOnClickListener {
            page++
            if (page > instaDotView.getNoOfPages() - 1) page = instaDotView.getNoOfPages() - 1
            counter.text = page.toString() + ""
            instaDotView.onPageChange(page)
        }
        decrease.setOnClickListener {
            page--
            if (page < 0) page = 0
            counter.text = page.toString() + ""
            instaDotView.onPageChange(page)
        }
    }
}


fun String.toInt()  = Integer.parseInt(this)