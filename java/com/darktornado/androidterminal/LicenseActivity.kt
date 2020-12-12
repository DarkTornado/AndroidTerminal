package com.darktornado.androidterminal

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader

class LicenseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar!!.setDisplayShowHomeEnabled(false)
        val layout = LinearLayout(this)
        layout.orientation = 1

        loadLicenseInfo(layout, "Android Terminal", "license", "GPL 3.0", "Dark Tornado", true)
        loadLicenseInfo(layout, "Roboto Mono", "license_font", "Apache License 2.0", "Christian Robertson", false)
        loadLicenseInfo(layout, "Shell Executor", "license_lib", "LGPL 3.0", "Dark Tornado", false)

        val pad = dip2px(20)
        layout.setPadding(pad, dip2px(10), pad, pad)
        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun loadLicenseInfo(layout: LinearLayout, name: String, fileName: String, license: String?, dev: String, tf: Boolean) {
        val pad = dip2px(10)
        val title = TextView(this)
        if (tf) title.text = Html.fromHtml("<b>$name<b>")
        else title.text = Html.fromHtml("<br><b>$name<b>")
        title.textSize = 24f
        title.setTextColor(Color.BLACK)
        title.setPadding(pad, 0, pad, dip2px(1))
        layout.addView(title)
        val subtitle = TextView(this)
        if (license == null) subtitle.text = "  by $dev"
        else subtitle.text = "  by $dev, $license"
        subtitle.textSize = 20f
        subtitle.setTextColor(Color.BLACK)
        subtitle.setPadding(pad, 0, pad, pad)
        layout.addView(subtitle)

        val value = loadLicense(fileName)
        val txt = TextView(this)
        if (value.length > 1500) {
            txt.text = Html.fromHtml(value.substring(0, 1500).replace("\n", "<br>") + "...<font color='#757575'><b>[Show All]</b></font>")
            txt.setOnClickListener { showDialog(license, value) }
        } else {
            txt.text = value
        }
        txt.textSize = 17f
        txt.setTextColor(Color.BLACK)
        txt.setPadding(pad, pad, pad, pad)
        txt.setBackgroundColor(Color.parseColor("#E0E0E0"))
        layout.addView(txt)
    }

    private fun loadLicense(name: String): String {
        try {
            val isr = InputStreamReader(assets.open("$name.txt"))
            val br = BufferedReader(isr)
            var str = br.readLine()
            var line = br.readLine()
            while (line != null) {
                str += "\n" + line
                line = br.readLine()
            }
            isr.close()
            br.close()
            return str
        } catch (e: Exception) {
            toast(e.toString())
            return "라이선스 정보 불러오기 실패"
        }
    }


    fun showDialog(title: String?, msg: CharSequence) {
        try {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(title)
            dialog.setMessage(msg)
            dialog.setNegativeButton("닫기", null)
            dialog.show()
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    fun toast(msg: String?) {
        runOnUiThread({
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        })
    }

    fun dip2px(dips: Int): Int {
        return Math.ceil((dips * this.resources.displayMetrics.density).toDouble()).toInt()
    }
}
