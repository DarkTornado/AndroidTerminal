package com.darktornado.androidterminal

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class TypefacedArrayAdapter(ctx: Context, res: Int, objects: List<*>) : ArrayAdapter<Any>(ctx, res, objects) {

    private val font: Typeface = Typeface.createFromAsset(ctx.assets, "RobotoMono-Regular.ttf")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textview = view as TextView
        textview.typeface = font
        return textview
    }
}