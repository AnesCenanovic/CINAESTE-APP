package com.example.lab2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var listView : ListView
    private lateinit var editText : EditText
    private lateinit var button : Button
    private val listaVrijednosti = arrayListOf<String>()
    private lateinit var adapter : MyArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button1);
        editText = findViewById(R.id.editText1)
        listView = findViewById(R.id.listView1)
        adapter = MyArrayAdapter(this, R.layout.layout, listaVrijednosti)
        listView.adapter=adapter

        button.setOnClickListener {
            addToList()
        }
    }

    private fun addToList() {

        listaVrijednosti.add(0,editText.text.toString())
        adapter.notifyDataSetChanged();
        editText.setText("");

    }
}

class MyArrayAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val elements: ArrayList<String>):
    ArrayAdapter<String>(context, layoutResource, elements) {
    override fun getView(position: Int, newView: View?, parent: ViewGroup): View {
        val newView =  LayoutInflater.from(context).inflate(R.layout.layout, parent, false)
        val textView = newView.findViewById<TextView>(R.id.textElement)
        val element = elements[position]
        textView.text=element
        return newView
    }
}