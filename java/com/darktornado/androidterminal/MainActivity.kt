package com.darktornado.androidterminal

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import java.io.File
import java.util.ArrayList

class MainActivity : Activity() {

    val VERSION = "1.0"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "앱 정보")
        menu.add(0, 1, 0, "오픈 소스 라이선스")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> showDialog("도움말", "앱 이름 : 터미널\n버전 : $VERSION\n개발자 : Dark Tornado\n\n 일부 터미널 명령어들을 실행할 수 있는 앱입니다. 개발자의 깃허브에 소스가 공개되어 있습니다.")
            1 -> startActivity(Intent(this, LicenseActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar!!.setDisplayShowHomeEnabled(false)
        val layout = LinearLayout(this)
        layout.orientation = 1

        val se = ShellExecutor(this, File("/"))
        val cmd = ArrayList<CharSequence>()
        val data = SpannableStringBuilder("/ $ ")
        data.setSpan(ForegroundColorSpan(Color.parseColor("#4CAF50")), 0, data.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        cmd.add(data)

        val layout2 = LinearLayout(this)
        val list = ListView(this)
        list.isFastScrollEnabled = true
        val adapter = TypefacedArrayAdapter(this, R.layout.cmd_line, cmd)
        list.adapter = adapter
        list.divider = null
        list.layoutParams = LinearLayout.LayoutParams(-1, -1)
        list.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        list.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, pos, id -> toast("길게 누르면 복사되는거에요.") }
        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, pos, id ->
            val txt = view as TextView
            copyToClipboard(txt.text.toString())
            toast("클립보드로 복사되었어요.")
            true
        }
        layout2.addView(list)
        var pad = dip2px(10)
        layout2.setPadding(pad, pad, pad, dip2px(60))

        val layoutf = FrameLayout(this)
        layoutf.layoutParams = LinearLayout.LayoutParams(-1, -1)
        layoutf.addView(layout2)

        val lay2 = LinearLayout(this)
        lay2.orientation = 0
        lay2.weightSum = 5f
        val params = FrameLayout.LayoutParams(-1, dip2px(50), 1)
        params.gravity = Gravity.BOTTOM or Gravity.CENTER
        lay2.layoutParams = params
        lay2.gravity = Gravity.CENTER
        lay2.setBackgroundColor(Color.parseColor("#616161"))
        pad = dip2px(5)
        lay2.setPadding(pad, pad, pad, pad)
        val txt = layoutInflater.inflate(R.layout.terminal_input, null) as EditText
        txt.hint = "Input Command..."
        txt.setTextColor(Color.WHITE)
        txt.setHintTextColor(Color.GRAY)
        txt.layoutParams = LinearLayout.LayoutParams(-1, dip2px(50), 1f)
        txt.setBackgroundColor(Color.BLACK)
        txt.setSingleLine(true)

        txt.setPadding(pad, pad, pad, pad)
        lay2.addView(txt)
        val btn = Button(this)
        btn.text = "Run"
        btn.setTextColor(Color.WHITE)
        btn.transformationMethod = null
        btn.setBackgroundColor(Color.parseColor("#424242"))
        btn.layoutParams = LinearLayout.LayoutParams(-1, -2, 4f)
        btn.setOnClickListener {
            val input = txt.text.toString()
            val last = cmd.size - 1
            val lastStr = cmd[last].toString()
            if (input == "") cmd.add(lastStr)
            else executeCommand(input, list, adapter, cmd, se, lastStr, last, txt)
        }
        lay2.addView(btn)
        layoutf.addView(lay2)
        layout.addView(layoutf)
        layout.setBackgroundColor(Color.DKGRAY)

        setContentView(layout)
        permissionCheck();
    }

    private fun executeCommand(input: String, list: ListView, adapter: TypefacedArrayAdapter, cmd: ArrayList<CharSequence>, se: ShellExecutor, lastStr: String, last: Int, txt: TextView) {
        try {
            val str = SpannableStringBuilder(lastStr + input)
            str.setSpan(ForegroundColorSpan(Color.parseColor("#4CAF50")), 0, lastStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            str.setSpan(ForegroundColorSpan(Color.WHITE), lastStr.length, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            cmd[last] = str
            adapter.notifyDataSetChanged()
            if (input == "cls") {
                cmd.clear()
            } else {
                val result = se.execute(input)
                if (result.data != null) {
                    val data = SpannableStringBuilder(result.data)
                    var color = Color.WHITE
                    if (result.failed) color = Color.RED
                    data.setSpan(ForegroundColorSpan(color), 0, data.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    cmd.add(data)
                }
            }
            val data = SpannableStringBuilder(se.currentDirectory + " $ ")
            data.setSpan(ForegroundColorSpan(Color.parseColor("#4CAF50")), 0, data.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            cmd.add(data)
            adapter.notifyDataSetChanged()
            txt.text = ""
            list.setSelection(adapter.count - 1)
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    fun permissionCheck() {
        if (Build.VERSION.SDK_INT < 23) return
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1);
            toast("내장메모리 접근 해당 권한을 허용해주세요.");
        }
    }

    fun copyToClipboard(value: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip = ClipData.newPlainText("label", value)
    }

    fun showDialog(title: String, msg: CharSequence?) {
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

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun dip2px(dips: Int): Int {
        return Math.ceil((dips * this.resources.displayMetrics.density).toDouble()).toInt()
    }
}
