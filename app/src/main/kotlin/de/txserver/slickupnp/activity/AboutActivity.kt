package de.txserver.slickupnp.activity

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView

import de.txserver.slickupnp.R
import de.txserver.slickupnp.app.SlickUPnP

class AboutActivity : AppCompatActivity() {

    private val TAG = AboutActivity::class.java.simpleName

    internal var actionBar: ActionBar? = null

    private var aboutVersionTextView: TextView? = null
    private var aboutDeveloperTextView: TextView? = null
    private var aboutWebsiteTextView: TextView? = null

    private val activity: AppCompatActivity
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val myToolbar = findViewById(R.id.myToolbar) as Toolbar
        setSupportActionBar(myToolbar)

        actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)

        setActivityTitle()

        val appVersionText = resources.getString(R.string.app_name) + ": " + SlickUPnP.instance.versionName + " (build " + SlickUPnP.instance.versionCode + ")"
        aboutVersionTextView = findViewById(R.id.aboutVersion_text) as TextView
        aboutVersionTextView!!.text = appVersionText

        val developerText = resources.getString(R.string.about_developer) + ": " + resources.getString(R.string.about_developer_name)
        aboutDeveloperTextView = findViewById(R.id.aboutDeveloper_text) as TextView
        aboutDeveloperTextView!!.text = developerText

        val websiteText = resources.getString(R.string.about_website) + ": " + resources.getString(R.string.about_website_url)
        aboutWebsiteTextView = findViewById(R.id.aboutWebsite_text) as TextView
        aboutWebsiteTextView!!.text = websiteText
    }

    private fun setActivityTitle() {

        this.title = resources.getString(R.string.menuItem_about)
    }
}
